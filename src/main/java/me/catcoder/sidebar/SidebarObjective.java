package me.catcoder.sidebar;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.catcoder.sidebar.wrapper.WrapperPlayServerScoreboardDisplayObjective;
import me.catcoder.sidebar.wrapper.WrapperPlayServerScoreboardObjective;
import org.bukkit.entity.Player;

/**
 * Задача для скорборда.
 *
 * @author CatCoder
 */
@Getter
@AllArgsConstructor
public class SidebarObjective {

    private static final int SIDEBAR = 1;

    private final String name;
    private final WrapperPlayServerScoreboardObjective.HealthDisplay healthDisplay;
    private String displayName;

    public void setDisplayName(String displayName, Sidebar sidebar) {
        this.displayName = displayName;

        WrapperPlayServerScoreboardObjective packet = getPacket();
        packet.setMode(WrapperPlayServerScoreboardObjective.Mode.UPDATE_VALUE);
        sidebar.broadcastPacket(packet);
    }

    public void create(Player player) {
        WrapperPlayServerScoreboardObjective packet = getPacket();
        packet.setMode(WrapperPlayServerScoreboardObjective.Mode.ADD_OBJECTIVE);

        packet.sendPacket(player);
    }

    public void remove(Player player) {
        WrapperPlayServerScoreboardObjective packet = getPacket();
        packet.setMode(WrapperPlayServerScoreboardObjective.Mode.REMOVE_OBJECTIVE);

        packet.sendPacket(player);
    }

    public void show(Player player) {
        WrapperPlayServerScoreboardDisplayObjective displayObjective = new WrapperPlayServerScoreboardDisplayObjective();
        displayObjective.setPosition(SIDEBAR);
        displayObjective.setScoreName(name);

        displayObjective.sendPacket(player);
    }

    private WrapperPlayServerScoreboardObjective getPacket() {
        WrapperPlayServerScoreboardObjective packet = new WrapperPlayServerScoreboardObjective();
        packet.setDisplayName(displayName);
        packet.setName(name);
        packet.setHealthDisplay(healthDisplay);
        return packet;
    }
}
