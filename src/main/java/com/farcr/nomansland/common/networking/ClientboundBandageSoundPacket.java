package com.farcr.nomansland.common.networking;

import com.farcr.nomansland.NoMansLand;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClientboundBandageSoundPacket(int playerId, int targetId) implements CustomPacketPayload {
    public static final StreamCodec<ByteBuf, ClientboundBandageSoundPacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, ClientboundBandageSoundPacket::playerId,
        ByteBufCodecs.VAR_INT, ClientboundBandageSoundPacket::targetId,
        ClientboundBandageSoundPacket::new
    );

    public static final Type<ClientboundBandageSoundPacket> TYPE = new Type<>(NoMansLand.location("client/bandage_sound"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handleData(final IPayloadContext context) {
        if (context.flow().isClientbound()) {
            context.enqueueWork(() -> {
                com.farcr.nomansland.client.animation.BandageAnimations.setTarget(playerId(), targetId());
                Entity entity = context.player().level().getEntity(playerId());
                if (entity instanceof Player p) {
                    com.farcr.nomansland.client.sound.BandageSoundInstance.play(p);
                }
            });
        }
    }
}
