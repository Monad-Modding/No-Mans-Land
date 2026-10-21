package com.farcr.nomansland.common.networking.barrel;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.BarrelCarrying;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record ClientboundBarrelCarriersPacket(List<Integer> players) implements CustomPacketPayload {
    public static final StreamCodec<ByteBuf, ClientboundBarrelCarriersPacket> STREAM_CODEC = ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()).map(ClientboundBarrelCarriersPacket::new, ClientboundBarrelCarriersPacket::players);
    public static final Type<ClientboundBarrelCarriersPacket> TYPE = new Type<>(NoMansLand.location("client/barrel_carriers"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handleData(IPayloadContext context) {
        context.enqueueWork(() -> BarrelCarrying.setCarriers(this.players));
    }
}
