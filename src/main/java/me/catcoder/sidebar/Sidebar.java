package me.catcoder.sidebar;

import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class Sidebar implements Listener {

    private final Set<UUID> viewers = new HashSet<>();
    private final List<SidebarLine> lines = new ArrayList<>();
    private final ScoreboardObjective objective;

    @Deprecated
    public Sidebar(@NonNull Plugin owner, @NonNull String objective, @NonNull String title) {
        this.objective = new ScoreboardObjective(objective, title);
        addListener(owner);
    }

    public Sidebar(@NonNull String objective, @NonNull String title) {
        this.objective = new ScoreboardObjective(objective, title);
    }

    public void addListener(@NonNull Plugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        viewers.remove(event.getPlayer().getUniqueId());
    }

    public void setTitle(@NonNull String title) {
        objective.setDisplayName(title);
        broadcast(objective::updateValue);
    }

    public void shiftLine(SidebarLine line, int offset) {
        lines.remove(line);
        lines.add(offset, line);

        updateAllLines(); // recalculate indices
    }

    public BukkitTask updatePeriodically(long delay, long period, @NonNull Plugin plugin) {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::updateAllLines, delay, period);
    }

    public SidebarLine addLine(@NonNull String text) {
        return addLine(x -> text, true);
    }

    public SidebarLine addBlankLine() {
        return addLine("");
    }

    public SidebarLine addDynamicLine(@NonNull Function<Player, String> updater) {
        return addLine(updater, false);
    }

    public SidebarLine addStaticLine(@NonNull Function<Player, String> updater) {
        return addLine(updater, true);
    }

    private SidebarLine addLine(@NonNull Function<Player, String> updater, boolean staticText) {
        SidebarLine line = new SidebarLine(updater, objective.getName() + lines.size(), staticText, lines.size());
        lines.add(line);
        return line;
    }

    public void removeLine(@NonNull SidebarLine line) {
        if (lines.remove(line) && line.getScore() != -1) {
            broadcast(p -> line.removeTeam(p, objective.getName()));
            updateAllLines();
        }
    }

    public Optional<SidebarLine> maxLine() {
        return lines.stream()
                .filter(line -> line.getScore() != -1)
                .max(Comparator.comparingInt(SidebarLine::getScore));
    }

    public Optional<SidebarLine> minLine() {
        return lines.stream()
                .filter(line -> line.getScore() != -1)
                .min(Comparator.comparingInt(SidebarLine::getScore));
    }

    /**
     * Update the single line.
     *
     * @param line - target line.
     */
    public void updateLine(@NonNull SidebarLine line) {
        if (lines.contains(line)) {
            broadcast(p -> line.updateTeam(p, line.getScore(), objective.getName()));
        }
    }

    /**
     * Update all dynamic lines of the sidebar.
     */
    public void updateAllLines() {
        int index = lines.size();

        for (SidebarLine line : lines) {
            // if line is not created yet
            if (line.getScore() == -1) {
                line.setScore(index--);
                broadcast(p -> line.createTeam(p, objective.getName()));
                continue;
            }

            int prevIndex = line.getScore();
            line.setScore(index--);

            broadcast(p -> line.updateTeam(p, prevIndex, objective.getName()));
        }
    }

    public void removeViewers() {
        for (UUID id : new ArrayList<>(viewers)) {
            Player player = Bukkit.getPlayer(id);
            if (player != null) {
                removeViewer(player);
            }
        }
    }

    public void addViewer(@NonNull Player player) {
        if (viewers.add(player.getUniqueId())) {
            updateAllLines();

            objective.create(player);
            lines.forEach(line -> line.createTeam(player, objective.getName()));
            objective.display(player);
        }
    }

    public void removeViewer(@NonNull Player player) {
        if (viewers.remove(player.getUniqueId())) {
            updateAllLines();

            lines.forEach(line -> line.removeTeam(player, objective.getName()));
            objective.remove(player);
        }
    }

    public Set<UUID> getViewers() {
        return Collections.unmodifiableSet(viewers);
    }

    public List<SidebarLine> getLines() {
        return Collections.unmodifiableList(lines);
    }

    public ScoreboardObjective getObjective() {
        return objective;
    }

    private void broadcast(@NonNull Consumer<Player> consumer) {
        viewers.stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .forEach(consumer);
    }
}
