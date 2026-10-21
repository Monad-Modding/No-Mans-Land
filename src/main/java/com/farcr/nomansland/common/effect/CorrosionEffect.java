package com.farcr.nomansland.common.effect;

import com.farcr.nomansland.common.registry.NMLDamageTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class CorrosionEffect extends MobEffect {
    public CorrosionEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean applyEffectTick(LivingEntity livingEntity, int amplifier) {
        Level level = livingEntity.level();
        float maxHealth = livingEntity.getMaxHealth();
        float damage = maxHealth * (0.10F + 0.025F * amplifier);
        float cap = 6.0F + amplifier;
        damage = Mth.clamp(damage, 1.0F, cap);
        livingEntity.hurt(NMLDamageTypes.getSimpleDamageSource(level, NMLDamageTypes.CORROSION), damage);
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 20 == 0;
    }
}
