package me.catcoder.sidebar.text;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value(staticConstructor = "of")
public class TextFrame {
    
    private final String text;
    private final long delay;
}
