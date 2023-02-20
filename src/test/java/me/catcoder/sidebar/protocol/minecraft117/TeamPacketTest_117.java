package me.catcoder.sidebar.protocol.minecraft117;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.comphenix.protocol.injector.netty.WirePacket;

import org.junit.BeforeClass;
import org.junit.Test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.NonNull;
import me.catcoder.sidebar.util.ProtocolUtil;
import me.catcoder.sidebar.util.VersionUtil;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam;

public class TeamPacketTest_117 {

    @BeforeClass
    public static void beforeClass() {
        BukkitInitialization_117.initializeItemMeta();
    }

    @Test
    public void testTeamPacketIntegrity_remove() {
        WirePacket packet = ProtocolUtil.createTeamPacket(
                ProtocolUtil.TEAM_REMOVED, 1, "test", VersionUtil.SERVER_VERSION, null);

        ByteBuf buffer = Unpooled.wrappedBuffer(packet.getBytes());

        PacketPlayOutScoreboardTeam vanillaPacket = createVanillaPacket(buffer);

        assertEquals("test", vanillaPacket.d());
        assertFalse(vanillaPacket.f().isPresent());
        assertFalse("buffer must not be readable", buffer.isReadable());
    }

    @Test
    public void testTeamPacketIntegrity_create() {
        WirePacket packet = ProtocolUtil.createTeamPacket(
                ProtocolUtil.TEAM_CREATED, 1, "test", VersionUtil.SERVER_VERSION, "text");
        ByteBuf buffer = Unpooled.wrappedBuffer(packet.getBytes());

        PacketPlayOutScoreboardTeam vanillaPacket = createVanillaPacket(buffer);

        assertEquals("test", vanillaPacket.d());
        assertTrue(vanillaPacket.f().isPresent());
        assertEquals("§ftext", vanillaPacket.f().get().f().getText());
        assertEquals("§f", vanillaPacket.f().get().g().getText());
        assertFalse("buffer must not be readable", buffer.isReadable());
    }

    private static PacketPlayOutScoreboardTeam createVanillaPacket(@NonNull ByteBuf packet) {
        return new PacketPlayOutScoreboardTeam(
                new PacketDataSerializer(packet));
    }
}
