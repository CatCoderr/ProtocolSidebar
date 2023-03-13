package me.catcoder.sidebar.text;

import me.catcoder.sidebar.util.lang.ThrowingFunction;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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


    public ThrowingFunction<Player, BaseComponent[], Throwable> asLineUpdater() {
        Map<String, BaseComponent[]> cache = new HashMap<>();
        return player -> cache.computeIfAbsent(next(), TextComponent::fromLegacyText);
    }

}
