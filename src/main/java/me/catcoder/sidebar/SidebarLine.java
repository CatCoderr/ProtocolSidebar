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
import me.catcoder.sidebar.util.ReflectionUtil;
import me.catcoder.sidebar.util.UnsafeUtil;
import me.catcoder.sidebar.util.VersionUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Function;

@Getter
@ToString
public class SidebarLine {

    private static final ChatColor[] COLORS = ChatColor.values();
    private static final Splitter SPLITTER = Splitter.fixedLength(16);

    private static final int TEAM_CREATED = 0;
    private static final int TEAM_REMOVED = 1;
    private static final int TEAM_UPDATED = 2;
    private static final int PLAYERS_ADDED = 3;
    private static final int PLAYERS_REMOVED = 4;

    private final String teamName;
    private int score = -1;

    private final int index;
    private final boolean staticText;

    private Function<Player, String> updater;

    SidebarLine(@NonNull Function<Player, String> updater, @NonNull String teamName, boolean staticText, int index) {
        this.updater = updater;
        this.teamName = teamName;
        this.staticText = staticText;
        this.index = index;
    }

    public void setUpdater(@NonNull Function<Player, String> updater) {
        Preconditions.checkState(!isStaticText(), "Cannot set updater for static text line");
        this.updater = updater;
    }

    void updateTeam(@NonNull Player player, int previousScore, @NonNull String objective) {
        if (!isStaticText()) {
            String text = updater.apply(player);
            sendPacket(player, createTeamPacket(TEAM_UPDATED, player, text));
        }

        if (previousScore != score) {
            sendPacket(player, createScorePacket(EnumWrappers.ScoreboardAction.CHANGE, objective));
        }
    }

    void removeTeam(@NonNull Player player, @NonNull String objective) {
        sendPacket(player, createScorePacket(EnumWrappers.ScoreboardAction.REMOVE, objective));
        sendPacket(player, createTeamPacket(TEAM_REMOVED, null, null));
    }

    void createTeam(@NonNull Player player, @NonNull String objective) {
        String text = updater.apply(player);
        sendPacket(player, createTeamPacket(TEAM_CREATED, player, text));
        sendPacket(player, createScorePacket(EnumWrappers.ScoreboardAction.CHANGE, objective));
    }

    void setScore(int score) {
        this.score = score;
    }

    private PacketContainer createTeamPacket(int mode, Player player, String text) {
        int version = VersionUtil.getPlayerVersion(player.getUniqueId());
        String teamEntry = COLORS[index].toString();

        PacketContainer packet = getProtocolManager().createPacket(PacketType.Play.Server.SCOREBOARD_TEAM);
        packet.getModifier().writeDefaults();

        packet.getStrings().write(0, teamName);
        if (version >= VersionUtil.MINECRAFT_1_17)
            packet.getIntegers().write(0, mode);
        else
            packet.getIntegers().write(1, mode);

        if (mode == TEAM_REMOVED) {
            return packet;
        }

        packet.getSpecificModifier(Collection.class).write(0, Collections.singletonList(teamEntry));

        // Since 1.13 character limit for prefix/suffix was removed
        if (version >= VersionUtil.MINECRAFT_1_13) {
            if (!text.isEmpty() && text.charAt(0) != ChatColor.COLOR_CHAR) {
                text = ChatColor.RESET + text;
            }

            if (VersionUtil.SERVER_VERSION >= VersionUtil.MINECRAFT_1_13) {
                if (VersionUtil.SERVER_VERSION >= VersionUtil.MINECRAFT_1_17) {
                    try {
                        Object entryObject = UnsafeUtil.getUnsafe().allocateInstance(Class.forName("net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam$b"));
                        ReflectionUtil.setField(entryObject, "a", WrappedChatComponent.fromText(teamName).getHandle());
                        ReflectionUtil.setField(entryObject, "b", WrappedChatComponent.fromText(text).getHandle());
                        ReflectionUtil.setField(entryObject, "c", WrappedChatComponent.fromText(ChatColor.RESET.toString()).getHandle());
                        ReflectionUtil.setField(entryObject, "d", "always");
                        ReflectionUtil.setField(entryObject, "e", "always");
                        Method method = Class.forName("net.minecraft.EnumChatFormat").getDeclaredMethod("b", String.class);
                        method.setAccessible(true);
                        ReflectionUtil.setField(entryObject, "f", method.invoke(null, "reset"));
                        ReflectionUtil.setField(entryObject, "g", 2);
                        packet.getSpecificModifier(Optional.class).write(0, Optional.of(entryObject));
                    } catch(ClassNotFoundException | InstantiationException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    packet.getChatComponents().write(1,
                            WrappedChatComponent.fromText(text)); // prefix
                    packet.getChatComponents().write(2,
                            WrappedChatComponent.fromText(ChatColor.RESET.toString())); // suffix
                }
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
        packet.getStrings().write(0, COLORS[index].toString());
        packet.getStrings().write(1, objectiveName);
        packet.getScoreboardActions().write(0, action);
        packet.getIntegers().write(0, score);
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
