package me.catcoder.sidebar;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.netty.WirePacket;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.ToString;
import me.catcoder.sidebar.util.ByteBufNetOutput;
import me.catcoder.sidebar.util.NetOutput;
import me.catcoder.sidebar.util.VersionUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Iterator;
import java.util.function.Function;

@Getter
@ToString
public class SidebarLine {

    private static final ChatColor[] COLORS = ChatColor.values();
    private static final Splitter SPLITTER = Splitter.fixedLength(16);

    public static final int TEAM_CREATED = 0;
    public static final int TEAM_REMOVED = 1;
    public static final int TEAM_UPDATED = 2;
    public static final int PLAYERS_ADDED = 3;
    public static final int PLAYERS_REMOVED = 4;

    private final String teamName;
    private int score = -1;

    private final int index;
    private final boolean staticText;

    private Function<Player, String> updater;

    SidebarLine(@NonNull Function<Player, String> updater, @NonNull String teamName, boolean staticText, int index) {
        this.updater = updater;
        this.teamName = teamName;
        this.staticText = staticText;
        this.index = index;
    }

    public BukkitTask updatePeriodically(long delay, long period, @NonNull Plugin plugin, @NonNull Sidebar sidebar) {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> sidebar.updateLine(this), delay, period);
    }

    public void setUpdater(@NonNull Function<Player, String> updater) {
        Preconditions.checkState(!isStaticText(), "Cannot set updater for static text line");
        this.updater = updater;
    }

    public void updateTeam(@NonNull Player player, int previousScore, @NonNull String objective) {
        if (!isStaticText()) {
            String text = updater.apply(player);
            sendWirePacket(player, createTeamPacket(TEAM_UPDATED, index, teamName, VersionUtil.getPlayerVersion(player.getUniqueId()), text));
        }

        if (previousScore != score) {
            sendPacket(player, createScorePacket(EnumWrappers.ScoreboardAction.CHANGE, objective));
        }
    }

    public void removeTeam(@NonNull Player player, @NonNull String objective) {
        sendPacket(player, createScorePacket(EnumWrappers.ScoreboardAction.REMOVE, objective));

        sendWirePacket(player, createTeamPacket(TEAM_REMOVED, index, teamName, VersionUtil.getPlayerVersion(player.getUniqueId()), null));
    }

    public void createTeam(@NonNull Player player, @NonNull String objective) {
        String text = updater.apply(player);

        sendWirePacket(player, createTeamPacket(TEAM_CREATED, index, teamName, VersionUtil.getPlayerVersion(player.getUniqueId()), text));

        sendPacket(player, createScorePacket(EnumWrappers.ScoreboardAction.CHANGE, objective));
    }

    public void setScore(int score) {
        this.score = score;
    }

    @SneakyThrows
    public static WirePacket createTeamPacket(int mode, int index, String teamName, int clientVersion, String text) {
        String teamEntry = COLORS[index].toString();

        PacketType type = PacketType.Play.Server.SCOREBOARD_TEAM;
        ByteBuf buffer = Unpooled.buffer();
        NetOutput packet = new ByteBufNetOutput(buffer);

        // construct the packet on lowest level for future compatibility

        packet.writeString(teamName);
        packet.writeByte(mode);

        if (mode == TEAM_REMOVED) {
            return new WirePacket(type, buffer.array());
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

            if (VersionUtil.SERVER_VERSION >= VersionUtil.MINECRAFT_1_13) {
                writeDefaults(packet);

                packet.writeString(WrappedChatComponent.fromText(text).getJson());
                packet.writeString(WrappedChatComponent.fromText(ChatColor.WHITE.toString()).getJson());
            } else {
                packet.writeString(text);
                packet.writeString(ChatColor.WHITE.toString());
                writeDefaults(packet);
            }

            packet.writeVarInt(1); // number of players
            packet.writeString(teamEntry); // entries

            return new WirePacket(type, buffer.array());
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

        if (VersionUtil.SERVER_VERSION < VersionUtil.MINECRAFT_1_13) {
            packet.writeString(prefix);
            packet.writeString(suffix);
            writeDefaults(packet);

        } else {
            writeDefaults(packet);
            packet.writeString(WrappedChatComponent.fromText(prefix).getJson()); // prefix
            packet.writeString(WrappedChatComponent.fromText(suffix).getJson()); // suffix
        }

        packet.writeVarInt(1); // number of players
        packet.writeString(teamEntry); // entries

        return new WirePacket(type, buffer.array());
    }

    private static void writeDefaults(@NonNull NetOutput packet) {
        packet.writeByte(10); // friendly tags
        packet.writeString("always"); // name tag visibility
        packet.writeString("always"); // collision rule
        if(VersionUtil.SERVER_VERSION < VersionUtil.MINECRAFT_1_13) {
            packet.writeByte(-1); // reset color
        } else {
            packet.writeVarInt(21);
        }
    }

    private PacketContainer createScorePacket(EnumWrappers.ScoreboardAction action, String objectiveName) {
        PacketContainer packet = getProtocolManager().createPacket(
                PacketType.Play.Server.SCOREBOARD_SCORE);
        packet.getStrings().write(0, COLORS[index].toString());
        packet.getStrings().write(1, objectiveName);
        packet.getScoreboardActions().write(0, action);
        packet.getIntegers().write(0, score);
        return packet;
    }

    private static ProtocolManager getProtocolManager() {
        return ProtocolLibrary.getProtocolManager();
    }

    @SneakyThrows
    static void sendWirePacket(Player player, @NonNull WirePacket packet) {
        if (player.isOnline()) {
            getProtocolManager().sendWirePacket(player, packet);
        }
    }

    @SneakyThrows
    static void sendPacket(Player player, PacketContainer packet) {
        if (player.isOnline()) {
            getProtocolManager().sendServerPacket(player, packet);
        }
    }
}
