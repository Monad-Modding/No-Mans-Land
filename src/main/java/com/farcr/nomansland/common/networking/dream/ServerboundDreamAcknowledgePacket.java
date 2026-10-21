package com.farcr.nomansland.common.networking.dream;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.dreams.dreamlevel.DreamLevelHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record ServerboundDreamAcknowledgePacket() implements CustomPacketPayload {
    public static final StreamCodec<ByteBuf, ServerboundDreamAcknowledgePacket> STREAM_CODEC = StreamCodec.unit(new ServerboundDreamAcknowledgePacket());

    public static final Type<ServerboundDreamAcknowledgePacket> TYPE = new Type<>(NoMansLand.location("client/dream_ack"));

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handleData(IPayloadContext context) {
        if (context.flow().isServerbound()) {
            context.enqueueWork(() -> {
                DreamLevelHandler.getInstance().playerHasReceivedPacket(context.player());
            });
        }
    }
}
