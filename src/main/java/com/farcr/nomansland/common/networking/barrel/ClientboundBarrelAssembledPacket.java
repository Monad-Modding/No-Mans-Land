package com.farcr.nomansland.common.networking.barrel;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.BarrelGhosts;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClientboundBarrelAssembledPacket(BlockPos world, BlockPos plot, BlockState state, long fuseEnd) implements CustomPacketPayload {
    public static final StreamCodec<ByteBuf, ClientboundBarrelAssembledPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ClientboundBarrelAssembledPacket::world,
            BlockPos.STREAM_CODEC, ClientboundBarrelAssembledPacket::plot,
            ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY), ClientboundBarrelAssembledPacket::state,
            ByteBufCodecs.VAR_LONG, ClientboundBarrelAssembledPacket::fuseEnd,
            ClientboundBarrelAssembledPacket::new
    );
    public static final Type<ClientboundBarrelAssembledPacket> TYPE = new Type<>(NoMansLand.location("client/barrel_assembled"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handleData(IPayloadContext context) {
        context.enqueueWork(() -> BarrelGhosts.add(this.world, this.plot, this.state, this.fuseEnd));
    }
}
