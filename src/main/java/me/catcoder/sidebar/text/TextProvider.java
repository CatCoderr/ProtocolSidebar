package me.catcoder.sidebar.text;

import lombok.NonNull;

public interface TextProvider<T> {

    String asJsonMessage(@NonNull T component);

    String asLegacyMessage(@NonNull T component);

    T fromLegacyMessage(@NonNull String message);
}
