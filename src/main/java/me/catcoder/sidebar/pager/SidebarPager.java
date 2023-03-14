package me.catcoder.sidebar.pager;

import com.google.common.collect.Iterators;
import lombok.NonNull;
import me.catcoder.sidebar.Sidebar;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

public class SidebarPager {

    private final List<Sidebar> sidebars;
    private final Iterator<Sidebar> pageIterator;
    private final Set<UUID> viewers;
    private final BukkitTask switchTask;
    private Sidebar currentPage;

    /**
     * Creates a new sidebar pager.
     *
     * @param sidebars         - list of sidebars to use
     * @param switchDelayTicks - delay between page switches in ticks
     * @param plugin           - plugin instance
     */
    public SidebarPager(@NonNull List<Sidebar> sidebars, long switchDelayTicks, @NonNull Plugin plugin) {
        this.sidebars = sidebars;
        this.viewers = new HashSet<>();
        this.pageIterator = Iterators.cycle(sidebars);
        this.currentPage = pageIterator.next();
        this.switchTask = plugin.getServer().getScheduler().runTaskTimer(plugin, this::switchPage, switchDelayTicks, switchDelayTicks);
    }

    /**
     * Switches to the next page.
     * Note: this method is called automatically by the scheduler.
     */
    public void switchPage() {
        currentPage.removeViewers();

        currentPage = pageIterator.next();

        for (UUID viewer : viewers) {
            Player player = Bukkit.getPlayer(viewer);
            if (player != null) {
                currentPage.addViewer(player);
            }
        }
    }

    public Set<UUID> getViewers() {
        return Collections.unmodifiableSet(viewers);
    }

    public List<Sidebar> getSidebars() {
        return Collections.unmodifiableList(sidebars);
    }

    /**
     * Adds a page status line to all sidebars in pager.
     *
     * @param builder    - component builder to use
     * @param pageFormat - page format, e.g. "Page %d/%d", where %d is a page number, and %d is a max page number
     * @param formatter  - formatter to use for each page, can be null
     */
    public void addPageLine(@NonNull ComponentBuilder builder,
                            @NonNull String pageFormat,
                            @Nullable Consumer<ComponentBuilder> formatter) {
        int page = 1;
        int maxPage = sidebars.size();

        for (Sidebar sidebar : sidebars) {
            builder.append(String.format(pageFormat, page, maxPage));

            if (formatter != null) {
                formatter.accept(builder);
            }

            sidebar.addLine(builder.create());

            builder.removeComponent(builder.getParts().size() - 1);

            page++;
        }
    }

    /**
     * Adds a page status line to all sidebars in pager.
     *
     * @param builder    - component builder to use
     * @param pageFormat - page format, e.g. "Page %d/%d", where %d is a page number, and %d is a max page number
     */
    public void addPageLine(@NonNull ComponentBuilder builder,
                            @NonNull String pageFormat) {
        addPageLine(builder, pageFormat, null);
    }

    /**
     * Adds a page status line to all sidebars in pager.
     *
     * @param builder - component builder to use
     */
    public void addPageLine(@NonNull ComponentBuilder builder) {
        addPageLine(builder, "Page %d/%d");
    }

    /**
     * Destroy all sidebars in pager.
     * Note: pager object will be unusable after this method call.
     */
    public void destroy() {
        switchTask.cancel();
        for (Sidebar sidebar : sidebars) {
            sidebar.destroy();
        }
        sidebars.clear();
        viewers.clear();
    }

    /**
     * Start showing all sidebars in pager to player.
     *
     * @param player - player to show sidebars to
     */
    public void show(@NonNull Player player) {
        viewers.add(player.getUniqueId());
        currentPage.addViewer(player);
    }

    /**
     * Stop showing all sidebars in pager to player.
     *
     * @param player - player to stop showing sidebars to
     */
    public void hide(@NonNull Player player) {
        viewers.remove(player.getUniqueId());
        currentPage.removeViewer(player);
    }
}
