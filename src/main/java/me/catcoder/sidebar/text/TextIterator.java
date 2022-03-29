package me.catcoder.sidebar.text;

import java.util.Iterator;

public abstract class TextIterator implements Iterator<String> {

    @Override
    public abstract String next();

    @Override
    public final boolean hasNext() {
        return true; 
    }


    public static TextIterator staticText(String text) {
        return new TextIterator() {
            @Override
            public String next() {
                return text;
            }
        };
    }
    
}
