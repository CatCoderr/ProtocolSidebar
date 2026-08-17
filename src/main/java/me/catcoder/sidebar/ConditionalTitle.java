package me.catcoder.sidebar;

import lombok.NonNull;
import me.catcoder.sidebar.util.lang.ThrowingFunction;
import me.catcoder.sidebar.util.lang.ThrowingPredicate;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * A title that is chosen per player from a list of conditions.
 * <p>
 * Conditions are evaluated in the order they were added and the first match wins.
 * If no condition matches, the title passed to {@link #otherwise} is used.
 * <p>
 * Example:
 * <pre>{@code
 * sidebar.setTitle(ConditionalTitle.<Component>create()
 *         .when(clientVersionAtMost(ProtocolConstants.MINECRAFT_1_8), legacyTitle)
 *         .when(clientVersionAtLeast(ProtocolConstants.MINECRAFT_1_19), modernTitle)
 *         .when(player -> player.getName().equals("playerA"), specialTitle)
 *         .otherwise(defaultTitle));
 * }</pre>
 *
 * @param <R> component entity type
 * @see me.catcoder.sidebar.util.PlayerPredicates
 */
public final class ConditionalTitle<R> {

    private final List<Condition<R>> conditions = new ArrayList<>();
    private ThrowingFunction<Player, R, Throwable> fallback;

    private ConditionalTitle() {
    }

    /**
     * Creates a new conditional title.
     *
     * @param <R> component entity type
     * @return new conditional title
     */
    public static <R> ConditionalTitle<R> create() {
        return new ConditionalTitle<>();
    }

    /**
     * Use the given title when the condition matches.
     *
     * @param condition - the condition
     * @param title     - the title to display
     * @return this instance
     */
    public ConditionalTitle<R> when(@NonNull ThrowingPredicate<Player, Throwable> condition,
                                    @NonNull R title) {
        return when(condition, player -> title);
    }

    /**
     * Use the title produced by the given function when the condition matches.
     *
     * @param condition - the condition
     * @param title     - the function that produces the title
     * @return this instance
     */
    public ConditionalTitle<R> when(@NonNull ThrowingPredicate<Player, Throwable> condition,
                                    @NonNull ThrowingFunction<Player, R, Throwable> title) {
        conditions.add(new Condition<>(condition, title));
        return this;
    }

    /**
     * Title to use when no condition matches.
     *
     * @param title - the fallback title
     * @return this instance
     */
    public ConditionalTitle<R> otherwise(@NonNull R title) {
        return otherwise(player -> title);
    }

    /**
     * Title to use when no condition matches.
     *
     * @param title - the function that produces the fallback title
     * @return this instance
     */
    public ConditionalTitle<R> otherwise(@NonNull ThrowingFunction<Player, R, Throwable> title) {
        this.fallback = title;
        return this;
    }

    /**
     * Converts this conditional title to a per-player title updater.
     *
     * @return the title updater
     */
    public ThrowingFunction<Player, R, Throwable> toUpdater() {
        // copy, so later mutation of this builder cannot affect an already applied title
        List<Condition<R>> snapshot = new ArrayList<>(conditions);
        ThrowingFunction<Player, R, Throwable> fallback = this.fallback;

        return player -> {
            for (Condition<R> condition : snapshot) {
                if (condition.predicate.test(player)) {
                    return condition.title.apply(player);
                }
            }

            if (fallback == null) {
                throw new IllegalStateException(
                        "No condition matched for player " + player.getName()
                                + " and no otherwise() title was set");
            }

            return fallback.apply(player);
        };
    }

    private static final class Condition<R> {
        private final ThrowingPredicate<Player, Throwable> predicate;
        private final ThrowingFunction<Player, R, Throwable> title;

        private Condition(ThrowingPredicate<Player, Throwable> predicate,
                          ThrowingFunction<Player, R, Throwable> title) {
            this.predicate = predicate;
            this.title = title;
        }
    }
}
