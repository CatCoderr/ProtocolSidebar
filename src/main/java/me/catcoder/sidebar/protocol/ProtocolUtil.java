package me.catcoder.sidebar.protocol;

import java.util.Iterator;

import com.comphenix.protocol.injector.netty.WirePacket;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;

import me.catcoder.sidebar.util.ByteBufNetOutput;
import me.catcoder.sidebar.util.NetOutput;
import me.catcoder.sidebar.util.VersionUtil;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.ChatColor;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ProtocolUtil {

    private static final Splitter SPLITTER = Splitter.fixedLength(16);
    private static final String JSON_TEAM_STUB = ComponentSerializer.toString(new ComponentBuilder("")
            .color(net.md_5.bungee.api.ChatColor.WHITE)
            .create());

    public static final ChatColor[] COLORS = ChatColor.values();
    public static final int TEAM_CREATED = 0;
    public static final int TEAM_REMOVED = 1;
    public static final int TEAM_UPDATED = 2;
    public WirePacket createTeamPacket(int mode, int index, String teamName, int clientVersion, BaseComponent[] components) {
        return createTeamPacket(mode, index, teamName, VersionUtil.SERVER_VERSION, clientVersion, components);
    }

    @SneakyThrows
    public WirePacket createTeamPacket(int mode, int index, String teamName, int serverVersion,
                                       int clientVersion, BaseComponent[] components) {
        Preconditions.checkArgument(mode >= TEAM_CREATED && mode <= TEAM_UPDATED, "Invalid team mode");

        String teamEntry = COLORS[index].toString();

        ByteBuf buffer = Unpooled.buffer();

        NetOutput packet = new ByteBufNetOutput(buffer);

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

            if (components.length > 0 && components[0] instanceof TextComponent textComponent) {
                textComponent.setColor(textComponent.getColor());
            }

            if (serverVersion >= VersionUtil.MINECRAFT_1_13) {
                writeDefaults(serverVersion, packet);
                packet.writeString(ComponentSerializer.toString(components));
                packet.writeString(JSON_TEAM_STUB);
            } else {
                String legacyText = BaseComponent.toLegacyText(components);

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

        String legacyText = BaseComponent.toLegacyText(components);

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
