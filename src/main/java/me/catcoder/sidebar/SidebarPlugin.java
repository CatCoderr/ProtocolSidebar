package me.catcoder.sidebar;

import com.google.common.base.Joiner;
import me.catcoder.sidebar.utilities.updater.SidebarUpdater;
import me.catcoder.sidebar.wrapper.WrapperPlayServerScoreboardObjective;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.Executors;

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

        SidebarUpdater sidebarUpdater = SidebarUpdater.newUpdater(sidebar, Executors.newSingleThreadExecutor());

        sidebarUpdater
                .newTask(
                        bar -> bar.setLine(1, "Время: " + player.getWorld().getTime()),
                        2L
                )
                .newTask(
                        bar -> bar.setLine(2, "XYZ: " + toString(player.getLocation())),
                        5L
                );

        sidebarUpdater.start();

        later(() -> {
            player.sendMessage("Остановка updater'а");
            sidebarUpdater.stop();
        }, (int) (5 * 20L));

    }

    private static String toString(Location location) {
        return Joiner
                .on('/')
                .join((int) location.getX(), (int) location.getY(), (int) location.getZ());
    }

    private void later(Runnable task, int delay) {
        Bukkit.getScheduler().runTaskLater(this, task, delay);
    }
}
