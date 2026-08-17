package me.catcoder.sidebar;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NonNull;
import me.catcoder.sidebar.protocol.ChannelInjector;
import me.catcoder.sidebar.protocol.PacketIds;
import me.catcoder.sidebar.protocol.ProtocolConstants;
import me.catcoder.sidebar.protocol.ScoreNumberFormat;
import me.catcoder.sidebar.text.TextProvider;
import lombok.SneakyThrows;
import me.catcoder.sidebar.util.buffer.ByteBufNetOutput;
import me.catcoder.sidebar.util.buffer.NetOutput;
import me.catcoder.sidebar.util.lang.ThrowingFunction;
import me.catcoder.sidebar.util.version.VersionUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import static me.catcoder.sidebar.SidebarLine.sendPacket;

/**
 * Encapsulates scoreboard objective
 *
 * @author CatCoder
 * @see <a href="https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/scoreboard/Objective.html">Bukkit
 * documentation</a>
 */
@Getter
public class ScoreboardObjective<R> {

    public static final int DISPLAY_SIDEBAR = 1;
    public static final int ADD_OBJECTIVE = 0;
    public static final int REMOVE_OBJECTIVE = 1;
    public static final int UPDATE_VALUE = 2;

    private final String name;
    private final TextProvider<R> textProvider;

    private ScoreNumberFormat numberFormat;
    private Function<Player, R> numberFormatter;

    private R displayName;

    /**
     * Resolves the title per player. When {@code null}, the static {@link #displayName} is used.
     * Volatile because it is read from the async broadcast threads.
     */
    private volatile ThrowingFunction<Player, R, Throwable> displayNameUpdater;

    ScoreboardObjective(@NonNull String name,
                        @NonNull R displayName,
                        @NonNull TextProvider<R> textProvider) {
        Preconditions.checkArgument(
                name.length() <= 16, "Objective name exceeds 16 symbols limit");

        this.name = name;
        this.textProvider = textProvider;
        this.displayName = displayName;
    }

    void setDisplayName(@NonNull R displayName) {
        this.displayName = displayName;
        // a static title supersedes any per-player one
        this.displayNameUpdater = null;
    }

    void setDisplayNameUpdater(@NonNull ThrowingFunction<Player, R, Throwable> displayNameUpdater) {
        this.displayNameUpdater = displayNameUpdater;
    }

    void updateValue(@NonNull Player player) {
        ByteBuf packet = getPacket(player, UPDATE_VALUE);
        sendPacket(player, packet);
    }

    public void scoreNumberFormatFixed(@NonNull Function<Player, R> numberFormatter) {
        this.numberFormat = ScoreNumberFormat.FIXED;
        this.numberFormatter = numberFormatter;
    }

    public void scoreNumberFormatStyled(@NonNull Function<Player, R> numberFormatter) {
        this.numberFormat = ScoreNumberFormat.STYLED;
        this.numberFormatter = numberFormatter;
    }

    public void scoreNumberFormatBlank() {
        this.numberFormat = ScoreNumberFormat.BLANK;
        this.numberFormatter = null;
    }

    void create(@NonNull Player player) {
        ByteBuf packet = getPacket(player, ADD_OBJECTIVE);
        sendPacket(player, packet);
    }

    void remove(@NonNull Player player) {
        ByteBuf packet = getPacket(player, REMOVE_OBJECTIVE);
        sendPacket(player, packet);
    }

    void display(@NonNull Player player) {
        ByteBuf buf = ChannelInjector.IMP.getChannel(player).alloc().buffer();

        NetOutput output = new ByteBufNetOutput(buf);

        output.writeVarInt(PacketIds.OBJECTIVE_DISPLAY.getServerPacketId());

        output.writeByte(DISPLAY_SIDEBAR);
        output.writeString(name);

        sendPacket(player, buf);
    }

    @SneakyThrows
    private ByteBuf getPacket(@NonNull Player player, int mode) {
        int version = VersionUtil.getPlayerVersion(player.getUniqueId());

        ByteBuf buf = ChannelInjector.IMP.getChannel(player).alloc().buffer();

        NetOutput output = new ByteBufNetOutput(buf);

        output.writeVarInt(PacketIds.OBJECTIVE.getServerPacketId());

        output.writeString(name);
        output.writeByte(mode);

        if (mode == ADD_OBJECTIVE || mode == UPDATE_VALUE) {
            // resolved per player, so conditional titles can differ between viewers
            R title = displayNameUpdater != null ? displayNameUpdater.apply(player) : displayName;

            String legacyText = textProvider.asLegacyMessage(player, title);
            // Since 1.13 characters limit for display name was removed
            boolean truncated = version < ProtocolConstants.MINECRAFT_1_13 && legacyText.length() > 32;

            if (truncated) {
                legacyText = legacyText.substring(0, 32);

                // don't leave a dangling colour char behind
                if (legacyText.endsWith(String.valueOf(ChatColor.COLOR_CHAR))) {
                    legacyText = legacyText.substring(0, legacyText.length() - 1);
                }
            }

            String jsonText = truncated
                    ? textProvider.asJsonMessage(player, textProvider.fromLegacyMessage(legacyText))
                    : textProvider.asJsonMessage(player, title);

            if (VersionUtil.SERVER_VERSION >= ProtocolConstants.MINECRAFT_1_20_3) {
                // what the heck 1.20.3?
                output.writeComponent(jsonText);
            } else if (VersionUtil.SERVER_VERSION >= ProtocolConstants.MINECRAFT_1_13) {
                output.writeString(jsonText);
            } else {
                output.writeString(legacyText);
            }

            if (VersionUtil.SERVER_VERSION >= ProtocolConstants.MINECRAFT_1_20_3) {
                output.writeVarInt(0);
                output.writeBoolean(numberFormat != null); // has number format

                if (numberFormat != null) {
                    numberFormat.accept(output, numberFormatter == null ?
                            null : textProvider.asJsonMessage(player, numberFormatter.apply(player))
                    );
                }

                return buf;
            }

            if (VersionUtil.SERVER_VERSION >= ProtocolConstants.MINECRAFT_1_13) {
                output.writeVarInt(0); // Health display
            } else {
                output.writeString("integer"); // Health display
            }
        }


        return buf;
    }
}
