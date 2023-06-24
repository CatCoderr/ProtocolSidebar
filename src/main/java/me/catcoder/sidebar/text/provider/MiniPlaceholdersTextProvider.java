package me.catcoder.sidebar.text.provider;

import io.github.miniplaceholders.api.MiniPlaceholders;
import lombok.NonNull;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

public class MiniPlaceholdersTextProvider extends MiniMessageTextProvider {

    public MiniPlaceholdersTextProvider(MiniMessage miniMessage) {
        super(miniMessage);
    }

    @Override
    public String asJsonMessage(@NonNull Player player, @NonNull String message) {
        return AdventureTextProvider.GSON_SERIALIZER.serialize(
                miniMessage.deserialize(message, MiniPlaceholders.getAudienceGlobalPlaceholders(player)));
    }


    @Override
    public String asLegacyMessage(@NonNull Player player, @NonNull String component) {
        return AdventureTextProvider.LEGACY_SERIALIZER.serialize(
                miniMessage.deserialize(component, MiniPlaceholders.getAudienceGlobalPlaceholders(player)));
    }
}
