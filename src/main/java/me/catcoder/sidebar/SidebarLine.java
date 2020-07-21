package me.catcoder.sidebar;

import com.comphenix.protocol.wrappers.EnumWrappers;
import com.google.common.base.Splitter;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import me.catcoder.sidebar.wrapper.AbstractPacket;
import me.catcoder.sidebar.wrapper.WrapperPlayServerScoreboardScore;
import me.catcoder.sidebar.wrapper.WrapperPlayServerScoreboardTeam;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import us.myles.ViaVersion.api.Via;

import java.util.Collections;
import java.util.Iterator;
import java.util.function.Consumer;

import static me.catcoder.sidebar.wrapper.WrapperPlayServerScoreboardTeam.Mode.*;

@Getter
public class SidebarLine {

	private static final ChatColor[] COLORS = ChatColor.values();

	private final int index;
	private final String objectiveName;

	@Setter
	private SidebarLineUpdater updater;

	SidebarLine(int index, @NonNull SidebarLineUpdater updater, @NonNull String objectiveName) {
		this.index = index;
		this.updater = updater;
		this.objectiveName = objectiveName;
	}

	public Consumer<Player> update() {
		return (player) -> {
			String text = updater.updateText(player);
			AbstractPacket scorePacket = createScorePacket(EnumWrappers.ScoreboardAction.CHANGE);
			AbstractPacket teamPacket = createTeamPacket(TEAM_UPDATED, player, text);
			teamPacket.sendPacket(player);
			scorePacket.sendPacket(player);
		};
	}

	public Consumer<Player> remove() {
		return (player) -> {
			AbstractPacket teamPacket = createTeamPacket(TEAM_REMOVED, null, null);
			AbstractPacket scorePacket = createScorePacket(EnumWrappers.ScoreboardAction.REMOVE);
			teamPacket.sendPacket(player);
			scorePacket.sendPacket(player);
		};
	}

	public Consumer<Player> render() {
		AbstractPacket scorePacket = createScorePacket(EnumWrappers.ScoreboardAction.CHANGE);
		return (player) -> {
			String text = updater.updateText(player);
			AbstractPacket teamPacket = createTeamPacket(TEAM_CREATED, player, text);
			teamPacket.sendPacket(player);
			scorePacket.sendPacket(player);
		};
	}

	public AbstractPacket createTeamPacket(int mode, Player player, String text) {
		String teamEntry = COLORS[index].toString();

		WrapperPlayServerScoreboardTeam team = new WrapperPlayServerScoreboardTeam();
		team.setName(objectiveName + "-text-" + index);
		team.setMode(mode);

		if (mode == TEAM_REMOVED) {
			return team;
		}

		int version = Via.getAPI().getPlayerVersion(player.getUniqueId());

		team.setPlayers(Collections.singletonList(teamEntry));

		// Since 1.13 characters limit for prefix/suffix was removed
		if (version >= ScoreboardObjective.MINECRAFT_1_13) {
			team.setPrefix(text);
			team.setSuffix(ChatColor.RESET.toString());
			return team;
		}

		Iterator<String> iterator = Splitter.fixedLength(16).split(text).iterator();
		String prefix = iterator.next();
		team.setPrefix(prefix);

		if (text.length() > 16) {
			String prefixColor = ChatColor.getLastColors(prefix);
			String suffix = iterator.next();

			if (prefix.endsWith(String.valueOf(ChatColor.COLOR_CHAR))) {
				prefix = prefix.substring(0, prefix.length() - 1);
				team.setPrefix(prefix);
				prefixColor = ChatColor.getByChar(suffix.charAt(0)).toString();
				suffix = suffix.substring(1);
			}

			if (prefixColor == null) {
				prefixColor = "";
			}

			suffix = ((prefixColor.equals("") ? ChatColor.RESET : prefixColor) + suffix);

			if (suffix.length() > 16) {
				suffix = suffix.substring(0, 13) + "...";
			}

			team.setSuffix(suffix);
		}

		return team;
	}

	public AbstractPacket createScorePacket(EnumWrappers.ScoreboardAction action) {
		WrapperPlayServerScoreboardScore score = new WrapperPlayServerScoreboardScore();
		score.setObjectiveName(objectiveName);
		score.setScoreboardAction(action);
		score.setValue(index);
		score.setScoreName(COLORS[index].toString());
		return score;
	}

}
