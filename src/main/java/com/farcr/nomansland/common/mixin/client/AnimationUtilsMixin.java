package com.farcr.nomansland.common.mixin.client;

import com.farcr.nomansland.client.renderer.context.StasisEntityContext;
import com.farcr.nomansland.common.extension.EntityExtension;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(AnimationUtils.class)
public class AnimationUtilsMixin {

    @ModifyVariable(method = "bobModelPart", at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private static float nml$bobModelPart(float multiplier) {
        if (StasisEntityContext.getEntityContext() != null) {
            EntityExtension entityContext = ((EntityExtension) StasisEntityContext.getEntityContext());
            return (multiplier * entityContext.nml$getVisualTickMultiplier());
        }
        return multiplier;
    }
}
