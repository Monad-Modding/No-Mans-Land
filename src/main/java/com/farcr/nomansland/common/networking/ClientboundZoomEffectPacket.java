package com.farcr.nomansland.common.networking;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.renderer.effect.AccumulateZoomRenderer;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClientboundZoomEffectPacket(int time) implements CustomPacketPayload {
    public static final StreamCodec<ByteBuf, ClientboundZoomEffectPacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT,
        ClientboundZoomEffectPacket::time,
        ClientboundZoomEffectPacket::new
    );

    public static final Type<ClientboundZoomEffectPacket> TYPE = new Type<>(NoMansLand.location("client/effect/zoom"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    public void handleData(final IPayloadContext context) {
        if (context.flow().isClientbound()) {
            context.enqueueWork(() -> {
                AccumulateZoomRenderer.getInstance()
                    .setEffectForTicks(this.time);
            });
        }
    }
}
