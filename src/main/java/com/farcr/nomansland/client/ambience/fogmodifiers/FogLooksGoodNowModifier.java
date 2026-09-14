package com.farcr.nomansland.client.ambience.fogmodifiers;

import com.farcr.nomansland.NMLConfig;
import com.farcr.nomansland.client.ambience.FogModifierHandler;
import net.minecraft.util.Mth;

public class FogLooksGoodNowModifier extends FogModifier {
    private static final float FULL_INTENSITY_START_MULTIPLIER = 0.15F;

    @Override
    public float getFogStartMultiplier() {
        return Mth.lerp(NMLConfig.SURFACE_FOG_INTENSITY.get().floatValue(), 1.0F, FULL_INTENSITY_START_MULTIPLIER);
    }

    @Override
    public boolean active(FogModifierHandler.FogContext context) {
        return NMLConfig.FOG_MODIFIERS.get()
            && NMLConfig.SURFACE_FOG_MODIFIER.get()
            && NMLConfig.SURFACE_FOG_INTENSITY.get() > 0.0;
    }
}
