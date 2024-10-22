package me.catcoder.sidebar.protocol;

import me.catcoder.sidebar.util.version.VersionUtil;

import static me.catcoder.sidebar.protocol.ProtocolConstants.map;


public enum PacketIds {

    UPDATE_TEAMS(
            map(ProtocolConstants.MINECRAFT_1_12_2, 0x44),
            map(ProtocolConstants.MINECRAFT_1_13, 0x47),
            map(ProtocolConstants.MINECRAFT_1_14, 0x4B),
            map(ProtocolConstants.MINECRAFT_1_15, 0x4C),
            map(ProtocolConstants.MINECRAFT_1_17, 0x55),
            map(ProtocolConstants.MINECRAFT_1_19_1, 0x58),
            map(ProtocolConstants.MINECRAFT_1_19_3, 0x56),
            map(ProtocolConstants.MINECRAFT_1_19_4, 0x5A),
            map(ProtocolConstants.MINECRAFT_1_20_2, 0x5C),
            map(ProtocolConstants.MINECRAFT_1_20_4, 0x5E),
            map(ProtocolConstants.MINECRAFT_1_20_4, 0x5E),
            map(ProtocolConstants.MINECRAFT_1_20_6, 0x60),
            map(ProtocolConstants.MINECRAFT_1_21_2, 0x67)
    ),
    UPDATE_SCORE(
            map(ProtocolConstants.MINECRAFT_1_12_2, 0x45),
            map(ProtocolConstants.MINECRAFT_1_13, 0x48),
            map(ProtocolConstants.MINECRAFT_1_14, 0x4C),
            map(ProtocolConstants.MINECRAFT_1_15, 0x4D),
            map(ProtocolConstants.MINECRAFT_1_17, 0x56),
            map(ProtocolConstants.MINECRAFT_1_19_1, 0x59),
            map(ProtocolConstants.MINECRAFT_1_19_3, 0x57),
            map(ProtocolConstants.MINECRAFT_1_19_4, 0x5B),
            map(ProtocolConstants.MINECRAFT_1_20_2, 0x5D),
            map(ProtocolConstants.MINECRAFT_1_20_4, 0x5F),
            map(ProtocolConstants.MINECRAFT_1_20_6, 0x61),
            map(ProtocolConstants.MINECRAFT_1_21_2, 0x68)


    ),
    RESET_SCORE(
            map(ProtocolConstants.MINECRAFT_1_20_3, 0x42),
            map(ProtocolConstants.MINECRAFT_1_20_4, 0x44),
            map(ProtocolConstants.MINECRAFT_1_21_2, 0x49)
    ),
    OBJECTIVE_DISPLAY(
            map(ProtocolConstants.MINECRAFT_1_12_2, 0x3B),
            map(ProtocolConstants.MINECRAFT_1_13, 0x3E),
            map(ProtocolConstants.MINECRAFT_1_14, 0x42),
            map(ProtocolConstants.MINECRAFT_1_15, 0x43),
            map(ProtocolConstants.MINECRAFT_1_17, 0x4C),
            map(ProtocolConstants.MINECRAFT_1_19_1, 0x4F),
            map(ProtocolConstants.MINECRAFT_1_19_3, 0x4D),
            map(ProtocolConstants.MINECRAFT_1_19_4, 0x51),
            map(ProtocolConstants.MINECRAFT_1_20_2, 0x53),
            map(ProtocolConstants.MINECRAFT_1_20_4, 0x55),
            map(ProtocolConstants.MINECRAFT_1_20_6, 0x57),
            map(ProtocolConstants.MINECRAFT_1_21_2, 0x5C)

    ),
    OBJECTIVE(
            map(ProtocolConstants.MINECRAFT_1_12_2, 0x42),
            map(ProtocolConstants.MINECRAFT_1_13, 0x45),
            map(ProtocolConstants.MINECRAFT_1_14, 0x49),
            map(ProtocolConstants.MINECRAFT_1_15, 0x4A),
            map(ProtocolConstants.MINECRAFT_1_17, 0x53),
            map(ProtocolConstants.MINECRAFT_1_19_1, 0x56),
            map(ProtocolConstants.MINECRAFT_1_19_3, 0x54),
            map(ProtocolConstants.MINECRAFT_1_19_4, 0x58),
            map(ProtocolConstants.MINECRAFT_1_20_2, 0x5A),
            map(ProtocolConstants.MINECRAFT_1_20_4, 0x5C),
            map(ProtocolConstants.MINECRAFT_1_20_6, 0x5E),
            map(ProtocolConstants.MINECRAFT_1_21_2, 0x64)
    );

    private final ProtocolConstants.ProtocolMapping[] mappings;

    PacketIds(ProtocolConstants.ProtocolMapping... mappings) {
        this.mappings = mappings;
    }

    public int getServerPacketId() {
        return getPacketId(VersionUtil.SERVER_VERSION);
    }

    public int getPacketId(int protocolVersion) {

        for (int protocol = ProtocolConstants.MINIMUM_SUPPORTED_VERSION;
             protocol <= ProtocolConstants.MAXIMUM_SUPPORTED_VERSION; protocol++) {
            int index = 0;

            for (ProtocolConstants.ProtocolMapping mapping : mappings) {
                if (mapping.getProtocol() == protocol
                        && mapping.getProtocol() <= protocolVersion
                        && (index == mappings.length - 1 || mappings[index + 1].getProtocol() > protocolVersion))
                    return mapping.getPacketId();

                index++;
            }

        }

        throw new IllegalArgumentException("Unsupported protocol version: " + protocolVersion);
    }
}
