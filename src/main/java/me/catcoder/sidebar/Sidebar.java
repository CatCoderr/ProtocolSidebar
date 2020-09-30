package me.catcoder.sidebar;

import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class Sidebar implements Listener {

    private final Set<UUID> viewers = new HashSet<>();
    private final List<SidebarLine> lines = new ArrayList<>();
    private final ScoreboardObjective objective;

    public Sidebar(@NonNull Plugin owner, @NonNull String objective, @NonNull String title) {
        this.objective = new ScoreboardObjective(objective, title);

        owner.getServer().getPluginManager().registerEvents(this, owner);
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

        update(); // recalculate indices
    }

    public SidebarLine addLine(@NonNull String text) {
        return addLine(x -> text, true);
    }

    public SidebarLine addBlankLine() {
        return addLine("");
    }

    public SidebarLine addLine(@NonNull Function<Player, String> updater) {
        return addLine(updater, false);
    }

    private SidebarLine addLine(@NonNull Function<Player, String> updater, boolean staticText) {
        SidebarLine line = new SidebarLine(updater, objective.getName() + lines.size(), staticText, lines.size());
        lines.add(line);
        return line;
    }

    public void removeLine(@NonNull SidebarLine line) {
        if (lines.remove(line) && line.getScore() != -1) {
            broadcast(p -> line.removeTeam(p, objective.getName()));
            update();
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

    public void update() {
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
            update();

            objective.create(player);
            lines.forEach(line -> line.createTeam(player, objective.getName()));
            objective.display(player);
        }
    }

    public void removeViewer(@NonNull Player player) {
        if (viewers.remove(player.getUniqueId())) {
            update();

            lines.forEach(line -> line.removeTeam(player, objective.getName()));
            objective.remove(player);
            viewers.remove(player.getUniqueId());
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
