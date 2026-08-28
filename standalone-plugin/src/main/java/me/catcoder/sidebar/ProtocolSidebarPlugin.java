package me.catcoder.sidebar;

import me.catcoder.sidebar.protocol.ProtocolConstants;
import me.catcoder.sidebar.util.PlayerPredicates;
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

        checkLineLimits();
        checkConditionalTitle();

        Bukkit.getPluginManager().registerEvents(this, this);
    }

    private void checkLineLimits() {
        Sidebar<String> probe = ProtocolSidebar.newMiniMessageSidebar("<gray>probe", this);

        for (int i = 0; i < 10; i++) {
            probe.addLine("plain " + i);
        }
        for (int i = 0; i < 12; i++) {
            int index = i;
            probe.addConditionalLine(player -> "legacy " + index,
                    PlayerPredicates.clientVersionAtMost(ProtocolConstants.MINECRAFT_1_8));
        }

        getLogger().info("Registered " + probe.getLines().size() + " lines (expected 22)");

        expectRejected("23rd line", () -> probe.addConditionalLine(p -> "overflow", p -> true));

        Sidebar<String> plainProbe = ProtocolSidebar.newMiniMessageSidebar("<gray>probe2", this);
        for (int i = 0; i < 15; i++) {
            plainProbe.addLine("plain " + i);
        }
        expectRejected("16th always visible line", () -> plainProbe.addLine("overflow"));

        // a freed slot must be reused instead of colliding with a surviving line
        Sidebar<String> reuseProbe = ProtocolSidebar.newMiniMessageSidebar("<gray>probe3", this);
        reuseProbe.addLine("a");
        SidebarLine<String> middle = reuseProbe.addLine("b");
        SidebarLine<String> last = reuseProbe.addLine("c");
        reuseProbe.removeLine(middle);
        SidebarLine<String> added = reuseProbe.addLine("d");

        if (added.getIndex() == last.getIndex()) {
            getLogger().severe("Slot collision: new line reuses the index of a live line");
        } else {
            getLogger().info("Slot reuse ok, new line got index " + added.getIndex()
                    + ", surviving line holds " + last.getIndex());
        }

        probe.destroy();
        plainProbe.destroy();
        reuseProbe.destroy();
    }

    private void checkConditionalTitle() {
        expectRejected("conditional title without otherwise(..)", () -> sidebar.setTitle(
                ConditionalTitle.<String>create().when(p -> false, "<red>never")));

        sidebar.setTitle(ConditionalTitle.<String>create()
                .when(PlayerPredicates.clientVersionAtMost(ProtocolConstants.MINECRAFT_1_8), "<gold>Legacy")
                .otherwise("<red>TEST</red>"));
    }

    private void expectRejected(String what, Runnable action) {
        try {
            action.run();
            getLogger().severe("Expected " + what + " to be rejected, but it was accepted");
        } catch (IllegalArgumentException | IllegalStateException e) {
            getLogger().info("Rejected " + what + " as expected: " + e.getMessage());
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        sidebar.addViewer(player);

        getLogger().info("Title for " + player.getName() + ": "
                + sidebar.getObjective().getDisplayName(player));
    }
}
