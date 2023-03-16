package me.catcoder.sidebar.text.provider;

import lombok.NonNull;
import me.catcoder.sidebar.text.TextProvider;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.entity.Player;

public class BungeeCordChatTextProvider implements TextProvider<BaseComponent[]> {

    @Override
    public String asJsonMessage(@NonNull Player player, BaseComponent @NonNull [] components) {
        if (components.length > 0 && components[0] instanceof TextComponent textComponent) {
            textComponent.setColor(textComponent.getColor());
        }
        return ComponentSerializer.toString(components);
    }

    @Override
    public BaseComponent[] fromLegacyMessage(@NonNull String message) {
        return TextComponent.fromLegacyText(message);
    }

    @Override
    public String asLegacyMessage(@NonNull Player player, BaseComponent @NonNull [] component) {
        return TextComponent.toLegacyText(component);
    }
}
