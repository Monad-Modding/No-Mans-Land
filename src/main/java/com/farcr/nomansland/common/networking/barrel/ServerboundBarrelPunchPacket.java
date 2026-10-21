package com.farcr.nomansland.common.networking.barrel;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.integration.Mods;
import com.farcr.nomansland.common.integration.sable.BarrelPhysics;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundBarrelPunchPacket(BlockPos barrel) implements CustomPacketPayload {
    public static final StreamCodec<ByteBuf, ServerboundBarrelPunchPacket> STREAM_CODEC = BlockPos.STREAM_CODEC.map(ServerboundBarrelPunchPacket::new, ServerboundBarrelPunchPacket::barrel);
    public static final Type<ServerboundBarrelPunchPacket> TYPE = new Type<>(NoMansLand.location("server/barrel_punch"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handleData(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (Mods.SABLE.isLoaded() && context.player() instanceof ServerPlayer player) {
                BarrelPhysics.punch(player, this.barrel);
            }
        });
    }
}
