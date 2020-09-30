package me.catcoder.sidebar.util;

import com.comphenix.protocol.utility.MinecraftProtocolVersion;
import lombok.NonNull;
import org.bukkit.Bukkit;
import us.myles.ViaVersion.api.Via;

import java.util.UUID;

public final class VersionUtil {

    public static final int MINECRAFT_1_13 = 393;
    public static final int SERVER_VERSION = MinecraftProtocolVersion.getCurrentVersion();

    public static int getPlayerVersion(@NonNull UUID id) {
        boolean isVia = Bukkit.getPluginManager().isPluginEnabled("ViaVersion");
        return isVia ? Via.getAPI().getPlayerVersion(id) : SERVER_VERSION;
    }
}
