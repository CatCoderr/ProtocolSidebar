package me.catcoder.sidebar;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.netty.WirePacket;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.google.common.base.Preconditions;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.ToString;
import me.catcoder.sidebar.util.ProtocolUtil;
import me.catcoder.sidebar.util.VersionUtil;
import me.catcoder.sidebar.util.lang.ThrowingFunction;

@Getter
@ToString
public class SidebarLine {

    private final String teamName;
    private int score = -1;

    private final int index;
    private final boolean staticText;

    private ThrowingFunction<Player, String, Throwable> updater;

    SidebarLine(@NonNull ThrowingFunction<Player, String, Throwable> updater, @NonNull String teamName,
            boolean staticText, int index) {
        this.updater = updater;
        this.teamName = teamName;
        this.staticText = staticText;
        this.index = index;
    }

    public BukkitTask updatePeriodically(long delay, long period, @NonNull Plugin plugin, @NonNull Sidebar sidebar) {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> sidebar.updateLine(this), delay, period);
    }

    public void setUpdater(@NonNull ThrowingFunction<Player, String, Throwable> updater) {
        Preconditions.checkState(!isStaticText(), "Cannot set updater for static text line");
        this.updater = updater;
    }

    public void updateTeam(@NonNull Player player, int previousScore, @NonNull String objective) throws Throwable {
        if (!isStaticText()) {
            String text = updater.apply(player);
            sendWirePacket(player, ProtocolUtil.createTeamPacket(ProtocolUtil.TEAM_UPDATED, index, teamName,
                    VersionUtil.getPlayerVersion(player.getUniqueId()), text));
        }

        if (previousScore != score) {
            sendPacket(player, createScorePacket(EnumWrappers.ScoreboardAction.CHANGE, objective));
        }
    }

    public void removeTeam(@NonNull Player player, @NonNull String objective) {
        sendPacket(player, createScorePacket(EnumWrappers.ScoreboardAction.REMOVE, objective));

        sendWirePacket(player, ProtocolUtil.createTeamPacket(ProtocolUtil.TEAM_REMOVED, index, teamName,
                VersionUtil.getPlayerVersion(player.getUniqueId()), null));
    }

    public void createTeam(@NonNull Player player, @NonNull String objective) throws Throwable {
        String text = updater.apply(player);

        sendWirePacket(player, ProtocolUtil.createTeamPacket(ProtocolUtil.TEAM_CREATED, index, teamName,
                VersionUtil.getPlayerVersion(player.getUniqueId()), text));

        sendPacket(player, createScorePacket(EnumWrappers.ScoreboardAction.CHANGE, objective));
    }

    public void setScore(int score) {
        this.score = score;
    }

    private PacketContainer createScorePacket(EnumWrappers.ScoreboardAction action, String objectiveName) {
        PacketContainer packet = getProtocolManager().createPacket(
                PacketType.Play.Server.SCOREBOARD_SCORE);
        packet.getStrings().write(0, ProtocolUtil.COLORS[index].toString());
        packet.getStrings().write(1, objectiveName);
        packet.getScoreboardActions().write(0, action);
        packet.getIntegers().write(0, score);
        return packet;
    }

    private static ProtocolManager getProtocolManager() {
        return ProtocolLibrary.getProtocolManager();
    }

    @SneakyThrows
    static void sendWirePacket(@NonNull Player player, @NonNull WirePacket packet) {
        if (player.isOnline()) {
            getProtocolManager().sendWirePacket(player, packet);
        }
    }

    @SneakyThrows
    static void sendPacket(@NonNull Player player, PacketContainer packet) {
        if (player.isOnline()) {
            getProtocolManager().sendServerPacket(player, packet);
        }
    }
}
