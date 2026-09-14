package com.farcr.nomansland.common.networking.alchemist_tools;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.extension.LivingEntityExtension;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundOathSwordAnimate() implements CustomPacketPayload {
    public static final Type<ServerboundOathSwordAnimate> TYPE = new Type<>(NoMansLand.location("server/ancestral_oath_sword/animate"));
    public static final StreamCodec<ByteBuf, ServerboundOathSwordAnimate> STREAM_CODEC = StreamCodec.unit(new ServerboundOathSwordAnimate());

    @Override
    public Type<? extends CustomPacketPayload> type() {return TYPE;}

    public void handleData(IPayloadContext context) {
        context.enqueueWork(() -> {
            PacketDistributor.sendToPlayersTrackingEntity(context.player(),
                new ClientboundOathSwordAnimate(context.player().getId()));
        });
    }
}
