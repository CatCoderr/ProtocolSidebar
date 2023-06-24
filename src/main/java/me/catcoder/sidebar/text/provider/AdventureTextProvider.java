package me.catcoder.sidebar.text.provider;

import lombok.NonNull;
import me.catcoder.sidebar.text.TextProvider;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.ComponentSerializer;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

public class AdventureTextProvider implements TextProvider<Component> {

    public static final ComponentSerializer<Component, Component, String> GSON_SERIALIZER =
            GsonComponentSerializer.gson();
    public static final ComponentSerializer<Component, TextComponent, String> LEGACY_SERIALIZER =
            LegacyComponentSerializer.legacySection();


    @Override
    public String asJsonMessage(@NonNull Player player, @NonNull Component component) {
        return GSON_SERIALIZER.serialize(component);
    }

    @Override
    public Component emptyMessage() {
        return Component.empty();
    }

    @Override
    public Component fromLegacyMessage(@NonNull String message) {
        return LEGACY_SERIALIZER.deserialize(message);
    }

    @Override
    public String asLegacyMessage(@NonNull Player player, @NonNull Component component) {
        return LEGACY_SERIALIZER.serialize(component);
    }
}
