package me.catcoder.sidebar;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class ProtocolSidebarPlugin extends JavaPlugin implements Listener {

    Sidebar<String> sidebar;

    @Override
    public void onEnable() {
        super.onEnable();
        getLogger().info("ProtocolSidebar plugin enabled");
        sidebar = ProtocolSidebar.newMiniMessageSidebar("<red>TEST</red>", this);
        sidebar.addLine("TEST");
        sidebar.addLine("TEST");
        sidebar.addLine("TEST");
        sidebar.addLine("TEST");

        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        sidebar.addViewer(player);
    }
}
