package com.farcr.nomansland.common.block;

import com.farcr.nomansland.common.mixin.MobEffectInstanceAccessor;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DirectionalBlock;

public class ThermalVentBlock extends VentBlock {
    public static final MapCodec<ThermalVentBlock> CODEC = simpleCodec(ThermalVentBlock::new);
    private static final int EFFECT_DRAIN_PER_TICK = 3;

    public ThermalVentBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends DirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    public void affectEntity(Level level, BlockPos ventPos, Entity entity, int distance) {
        if (!(entity instanceof LivingEntity living) || !living.isInWater()) return;

        boolean sync = level.getGameTime() % 20 == 0;
        for (MobEffectInstance instance : living.getActiveEffects()) {
            if (instance.getEffect().value().getCategory() != MobEffectCategory.HARMFUL) continue;
            if (instance.isInfiniteDuration()) continue;
            ((MobEffectInstanceAccessor) instance).setDuration(Math.max(1, instance.getDuration() - EFFECT_DRAIN_PER_TICK));
            if (sync && living instanceof ServerPlayer player) {
                player.connection.send(new ClientboundUpdateMobEffectPacket(living.getId(), instance, false));
            }
        }
    }
}
