package me.catcoder.sidebar.util;

import java.util.UUID;

import com.comphenix.protocol.utility.MinecraftProtocolVersion;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.viaversion.viaversion.ViaVersionPlugin;

import lombok.extern.log4j.Log4j;
import me.catcoder.sidebar.protocol.ProtocolConstants;
import org.bukkit.Bukkit;

import lombok.NonNull;

public final class VersionUtil {

    public static final int MINECRAFT_1_13 = 393;
    public static final int SERVER_VERSION = MinecraftProtocolVersion.getCurrentVersion();

    static {

        Bukkit.getLogger().info("[ProtocolSidebar] Server version: " + MinecraftVersion.getCurrentVersion().getVersion());
        Bukkit.getLogger().info("[ProtocolSidebar] Please report any bugs to the developer: https://github.com/CatCoderr/ProtocolSidebar/issues");

        if (SERVER_VERSION < ProtocolConstants.MINIMUM_SUPPORTED_VERSION) {
            throw new IllegalStateException("[ProtocolSidebar] Unsupported server version: " + SERVER_VERSION);
        }

        if (SERVER_VERSION > ProtocolConstants.MAXIMUM_SUPPORTED_VERSION) {
            Bukkit.getLogger().warning("[ProtocolSidebar] This Minecraft version (" + SERVER_VERSION + ") is not tested. " +
                    "Please report any bugs to the developer: https://github.com/CatCoderr/ProtocolSidebar/issues");
        }
    }

    public static int getPlayerVersion(@NonNull UUID id) {
        boolean isVia = Bukkit.getPluginManager().isPluginEnabled("ViaVersion");
        return isVia ? ViaVersionPlugin.getInstance().getApi().getPlayerVersion(id) : SERVER_VERSION;
    }
}
