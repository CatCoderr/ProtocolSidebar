package me.catcoder.sidebar;

import me.catcoder.sidebar.wrapper.WrapperPlayServerScoreboardObjective;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class SidebarPlugin extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    /**
     * Тестирование
     *
     * @param event - Событие {@link PlayerJoinEvent}
     */
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Sidebar sidebar = new Sidebar();
        sidebar.setObjective(new SidebarObjective(
                "test",
                WrapperPlayServerScoreboardObjective.HealthDisplay.INTEGER,
                "Test"));

        sidebar.setLine(1, "Первая линия");
        sidebar.setLine(2, "Вторая линия");
        sidebar.setLine(3, "Третья линия");

        sidebar.send(event.getPlayer());

        Player player = event.getPlayer();

        later(() -> player.sendMessage("Начинаем тест"), 20);

        later(() -> {
            player.sendMessage("Изменяем тайтл");
            sidebar.getObjective().setDisplayName("Изменено", sidebar);
        }, 40);

        later(() -> {
            player.sendMessage("Изменяем строку");
            sidebar.setLine(2, "Изменено");
        }, 60);

        later(() -> {
            player.sendMessage("Изменяем objective");
            sidebar.setObjective(new SidebarObjective(
                    "test2",
                    WrapperPlayServerScoreboardObjective.HealthDisplay.INTEGER,
                    "Objective #2"
            ));
        }, 80);
    }

    private void later(Runnable task, int delay) {
        Bukkit.getScheduler().runTaskLater(this, task, delay);
    }
}
