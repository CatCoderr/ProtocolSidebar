package me.catcoder.sidebar.text.impl;

import lombok.NonNull;
import lombok.experimental.Delegate;
import me.catcoder.sidebar.text.FrameIterator;
import me.catcoder.sidebar.text.TextFrame;
import me.catcoder.sidebar.text.TextIterator;

import java.util.ArrayList;
import java.util.List;

public class TextTypingAnimation extends TextIterator {

    private final String text;
    private final String cursor;
    private final int idleTimeTicks;
    private final int typingSpeed;

    @Delegate(types = {FrameIterator.class})
    private final FrameIterator frameIterator;

    public TextTypingAnimation(@NonNull String text, @NonNull String cursor, int idleTicks, int typingSpeedTicks) {
        this.text = text;
        this.cursor = cursor;
        this.idleTimeTicks = idleTicks;
        this.typingSpeed = typingSpeedTicks;

        this.frameIterator = new FrameIterator(createAnimationFrames());
    }

    @Override
    protected void start(List<TextFrame> frames) {
        flickWithCursor(idleTimeTicks, frames, "");
    }

    private void flickWithCursor(int times, List<TextFrame> frames, String text) {
        for (int i = 0; i <= times; i++) {
            frames.add(
                    i % 2 == 0
                            ? TextFrame.of(text + cursor, 10)
                            : TextFrame.of(text, 10)
            );
        }
    }

    public List<TextFrame> createAnimationFrames() {
        // flick with cursor

        List<TextFrame> frames = new ArrayList<>();

        start(frames);

        for (int i = 0; i < text.length(); i++) {
            String frame = text.substring(0, i + 1);
            frames.add(TextFrame.of(frame + cursor, typingSpeed));
        }

        flickWithCursor(idleTimeTicks, frames, text);

        // erase text simulating backspace

        for (int i = text.length() - 1; i >= 0; i--) {
            String frame = text.substring(0, i);
            frames.add(TextFrame.of(frame + cursor, typingSpeed));
        }

        return frames;
    }

}
