package me.catcoder.sidebar.util;

import lombok.experimental.UtilityClass;
import me.catcoder.sidebar.util.lang.ThrowingPredicate;
import me.catcoder.sidebar.util.version.VersionUtil;
import org.bukkit.entity.Player;

/**
 * Common player conditions, usable both for conditional titles
 * ({@link me.catcoder.sidebar.ConditionalTitle}) and conditional lines
 * ({@link me.catcoder.sidebar.Sidebar#addConditionalLine}).
 * <p>
 * Protocol numbers are available in
 * {@link me.catcoder.sidebar.protocol.ProtocolConstants}.
 * <p>
 * Client versions are resolved through ViaVersion. Without ViaVersion installed
 * every player reports the server version, see {@link VersionUtil#getPlayerVersion}.
 */
@UtilityClass
public class PlayerPredicates {

    /**
     * Matches players whose client protocol is greater than or equal to the given version.
     *
     * @param protocol - protocol version
     * @return the predicate
     */
    public ThrowingPredicate<Player, Throwable> clientVersionAtLeast(int protocol) {
        return player -> clientVersion(player) >= protocol;
    }

    /**
     * Matches players whose client protocol is less than or equal to the given version.
     *
     * @param protocol - protocol version
     * @return the predicate
     */
    public ThrowingPredicate<Player, Throwable> clientVersionAtMost(int protocol) {
        return player -> clientVersion(player) <= protocol;
    }

    /**
     * Matches players whose client protocol is within the given range, both bounds inclusive.
     *
     * @param min - lowest accepted protocol version
     * @param max - highest accepted protocol version
     * @return the predicate
     */
    public ThrowingPredicate<Player, Throwable> clientVersionBetween(int min, int max) {
        return player -> {
            int version = clientVersion(player);
            return version >= min && version <= max;
        };
    }

    /**
     * Matches players whose client protocol is exactly the given version.
     *
     * @param protocol - protocol version
     * @return the predicate
     */
    public ThrowingPredicate<Player, Throwable> clientVersionIs(int protocol) {
        return player -> clientVersion(player) == protocol;
    }

    private int clientVersion(Player player) {
        return VersionUtil.getPlayerVersion(player.getUniqueId());
    }
}
