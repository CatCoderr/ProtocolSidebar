package me.catcoder.sidebar.text.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;

import org.bukkit.ChatColor;

import lombok.NonNull;
import me.catcoder.sidebar.text.TextFrame;
import me.catcoder.sidebar.text.TextIterator;

/**
 * Simple text animation with 3 colors.
 * Just like on Hypixel server sidebars title.
 * 
 * @author CatCoder
 */
public class TextFadeAnimation extends TextIterator {

    private static final long TICKS_PER_SECOND = 20L;

    private final String text;
    private final ChatColor primaryColor; 
    private final ChatColor fadeColor; 
    private final ChatColor secondaryColor;

    private final Iterator<TextFrame> frameIterator;
    private final String colorCodes;

    private long currentFrameDelayTicks = 0;
    private TextFrame currentFrame;

    public TextFadeAnimation(
            @NonNull String text,
            @NonNull ChatColor primaryColor,
            @NonNull ChatColor fadeColor,
            @NonNull ChatColor secondaryColor) {
        Preconditions.checkArgument(text.length() >= 3, "Text length must be at least 3 characters");

        this.text = ChatColor.stripColor(text);
        this.primaryColor = primaryColor;
        this.fadeColor = fadeColor;
        this.secondaryColor = secondaryColor;
        this.colorCodes = ChatColor.getLastColors(text);

        this.frameIterator = Iterators.cycle(createAnimationFrames());

    }

    void start(List<TextFrame> frames) {
        frames.add(TextFrame.of(primaryColor + colorCodes + text, 3 * TICKS_PER_SECOND));
    }

    void end(List<TextFrame> frames) {
        // flick
        TextFrame secondary = TextFrame.of(secondaryColor + colorCodes + text, 8);
        TextFrame primary = TextFrame.of(primaryColor + colorCodes + text, 8);

        frames.add(secondary);
        frames.add(primary);
        frames.add(secondary);
        frames.add(primary);
    }

    public List<TextFrame> createAnimationFrames() {
        List<TextFrame> frames = new ArrayList<>();

        start(frames);

        int primaryColorLength = primaryColor.toString().length();

        for (int i = 0; i < text.length(); i++) {
            StringBuilder builder = new StringBuilder(text);

            int offset = 0;

            // put secondary color in front if fadeColor was used
            if (i > 0) {
                builder.insert(0, secondaryColor);

                offset += colorCodes.length();
                builder.insert(offset, colorCodes);

                offset += primaryColorLength;
            }


            // fadeColor + colorCodes
            builder.insert(i + offset, colorCodes);
            builder.insert(i + 1 + offset + colorCodes.length(), primaryColor);

            // primaryColor + colorCodes
            builder.insert(i + offset + primaryColorLength + colorCodes.length() + 1, colorCodes);
            builder.insert(i + offset, fadeColor);


            if (i + 1 == text.length()) {
                // remove primary color and other color codes from the end
                builder.setLength(builder.length() - primaryColorLength - colorCodes.length());
            }

            frames.add(TextFrame.of(builder.toString(), 2));

        }

        end(frames);

        return frames;
    }

    @Override
    public String next() {
        if (currentFrame == null || --currentFrameDelayTicks <= 0) {
            currentFrame = frameIterator.next();
            currentFrameDelayTicks = currentFrame.getDelay();
        }

        return currentFrame.getText();
    }

}
