package me.catcoder.sidebar.util;

import java.util.UUID;

import com.comphenix.protocol.utility.MinecraftProtocolVersion;
import com.viaversion.viaversion.ViaVersionPlugin;

import org.bukkit.Bukkit;

import lombok.NonNull;

public final class VersionUtil {

    public static final int MINECRAFT_1_13 = 393;
    public static final int SERVER_VERSION = MinecraftProtocolVersion.getCurrentVersion();

    public static int getPlayerVersion(@NonNull UUID id) {
        boolean isVia = Bukkit.getPluginManager().isPluginEnabled("ViaVersion");
        return isVia ? ViaVersionPlugin.getInstance().getApi().getPlayerVersion(id) : SERVER_VERSION;
    }
}
