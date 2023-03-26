package me.catcoder.sidebar;

import me.catcoder.sidebar.protocol.PacketIds;
import me.catcoder.sidebar.protocol.ProtocolConstants;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PacketIdsTest {


    @Test
    public void testPacketIds() {
        assertEquals(0x44, PacketIds.UPDATE_TEAMS.getPacketId(ProtocolConstants.MINECRAFT_1_12_2));
        assertEquals(0x47, PacketIds.UPDATE_TEAMS.getPacketId(ProtocolConstants.MINECRAFT_1_13));
        assertEquals(0x47, PacketIds.UPDATE_TEAMS.getPacketId(ProtocolConstants.MINECRAFT_1_13_2));
        assertEquals(0x47, PacketIds.UPDATE_TEAMS.getPacketId(ProtocolConstants.MINECRAFT_1_13_1));

        assertEquals(0x4B, PacketIds.UPDATE_TEAMS.getPacketId(ProtocolConstants.MINECRAFT_1_14));
        assertEquals(0x4B, PacketIds.UPDATE_TEAMS.getPacketId(ProtocolConstants.MINECRAFT_1_14_1));
        assertEquals(0x4B, PacketIds.UPDATE_TEAMS.getPacketId(ProtocolConstants.MINECRAFT_1_14_2));
        assertEquals(0x4B, PacketIds.UPDATE_TEAMS.getPacketId(ProtocolConstants.MINECRAFT_1_14_3));
        assertEquals(0x4B, PacketIds.UPDATE_TEAMS.getPacketId(ProtocolConstants.MINECRAFT_1_14_4));


        assertEquals(0x56, PacketIds.UPDATE_TEAMS.getPacketId(ProtocolConstants.MINECRAFT_1_19_3));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnsupportedVersion() {
        PacketIds.UPDATE_TEAMS.getPacketId(47); // 1.8
    }
}
