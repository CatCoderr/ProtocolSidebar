package me.catcoder.sidebar.util;

import java.util.Iterator;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.injector.netty.WirePacket;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.google.common.base.Splitter;

import org.bukkit.ChatColor;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ProtocolUtil {

    private static final Splitter SPLITTER = Splitter.fixedLength(16);
    
    public static final ChatColor[] COLORS = ChatColor.values();
    public static final int TEAM_CREATED = 0;
    public static final int TEAM_REMOVED = 1;
    public static final int TEAM_UPDATED = 2;
    public static final int PLAYERS_ADDED = 3;
    public static final int PLAYERS_REMOVED = 4;

    public  WirePacket createTeamPacket(int mode, int index, String teamName, int clientVersion, String text) {
        return createTeamPacket(mode, index, teamName, VersionUtil.SERVER_VERSION, clientVersion, text);
    }

    @SneakyThrows
    public  WirePacket createTeamPacket(int mode, int index, String teamName, int serverVersion,
            int clientVersion, String text) {
        String teamEntry = COLORS[index].toString();

        PacketType type = PacketType.Play.Server.SCOREBOARD_TEAM;
        ByteBuf buffer = Unpooled.buffer();

        NetOutput packet = new ByteBufNetOutput(buffer);

        // construct the packet on lowest level for future compatibility

        packet.writeString(teamName);
        packet.writeByte(mode);

        if (mode == TEAM_REMOVED) {
            return new WirePacket(type, packet.toByteArray());
        }

        if (clientVersion >= VersionUtil.MINECRAFT_1_13) {
            packet.writeString(WrappedChatComponent.fromText("").getJson()); // team display name
        } else {
            packet.writeString("");
        }

        // Since 1.13 character limit for prefix/suffix was removed
        if (clientVersion >= VersionUtil.MINECRAFT_1_13) {
            if (!text.isEmpty() && text.charAt(0) != ChatColor.COLOR_CHAR) {
                text = ChatColor.WHITE + text;
            }

            if (serverVersion >= VersionUtil.MINECRAFT_1_13) {
                writeDefaults(serverVersion, packet);

                packet.writeString(WrappedChatComponent.fromText(text).getJson());
                packet.writeString(WrappedChatComponent.fromText(ChatColor.WHITE.toString()).getJson());
            } else {
                packet.writeString(text);
                packet.writeString(ChatColor.WHITE.toString());
                writeDefaults(serverVersion, packet);
            }

            if (mode == TEAM_CREATED || mode == PLAYERS_REMOVED || mode == PLAYERS_ADDED) {
                packet.writeVarInt(1); // number of players
                packet.writeString(teamEntry); // entries
            }

            return new WirePacket(type, packet.toByteArray());
        }

        Iterator<String> iterator = SPLITTER.split(text).iterator();
        String prefix = iterator.next();
        String suffix = "";

        if (text.length() > 16) {
            String prefixColor = ChatColor.getLastColors(prefix);
            suffix = iterator.next();

            if (prefix.endsWith(String.valueOf(ChatColor.COLOR_CHAR))) {
                prefix = prefix.substring(0, prefix.length() - 1);

                prefixColor = ChatColor.getByChar(suffix.charAt(0)).toString();
                suffix = suffix.substring(1);
            }

            if (prefixColor == null) {
                prefixColor = "";
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

        packet.writeVarInt(1); // number of players
        packet.writeString(teamEntry); // entries

        return new WirePacket(type, packet.toByteArray());
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
