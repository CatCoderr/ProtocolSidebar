package me.catcoder.sidebar.protocol;

import com.comphenix.protocol.injector.netty.WirePacket;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.concurrent.FastThreadLocal;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import me.catcoder.sidebar.text.TextProvider;
import me.catcoder.sidebar.util.ByteBufNetOutput;
import me.catcoder.sidebar.util.NetOutput;
import me.catcoder.sidebar.util.VersionUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Iterator;

@UtilityClass
public class ProtocolUtil {

    private final Splitter SPLITTER = Splitter.fixedLength(16);
    // use thread local buffer to avoid unnecessary allocations
    public final ThreadLocal<ByteBuf> BUFFER = ThreadLocal.withInitial(Unpooled::buffer);
    public final ChatColor[] COLORS = ChatColor.values();
    public final int TEAM_CREATED = 0;
    public final int TEAM_REMOVED = 1;
    public final int TEAM_UPDATED = 2;

    public <R> WirePacket createTeamPacket(int mode, int index,
                                           @NonNull String teamName,
                                           @NonNull Player player,
                                           R text,
                                           @NonNull TextProvider<R> textProvider) {
        return createTeamPacket(mode, index, teamName, VersionUtil.SERVER_VERSION, player, text, textProvider);
    }

    public WirePacket createScorePacket(int action, String objectiveName, int score, int index) {
        ByteBuf buf = BUFFER.get();

        buf.clear(); // clear buffer

        NetOutput output = new ByteBufNetOutput(buf);

        output.writeString(ProtocolUtil.COLORS[index].toString());

        if (VersionUtil.SERVER_VERSION >= VersionUtil.MINECRAFT_1_13) {
            output.writeVarInt(action);
        } else {
            output.writeByte(action);
        }

        output.writeString(objectiveName);

        if (action != 1) {
            output.writeVarInt(score);
        }

        return new WirePacket(PacketIds.UPDATE_SCORE.getPacketId(VersionUtil.SERVER_VERSION), output.toByteArray());
    }

    @SneakyThrows
    public <R> WirePacket createTeamPacket(int mode, int index,
                                           @NonNull String teamName,
                                           int serverVersion,
                                           @NonNull Player player,
                                           R text,
                                           @NonNull TextProvider<R> provider) {
        Preconditions.checkArgument(mode >= TEAM_CREATED && mode <= TEAM_UPDATED, "Invalid team mode");

        String teamEntry = COLORS[index].toString();
        int clientVersion = VersionUtil.getPlayerVersion(player.getUniqueId());

        ByteBuf buf = BUFFER.get();

        buf.clear(); // clear buffer

        NetOutput packet = new ByteBufNetOutput(buf);

        // construct the packet on lowest level for future compatibility

        packet.writeString(teamName);
        packet.writeByte(mode);

        if (mode == TEAM_REMOVED) {
            return new WirePacket(PacketIds.UPDATE_TEAMS.getPacketId(VersionUtil.SERVER_VERSION), packet.toByteArray());
        }

        if (clientVersion >= VersionUtil.MINECRAFT_1_13) {
            packet.writeString(WrappedChatComponent.fromText("").getJson()); // team display name
        } else {
            packet.writeString("");
        }

        // Since 1.13 character limit for prefix/suffix was removed
        if (clientVersion >= VersionUtil.MINECRAFT_1_13) {

            if (serverVersion >= VersionUtil.MINECRAFT_1_13) {
                writeDefaults(serverVersion, packet);
                packet.writeString(provider.asJsonMessage(player, text));
                packet.writeString("{\"text\":\"\"}");
            } else {
                String legacyText = provider.asLegacyMessage(player, text);

                packet.writeString(legacyText);
                packet.writeString(ChatColor.WHITE.toString());
                writeDefaults(serverVersion, packet);
            }

            if (mode == TEAM_CREATED) {
                packet.writeVarInt(1); // number of players
                packet.writeString(teamEntry); // entries
            }

            return new WirePacket(PacketIds.UPDATE_TEAMS.getPacketId(VersionUtil.SERVER_VERSION), packet.toByteArray());
        }

        // 1.12 and below stuff :(
        // I'll remove it in future

        String legacyText = provider.asLegacyMessage(player, text);

        Iterator<String> iterator = SPLITTER.split(legacyText).iterator();
        String prefix = iterator.next();
        String suffix = "";

        if (legacyText.length() > 16) {
            String prefixColor = ChatColor.getLastColors(prefix);
            suffix = iterator.next();

            if (prefix.endsWith(String.valueOf(ChatColor.COLOR_CHAR))) {
                prefix = prefix.substring(0, prefix.length() - 1);

                prefixColor = ChatColor.getByChar(suffix.charAt(0)).toString();
                suffix = suffix.substring(1);
            }

            suffix = ((prefixColor.equals("") ? ChatColor.RESET : prefixColor) + suffix);

            if (suffix.length() > 16) {
                suffix = suffix.substring(0, 13) + "...";
            }
        }

        if (serverVersion < VersionUtil.MINECRAFT_1_13) {
            packet.writeString(prefix);
            packet.writeString(suffix);
            writeDefaults(serverVersion, packet);

        } else {
            writeDefaults(serverVersion, packet);
            packet.writeString(WrappedChatComponent.fromText(prefix).getJson()); // prefix
            packet.writeString(WrappedChatComponent.fromText(suffix).getJson()); // suffix
        }

        if (mode == TEAM_CREATED) {
            packet.writeVarInt(1); // number of players
            packet.writeString(teamEntry); // entries
        }

        return new WirePacket(PacketIds.UPDATE_TEAMS.getPacketId(VersionUtil.SERVER_VERSION), packet.toByteArray());
    }

    private static void writeDefaults(int serverVersion, @NonNull NetOutput packet) {
        packet.writeByte(10); // friendly tags
        packet.writeString("always"); // name tag visibility
        packet.writeString("always"); // collision rule
        if (serverVersion < VersionUtil.MINECRAFT_1_13) {
            packet.writeByte(-1); // reset color
        } else {
            packet.writeVarInt(21);
        }
    }


}
