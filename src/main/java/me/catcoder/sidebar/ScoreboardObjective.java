package me.catcoder.sidebar;

import com.comphenix.packetwrapper.WrapperPlayServerScoreboardDisplayObjective;
import com.comphenix.packetwrapper.WrapperPlayServerScoreboardObjective;
import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.NonNull;
import me.catcoder.sidebar.util.VersionUtil;
import org.bukkit.entity.Player;

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

    private final String name;
    private String displayName;

    public ScoreboardObjective(@NonNull String name, @NonNull String displayName) {
        Preconditions.checkArgument(
                name.length() <= 16, "Objective name exceeds 16 symbols limit");

        this.name = name;
        this.displayName = displayName;
    }

    void setDisplayName(@NonNull String displayName) {
        this.displayName = displayName;
    }

    void updateValue(@NonNull Player player) {
        WrapperPlayServerScoreboardObjective packet = getPacket(player);
        packet.setMode(WrapperPlayServerScoreboardObjective.Mode.UPDATE_VALUE);
        packet.sendPacket(player);
    }

    void create(@NonNull Player player) {
        WrapperPlayServerScoreboardObjective packet = getPacket(player);
        packet.setMode(WrapperPlayServerScoreboardObjective.Mode.ADD_OBJECTIVE);

        packet.sendPacket(player);
    }

    void remove(@NonNull Player player) {
        WrapperPlayServerScoreboardObjective packet = getPacket(player);
        packet.setMode(WrapperPlayServerScoreboardObjective.Mode.REMOVE_OBJECTIVE);

        packet.sendPacket(player);
    }

    void display(@NonNull Player player) {
        WrapperPlayServerScoreboardDisplayObjective displayObjective = new WrapperPlayServerScoreboardDisplayObjective();
        displayObjective.setPosition(DISPLAY_SIDEBAR);
        displayObjective.setScoreName(name);

        displayObjective.sendPacket(player);
    }

    private WrapperPlayServerScoreboardObjective getPacket(@NonNull Player player) {
        int version = VersionUtil.getPlayerVersion(player.getUniqueId());

        WrapperPlayServerScoreboardObjective packet = new WrapperPlayServerScoreboardObjective();

        // Since 1.13 characters limit for display name was removed
        if (version < VersionUtil.MINECRAFT_1_13 && displayName.length() > 32) {
            displayName = displayName.substring(0, 32);
        }

        packet.setDisplayName(displayName);
        packet.setName(name);
        packet.setHealthDisplay(WrapperPlayServerScoreboardObjective.HealthDisplay.INTEGER);
        return packet;
    }

}
