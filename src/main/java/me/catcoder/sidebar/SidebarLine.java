package me.catcoder.sidebar;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.injector.netty.WirePacket;
import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.ToString;
import me.catcoder.sidebar.protocol.PacketIds;
import me.catcoder.sidebar.protocol.ProtocolUtil;
import me.catcoder.sidebar.text.TextProvider;
import me.catcoder.sidebar.util.ByteBufNetOutput;
import me.catcoder.sidebar.util.NetOutput;
import me.catcoder.sidebar.util.VersionUtil;
import me.catcoder.sidebar.util.lang.ThrowingFunction;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

@Getter
@ToString
public class SidebarLine<R> {

    private final String teamName;
    private int score = -1;

    private final int index;
    private final boolean staticText;

    // for internal use
    BukkitTask updateTask;

    private ThrowingFunction<Player, R, Throwable> updater;
    private final TextProvider<R> textProvider;

    SidebarLine(@NonNull ThrowingFunction<Player, R, Throwable> updater, @NonNull String teamName,
                boolean staticText, int index, @NonNull TextProvider<R> textProvider) {
        this.updater = updater;
        this.teamName = teamName;
        this.staticText = staticText;
        this.index = index;
        this.textProvider = textProvider;
    }

    public BukkitTask updatePeriodically(long delay, long period, @NonNull Sidebar<R> sidebar) {
        Preconditions.checkState(!isStaticText(), "Cannot set updater for static text line");

        if (updateTask != null && !updateTask.isCancelled()) {
            throw new IllegalStateException("Update task for line " + this + " is already running. Cancel it first.");
        }

        BukkitTask task = Bukkit.getScheduler().runTaskTimerAsynchronously(sidebar.getPlugin(),
                () -> sidebar.updateLine(this), delay, period);

        this.updateTask = task;

        sidebar.taskIds.add(task.getTaskId());

        return task;
    }

    public void setUpdater(@NonNull ThrowingFunction<Player, R, Throwable> updater) {
        Preconditions.checkState(!isStaticText(), "Cannot set updater for static text line");
        this.updater = updater;
    }

    public void updateTeam(@NonNull Player player, int previousScore, @NonNull String objective) throws Throwable {
        if (!isStaticText()) {
            R text = updater.apply(player);
            sendWirePacket(player, ProtocolUtil.createTeamPacket(ProtocolUtil.TEAM_UPDATED, index, teamName,
                    VersionUtil.getPlayerVersion(player.getUniqueId()), text, textProvider));
        }

        if (previousScore != score) {
            sendWirePacket(player, createScorePacket(0, objective));
        }
    }

    public void removeTeam(@NonNull Player player, @NonNull String objective) {
        sendWirePacket(player, createScorePacket(1, objective));

        sendWirePacket(player, ProtocolUtil.createTeamPacket(ProtocolUtil.TEAM_REMOVED, index, teamName,
                VersionUtil.getPlayerVersion(player.getUniqueId()), null, textProvider));
    }

    public void createTeam(@NonNull Player player, @NonNull String objective) throws Throwable {
        R text = updater.apply(player);

        sendWirePacket(player, ProtocolUtil.createTeamPacket(ProtocolUtil.TEAM_CREATED, index, teamName,
                VersionUtil.getPlayerVersion(player.getUniqueId()), text, textProvider));

        sendWirePacket(player, createScorePacket(0, objective));
    }

    public void setScore(int score) {
        this.score = score;
    }

    private WirePacket createScorePacket(int action, String objectiveName) {
        ByteBuf buf = Unpooled.buffer();
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

    private static ProtocolManager getProtocolManager() {
        return ProtocolLibrary.getProtocolManager();
    }

    @SneakyThrows
    static void sendWirePacket(@NonNull Player player, @NonNull WirePacket packet) {
        if (player.isOnline()) {
            getProtocolManager().sendWirePacket(player, packet);
        }
    }
}
