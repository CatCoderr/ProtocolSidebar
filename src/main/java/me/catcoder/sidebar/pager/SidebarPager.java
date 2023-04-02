package me.catcoder.sidebar.pager;

import com.google.common.collect.Iterators;
import lombok.NonNull;
import me.catcoder.sidebar.Sidebar;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.function.Consumer;

public class SidebarPager<R> {

    private final List<Sidebar<R>> sidebars;
    private final Iterator<Sidebar<R>> pageIterator;
    private final Set<UUID> viewers;
    private final BukkitTask switchTask;
    private Sidebar<R> currentPage;

    /**
     * Creates a new sidebar pager.
     *
     * @param sidebars         - list of sidebars to use
     * @param switchDelayTicks - delay between page switches in ticks (if value is 0, pages will not be switched automatically)
     * @param plugin           - plugin instance
     */
    public SidebarPager(@NonNull List<Sidebar<R>> sidebars, long switchDelayTicks, @NonNull Plugin plugin) {
        this.sidebars = sidebars;
        this.viewers = new HashSet<>();
        this.pageIterator = Iterators.cycle(sidebars);
        this.currentPage = pageIterator.next();

        if (switchDelayTicks > 0) {
            this.switchTask = plugin.getServer().getScheduler().runTaskTimer(plugin, this::switchPage, switchDelayTicks, switchDelayTicks);
        } else {
            this.switchTask = null;
        }
    }

    public void applyToAll(Consumer<Sidebar<R>> consumer) {
        sidebars.forEach(consumer);
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

    public Sidebar<R> getCurrentPage() {
        return currentPage;
    }

    public Set<UUID> getViewers() {
        return Collections.unmodifiableSet(viewers);
    }

    public List<Sidebar<R>> getSidebars() {
        return Collections.unmodifiableList(sidebars);
    }

    /**
     * Adds a page status line to all sidebars in pager.
     */
    public void addPageLine(PageConsumer<R> consumer) {
        int page = 1;
        int maxPage = sidebars.size();

        for (Sidebar<R> sidebar : sidebars) {
            consumer.accept(page, maxPage, sidebar);
            page++;
        }
    }

    /**
     * Destroy all sidebars in pager.
     * Note: pager object will be unusable after this method call.
     */
    public void destroy() {
        if (switchTask != null) {
            switchTask.cancel();
        }
        for (Sidebar<R> sidebar : sidebars) {
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
