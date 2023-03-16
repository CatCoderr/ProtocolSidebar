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
import org.bukkit.entity.Player;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.UUID;

import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(VersionUtil.class)
public class TeamPacketTest_117 {

    private final BungeeCordChatTextProvider textProvider = new BungeeCordChatTextProvider();

    @BeforeClass
    public static void beforeClass() {
        BukkitInitialization_117.initializeItemMeta();
    }

    @Test
    public void testTeamPacketIntegrity_remove() {
        Player player = mock(Player.class);

        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        mockStatic(VersionUtil.class);
        when(VersionUtil.getPlayerVersion(player.getUniqueId())).thenReturn(VersionUtil.SERVER_VERSION);

        WirePacket packet = ProtocolUtil.createTeamPacket(
                ProtocolUtil.TEAM_REMOVED, 1, "test", player, null, textProvider);

        ByteBuf buffer = Unpooled.wrappedBuffer(packet.getBytes());

        PacketPlayOutScoreboardTeam vanillaPacket = createVanillaPacket(buffer);

        assertEquals("test", vanillaPacket.d());
        assertFalse(vanillaPacket.f().isPresent());
        assertFalse("buffer must not be readable", buffer.isReadable());
    }

    @Test
    public void testTeamPacketIntegrity_create() {
        Player player = mock(Player.class);

        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        mockStatic(VersionUtil.class);
        when(VersionUtil.getPlayerVersion(player.getUniqueId())).thenReturn(VersionUtil.SERVER_VERSION);

        WirePacket packet = ProtocolUtil.createTeamPacket(
                ProtocolUtil.TEAM_CREATED, 1, "test", player,
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
