package com.farcr.nomansland.common.mixin.client;

import com.farcr.nomansland.client.renderer.dreams.ClientDreamRenderer;
import net.minecraft.client.CameraType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CameraType.class)
public class CameraTypeMixin {

    @Inject(method = "isFirstPerson", at = @At("RETURN"), cancellable = true)
    private void nml$isFirstPerson(CallbackInfoReturnable<Boolean> cir) {
        if (ClientDreamRenderer.getInstance().dreamShouldRender())
            cir.setReturnValue(true);
    }

    @Inject(method = "isMirrored", at = @At("RETURN"), cancellable = true)
    private void nml$isMirrored(CallbackInfoReturnable<Boolean> cir) {
        if (ClientDreamRenderer.getInstance().dreamShouldRender())
            cir.setReturnValue(false);
    }
}
