package me.catcoder.sidebar.text;

import org.bukkit.ChatColor;

import lombok.NonNull;
import lombok.experimental.UtilityClass;


@UtilityClass
public class TextIterators {

    public static TextIterator staticText(String text) {
        return new TextIterator() {
            @Override
            public String next() {
                return text;
            }
        };
    }
    
    public TextIterator textFade(@NonNull String text, @NonNull ChatColor primaryColor, @NonNull ChatColor fadeColor, @NonNull ChatColor secondaryColor) {
        return new TextFadeAnimation(text, primaryColor, fadeColor, secondaryColor);
    }

    public TextIterator textFadeHypixel(@NonNull String text) {
        return textFade(text, ChatColor.YELLOW, ChatColor.GOLD, ChatColor.WHITE);
    }
}
