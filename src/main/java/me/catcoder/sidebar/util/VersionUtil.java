package me.catcoder.sidebar.util;

import com.comphenix.protocol.utility.MinecraftProtocolVersion;
import org.bukkit.Bukkit;
import us.myles.ViaVersion.api.Via;

import java.util.UUID;

public class VersionUtil {

    public static final int MINECRAFT_1_13 = 393;

    private static final int SERVER_VERSION = MinecraftProtocolVersion.getCurrentVersion();

    public static int getPlayerVersion(UUID id) {
        boolean isVia = Bukkit.getPluginManager().isPluginEnabled("ViaVersion");
        return isVia ? Via.getAPI().getPlayerVersion(id) : SERVER_VERSION;
    }
}
