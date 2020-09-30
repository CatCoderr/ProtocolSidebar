package me.catcoder.sidebar;

import com.comphenix.packetwrapper.AbstractPacket;
import com.comphenix.packetwrapper.WrapperPlayServerScoreboardScore;
import com.comphenix.packetwrapper.WrapperPlayServerScoreboardTeam;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import me.catcoder.sidebar.util.VersionUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Iterator;
import java.util.function.Function;

@Getter
@ToString
public class SidebarLine {

    private static final ChatColor[] COLORS = ChatColor.values();
    private static final Splitter SPLITTER = Splitter.fixedLength(16);

    private final String teamName;
    private int currentIndex = -1;
    private final boolean staticText;

    private Function<Player, String> updater;

    SidebarLine(@NonNull Function<Player, String> updater, @NonNull String teamName, boolean staticText) {
        this.updater = updater;
        this.teamName = teamName;
        this.staticText = staticText;
    }

    public void setUpdater(@NonNull Function<Player, String> updater) {
        Preconditions.checkState(!isStaticText(), "Cannot set updater for static text line");
        this.updater = updater;
    }

    void updateTeam(@NonNull Player player, int previousIndex, @NonNull String objective) {
        if (!isStaticText()) {
            String text = updater.apply(player);
            createTeamPacket(WrapperPlayServerScoreboardTeam.Mode.TEAM_UPDATED, player, text).sendPacket(player);
        }

        if (previousIndex != currentIndex) {
            createScorePacket(EnumWrappers.ScoreboardAction.CHANGE, objective).sendPacket(player);
        }
    }

    void removeTeam(@NonNull Player player, @NonNull String objective) {
        createTeamPacket(WrapperPlayServerScoreboardTeam.Mode.TEAM_REMOVED, null, null).sendPacket(player);
        createScorePacket(EnumWrappers.ScoreboardAction.REMOVE, objective).sendPacket(player);
    }

    void createTeam(@NonNull Player player, @NonNull String objective) {
        String text = updater.apply(player);
        createTeamPacket(WrapperPlayServerScoreboardTeam.Mode.TEAM_CREATED, player, text).sendPacket(player);
        createScorePacket(EnumWrappers.ScoreboardAction.CHANGE, objective).sendPacket(player);
    }

    void setCurrentIndex(int currentIndex) {
        this.currentIndex = currentIndex;
    }

    private AbstractPacket createTeamPacket(int mode, Player player, String text) {
        String teamEntry = COLORS[currentIndex].toString();

        WrapperPlayServerScoreboardTeam team = new WrapperPlayServerScoreboardTeam();
        team.setName(teamName);
        team.setMode(mode);

        if (mode == WrapperPlayServerScoreboardTeam.Mode.TEAM_REMOVED) {
            return team;
        }

        int version = VersionUtil.getPlayerVersion(player.getUniqueId());

        team.setPlayers(Collections.singletonList(teamEntry));

        // Since 1.13 character limit for prefix/suffix was removed
        if (version >= VersionUtil.MINECRAFT_1_13) {
            if (!text.isEmpty() && text.charAt(0) != ChatColor.COLOR_CHAR) {
                text = ChatColor.RESET + text;
            }
            team.setPrefix(text);
            team.setSuffix(ChatColor.RESET.toString());
            return team;
        }

        Iterator<String> iterator = SPLITTER.split(text).iterator();
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

    private AbstractPacket createScorePacket(EnumWrappers.ScoreboardAction action, String objectiveName) {
        WrapperPlayServerScoreboardScore score = new WrapperPlayServerScoreboardScore();
        score.setObjectiveName(objectiveName);
        score.setScoreboardAction(action);
        score.setValue(currentIndex);
        score.setScoreName(COLORS[currentIndex].toString());
        return score;
    }
}
