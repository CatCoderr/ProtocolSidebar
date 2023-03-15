package me.catcoder.sidebar.text;

import java.util.Iterator;
import java.util.List;

public abstract class TextIterator implements Iterator<String> {

    @Override
    public abstract String next();

    protected void end(List<TextFrame> frames) {

    }

    protected void start(List<TextFrame> frames) {

    }

    @Override
    public boolean hasNext() {
        return true;
    }

}
