package me.catcoder.sidebar;

import me.catcoder.sidebar.util.lang.ThrowingFunction;
import org.bukkit.entity.Player;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class ConditionalTitleTest {

    private static Player player(String name) {
        Player player = Mockito.mock(Player.class);
        Mockito.when(player.getName()).thenReturn(name);
        return player;
    }

    @Test
    public void testFirstMatchWins() throws Throwable {
        ThrowingFunction<Player, String, Throwable> updater = ConditionalTitle.<String>create()
                .when(p -> p.getName().startsWith("a"), "first")
                .when(p -> p.getName().equals("alice"), "second")
                .otherwise("fallback")
                .toUpdater();

        // both conditions match, the one added first must win
        assertEquals("first", updater.apply(player("alice")));
    }

    @Test
    public void testPicksMatchingCondition() throws Throwable {
        ThrowingFunction<Player, String, Throwable> updater = ConditionalTitle.<String>create()
                .when(p -> p.getName().equals("alice"), "titleA")
                .when(p -> p.getName().equals("bob"), "titleB")
                .otherwise("fallback")
                .toUpdater();

        assertEquals("titleA", updater.apply(player("alice")));
        assertEquals("titleB", updater.apply(player("bob")));
    }

    @Test
    public void testFallbackWhenNothingMatches() throws Throwable {
        ThrowingFunction<Player, String, Throwable> updater = ConditionalTitle.<String>create()
                .when(p -> p.getName().equals("alice"), "titleA")
                .otherwise("fallback")
                .toUpdater();

        assertEquals("fallback", updater.apply(player("carol")));
    }

    @Test
    public void testTitleFunctionReceivesPlayer() throws Throwable {
        ThrowingFunction<Player, String, Throwable> updater = ConditionalTitle.<String>create()
                .when(p -> true, p -> "hello " + p.getName())
                .toUpdater();

        assertEquals("hello alice", updater.apply(player("alice")));
    }

    @Test
    public void testThrowsWhenNoMatchAndNoFallback() {
        ThrowingFunction<Player, String, Throwable> updater = ConditionalTitle.<String>create()
                .when(p -> p.getName().equals("alice"), "titleA")
                .toUpdater();

        assertThrows(IllegalStateException.class, () -> updater.apply(player("carol")));
    }

    @Test
    public void testUpdaterIsSnapshotOfConditions() throws Throwable {
        ConditionalTitle<String> title = ConditionalTitle.<String>create()
                .otherwise("fallback");

        ThrowingFunction<Player, String, Throwable> updater = title.toUpdater();

        // mutating the builder afterwards must not affect an already applied title
        title.when(p -> true, "added later");

        assertEquals("fallback", updater.apply(player("alice")));
    }
}
