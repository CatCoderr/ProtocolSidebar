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

    public Sidebar(@NonNull String objective, @NonNull String title, @NonNull Plugin owner) {
        this.objective = new ScoreboardObjective(objective, title);
        owner.getServer().getPluginManager().registerEvents(this, owner);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // keep the viewers set actual
        viewers.remove(event.getPlayer().getUniqueId());
    }

    public void setTitle(@NonNull String title) {
        objective.setDisplayName(title);
        broadcast(objective::updateValue);
    }

    // handy methods
    public SidebarLine addStaticLine(@NonNull String text) {
        return addLine0((x) -> text, true);
    }

    public SidebarLine addBlankLine() {
        return addStaticLine("");
    }

    public SidebarLine addLine(@NonNull Function<Player, String> updater) {
        return addLine0(updater, false);
    }

    private SidebarLine addLine0(@NonNull Function<Player, String> updater, boolean staticText) {
        SidebarLine line = new SidebarLine(updater, objective.getName(), staticText);
        lines.add(line);
        return line;
    }

    public void removeLine(@NonNull SidebarLine line) {
        if (lines.remove(line)) {
            broadcast(line::removeTeam);
            update();
        }
    }

    public Optional<SidebarLine> maxLine() {
        return lines.stream()
                .filter(line -> line.getCurrentIndex() != -1)
                .max(Comparator.comparingInt(SidebarLine::getCurrentIndex));
    }

    public Optional<SidebarLine> minLine() {
        return lines.stream()
                .filter(line -> line.getCurrentIndex() != -1)
                .min(Comparator.comparingInt(SidebarLine::getCurrentIndex));
    }

    public void update() {
        int index = lines.size();

        for (SidebarLine line : lines) {
            // if line is not created yet
            if (line.getCurrentIndex() == -1) {
                line.setCurrentIndex(index--);
                broadcast(line::createTeam);
                continue;
            }
            int prevIndex = line.getCurrentIndex();
            line.setCurrentIndex(index--);

            broadcast(p -> line.updateTeam(p, prevIndex));
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

    public void removeViewer(@NonNull Player player) {
        if (viewers.remove(player.getUniqueId())) {
            update();

            lines.forEach(line -> line.removeTeam(player));
            objective.remove(player);
            viewers.remove(player.getUniqueId());
        }
    }

    public void addViewer(@NonNull Player player) {
        if (viewers.add(player.getUniqueId())) {
            update();

            objective.create(player);
            lines.forEach(line -> line.createTeam(player));
            objective.display(player, ScoreboardObjective.DISPLAY_SIDEBAR);
        }
    }

    public Set<UUID> getViewers() {
        return Collections.unmodifiableSet(viewers);
    }

    public List<SidebarLine> getLines() {
        return Collections.unmodifiableList(lines);
    }

    public void broadcast(@NonNull Consumer<Player> consumer) {
        viewers.stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .forEach(consumer);
    }
}
