package me.catcoder.sidebar.util.version;

import com.viaversion.viaversion.ViaVersionPlugin;
import com.viaversion.viaversion.api.ViaAPI;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

public final class VersionUtil {

    public static final int SERVER_VERSION = MinecraftProtocolVersion.getCurrentVersion();

    static {
        Bukkit.getLogger().info("[ProtocolSidebar] Server version: "
                + MinecraftVersion.getCurrentVersion() + " (protocol " + SERVER_VERSION + ")");
        Bukkit.getLogger().info("[ProtocolSidebar] Please report any bugs to the developer: https://github.com/CatCoderr/ProtocolSidebar/issues");
    }

    public static int getPlayerVersion(@NonNull UUID id) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("ViaVersion");

        if (plugin == null || !plugin.isEnabled()) {
            return SERVER_VERSION;
        }

        return ViaLookup.playerVersion(plugin, id);
    }

    private static final class ViaLookup {

        private static volatile ViaLookup current;

        private final Plugin plugin;
        private final ViaAPI<?> api;

        private ViaLookup(Plugin plugin, ViaAPI<?> api) {
            this.plugin = plugin;
            this.api = api;
        }

        static int playerVersion(Plugin plugin, UUID id) {
            ViaLookup lookup = current;

            if (lookup == null || lookup.plugin != plugin) {
                current = lookup = new ViaLookup(plugin, ((ViaVersionPlugin) plugin).getApi());
            }

            return lookup.api.getPlayerProtocolVersion(id).getVersion();
        }
    }
}
