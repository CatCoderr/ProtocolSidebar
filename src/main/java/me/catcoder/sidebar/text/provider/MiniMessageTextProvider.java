package me.catcoder.sidebar.text.provider;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.catcoder.sidebar.text.TextProvider;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class MiniMessageTextProvider implements TextProvider<String> {

    protected final MiniMessage miniMessage;

    @Override
    public String asJsonMessage(@NonNull Player player, @NonNull String message) {
        return AdventureTextProvider.GSON_SERIALIZER.serialize(miniMessage.deserialize(message));
    }

    @Override
    public String asLegacyMessage(@NonNull Player player, @NonNull String component) {
        return AdventureTextProvider.LEGACY_SERIALIZER.serialize(miniMessage.deserialize(component));
    }

    @Override
    public String fromLegacyMessage(@NonNull String message) {
        return message; // will be transformed on packet level
    }
}
