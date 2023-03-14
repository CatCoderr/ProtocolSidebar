package me.catcoder.sidebar;

import me.catcoder.sidebar.pager.SidebarPager;
import me.catcoder.sidebar.text.TextIterator;
import me.catcoder.sidebar.text.TextIterators;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public class TestPlugin extends JavaPlugin implements Listener {

    private SidebarPager pager;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);

        TextIterator typingAnimation = TextIterators.textTypingOldSchool("Hello World! It's a test plugin for ProtocolSidebar!");
        TextIterator lineFade = TextIterators.textFadeHypixel("https://github.com/CatCoderr/ProtocolSidebar");
        TextIterator title = TextIterators.textFadeHypixel("Hello World!");

        Sidebar sidebar = new Sidebar(title, this);
        Sidebar testSidebar = new Sidebar(TextIterators.textFadeHypixel("Test"), this);

        testSidebar.addBlankLine();
        testSidebar.addLine("Test Static Line");
        testSidebar.addLine("Test Static Line2");
        testSidebar.addLine("Test Static Line3");
        testSidebar.addBlankLine();

        ///

        pager = new SidebarPager(Arrays.asList(sidebar, testSidebar), 3 * 20L, this);

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

        sidebar.addUpdatableLine(typingAnimation.asLineUpdater());

        sidebar.addBlankLine();
        sidebar.addUpdatableLine(lineFade.asLineUpdater());
        sidebar.addBlankLine();

        pager.addPageLine(new ComponentBuilder("Page: ")
                .color(ChatColor.YELLOW),
                "%d/%d",
                builder -> builder.bold(true).color(ChatColor.GREEN));

        sidebar.updateLinesPeriodically(0L, 1L);
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        pager.show(event.getPlayer());
    }
}
