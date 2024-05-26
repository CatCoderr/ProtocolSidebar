package me.catcoder.sidebar.util.version;

import me.catcoder.sidebar.protocol.ProtocolConstants;

import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * A lookup of the associated protocol version of a given Minecraft server.
 *
 * @author Kristian
 */
public final class MinecraftProtocolVersion {

    private static final NavigableMap<MinecraftVersion, Integer> LOOKUP = createLookup();

    private static NavigableMap<MinecraftVersion, Integer> createLookup() {
        TreeMap<MinecraftVersion, Integer> map = new TreeMap<>();

        // Source: http://wiki.vg/Protocol_version_numbers
        // Doesn't include pre-releases

        map.put(new MinecraftVersion(1, 12, 2), ProtocolConstants.MINECRAFT_1_12_2);

        map.put(new MinecraftVersion(1, 13, 0), ProtocolConstants.MINECRAFT_1_13);
        map.put(new MinecraftVersion(1, 13, 1), ProtocolConstants.MINECRAFT_1_13_1);
        map.put(new MinecraftVersion(1, 13, 2), ProtocolConstants.MINECRAFT_1_13_2);

        map.put(new MinecraftVersion(1, 14, 0), ProtocolConstants.MINECRAFT_1_14);
        map.put(new MinecraftVersion(1, 14, 1), ProtocolConstants.MINECRAFT_1_14_1);
        map.put(new MinecraftVersion(1, 14, 2), ProtocolConstants.MINECRAFT_1_14_2);
        map.put(new MinecraftVersion(1, 14, 3), ProtocolConstants.MINECRAFT_1_14_3);
        map.put(new MinecraftVersion(1, 14, 4), ProtocolConstants.MINECRAFT_1_14_4);

        map.put(new MinecraftVersion(1, 15, 0), ProtocolConstants.MINECRAFT_1_15);
        map.put(new MinecraftVersion(1, 15, 1), ProtocolConstants.MINECRAFT_1_15_1);
        map.put(new MinecraftVersion(1, 15, 2), ProtocolConstants.MINECRAFT_1_15_2);

        map.put(new MinecraftVersion(1, 16, 0), ProtocolConstants.MINECRAFT_1_16);
        map.put(new MinecraftVersion(1, 16, 1), ProtocolConstants.MINECRAFT_1_16_1);
        map.put(new MinecraftVersion(1, 16, 2), ProtocolConstants.MINECRAFT_1_16_2);
        map.put(new MinecraftVersion(1, 16, 3), ProtocolConstants.MINECRAFT_1_16_3);
        map.put(new MinecraftVersion(1, 16, 4), ProtocolConstants.MINECRAFT_1_16_4);
        map.put(new MinecraftVersion(1, 16, 5), ProtocolConstants.MINECRAFT_1_16_5);

        map.put(new MinecraftVersion(1, 17, 0), ProtocolConstants.MINECRAFT_1_17);
        map.put(new MinecraftVersion(1, 17, 1), ProtocolConstants.MINECRAFT_1_17_1);

        map.put(new MinecraftVersion(1, 18, 0), ProtocolConstants.MINECRAFT_1_18);
        map.put(new MinecraftVersion(1, 18, 1), ProtocolConstants.MINECRAFT_1_18_1);
        map.put(new MinecraftVersion(1, 18, 2), ProtocolConstants.MINECRAFT_1_18_2);

        map.put(new MinecraftVersion(1, 19, 0), ProtocolConstants.MINECRAFT_1_19);
        map.put(new MinecraftVersion(1, 19, 2), ProtocolConstants.MINECRAFT_1_19_2);
        map.put(new MinecraftVersion(1, 19, 3), ProtocolConstants.MINECRAFT_1_19_3);
        map.put(new MinecraftVersion(1, 19, 4), ProtocolConstants.MINECRAFT_1_19_4);

        map.put(new MinecraftVersion(1, 20, 0), ProtocolConstants.MINECRAFT_1_20);

        map.put(new MinecraftVersion(1, 20, 2), ProtocolConstants.MINECRAFT_1_20_2);
        map.put(new MinecraftVersion(1, 20, 3), ProtocolConstants.MINECRAFT_1_20_3);
        map.put(new MinecraftVersion(1, 20, 4), ProtocolConstants.MINECRAFT_1_20_4);
        map.put(new MinecraftVersion(1, 20, 5), ProtocolConstants.MINECRAFT_1_20_5);
        map.put(new MinecraftVersion(1, 20, 6), ProtocolConstants.MINECRAFT_1_20_6);

        return map;
    }

    /**
     * Retrieve the version of the Minecraft protocol for the current version of Minecraft.
     *
     * @return The version number.
     */
    public static int getCurrentVersion() {
        return getVersion(MinecraftVersion.getCurrentVersion());
    }

    /**
     * Retrieve the version of the Minecraft protocol for this version of Minecraft.
     *
     * @param version - the version.
     * @return The version number.
     */
    public static int getVersion(MinecraftVersion version) {
        Entry<MinecraftVersion, Integer> result = LOOKUP.floorEntry(version);
        return result != null ? result.getValue() : Integer.MIN_VALUE;
    }
}
