package me.catcoder.sidebar.protocol.inject;

import com.comphenix.protocol.injector.netty.WirePacket;
import io.netty.buffer.Unpooled;
import lombok.NonNull;
import me.catcoder.sidebar.SidebarLine;
import me.catcoder.sidebar.protocol.BukkitInitialization;
import me.catcoder.sidebar.util.VersionUtil;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class TeamPacketTest {


    @BeforeClass
    public static void beforeClass() {
        BukkitInitialization.initializeItemMeta();
    }

    @Test
    public void testTeamPacketIntegrity_remove() {
        WirePacket packet = SidebarLine.createTeamPacket(
                SidebarLine.TEAM_REMOVED, 1, "test", VersionUtil.SERVER_VERSION, null);
        PacketPlayOutScoreboardTeam vanillaPacket = createVanillaPacket(packet);

        assertEquals("test", vanillaPacket.d());
        assertFalse(vanillaPacket.f().isPresent());
    }


    @Test
    public void testTeamPacketIntegrity_create() {
        WirePacket packet = SidebarLine.createTeamPacket(
                SidebarLine.TEAM_CREATED, 1, "test", VersionUtil.SERVER_VERSION, "text");
        PacketPlayOutScoreboardTeam vanillaPacket = createVanillaPacket(packet);


        assertEquals("test", vanillaPacket.d());
        assertTrue(vanillaPacket.f().isPresent());
        assertEquals("§ftext", vanillaPacket.f().get().f().getText());
        assertEquals("§f", vanillaPacket.f().get().g().getText());
    }


    private static PacketPlayOutScoreboardTeam createVanillaPacket(@NonNull WirePacket packet) {
        return new PacketPlayOutScoreboardTeam(
                new PacketDataSerializer(Unpooled.wrappedBuffer(packet.getBytes()))
        );
    }
}
