package com.farcr.nomansland.common.effect;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.level.Level;

public class StasisEffect extends MobEffect {
    public static final int MAX_SPEED = 4;
    public StasisEffect(MobEffectCategory category, int color) { super(category, color); }
    public boolean immuneToDamage(DamageSource source, Level level) {
        return source.equals(level.damageSources().onFire());
    }
}
