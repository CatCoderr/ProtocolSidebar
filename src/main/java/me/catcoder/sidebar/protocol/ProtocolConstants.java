package me.catcoder.sidebar.protocol;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class ProtocolConstants {
    public static final int MINECRAFT_1_12_2 = 340;
    public static final int MINECRAFT_1_13 = 393;
    public static final int MINECRAFT_1_13_1 = 401;
    public static final int MINECRAFT_1_13_2 = 404;
    public static final int MINECRAFT_1_14 = 477;
    public static final int MINECRAFT_1_14_1 = 480;
    public static final int MINECRAFT_1_14_2 = 485;
    public static final int MINECRAFT_1_14_3 = 490;
    public static final int MINECRAFT_1_14_4 = 498;
    public static final int MINECRAFT_1_15 = 573;
    public static final int MINECRAFT_1_17 = 755;
    public static final int MINECRAFT_1_19_1 = 760;
    public static final int MINECRAFT_1_19_3 = 761;

    public static final int MINIMUM_SUPPORTED_VERSION = MINECRAFT_1_12_2;
    public static final int MAXIMUM_SUPPORTED_VERSION = MINECRAFT_1_19_3;

    @Getter
    @RequiredArgsConstructor
    public static class ProtocolMapping {
        public final int protocol;
        public final int packetId;
    }

    public static ProtocolMapping map(int protocol, int packetId) {
        return new ProtocolMapping(protocol, packetId);
    }
}
