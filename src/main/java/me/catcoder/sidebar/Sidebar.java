package me.catcoder.sidebar;

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import me.catcoder.sidebar.text.TextIterator;
import me.catcoder.sidebar.text.TextProvider;
import me.catcoder.sidebar.util.RandomString;
import me.catcoder.sidebar.util.lang.ThrowingConsumer;
import me.catcoder.sidebar.util.lang.ThrowingFunction;
import me.catcoder.sidebar.util.lang.ThrowingPredicate;
import me.catcoder.sidebar.util.lang.ThrowingSupplier;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

/**
 * Represents a sidebar.
 * <p>
 * Sidebar is a scoreboard with a title and lines.
 * <p>
 */

@FieldDefaults(level = AccessLevel.PACKAGE)
public class Sidebar<R> {

    private static final String OBJECTIVE_PREFIX = "PS-";
    private static final int MAX_LINES_COUNT = 15;

    private final Set<UUID> viewers = Collections.synchronizedSet(new HashSet<>());
    private final List<SidebarLine<R>> lines = new ArrayList<>();
    @Getter
    private final ScoreboardObjective<R> objective;

    private TextIterator titleText;
    private BukkitTask titleUpdater;

    final Set<Integer> taskIds = new HashSet<>();
    final TextProvider<R> textProvider;

    @Getter
    private final Plugin plugin;

    /**
     * Construct a new sidebar instance.
     *
     * @param title  a title of sidebar
     * @param plugin plugin instance
     */
    Sidebar(@NonNull R title, @NonNull Plugin plugin, @NonNull TextProvider<R> textProvider) {
        this.plugin = plugin;
        this.textProvider = textProvider;
        this.objective = new ScoreboardObjective<>(OBJECTIVE_PREFIX + RandomString.generate(3), title, textProvider);
    }

    /**
     * Construct a new sidebar instance.
     *
     * @param titleIterator a title iterator of sidebar
     * @param plugin        plugin instance
     */
    Sidebar(@NonNull TextIterator titleIterator, @NonNull Plugin plugin, @NonNull TextProvider<R> textProvider) {
        this.plugin = plugin;
        this.textProvider = textProvider;

        this.objective = new ScoreboardObjective<>(
                OBJECTIVE_PREFIX + RandomString.generate(3),
                textProvider.fromLegacyMessage(titleIterator.next()),
                textProvider);

        setTitleIter(titleIterator);
    }

    /**
     * Converts TextIterator to line updater.
     *
     * @param iterator - iterator
     * @return line updater
     */
    public ThrowingFunction<Player, R, Throwable> toLineUpdater(@NonNull TextIterator iterator) {
        return player -> textProvider.fromLegacyMessage(iterator.next());
    }

    /**
     * Update the title of the sidebar.
     *
     * @param title title to be updated
     */
    public void setTitle(@NonNull R title) {
        cancelTitleUpdater();

        objective.setDisplayName(title);
        broadcast(objective::updateValue);
    }

    /**
     * Update the title of the sidebar.
     *
     * @param iterator - title iterator
     */
    public void setTitle(@NonNull TextIterator iterator) {
        setTitleIter(iterator);
    }

    private void cancelTitleUpdater() {
        if (titleUpdater != null) {
            titleUpdater.cancel();
            titleUpdater = null;
        }

        this.titleText = null;
    }

    private void setTitleIter(@NonNull TextIterator iterator) {
        cancelTitleUpdater();

        this.titleText = iterator;
        this.titleUpdater = plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            String next = titleText.next();

            objective.setDisplayName(textProvider.fromLegacyMessage(next));
            broadcast(objective::updateValue);
        }, 0, 1);
    }

    /**
     * Updates the index of the line shifting it by an offset.
     *
     * @param line   the line
     * @param offset the offset
     */
    public void shiftLine(SidebarLine<R> line, int offset) {
        synchronized (lines) {
            lines.remove(line);
            lines.add(offset, line);
        }

        updateAllLines(); // recalculate indices
    }

    /**
     * Binds a bukkit task to this sidebar.
     * When the sidebar is destroyed, the task will be cancelled.
     *
     * @param task - task to bind
     * @return the task
     */
    public BukkitTask bindBukkitTask(@NonNull BukkitTask task) {
        this.taskIds.add(task.getTaskId());
        return task;
    }

    /**
     * Schedules the async task to update all dynamic lines at fixed rate.
     *
     * @param delay  delay in ticks
     * @param period period in ticks
     * @return the scheduled task
     */
    public BukkitTask updateLinesPeriodically(long delay, long period) {
        return updateLinesPeriodically(delay, period, true);
    }

    /**
     * Schedules the task to update all dynamic lines at fixed rate.
     *
     * @param delay  delay in ticks
     * @param period period in ticks
     * @param async  whether the task should be executed asynchronously
     * @return the scheduled task
     */
    public BukkitTask updateLinesPeriodically(long delay, long period, boolean async) {
        return async ?
                bindBukkitTask(Bukkit.getScheduler()
                        .runTaskTimerAsynchronously(plugin, this::updateAllLines, delay, period)) :
                bindBukkitTask(Bukkit.getScheduler()
                        .runTaskTimer(plugin, this::updateAllLines, delay, period));
    }

    /**
     * Add a line with specific display condition.
     * If the condition is false, the line will be hidden for player.
     * <p>
     *
     * @param updater   - the function that updates the text
     * @param condition - the condition
     * @return SidebarLine instance
     */
    public SidebarLine<R> addConditionalLine(@NonNull ThrowingFunction<Player, R, Throwable> updater,
                                             @NonNull ThrowingPredicate<Player, Throwable> condition) {
        return addLine(updater, false, condition);
    }

    /**
     * Add a line with static text.
     *
     * @param text - the text
     * @return SidebarLine instance
     */
    public SidebarLine<R> addTextLine(@NonNull String text) {
        return addLine(textProvider.fromLegacyMessage(text));
    }

    /**
     * Add a line to the sidebar with dynamic text.
     *
     * @param updater - the function that updates the text
     * @return SidebarLine instance
     */
    public SidebarLine<R> addUpdatableLine(@NonNull ThrowingFunction<Player, R, Throwable> updater) {
        return addLine(updater, false, x -> true);
    }

    /**
     * Add a line to the sidebar with dynamic text with no player argument.
     *
     * @param updater - the function that updates the text
     * @return SidebarLine instance
     */
    public SidebarLine<R> addUpdatableLine(ThrowingSupplier<R, Throwable> updater) {
        return addUpdatableLine(player -> updater.get());
    }

    /**
     * Add a line to the sidebar with static text.
     *
     * @param text the text
     * @return SidebarLine instance
     */
    public SidebarLine<R> addLine(@NonNull R text) {
        return addLine(x -> text, true, x -> true);
    }

    /**
     * Add a blank line to the sidebar.
     *
     * @return SidebarLine instance
     */
    public SidebarLine<R> addBlankLine() {
        return addTextLine("");
    }

    private SidebarLine<R> addLine(@NonNull ThrowingFunction<Player, R, Throwable> updater, boolean staticText,
                                   @NonNull ThrowingPredicate<Player, Throwable> predicate) {
        synchronized (lines) {
            Preconditions.checkArgument(
                    lines.size() <= MAX_LINES_COUNT, "Cannot add more than %s lines to a sidebar", MAX_LINES_COUNT);

            SidebarLine<R> line = new SidebarLine<>(
                    updater, objective.getName() + lines.size(),
                    staticText, lines.size(), textProvider, predicate);

            lines.add(line);
            return line;
        }
    }

    /**
     * Removes line from sidebar.
     *
     * @param line the line
     */
    public void removeLine(@NonNull SidebarLine<R> line) {
        synchronized (lines) {
            if (lines.remove(line) && line.getScore() != -1) {
                broadcast(p -> line.removeTeam(p, objective.getName()));
                updateAllLines();
            }
        }
    }

    /**
     * Get line with maximum score.
     *
     * @return SidebarLine
     */
    public Optional<SidebarLine<R>> maxLine() {
        synchronized (lines) {
            return lines.stream()
                    .filter(line -> line.getScore() != -1)
                    .max(Comparator.comparingInt(SidebarLine::getScore));
        }
    }

    /**
     * Get the line with minimum score.
     *
     * @return SidebarLine
     */
    public Optional<SidebarLine<R>> minLine() {
        synchronized (lines) {
            return lines.stream()
                    .filter(line -> line.getScore() != -1)
                    .min(Comparator.comparingInt(SidebarLine::getScore));
        }
    }

    /**
     * Update the single line.
     *
     * @param line target line.
     */
    public void updateLine(@NonNull SidebarLine<R> line) {
        synchronized (lines) {
            Preconditions.checkArgument(lines.contains(line), "Line %s is not a part of this sidebar", line);

            broadcast(p -> line.updateTeam(p, objective.getName()));
        }
    }

    /**
     * Update all dynamic lines of the sidebar.
     * Except lines with their own update task. (see {@link SidebarLine#updatePeriodically(long, long, Sidebar)})
     */
    public void updateAllLines() {
        synchronized (lines) {
            int index = lines.size();

            for (SidebarLine<R> line : lines) {
                // if line is not created yet
                if (line.getScore() == -1) {
                    line.setScore(index--);
                    broadcast(p -> line.createTeam(p, objective.getName()));
                    continue;
                }

                if (line.updateTask != null && !line.updateTask.isCancelled()) {
                    // Don't update the line if it's already has its own update task
                    continue;
                }

                line.setScore(index--);

                broadcast(p -> line.updateTeam(p, objective.getName()));
            }
        }
    }

    /**
     * Remove all viewers currently receiving this sidebar.
     */
    public void removeViewers() {
        synchronized (viewers) {
            for (Iterator<UUID> iterator = viewers.iterator(); iterator.hasNext(); ) {
                UUID uuid = iterator.next();
                Player player = Bukkit.getPlayer(uuid);

                if (player != null) {
                    removeViewer0(player);
                }

                iterator.remove();
            }
        }
    }

    /**
     * Remove all viewers and cancel all tasks.
     * <p>
     * This method should be called when the sidebar is no longer needed.
     * Otherwise, the sidebar will be kept in memory and will be updated
     * for all players.
     * <p>
     */
    public void destroy() {
        cancelTitleUpdater();

        for (int taskId : taskIds) {
            Bukkit.getScheduler().cancelTask(taskId);
        }

        removeViewers();

        synchronized (lines) {
            lines.clear();
        }

        taskIds.clear();
    }

    /**
     * Sends this sidebar with all lines to the player.
     *
     * @param player target player
     */

    @SneakyThrows
    public void addViewer(@NonNull Player player) {
        if (!viewers.contains(player.getUniqueId())) {
            objective.create(player);

            synchronized (lines) {
                for (SidebarLine<R> line : lines) {
                    line.createTeam(player, objective.getName());
                }
            }

            objective.display(player);

            viewers.add(player.getUniqueId());
        }
    }

    /**
     * Removes sidebar for the target player.
     *
     * @param player target player
     */
    public void removeViewer(@NonNull Player player) {
        synchronized (viewers) {
            if (viewers.remove(player.getUniqueId())) {
                removeViewer0(player);
            }
        }
    }

    private void removeViewer0(@NonNull Player player) {
        lines.forEach(line -> line.removeTeam(player, objective.getName()));
        objective.remove(player);
    }

    /**
     * Returns the set of all players currently receiving this sidebar.
     *
     * @return a set with player UUIDs
     */
    public Set<UUID> getViewers() {
        return Collections.unmodifiableSet(viewers);
    }

    /**
     * Returns the list of all lines in this sidebar.
     *
     * @return a list of lines
     */
    public List<SidebarLine<R>> getLines() {
        synchronized (lines) {
            return Collections.unmodifiableList(lines);
        }
    }

    private void broadcast(@NonNull ThrowingConsumer<Player, Throwable> consumer) {
        synchronized (viewers) {
            viewers.removeIf(uuid -> Bukkit.getPlayer(uuid) == null);

            for (UUID id : viewers) {
                // double check
                Player player = Bukkit.getPlayer(id);
                if (player == null) {
                    continue;
                }

                try {
                    consumer.accept(player);
                } catch (Throwable e) {
                    throw new RuntimeException("An error occurred while updating sidebar for player: " + player.getName(),
                            e);
                }
            }
        }
    }
}
