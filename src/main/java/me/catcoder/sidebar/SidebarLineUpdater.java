package me.catcoder.sidebar;

import lombok.NonNull;
import org.bukkit.entity.Player;

@FunctionalInterface
public interface SidebarLineUpdater {

	String updateText(@NonNull Player player);
}
