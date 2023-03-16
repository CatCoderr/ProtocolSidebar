package me.catcoder.sidebar;

import me.catcoder.sidebar.text.TextIterators;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class LeaderboardExample extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        Map<String, Double> scores = new HashMap<>();

        // put some scores
        scores.put("Bob", 0.0);
        scores.put("Alice", 0.0);
        scores.put("John", 0.0);

        Sidebar<Component> sidebar = ProtocolSidebar.newAdventureSidebar(
                TextIterators.textFadeHypixel("SIDEBAR"), this);

        // create a list for lines that will display top positions
        List<SidebarLine<Component>> lines = new ArrayList<>();

        sidebar.addLine(Component
                .text("TOP 3:")
                .decorate(TextDecoration.BOLD)
                .color(NamedTextColor.GREEN));

        sidebar.addBlankLine();

        lines.add(sidebar.addUpdatableLine(() -> Component.text("Hello World! 1")));
        lines.add(sidebar.addUpdatableLine(() -> Component.text("Hello World! 2")));
        lines.add(sidebar.addUpdatableLine(() -> Component.text("Hello World! 3")));

        sidebar.addBlankLine();
        sidebar.addUpdatableLine(player ->
                Component.text("www.example.com").color(NamedTextColor.YELLOW));

        sidebar.updateLinesPeriodically(0, 10);

        getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            // fill scores with random values

            scores.forEach((name, score) -> scores.put(name, Math.round(Math.random() * 100.0) / 100.0));

            updateLeaderboard(lines, scores);
        }, 0, 20);

        getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onJoin(org.bukkit.event.player.PlayerJoinEvent event) {
                sidebar.addViewer(event.getPlayer());
            }
        }, this);

    }

    public void updateLeaderboard(List<SidebarLine<Component>> lines, Map<String, Double> scores) {
        // sort scores
        List<Map.Entry<String, Double>> sorted = scores.entrySet().stream()
                .sorted(Comparator.comparingDouble(Map.Entry::getValue))
                .toList();

        // update lines
        for (int i = 0; i < lines.size(); i++) {
            SidebarLine<Component> line = lines.get(i);
            Map.Entry<String, Double> entry = sorted.get(i);

            line.setUpdater(() -> Component.text(entry.getKey())
                    .color(NamedTextColor.GREEN)
                    .append(Component.text(": "))
                    .append(Component.text(entry.getValue()).color(NamedTextColor.YELLOW)));
        }
    }
}
