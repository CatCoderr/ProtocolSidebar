package me.catcoder.sidebar;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.catcoder.sidebar.wrapper.WrapperPlayServerScoreboardDisplayObjective;
import me.catcoder.sidebar.wrapper.WrapperPlayServerScoreboardObjective;
import org.bukkit.entity.Player;

/**
 * Encapsulates scoreboard objective
 *
 * @author CatCoder
 * @see <a href="https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/scoreboard/Objective.html">Bukkit
 * documentation</a>
 */
@Getter
@AllArgsConstructor
class SidebarObjective {

  private static final int SIDEBAR = 1;

  private final String name;
  private String displayName;

  void setDisplayName(String displayName, Sidebar sidebar) {
    this.displayName = displayName;

    WrapperPlayServerScoreboardObjective packet = getPacket();
    packet.setMode(WrapperPlayServerScoreboardObjective.Mode.UPDATE_VALUE);
    sidebar.broadcastPacket(packet);
  }

  void create(Player player) {
    WrapperPlayServerScoreboardObjective packet = getPacket();
    packet.setMode(WrapperPlayServerScoreboardObjective.Mode.ADD_OBJECTIVE);

    packet.sendPacket(player);
  }

  void remove(Player player) {
    WrapperPlayServerScoreboardObjective packet = getPacket();
    packet.setMode(WrapperPlayServerScoreboardObjective.Mode.REMOVE_OBJECTIVE);

    packet.sendPacket(player);
  }

  void show(Player player) {
    WrapperPlayServerScoreboardDisplayObjective displayObjective = new WrapperPlayServerScoreboardDisplayObjective();
    displayObjective.setPosition(SIDEBAR);
    displayObjective.setScoreName(name);

    displayObjective.sendPacket(player);
  }

  private WrapperPlayServerScoreboardObjective getPacket() {
    WrapperPlayServerScoreboardObjective packet = new WrapperPlayServerScoreboardObjective();
    packet.setDisplayName(displayName);
    packet.setName(name);
    packet.setHealthDisplay(WrapperPlayServerScoreboardObjective.HealthDisplay.INTEGER);
    return packet;
  }
}
