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
import me.catcoder.sidebar.util.buffer.ByteBufNetOutput;
import me.catcoder.sidebar.util.buffer.NetOutput;
import me.catcoder.sidebar.util.version.VersionUtil;
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

    private ByteBuf getPacket(@NonNull Player player, int mode) {
        int version = VersionUtil.getPlayerVersion(player.getUniqueId());

        ByteBuf buf = ChannelInjector.IMP.getChannel(player).alloc().buffer();

        NetOutput output = new ByteBufNetOutput(buf);

        output.writeVarInt(PacketIds.OBJECTIVE.getServerPacketId());

        output.writeString(name);
        output.writeByte(mode);

        if (mode == ADD_OBJECTIVE || mode == UPDATE_VALUE) {
            String legacyText = textProvider.asLegacyMessage(player, displayName);
            // Since 1.13 characters limit for display name was removed
            if (version < ProtocolConstants.MINECRAFT_1_13 && legacyText.length() > 32) {
                legacyText = legacyText.substring(0, 32);
            }

            if (VersionUtil.SERVER_VERSION >= ProtocolConstants.MINECRAFT_1_20_3) {
                // what the heck 1.20.3?
                output.writeComponent(textProvider.asJsonMessage(player, displayName));
            } else if (VersionUtil.SERVER_VERSION >= ProtocolConstants.MINECRAFT_1_13) {
                output.writeString(textProvider.asJsonMessage(player, displayName));
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
