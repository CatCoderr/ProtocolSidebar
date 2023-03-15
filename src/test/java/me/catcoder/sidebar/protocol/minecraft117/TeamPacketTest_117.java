package me.catcoder.sidebar.protocol.minecraft117;

import com.comphenix.protocol.injector.netty.WirePacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.NonNull;
import me.catcoder.sidebar.protocol.ProtocolUtil;
import me.catcoder.sidebar.text.provider.BungeeCordChatTextProvider;
import me.catcoder.sidebar.util.VersionUtil;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.EnumChatFormat;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.chat.ChatHexColor;
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class TeamPacketTest_117 {

    private final BungeeCordChatTextProvider textProvider = new BungeeCordChatTextProvider();

    @BeforeClass
    public static void beforeClass() {
        BukkitInitialization_117.initializeItemMeta();
    }

    @Test
    public void testTeamPacketIntegrity_remove() {
        WirePacket packet = ProtocolUtil.createTeamPacket(
                ProtocolUtil.TEAM_REMOVED, 1, "test", VersionUtil.SERVER_VERSION, null, textProvider);

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
                TextComponent.fromLegacyText("text"), textProvider);
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
