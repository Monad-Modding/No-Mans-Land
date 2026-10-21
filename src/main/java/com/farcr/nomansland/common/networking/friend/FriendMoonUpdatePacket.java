package com.farcr.nomansland.common.networking.friend;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.friend.FriendMoon;
import com.farcr.nomansland.common.friend.FriendMoonUpdate;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class FriendMoonUpdatePacket {

    public static void toServer(FriendMoonUpdate.ToServer updateEvent) {
        PacketDistributor.sendToServer(
            new ToServer(updateEvent)
        );
    }

    public static void toClient(ServerPlayer player, FriendMoonUpdate.ToClient updateEvent) {
        if (player.connection != null) {
            PacketDistributor.sendToPlayer(player,
                new ToClient(updateEvent)
            );
        }
    }

    public record ToServer(
        FriendMoonUpdate.ToServer packetType
    ) implements CustomPacketPayload {
        public static final StreamCodec<ByteBuf, ToServer> STREAM_CODEC = StreamCodec.composite(
            FriendMoonUpdate.ToServer.STREAM_CODEC,
            ToServer::packetType,
            ToServer::new
        );
        public static final CustomPacketPayload.Type<ToServer> TYPE =
            new CustomPacketPayload.Type<>(NoMansLand.location("server/friend_moon/update_packet"));

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public void handleData(final IPayloadContext context) {
            if (context.flow().isServerbound()) {
                context.enqueueWork(() -> {
                    FriendMoon friendMoon = FriendMoon.getOrDefault(context.player().getServer().overworld());
                    if (friendMoon.playerHasFriendship((ServerPlayer) context.player()))
                        friendMoon.packetUpdateEvent(packetType(), (ServerPlayer) context.player());
                });
            }
        }
    }

    public record ToClient(
        FriendMoonUpdate.ToClient packetType
    ) implements CustomPacketPayload {
        public static final StreamCodec<ByteBuf, ToClient> STREAM_CODEC = StreamCodec.composite(
            FriendMoonUpdate.ToClient.STREAM_CODEC,
            ToClient::packetType,
            ToClient::new
        );
        public static final CustomPacketPayload.Type<ToClient> TYPE =
            new CustomPacketPayload.Type<>(NoMansLand.location("client/friend_moon/update_packet"));

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public void handleData(final IPayloadContext context) {
            if (context.flow().isClientbound()) {
                context.enqueueWork(() -> {
                    packetType().getConsumer().accept(context.player());
                });
            }
        }
    }
}