package me.catcoder.sidebar;

import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.ToString;
import me.catcoder.sidebar.protocol.ChannelInjector;
import me.catcoder.sidebar.protocol.ScoreboardPackets;
import me.catcoder.sidebar.text.TextProvider;
import me.catcoder.sidebar.util.lang.ThrowingFunction;
import me.catcoder.sidebar.util.lang.ThrowingSupplier;
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

        if (updateTask != null) {
            if (!updateTask.isCancelled()) {
                throw new IllegalStateException("Update task for line " + this + " is already running. Cancel it first.");
            }
            sidebar.taskIds.remove(updateTask.getTaskId());
        }

        BukkitTask task = Bukkit.getScheduler().runTaskTimerAsynchronously(sidebar.getPlugin(),
                () -> sidebar.updateLine(this), delay, period);

        this.updateTask = task;

        sidebar.bindBukkitTask(task);

        return task;
    }

    /**
     * Sets updater for this line. Updater is a function that takes player as an argument and returns
     * text that will be displayed for this player.
     *
     * @param updater - updater function
     */
    public void setUpdater(@NonNull ThrowingFunction<Player, R, Throwable> updater) {
        Preconditions.checkState(!isStaticText(), "Cannot set updater for static text line");
        this.updater = updater;
    }

    /**
     * Sets updater for this line without player parameter
     *
     * @param updater - updater function
     */
    public void setUpdater(ThrowingSupplier<R, Throwable> updater) {
        Preconditions.checkState(!isStaticText(), "Cannot set updater for static text line");
        this.updater = player -> updater.get();
    }

    void updateTeam(@NonNull Player player, int previousScore, @NonNull String objective) throws Throwable {
        if (!isStaticText()) {
            R text = updater.apply(player);
            sendPacket(player, ScoreboardPackets.createTeamPacket(ScoreboardPackets.TEAM_UPDATED, index, teamName,
                    player, text, textProvider));
        }

        if (previousScore != score) {
            sendPacket(player, ScoreboardPackets.createScorePacket(player, 0, objective, score, index));
        }
    }

    void removeTeam(@NonNull Player player, @NonNull String objective) {
        sendPacket(player, ScoreboardPackets.createScorePacket(player, 1, objective, score, index));

        sendPacket(player, ScoreboardPackets.createTeamPacket(ScoreboardPackets.TEAM_REMOVED, index, teamName,
                player, null, textProvider));
    }

    void createTeam(@NonNull Player player, @NonNull String objective) throws Throwable {
        R text = updater.apply(player);

        sendPacket(player, ScoreboardPackets.createTeamPacket(ScoreboardPackets.TEAM_CREATED, index, teamName,
                player, text, textProvider));

        sendPacket(player, ScoreboardPackets.createScorePacket(player, 0, objective, score, index));
    }

    public void setScore(int score) {
        this.score = score;
    }

    @SneakyThrows
    static void sendPacket(@NonNull Player player, @NonNull ByteBuf packet) {
        ChannelInjector.IMP.sendPacket(player, packet);
    }
}
