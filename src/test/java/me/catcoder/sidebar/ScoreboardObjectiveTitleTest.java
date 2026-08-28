package me.catcoder.sidebar;

import me.catcoder.sidebar.text.TextProvider;
import org.bukkit.entity.Player;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;

public class ScoreboardObjectiveTitleTest {

    private static final TextProvider<String> TEXT_PROVIDER = new TextProvider<String>() {
        @Override
        public String asJsonMessage(Player player, String component) {
            return component;
        }

        @Override
        public String asLegacyMessage(Player player, String component) {
            return component;
        }

        @Override
        public String emptyMessage() {
            return "";
        }

        @Override
        public String fromLegacyMessage(String message) {
            return message;
        }
    };

    private static Player player(String name) {
        Player player = Mockito.mock(Player.class);
        Mockito.when(player.getName()).thenReturn(name);
        return player;
    }

    private static ScoreboardObjective<String> objective() {
        return new ScoreboardObjective<>("PS-test", "static", TEXT_PROVIDER);
    }

    @Test
    public void testResolvesStaticTitleWithoutUpdater() {
        assertEquals("static", objective().getDisplayName(player("alice")));
    }

    @Test
    public void testResolvesUpdaterPerPlayer() {
        ScoreboardObjective<String> objective = objective();
        objective.setDisplayNameUpdater(p -> "title for " + p.getName());

        assertEquals("title for alice", objective.getDisplayName(player("alice")));
        assertEquals("title for bob", objective.getDisplayName(player("bob")));
    }

    @Test
    public void testStaticTitleClearsUpdater() {
        ScoreboardObjective<String> objective = objective();
        objective.setDisplayNameUpdater(p -> "conditional");

        objective.setDisplayName("plain");

        assertEquals("plain", objective.getDisplayName(player("alice")));
    }

    @Test
    public void testGetDisplayNameWithoutPlayerIgnoresUpdater() {
        ScoreboardObjective<String> objective = objective();
        objective.setDisplayNameUpdater(p -> "conditional");

        assertEquals("static", objective.getDisplayName());
        assertEquals("conditional", objective.getDisplayName(player("alice")));
    }
}
