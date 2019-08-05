package me.catcoder.sidebar;

import com.google.common.base.Joiner;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class SidebarPlugin extends JavaPlugin implements Listener {

  @Override
  public void onEnable() {
    getServer().getPluginManager().registerEvents(this, this);
  }

  @EventHandler
  public void onJoin(PlayerJoinEvent event) {
    Sidebar sidebar = new Sidebar("Test", "Test", this);
    Player player = event.getPlayer();

    sidebar.setLine(1, "Первая линия");
    sidebar.setLine(2, "Вторая линия");
    sidebar.setLine(3, "Третья линия");

    sidebar.send(event.getPlayer());

    BukkitTask updater = periodic(
        () -> sidebar.setLine(2, "§cX§fY§bZ: §e" + toString(player.getLocation())), 1L);

    later(() -> {
      sidebar.unregister(player);
      updater.cancel();
    }, 20 * 20);
  }

  private static String toString(Location location) {
    return Joiner
        .on('/')
        .join((int) location.getX(), (int) location.getY(), (int) location.getZ());
  }

  private BukkitTask later(Runnable task, long delay) {
    return Bukkit.getScheduler().runTaskLaterAsynchronously(this, task, delay);
  }

  private BukkitTask periodic(Runnable task, long period) {
    return Bukkit.getScheduler().runTaskTimerAsynchronously(this, task, 0, period);
  }
}
