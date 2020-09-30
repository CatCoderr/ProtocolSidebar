package me.catcoder.sidebar;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.ToString;
import me.catcoder.sidebar.util.VersionUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.function.Function;

@Getter
@ToString
public class SidebarLine {

    private static final ChatColor[] COLORS = ChatColor.values();
    private static final Splitter SPLITTER = Splitter.fixedLength(16);

    private static final int TEAM_CREATED = 0;
    private static final int TEAM_REMOVED = 1;
    private static final int TEAM_UPDATED = 2;

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
            sendPacket(player, createTeamPacket(TEAM_UPDATED, player, text));
        }

        if (previousIndex != currentIndex) {
            sendPacket(player, createScorePacket(EnumWrappers.ScoreboardAction.CHANGE, objective));
        }
    }

    void removeTeam(@NonNull Player player, @NonNull String objective) {
        sendPacket(player, createTeamPacket(TEAM_REMOVED, null, null));
        sendPacket(player, createScorePacket(EnumWrappers.ScoreboardAction.REMOVE, objective));
    }

    void createTeam(@NonNull Player player, @NonNull String objective) {
        String text = updater.apply(player);
        sendPacket(player, createTeamPacket(TEAM_CREATED, player, text));
        sendPacket(player, createScorePacket(EnumWrappers.ScoreboardAction.CHANGE, objective));
    }

    void setCurrentIndex(int currentIndex) {
        this.currentIndex = currentIndex;
    }

    private PacketContainer createTeamPacket(int mode, Player player, String text) {
        String teamEntry = COLORS[currentIndex].toString();

        PacketContainer packet = getProtocolManager().createPacket(PacketType.Play.Server.SCOREBOARD_TEAM);
        packet.getModifier().writeDefaults();

        packet.getStrings().write(0, teamName);
        packet.getIntegers().write(1, mode);

        if (mode == TEAM_REMOVED) {
            return packet;
        }

        int version = VersionUtil.getPlayerVersion(player.getUniqueId());

        packet.getSpecificModifier(Collection.class).write(0, Collections.singletonList(teamEntry));

        // Since 1.13 character limit for prefix/suffix was removed
        if (version >= VersionUtil.MINECRAFT_1_13) {
            if (!text.isEmpty() && text.charAt(0) != ChatColor.COLOR_CHAR) {
                text = ChatColor.RESET + text;
            }

            if (VersionUtil.SERVER_VERSION >= VersionUtil.MINECRAFT_1_13) {
                packet.getChatComponents().write(1,
                        WrappedChatComponent.fromText(text)); // prefix
                packet.getChatComponents().write(2,
                        WrappedChatComponent.fromText(ChatColor.RESET.toString())); // suffix
            } else {
                packet.getStrings().write(2, text); // prefix
                packet.getStrings().write(3, ChatColor.RESET.toString()); // suffix

            }
            return packet;
        }

        Iterator<String> iterator = SPLITTER.split(text).iterator();
        String prefix = iterator.next();

        packet.getStrings().write(2, prefix);

        if (text.length() > 16) {
            String prefixColor = ChatColor.getLastColors(prefix);
            String suffix = iterator.next();

            if (prefix.endsWith(String.valueOf(ChatColor.COLOR_CHAR))) {
                prefix = prefix.substring(0, prefix.length() - 1);

                packet.getStrings().write(2, prefix);

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

            packet.getStrings().write(3, suffix);
        }

        return packet;
    }

    private PacketContainer createScorePacket(EnumWrappers.ScoreboardAction action, String objectiveName) {
        PacketContainer packet = getProtocolManager().createPacket(
                PacketType.Play.Server.SCOREBOARD_SCORE);
        packet.getStrings().write(0, COLORS[currentIndex].toString());
        packet.getStrings().write(1, objectiveName);
        packet.getScoreboardActions().write(0, action);
        packet.getIntegers().write(0, currentIndex);
        return packet;
    }

    private static ProtocolManager getProtocolManager() {
        return ProtocolLibrary.getProtocolManager();
    }

    @SneakyThrows
    static void sendPacket(Player player, PacketContainer packet) {
        getProtocolManager().sendServerPacket(player, packet);
    }
}
