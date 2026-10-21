package com.farcr.nomansland.common.networking.alchemist_tools;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.extension.LivingEntityExtension;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClientboundOathSwordParry(
    int playerId, int arm
) implements CustomPacketPayload {
    public static final Type<ClientboundOathSwordParry> TYPE = new Type<>(NoMansLand.location("client/ancestral_oath_sword/parry"));
    public static final StreamCodec<ByteBuf, ClientboundOathSwordParry> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT, ClientboundOathSwordParry::playerId,
        ByteBufCodecs.INT, ClientboundOathSwordParry::arm,
        ClientboundOathSwordParry::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {return TYPE;}

    public void handleData(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getEntity(playerId) instanceof LivingEntity livingEntity)
                ((LivingEntityExtension) livingEntity).nml$parryArmAnimation(HumanoidArm.values()[arm]);
        });
    }
}
