package me.catcoder.sidebar;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
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

    public static final int ADD_OBJECTIVE = 0;
    public static final int REMOVE_OBJECTIVE = 1;
    public static final int UPDATE_VALUE = 2;

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
        PacketContainer packet = getPacket(player);
        packet.getIntegers().write(0, UPDATE_VALUE);
        sendPacket(player, packet);
    }

    void create(@NonNull Player player) {
        PacketContainer packet = getPacket(player);
        packet.getIntegers().write(0, ADD_OBJECTIVE);

        sendPacket(player, packet);
    }

    void remove(@NonNull Player player) {
        PacketContainer packet = getPacket(player);
        packet.getIntegers().write(0, REMOVE_OBJECTIVE);

        sendPacket(player, packet);
    }

    void display(@NonNull Player player) {
        PacketContainer packet = getProtocolManager().createPacket(PacketType.Play.Server.SCOREBOARD_DISPLAY_OBJECTIVE);
        packet.getIntegers().write(0, DISPLAY_SIDEBAR);
        packet.getStrings().write(0, name);

        sendPacket(player, packet);
    }

    private PacketContainer getPacket(@NonNull Player player) {
        int version = VersionUtil.getPlayerVersion(player.getUniqueId());

        PacketContainer packet = getProtocolManager().createPacket(PacketType.Play.Server.SCOREBOARD_OBJECTIVE);

        // Since 1.13 characters limit for display name was removed
        if (version < VersionUtil.MINECRAFT_1_13 && displayName.length() > 32) {
            displayName = displayName.substring(0, 32);
        }

        if (VersionUtil.SERVER_VERSION >= VersionUtil.MINECRAFT_1_13) {
            packet.getChatComponents().write(0, WrappedChatComponent.fromText(displayName));
        } else {
            packet.getStrings().write(1, displayName);
        }

        packet.getStrings().write(0, name);
        packet.getEnumModifier(HealthDisplay.class, 2).write(0, HealthDisplay.INTEGER);
        return packet;
    }

    public enum HealthDisplay {
        INTEGER, HEARTS
    }

    private static ProtocolManager getProtocolManager() {
        return ProtocolLibrary.getProtocolManager();
    }

    @SneakyThrows
    static void sendPacket(Player player, PacketContainer packet) {
        getProtocolManager().sendServerPacket(player, packet);
    }
}
