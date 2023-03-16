package me.catcoder.sidebar.text;

import me.catcoder.sidebar.text.impl.TextFadeAnimation;
import org.bukkit.ChatColor;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class TextAnimationTest {

    @Test
    public void testTextFadeHypixelStyle() {
        // taken from mc.hypixel.net
        List<String> sampleAnimation = List.of(
            "§e§lPROTOTYPE",
            "§6§lP§e§lROTOTYPE",
            "§f§lP§6§lR§e§lOTOTYPE",
            "§f§lPR§6§lO§e§lTOTYPE",
            "§f§lPRO§6§lT§e§lOTYPE",
            "§f§lPROT§6§lO§e§lTYPE",
            "§f§lPROTO§6§lT§e§lYPE",
            "§f§lPROTOT§6§lY§e§lPE",
            "§f§lPROTOTY§6§lP§e§lE",
            "§f§lPROTOTYP§6§lE",
            "§f§lPROTOTYPE",
            "§e§lPROTOTYPE",
            "§f§lPROTOTYPE",
            "§e§lPROTOTYPE"
        );

        TextFadeAnimation animation = new TextFadeAnimation("§lPROTOTYPE", ChatColor.YELLOW, ChatColor.GOLD, ChatColor.WHITE);

        List<String> generatedAnimation = animation.createAnimationFrames().stream()
            .map(TextFrame::getText)
            .toList();

        assertEquals(sampleAnimation, generatedAnimation);
    }
    
}
