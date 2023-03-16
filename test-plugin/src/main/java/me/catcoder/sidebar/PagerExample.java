package me.catcoder.sidebar;

import me.catcoder.sidebar.pager.SidebarPager;
import me.catcoder.sidebar.text.TextIterators;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;

public class PagerExample {

    public SidebarPager<Component> createPager(Plugin plugin) {
        Sidebar<Component> anotherSidebar = ProtocolSidebar.newAdventureSidebar(
                TextIterators.textFadeHypixel("ANOTHER SIDEBAR"), plugin);

        Sidebar<Component> firstSidebar = ProtocolSidebar.newAdventureSidebar(
                TextIterators.textFadeHypixel("SIDEBAR"), plugin);

        SidebarPager<Component> pager = new SidebarPager<>(Arrays.asList(firstSidebar, anotherSidebar), 20 * 5, plugin);

        pager.addPageLine((page, maxPage, sidebar) ->
                sidebar.addLine(Component
                        .text("Page " + page + "/" + maxPage)
                        .color(NamedTextColor.GREEN)));

        pager.applyToAll(Sidebar::addBlankLine);

        firstSidebar.addLine(
                Component.text("Just a static line").color(NamedTextColor.GREEN));
        firstSidebar.addBlankLine();
        firstSidebar.addUpdatableLine(
                player -> Component.text("Your Hunger: ")
                        .append(Component.text(player.getFoodLevel()).color(NamedTextColor.GREEN))
        );
        firstSidebar.addBlankLine();
        firstSidebar.addUpdatableLine(
                player -> Component.text("Your Health: ")
                        .append(Component.text(player.getHealth()).color(NamedTextColor.GREEN))
        );
        firstSidebar.addBlankLine();
        firstSidebar.addLine(
                Component.text("https://github.com/CatCoderr/ProtocolSidebar")
                        .color(NamedTextColor.YELLOW
                        ));

        firstSidebar.updateLinesPeriodically(0, 10);


        anotherSidebar.addBlankLine();
        anotherSidebar.addLine(
                Component.text("Just a static line").color(NamedTextColor.GREEN));
        anotherSidebar.addLine(
                Component.text("Just a static line 2").color(NamedTextColor.YELLOW)
        );

        anotherSidebar.addBlankLine();

        return pager;
    }
}
