package com.farcr.nomansland.client;

import net.minecraft.world.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;

public final class TranslucentArmorVisibility {

    private static final int REVEAL_TICKS = 100;
    private static final int FADE_IN_TICKS = 5;
    private static final int FADE_OUT_TICKS = 10;

    private static final Map<Integer, Long> REVEAL_UNTIL = new HashMap<>();

    private TranslucentArmorVisibility() {
    }

    public static float getAlpha(LivingEntity entity) {
        long now = entity.level().getGameTime();

        if (entity.hurtTime > 0 && entity.hurtTime >= entity.hurtDuration) {
            REVEAL_UNTIL.put(entity.getId(), now + REVEAL_TICKS);
        }

        Long until = REVEAL_UNTIL.get(entity.getId());
        if (until == null) return 0.0F;
        if (now >= until) {
            REVEAL_UNTIL.remove(entity.getId());
            return 0.0F;
        }

        long remaining = until - now;
        long elapsed = REVEAL_TICKS - remaining;
        if (elapsed < FADE_IN_TICKS) return (float) elapsed / FADE_IN_TICKS;
        if (remaining < FADE_OUT_TICKS) return (float) remaining / FADE_OUT_TICKS;
        return 1.0F;
    }
}
