package me.catcoder.sidebar.text;

import lombok.NonNull;
import org.bukkit.entity.Player;

public interface TextProvider<T> {

    String asJsonMessage(@NonNull Player player, @NonNull T component);

    String asLegacyMessage(@NonNull Player player, @NonNull T component);

    T emptyMessage();

    T fromLegacyMessage(@NonNull String message);
}
