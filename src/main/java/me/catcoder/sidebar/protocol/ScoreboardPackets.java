package me.catcoder.sidebar.protocol;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import io.netty.buffer.ByteBuf;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import me.catcoder.sidebar.text.TextProvider;
import me.catcoder.sidebar.util.buffer.ByteBufNetOutput;
import me.catcoder.sidebar.util.buffer.NetOutput;
import me.catcoder.sidebar.util.version.VersionUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.Iterator;

@UtilityClass
public class ScoreboardPackets {

    private final Splitter SPLITTER = Splitter.fixedLength(16);

    public final ChatColor[] COLORS = ChatColor.values();

    public final int TEAM_CREATED = 0;
    public final int TEAM_REMOVED = 1;
    public final int TEAM_UPDATED = 2;

    public <R> ByteBuf createTeamPacket(int mode, int index,
                                        @NonNull String teamName,
                                        @NonNull Player player,
                                        R text,
                                        @NonNull TextProvider<R> textProvider) {
        return createTeamPacket(mode, index, teamName, VersionUtil.SERVER_VERSION, player, text, textProvider);
    }

    public <R> ByteBuf createScorePacket(@NonNull Player player,
                                         int action,
                                         String objectiveName,
                                         int score,
                                         int index,
                                         TextProvider<R> textProvider,
                                         @Nullable ScoreNumberFormat numberFormat,
                                         @Nullable Function<Player, R> scoreNumberFormatter) {
        ByteBuf buf = ChannelInjector.IMP.getChannel(player).alloc().buffer();

        NetOutput output = new ByteBufNetOutput(buf);

        if (VersionUtil.SERVER_VERSION >= ProtocolConstants.MINECRAFT_1_20_3) {
            if (action == 1) {
                output.writeVarInt(PacketIds.RESET_SCORE.getServerPacketId());
                output.writeString(ScoreboardPackets.COLORS[index].toString());
                output.writeBoolean(true); // has objective name
                output.writeString(objectiveName);
                return buf;
            }

            output.writeVarInt(PacketIds.UPDATE_SCORE.getServerPacketId());
            output.writeString(ScoreboardPackets.COLORS[index].toString());
            output.writeString(objectiveName);
            output.writeVarInt(score);

            output.writeBoolean(false); // has display name
            output.writeBoolean(numberFormat != null);

            if (numberFormat != null) {
                numberFormat.accept(output, scoreNumberFormatter == null ?
                        null :
                        textProvider.asJsonMessage(player, scoreNumberFormatter.apply(player)));
            }

            return buf;
        }

        output.writeVarInt(PacketIds.UPDATE_SCORE.getServerPacketId());

        output.writeString(ScoreboardPackets.COLORS[index].toString());

        if (VersionUtil.SERVER_VERSION >= ProtocolConstants.MINECRAFT_1_13) {
            output.writeVarInt(action);
        } else {
            output.writeByte(action);
        }

        output.writeString(objectiveName);

        if (action != 1) {
            output.writeVarInt(score);
        }

        return buf;
    }

    @SneakyThrows
    public <R> ByteBuf createTeamPacket(int mode, int index,
                                        @NonNull String teamName,
                                        int serverVersion,
                                        @NonNull Player player,
                                        R text,
                                        @NonNull TextProvider<R> provider) {
        Preconditions.checkArgument(mode >= TEAM_CREATED && mode <= TEAM_UPDATED, "Invalid team mode");

        String teamEntry = COLORS[index].toString();
        int clientVersion = VersionUtil.getPlayerVersion(player.getUniqueId());

        ByteBuf buf = ChannelInjector.IMP.getChannel(player).alloc().buffer();

        NetOutput packet = new ByteBufNetOutput(buf);

        // construct the packet on lowest level for future compatibility

        packet.writeVarInt(PacketIds.UPDATE_TEAMS.getServerPacketId());

        packet.writeString(teamName);
        packet.writeByte(mode);

        if (mode == TEAM_REMOVED) {
            return buf;
        }

        if (clientVersion >= ProtocolConstants.MINECRAFT_1_13) {
            if (serverVersion >= ProtocolConstants.MINECRAFT_1_20_3) {
                packet.writeComponent("{\"text\":\"\"}");
            } else {
                packet.writeString("{\"text\":\"\"}"); // team display name
            }
        } else {
            packet.writeString("");
        }

        // Since 1.13 character limit for prefix/suffix was removed
        if (clientVersion >= ProtocolConstants.MINECRAFT_1_13) {

            if (serverVersion >= ProtocolConstants.MINECRAFT_1_20_3) {
                writeDefaults(serverVersion, packet);
                packet.writeComponent(provider.asJsonMessage(player, text));
                packet.writeComponent("{\"text\":\"\"}");

            } else if (serverVersion >= ProtocolConstants.MINECRAFT_1_13) {
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

            return buf;
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

        if (serverVersion < ProtocolConstants.MINECRAFT_1_13) {
            packet.writeString(prefix);
            packet.writeString(suffix);
            writeDefaults(serverVersion, packet);

        } else {
            writeDefaults(serverVersion, packet);
            packet.writeString(provider.asJsonMessage(player, provider.fromLegacyMessage(prefix))); // prefix
            packet.writeString(provider.asJsonMessage(player, provider.fromLegacyMessage(suffix))); // suffix
        }

        if (mode == TEAM_CREATED) {
            packet.writeVarInt(1); // number of players
            packet.writeString(teamEntry); // entries
        }

        return buf;
    }

    private static void writeDefaults(int serverVersion, @NonNull NetOutput packet) {
        packet.writeByte(10); // friendly tags
        if (serverVersion <= ProtocolConstants.MINECRAFT_1_21_4) {
            packet.writeString("always"); // name tag visibility
            packet.writeString("always"); // collision rule
        } else {
            packet.writeVarInt(0); // name tag visibility
            packet.writeVarInt(0); // collision rule
        }
        if (serverVersion < ProtocolConstants.MINECRAFT_1_13) {
            packet.writeByte(-1); // reset color
        } else {
            packet.writeVarInt(21);
        }
    }


}
