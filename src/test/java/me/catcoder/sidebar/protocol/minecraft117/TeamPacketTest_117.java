package me.catcoder.sidebar.protocol.minecraft117;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.comphenix.protocol.injector.netty.WirePacket;

import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.EnumChatFormat;
import net.minecraft.network.chat.ChatHexColor;
import org.junit.BeforeClass;
import org.junit.Test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.NonNull;
import me.catcoder.sidebar.protocol.ProtocolUtil;
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
                ProtocolUtil.TEAM_CREATED, 1, "test", VersionUtil.SERVER_VERSION,
                TextComponent.fromLegacyText("text"));
        ByteBuf buffer = Unpooled.wrappedBuffer(packet.getBytes());

        PacketPlayOutScoreboardTeam vanillaPacket = createVanillaPacket(buffer);

        assertEquals("test", vanillaPacket.d());
        assertTrue(vanillaPacket.f().isPresent());
        assertEquals("text", vanillaPacket.f().get().f().getText());
        assertEquals(ChatHexColor.a(EnumChatFormat.p), vanillaPacket.f().get().f().getChatModifier().getColor());
        assertFalse("buffer must not be readable", buffer.isReadable());
    }

    private static PacketPlayOutScoreboardTeam createVanillaPacket(@NonNull ByteBuf packet) {
        return new PacketPlayOutScoreboardTeam(
                new PacketDataSerializer(packet));
    }
}
