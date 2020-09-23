package me.catcoder.sidebar;

import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

public class Sidebar {

	private final Set<UUID> viewers = new HashSet<>();
	private final Map<Integer, SidebarLine> lines = new HashMap<>();

	private final ScoreboardObjective objective;

	public Sidebar(@NonNull String objectiveName, @NonNull String displayName) {
		this.objective = new ScoreboardObjective(objectiveName, ScoreboardObjective.DISPLAY_SIDEBAR, displayName);
	}

	public void setDisplayName(@NonNull String displayName) {
		objective.setDisplayName(displayName);
		broadcast(p -> objective.updateDisplayName(p).sendPacket(p));
	}

	public void setLine(int index, @NonNull SidebarLineUpdater updater) {
		SidebarLine line = getLine(index).orElse(null);

		if (line == null) {
			line = new SidebarLine(index, updater, objective.getName());
			lines.put(index, line);
			broadcast(line.render());
		} else {
			line.setUpdater(updater);
			broadcast(line.update());
		}
	}

	public void setLine(int index, @NonNull String text) {
		setLine(index, (__) -> text);
	}

	public void unregister(@NonNull Player... receivers) {
		for (Player player : receivers) {
			checkState(viewers.contains(player.getUniqueId()),
					"Player %s doesn't receiving this sidebar", player.getName());

			lines.values().forEach(line -> line.remove().accept(player));
			objective.remove(player);
			viewers.remove(player.getUniqueId());
		}
	}

	public void unregisterForAll() {
		for (UUID id : new ArrayList<>(viewers)) {
			Player player = Bukkit.getPlayer(id);

			if (player == null) {
				continue;
			}

			unregister(player);
		}

		viewers.clear();
	}

	public void send(@NonNull Player... players) {
		for (Player player : players) {
			checkArgument(!this.viewers.contains(player.getUniqueId()),
					"Player %s has already receiving this board", player.getName());

			objective.create(player);
			lines.values().forEach(line -> line.render().accept(player));
			objective.show(player);

			this.viewers.add(player.getUniqueId());
		}
	}

	public Optional<SidebarLine> getLine(int index) {
		return Optional.ofNullable(lines.get(index));
	}

	public Set<UUID> getViewers() {
		return Collections.unmodifiableSet(viewers);
	}

	public Map<Integer, SidebarLine> getLines() {
		return Collections.unmodifiableMap(lines);
	}

	public void unregister(@NonNull UUID playerUuid) {
		unregister(Bukkit.getPlayer(playerUuid));
	}

	public ScoreboardObjective getObjective() {
		return objective;
	}

	public void broadcast(@NonNull Consumer<Player> consumer) {
		viewers.stream()
				.map(Bukkit::getPlayer)
				.filter(Objects::nonNull)
				.forEach(consumer);
	}

}
