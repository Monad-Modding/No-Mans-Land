package com.farcr.nomansland.common.mixin.client;

import com.farcr.nomansland.client.handler.InvertedBellClientHandler;
import com.farcr.nomansland.utility.SmootherDouble;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalDoubleRef;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
    @Shadow
    @Final
    private Minecraft minecraft;
    @Unique
    private final SmootherDouble nml$dx = new SmootherDouble();
    @Unique
    private final SmootherDouble nml$dy = new SmootherDouble();

    @Inject(method = "turnPlayer", at = @At(value = "CONSTANT", args = "intValue=1", ordinal = 0))
    private void nml$useCustomBellSmoothing(double movementTime, CallbackInfo ci,
                                        @Local(name = "d0") LocalDoubleRef d0, @Local(name = "d1") LocalDoubleRef d1) {
        double intensity = InvertedBellClientHandler.instance.getIntensity(Minecraft.getInstance().getTimer().getRealtimeDeltaTicks());
        if (intensity > 0) {
            double control = Mth.clamp(1 - intensity * 1.5, 0.15, 1);
            this.nml$dx.setStrength(intensity);
            this.nml$dx.deltaTarget(d0.get() * control);
            double bonusDx = 0;
            if (InvertedBellClientHandler.instance.getState() == InvertedBellClientHandler.State.FADE_IN) {
                bonusDx = 400 * movementTime * Math.sin(intensity * 12) * easeInOutInOut(intensity);
            }
            d0.set(this.nml$dx.getUpdatedDelta(movementTime) + bonusDx);

            this.nml$dy.setStrength(intensity);
            this.nml$dy.deltaTarget(d1.get() * control
            );
            double bonusDy = 0;
            if (InvertedBellClientHandler.instance.getState() == InvertedBellClientHandler.State.FADE_IN) {
                bonusDy = 400 * movementTime * easeInOutInOut(intensity);
            }
            d1.set(this.nml$dy.getUpdatedDelta(movementTime) + bonusDy);
        } else {
            this.nml$dx.reset();
            this.nml$dy.reset();
        }
    }

    // idk. 0 at 0 and 1. 1 at 0.5. dy/dx is 0 at 0, 0.5, and 1.
    private static double easeInOutInOut(double x) {
        return x*x*(1-x)*(1-x) * 8;
    }
}
