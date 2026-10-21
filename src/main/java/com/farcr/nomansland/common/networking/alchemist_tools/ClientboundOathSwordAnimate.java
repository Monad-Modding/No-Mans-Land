package com.farcr.nomansland.common.networking.alchemist_tools;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.extension.LivingEntityExtension;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClientboundOathSwordAnimate(
    int playerId
) implements CustomPacketPayload {
    public static final Type<ClientboundOathSwordAnimate> TYPE = new Type<>(NoMansLand.location("client/ancestral_oath_sword/animate"));
    public static final StreamCodec<ByteBuf, ClientboundOathSwordAnimate> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT, ClientboundOathSwordAnimate::playerId,
        ClientboundOathSwordAnimate::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {return TYPE;}

    public void handleData(IPayloadContext context) {
        context.enqueueWork( () -> {
            if (context.player().level().getEntity(playerId) instanceof LivingEntity livingEntity)
                ((LivingEntityExtension) livingEntity).nml$shakeArmAnimation();
        });
    }
}
