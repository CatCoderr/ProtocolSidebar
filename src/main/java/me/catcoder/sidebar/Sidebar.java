package me.catcoder.sidebar;

import com.google.common.base.Preconditions;
import com.tcoded.folialib.FoliaLib;
import com.tcoded.folialib.wrapper.task.WrappedBukkitTask;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import me.catcoder.sidebar.protocol.ScoreboardPackets;
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
import java.util.logging.Level;

/**
 * Represents a sidebar.
 * <p>
 * Sidebar is a scoreboard with a title and lines.
 * <p>
 */

@FieldDefaults(level = AccessLevel.PACKAGE)
public class Sidebar<R> {

    private static final String OBJECTIVE_PREFIX = "PS-";
    public static final int MAX_VISIBLE_LINES = 15;
    public static final int MAX_LINES_COUNT = ScoreboardPackets.COLORS.length;

    private final Set<UUID> viewers = Collections.synchronizedSet(new HashSet<>());
    private final List<SidebarLine<R>> lines = new ArrayList<>();
    private final LineIndexAllocator indexAllocator = new LineIndexAllocator(MAX_LINES_COUNT);

    private boolean warnedAboutOverflow;
    @Getter
    private final ScoreboardObjective<R> objective;

    private TextIterator titleText;
    private WrappedTask titleUpdater;

    final Set<WrappedTask> tasks = new HashSet<>();
    final TextProvider<R> textProvider;

    @Getter
    private final Plugin plugin;

    @Getter
    private final FoliaLib foliaLib;

    /**
     * Construct a new sidebar instance.
     *
     * @param title  a title of sidebar
     * @param plugin plugin instance
     */
    Sidebar(@NonNull R title, @NonNull Plugin plugin, @NonNull TextProvider<R> textProvider) {
        this.plugin = plugin;
        this.foliaLib = new FoliaLib(plugin);
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
        this.foliaLib = new FoliaLib(plugin);
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

    /**
     * Update the title of the sidebar with a per-player title.
     * The updater is invoked for each viewer separately, so the title
     * may differ between players.
     * <p>
     * The title is resolved for every objective packet sent to a player, so it is
     * re-evaluated whenever a player is added as a viewer. Call {@link #updateTitle()}
     * to re-evaluate it for all current viewers.
     *
     * @param updater - the function that produces the title for a player
     */
    public void setTitle(@NonNull ThrowingFunction<Player, R, Throwable> updater) {
        // otherwise a running title animation would keep overwriting the per-player title
        cancelTitleUpdater();

        objective.setDisplayNameUpdater(updater);
        broadcast(objective::updateValue);
    }

    /**
     * Update the title of the sidebar with a title chosen by conditions.
     * Resolved per player like {@link #setTitle(ThrowingFunction)}, and the conditions are
     * snapshotted, so mutating {@code title} afterward has no effect.
     *
     * @param title - the conditional title, requires a {@link ConditionalTitle#otherwise} title
     */
    public void setTitle(@NonNull ConditionalTitle<R> title) {
        // fail here rather than while building a packet for the first player that matches nothing
        Preconditions.checkArgument(title.hasFallback(),
                "ConditionalTitle requires an otherwise(...) title, "
                        + "players matching no condition cannot be given a title");

        setTitle(title.toUpdater());
    }

    /**
     * Re-sends the title to all current viewers, re-evaluating any
     * per-player or conditional title.
     */
    public void updateTitle() {
        broadcast(objective::updateValue);
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
        this.titleUpdater = foliaLib.getScheduler().runTimerAsync(() -> {
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
            Preconditions.checkArgument(lines.contains(line), "Line %s is not a part of this sidebar", line);
            Preconditions.checkPositionIndex(offset, lines.size() - 1, "Line offset");

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
     *
     * @deprecated Use {@link #bindWrappedTask(WrappedTask)} instead.
     */
    @Deprecated(since = "6.2.9")
    public BukkitTask bindBukkitTask(@NonNull BukkitTask task) {
        this.tasks.add(new WrappedBukkitTask(task));
        return task;
    }

    /**
     * Binds a {@link WrappedTask} to this sidebar.
     * When the sidebar is destroyed, the task will be cancelled.
     *
     * @param task - task to bind
     * @return the task
     */
    public WrappedTask bindWrappedTask(@NonNull WrappedTask task) {
        this.tasks.add(task);
        return task;
    }

    /**
     * Schedules the async task to update all dynamic lines at fixed rate.
     *
     * @param delay  delay in ticks
     * @param period period in ticks
     * @return the scheduled task
     */
    public WrappedTask updateLinesPeriodically(long delay, long period) {
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
    public WrappedTask updateLinesPeriodically(long delay, long period, boolean async) {
        return async ?
                bindWrappedTask(foliaLib.getScheduler()
                        .runTimerAsync(this::updateAllLines, delay, period)) :
                bindWrappedTask(foliaLib.getScheduler()
                        .runTimer(this::updateAllLines, delay, period));
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
        return addLine(updater, false, SidebarLine.ALWAYS_VISIBLE);
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
        return addLine(x -> text, true, SidebarLine.ALWAYS_VISIBLE);
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
            Preconditions.checkArgument(lines.size() < MAX_LINES_COUNT,
                    "Cannot add more than %s lines to a sidebar", MAX_LINES_COUNT);

            if (predicate == SidebarLine.ALWAYS_VISIBLE) {
                Preconditions.checkArgument(unconditionalLineCount() < MAX_VISIBLE_LINES,
                        "Cannot add more than %s always visible lines to a sidebar, the client renders "
                                + "at most that many. Use addConditionalLine(..) for lines that are not "
                                + "always shown.", MAX_VISIBLE_LINES);
            }

            int index = indexAllocator.allocate();

            SidebarLine<R> line = new SidebarLine<>(
                    updater, objective.getName() + index,
                    staticText, index, textProvider, predicate);

            lines.add(line);
            return line;
        }
    }

    private int unconditionalLineCount() {
        int count = 0;

        for (SidebarLine<R> line : lines) {
            if (!line.isConditional()) {
                count++;
            }
        }

        return count;
    }

    /**
     * Removes line from sidebar.
     *
     * @param line the line
     */
    public void removeLine(@NonNull SidebarLine<R> line) {
        synchronized (lines) {
            if (!lines.remove(line)) {
                return;
            }

            indexAllocator.release(line.getIndex());

            if (line.getScore() != -1) {
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
     * Lines with their own update task keep their text, only their score is sent.
     * (see {@link SidebarLine#updatePeriodically(long, long, Sidebar)})
     */
    public void updateAllLines() {
        synchronized (lines) {
            LineAction[] actions = new LineAction[lines.size()];
            int score = lines.size();

            for (int i = 0; i < lines.size(); i++) {
                SidebarLine<R> line = lines.get(i);

                // if line is not created yet
                if (line.getScore() == -1) {
                    line.setScore(score--);
                    actions[i] = LineAction.CREATE;
                    continue;
                }

                if (line.updateTask != null && !line.updateTask.isCancelled()) {
                    // its own task owns the text, but the score is positional and has to follow
                    line.setScore(score--);
                    actions[i] = LineAction.SCORE_ONLY;
                    continue;
                }

                line.setScore(score--);
                actions[i] = LineAction.UPDATE;
            }

            broadcast(player -> {
                int visible = 0;

                for (int i = 0; i < actions.length; i++) {
                    SidebarLine<R> line = lines.get(i);
                    boolean shown;

                    switch (actions[i]) {
                        case CREATE:
                            shown = line.createTeam(player, objective.getName());
                            break;
                        case UPDATE:
                            shown = line.updateTeam(player, objective.getName());
                            break;
                        default:
                            shown = line.updateScore(player, objective.getName());
                            break;
                    }

                    if (shown) {
                        visible++;
                    }
                }

                warnOnOverflow(player, visible);
            });
        }
    }

    private void warnOnOverflow(@NonNull Player player, int visible) {
        if (warnedAboutOverflow || visible <= MAX_VISIBLE_LINES) {
            return;
        }

        warnedAboutOverflow = true;

        plugin.getLogger().warning(String.format(
                "Sidebar %s has %s lines visible for %s, but the client renders at most %s. "
                        + "The lowest scored lines will not be shown. "
                        + "This is logged once per sidebar.",
                objective.getName(), visible, player.getName(), MAX_VISIBLE_LINES));
    }

    private enum LineAction {
        CREATE,
        UPDATE,
        SCORE_ONLY
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

        for (WrappedTask task : tasks) {
            foliaLib.getScheduler().cancelTask(task);
        }

        removeViewers();

        synchronized (lines) {
            lines.clear();
            indexAllocator.releaseAll();
        }

        tasks.clear();
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
                if (hasUnscoredLines()) {
                    updateAllLines();
                }

                int visible = 0;

                for (SidebarLine<R> line : lines) {
                    if (line.createTeam(player, objective.getName())) {
                        visible++;
                    }
                }

                warnOnOverflow(player, visible);
            }

            objective.display(player);

            viewers.add(player.getUniqueId());
        }
    }

    private boolean hasUnscoredLines() {
        for (SidebarLine<R> line : lines) {
            if (line.getScore() == -1) {
                return true;
            }
        }

        return false;
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
                    plugin.getLogger().log(Level.SEVERE,
                            "An error occurred while updating sidebar for player: " + player.getName(), e);
                }
            }
        }
    }
}
