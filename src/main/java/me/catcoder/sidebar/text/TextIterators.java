package me.catcoder.sidebar.text;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import me.catcoder.sidebar.text.impl.TextFadeAnimation;
import me.catcoder.sidebar.text.impl.TextTypingAnimation;
import org.bukkit.ChatColor;


@UtilityClass
public class TextIterators {

    /**
     * Creates new text typing animation.
     *
     * @param text - text to type
     * @return new text typing animation
     */
    public static TextIterator textTypingOldSchool(@NonNull String text) {
        return textTyping(text, "_", 10, 2);
    }

    /**
     * Creates new text typing animation.
     *
     * @param text             - text to type
     * @param cursor           - cursor to flick
     * @param idleTicks        - idle ticks between typing
     * @param typingSpeedTicks - ticks between each character
     * @return new text typing animation
     */
    public static TextIterator textTyping(@NonNull String text, @NonNull String cursor, int idleTicks, int typingSpeedTicks) {
        return new TextTypingAnimation(text, cursor, idleTicks, typingSpeedTicks);
    }

    /**
     * Creates new text fade animation.
     *
     * @param text           - text to fade
     * @param primaryColor   - primary color
     * @param fadeColor      - fade color
     * @param secondaryColor - secondary color
     * @return new text fade animation
     */
    public TextIterator textFade(@NonNull String text, @NonNull ChatColor primaryColor, @NonNull ChatColor fadeColor, @NonNull ChatColor secondaryColor) {
        return new TextFadeAnimation(text, primaryColor, fadeColor, secondaryColor);
    }

    /**
     * Creates new text fade animation. Just like on Hypixel server sidebars title.
     *
     * @param text - text to fade
     * @return new text fade animation
     */
    public TextIterator textFadeHypixel(@NonNull String text) {
        return textFade(text, ChatColor.YELLOW, ChatColor.GOLD, ChatColor.WHITE);
    }
}
