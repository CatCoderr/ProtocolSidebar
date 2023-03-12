package me.catcoder.sidebar;

import me.catcoder.sidebar.text.TextIterator;
import me.catcoder.sidebar.text.TextIterators;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class TestPlugin extends JavaPlugin implements Listener {

    private Sidebar sidebar;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);

        sidebar = new Sidebar(TextIterators.textFadeHypixel("Hello World!"), this);

        sidebar.addLine("Test Static Line");
        sidebar.addBlankLine();
        sidebar.addUpdatableLine(player -> new ComponentBuilder("Your Health: ")
                .append(player.getHealth() + "")
                .color(ChatColor.GREEN)
                .create());

        sidebar.addBlankLine();
        sidebar.addUpdatableLine(player -> new ComponentBuilder("Your Hunger: ")
                .append(player.getFoodLevel() + "")
                .color(ChatColor.GREEN)
                .create());
        sidebar.addBlankLine();
        sidebar.addLine("§ehttps://github.com/CatCoderr/ProtocolSidebar");

        sidebar.updateLinesPeriodically(0L, 20L, this);
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        sidebar.addViewer(event.getPlayer());
    }
}
