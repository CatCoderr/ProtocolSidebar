package me.catcoder.sidebar.text;

import com.google.common.collect.Iterators;
import lombok.NonNull;

import java.util.Iterator;

public final class FrameIterator implements Iterator<String> {

    private final Iterator<TextFrame> frameIterator;

    private long currentFrameDelayTicks = 0;
    private TextFrame currentFrame;

    public FrameIterator(@NonNull Iterable<TextFrame> frames) {
        this.frameIterator = Iterators.cycle(frames);
    }
    

    @Override
    public String next() {
        if (currentFrame == null || --currentFrameDelayTicks <= 0) {
            currentFrame = frameIterator.next();
            currentFrameDelayTicks = currentFrame.getDelay();
        }

        return currentFrame.getText();
    }


    @Override
    public boolean hasNext() {
        return true;
    }
}
