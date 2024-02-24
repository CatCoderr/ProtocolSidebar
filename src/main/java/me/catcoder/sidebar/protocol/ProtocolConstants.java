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
    public static final int MINECRAFT_1_15_1 = 575;
    public static final int MINECRAFT_1_15_2 = 578;

    public static final int MINECRAFT_1_16 = 735;
    public static final int MINECRAFT_1_16_1 = 736;
    public static final int MINECRAFT_1_16_2 = 751;
    public static final int MINECRAFT_1_16_3 = 753;
    public static final int MINECRAFT_1_16_4 = 754;
    public static final int MINECRAFT_1_16_5 = 754;

    public static final int MINECRAFT_1_17 = 755;
    public static final int MINECRAFT_1_17_1 = 756;

    public static final int MINECRAFT_1_18 = 757;
    public static final int MINECRAFT_1_18_1 = 757;
    public static final int MINECRAFT_1_18_2 = 758;

    public static final int MINECRAFT_1_19 = 759;

    public static final int MINECRAFT_1_19_1 = 760;
    public static final int MINECRAFT_1_19_2 = 760;

    public static final int MINECRAFT_1_19_3 = 761;

    public static final int MINECRAFT_1_19_4 = 762;

    public static final int MINECRAFT_1_20 = 763;
    public static final int MINECRAFT_1_20_1 = 763;
    public static final int MINECRAFT_1_20_2 = 764;
    public static final int MINECRAFT_1_20_3 = 765;
    public static final int MINECRAFT_1_20_4 = 766;

    public static final int MINIMUM_SUPPORTED_VERSION = MINECRAFT_1_12_2;
    public static final int MAXIMUM_SUPPORTED_VERSION = MINECRAFT_1_20_4;

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
