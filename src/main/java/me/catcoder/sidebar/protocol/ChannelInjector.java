package me.catcoder.sidebar.protocol;

import com.google.common.collect.MapMaker;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import lombok.NonNull;
import lombok.SneakyThrows;
import me.catcoder.sidebar.util.Reflection;
import org.bukkit.entity.Player;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.Map;

public class ChannelInjector {

    private static final MethodHandle GET_PLAYER_HANDLE;
    private static final MethodHandle GET_CONNECTION;
    private static final MethodHandle GET_MANAGER;
    private static final MethodHandle GET_CHANNEL;

    static {
        MethodHandles.Lookup lookup = MethodHandles.lookup();

        try {
            GET_PLAYER_HANDLE = lookup.unreflect(
                    Reflection.getMethod("{obc}.entity.CraftPlayer", "getHandle").handle());

            Class<?> entityPlayer = Reflection.getClass(
                    "net.minecraft.server.level.EntityPlayer",
                    "net.minecraft.server.level.ServerPlayer",
                    "{nms}.EntityPlayer");

            Class<?> playerConnection = Reflection.getClass(
                    "{nms}.PlayerConnection",
                    "net.minecraft.server.network.PlayerConnection",
                    "net.minecraft.server.network.ServerGamePacketListenerImpl");
            Class<?> networkManager = Reflection.getClass(
                    "{nms}.NetworkManager",
                    "net.minecraft.network.NetworkManager",
                    "net.minecraft.network.Connection");

            GET_CONNECTION = lookup.unreflectGetter(
                    Reflection.getField(entityPlayer, playerConnection, 0).handle());
            GET_MANAGER = lookup.unreflectGetter(
                    Reflection.getField(playerConnection, networkManager, 0).handle());

            GET_CHANNEL = lookup.unreflectGetter(
                    Reflection.getField(networkManager, Channel.class, 0).handle());

        } catch (Throwable throwable) {
            throw new ExceptionInInitializerError(throwable);
        }
    }

    // weakValues means that channel will be removed from map when player disconnects
    private final Map<String, Channel> channelLookup = new MapMaker().weakValues().makeMap();

    public static final ChannelInjector IMP = new ChannelInjector();

    private ChannelInjector() {
        // Seal class
    }

    @SneakyThrows
    public Channel getChannel(Player player) {
        Channel channel = channelLookup.get(player.getName());

        // Lookup channel again
        if (channel == null || !channel.isOpen()) {
            Object connection = GET_CONNECTION.invoke(GET_PLAYER_HANDLE.invoke(player));
            Object manager = GET_MANAGER.invoke(connection);

            channelLookup.put(player.getName(), channel = (Channel) GET_CHANNEL.invoke(manager));
        }

        return channel;
    }

    public ChannelFuture sendPacket(@NonNull Player player, @NonNull Object packet) {
        return getChannel(player).writeAndFlush(packet);
    }
}
