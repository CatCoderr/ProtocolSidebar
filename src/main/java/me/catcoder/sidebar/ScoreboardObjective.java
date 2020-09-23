package me.catcoder.sidebar;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.catcoder.sidebar.wrapper.AbstractPacket;
import me.catcoder.sidebar.wrapper.WrapperPlayServerScoreboardDisplayObjective;
import me.catcoder.sidebar.wrapper.WrapperPlayServerScoreboardObjective;
import org.bukkit.entity.Player;
import us.myles.ViaVersion.api.Via;

import static com.google.common.base.Preconditions.checkState;

/**
 * Encapsulates scoreboard objective
 *
 * @author CatCoder
 * @see <a href="https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/scoreboard/Objective.html">Bukkit
 * documentation</a>
 */
@Getter
public class ScoreboardObjective {

	public static final int DISPLAY_SIDEBAR = 1;
	public static final int MINECRAFT_1_13 = 393;

	private final String name;
	private final int displaySlot;

	private String displayName;

	public ScoreboardObjective(@NonNull String name, int displaySlot, @NonNull String displayName) {
		checkState(name.length() <= 16, "Objective name exceeds 16 symbols limit");

		this.name = name;
		this.displaySlot = displaySlot;
		this.displayName = displayName;
	}

	public void setDisplayName(@NonNull String displayName) {
		this.displayName = displayName;
	}

	AbstractPacket updateDisplayName(Player player) {
		WrapperPlayServerScoreboardObjective packet = getPacket(player);
		packet.setMode(WrapperPlayServerScoreboardObjective.Mode.UPDATE_VALUE);
		return packet;
	}

	void create(Player player) {
		WrapperPlayServerScoreboardObjective packet = getPacket(player);
		packet.setMode(WrapperPlayServerScoreboardObjective.Mode.ADD_OBJECTIVE);

		packet.sendPacket(player);
	}

	void remove(Player player) {
		WrapperPlayServerScoreboardObjective packet = getPacket(player);
		packet.setMode(WrapperPlayServerScoreboardObjective.Mode.REMOVE_OBJECTIVE);

		packet.sendPacket(player);
	}

	void show(Player player) {
		WrapperPlayServerScoreboardDisplayObjective displayObjective = new WrapperPlayServerScoreboardDisplayObjective();
		displayObjective.setPosition(displaySlot);
		displayObjective.setScoreName(name);

		displayObjective.sendPacket(player);
	}

	private WrapperPlayServerScoreboardObjective getPacket(@NonNull Player player) {
		int version = Via.getAPI().getPlayerVersion(player.getUniqueId());

		WrapperPlayServerScoreboardObjective packet = new WrapperPlayServerScoreboardObjective();

		// Since 1.13 characters limit for display name was removed
		if (version < MINECRAFT_1_13 && displayName.length() > 32) {
			displayName = displayName.substring(0, 32);
		}

		packet.setDisplayName(displayName);
		packet.setName(name);
		packet.setHealthDisplay(WrapperPlayServerScoreboardObjective.HealthDisplay.INTEGER);
		return packet;
	}

}
