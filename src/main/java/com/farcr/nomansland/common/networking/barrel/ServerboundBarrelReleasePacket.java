package com.farcr.nomansland.common.networking.barrel;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.block.PhysicsBarrelCarriers;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ServerboundBarrelReleasePacket implements CustomPacketPayload {
    public static final ServerboundBarrelReleasePacket INSTANCE = new ServerboundBarrelReleasePacket();
    public static final StreamCodec<ByteBuf, ServerboundBarrelReleasePacket> STREAM_CODEC = StreamCodec.unit(INSTANCE);
    public static final Type<ServerboundBarrelReleasePacket> TYPE = new Type<>(NoMansLand.location("server/barrel_release"));

    private ServerboundBarrelReleasePacket() {
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handleData(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                PhysicsBarrelCarriers.release(player);
            }
        });
    }
}
