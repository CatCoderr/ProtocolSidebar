package me.catcoder.sidebar;

import me.catcoder.sidebar.protocol.ScoreboardPackets;
import org.bukkit.ChatColor;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SidebarLimitsTest {

    @Test
    public void testLineLimitMatchesEntryAlphabet() {
        assertEquals(ScoreboardPackets.COLORS.length, Sidebar.MAX_LINES_COUNT);
        assertEquals(ChatColor.values().length, Sidebar.MAX_LINES_COUNT);

        assertEquals(22, Sidebar.MAX_LINES_COUNT);
    }

    @Test
    public void testEntryAlphabetCanCoverEveryRenderedLine() {
        assertTrue("The entry alphabet must be able to fill the sidebar the client renders",
                Sidebar.MAX_LINES_COUNT >= Sidebar.MAX_VISIBLE_LINES);
    }

    @Test
    public void testEntriesAreUnique() {
        Set<String> entries = new HashSet<>();

        for (int index = 0; index < Sidebar.MAX_LINES_COUNT; index++) {
            assertTrue("Duplicate team entry at index " + index,
                    entries.add(ScoreboardPackets.COLORS[index].toString()));
        }

        assertEquals(Sidebar.MAX_LINES_COUNT, entries.size());
    }

    @Test
    public void testEntriesRenderAsNothing() {
       for (int index = 0; index < Sidebar.MAX_LINES_COUNT; index++) {
            String entry = ScoreboardPackets.COLORS[index].toString();

            assertEquals("Entry at index " + index + " is not a bare formatting code", 2, entry.length());
            assertEquals("Entry at index " + index + " does not start with the colour char",
                    ChatColor.COLOR_CHAR, entry.charAt(0));
        }
    }

    @Test
    public void testVisibleLimitIsTheVanillaSidebarHeight() {
        assertEquals(15, Sidebar.MAX_VISIBLE_LINES);
    }
}
