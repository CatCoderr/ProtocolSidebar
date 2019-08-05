package me.catcoder.sidebar;

import com.comphenix.protocol.wrappers.EnumWrappers;
import com.google.common.base.Preconditions;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.NonNull;
import me.catcoder.sidebar.wrapper.AbstractPacket;
import me.catcoder.sidebar.wrapper.WrapperPlayServerScoreboardTeam;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

public class Sidebar implements Listener {

  private final Set<UUID> players = new HashSet<>();
  private final Map<Integer, SidebarLine> lines = new HashMap<>();

  private SidebarObjective objective;

  public Sidebar(@NonNull String objectiveName, @NonNull String displayName,
      @NonNull Plugin owner) {
    this.objective = new SidebarObjective(objectiveName, displayName);

    owner.getServer().getPluginManager().registerEvents(this, owner);
  }

  public String getObjectiveName() {
    return objective.getName();
  }

  public String getDisplayName() {
    return objective.getDisplayName();
  }

  public void setDisplayName(@NonNull String displayName) {
    objective.setDisplayName(displayName, this);
  }


  public void setLine(int index, String text) {
    SidebarLine line = getLine(index);
    if (line == null) {
      lines.put(index, new SidebarLine(index, text, this));
    } else {
      line.setText(text);
    }
  }

  public SidebarLine getLine(int index) {
    return lines.get(index);
  }

  public Set<UUID> getPlayerUuids() {
    return Collections.unmodifiableSet(players);
  }

  public Map<Integer, SidebarLine> getLines() {
    return Collections.unmodifiableMap(lines);
  }

  public void unregister(@NonNull UUID playerUuid) {
    unregister(Bukkit.getPlayer(playerUuid));
  }

  public void unregister(@NonNull Player player) {
    Preconditions.checkState(players.contains(player.getUniqueId()),
        "Player %s is not receiving this sidebar.", player.getName());

    lines.values().forEach(line -> {
      line.getTeamPacket(WrapperPlayServerScoreboardTeam.Mode.TEAM_REMOVED).sendPacket(player);
      line.getScorePacket(EnumWrappers.ScoreboardAction.REMOVE).sendPacket(player);
    });

    objective.remove(player);
    players.remove(player.getUniqueId());
  }

  public void unregisterForAll() {
    players.stream()
        .map(Bukkit::getPlayer)
        .filter(Objects::nonNull)
        .forEach(this::unregister);
    players.clear();
  }


  public void send(@NonNull Player... players) {
    for (Player player : players) {
      Preconditions.checkArgument(!this.players.contains(player.getUniqueId()),
          "Player %s already receiving this sidebar.", player.getName());
      objective.create(player);
      lines.values().forEach(line -> {
        line.getTeamPacket(WrapperPlayServerScoreboardTeam.Mode.TEAM_CREATED).sendPacket(player);
        line.getScorePacket(EnumWrappers.ScoreboardAction.CHANGE).sendPacket(player);
      });
      objective.show(player);
      this.players.add(player.getUniqueId());
    }
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    players.remove(event.getPlayer().getUniqueId());
  }

  void broadcastPacket(@NonNull AbstractPacket packet) {
    players
        .stream()
        .map(Bukkit::getPlayer)
        .filter(Objects::nonNull)
        .forEach(packet::sendPacket);
  }
}
