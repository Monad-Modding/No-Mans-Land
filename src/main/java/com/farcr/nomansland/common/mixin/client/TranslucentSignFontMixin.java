package com.farcr.nomansland.common.mixin.client;

import com.farcr.nomansland.client.TranslucentSignFading;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Font.class)
public class TranslucentSignFontMixin {

    @Inject(method = "drawInBatch(Lnet/minecraft/util/FormattedCharSequence;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)I", at = @At("HEAD"), cancellable = true)
    private void nml$hideSignText(FormattedCharSequence text, float x, float y, int color, boolean dropShadow, Matrix4f matrix, MultiBufferSource buffer, Font.DisplayMode mode, int backgroundColor, int packedLight, CallbackInfoReturnable<Integer> cir) {
        if (TranslucentSignFading.isHidden()) {
            cir.setReturnValue((int) x);
        }
    }

    @ModifyVariable(method = "drawInBatch(Lnet/minecraft/util/FormattedCharSequence;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)I", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private int nml$fadeSignText(int color) {
        return TranslucentSignFading.fade(color);
    }

    @Inject(method = "drawInBatch8xOutline", at = @At("HEAD"), cancellable = true)
    private void nml$hideSignOutline(FormattedCharSequence text, float x, float y, int color, int outlineColor, Matrix4f matrix, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        if (TranslucentSignFading.isHidden()) {
            ci.cancel();
        }
    }

    @ModifyVariable(method = "drawInBatch8xOutline", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private int nml$fadeSignOutlineText(int color) {
        return TranslucentSignFading.fade(color);
    }

    @ModifyVariable(method = "drawInBatch8xOutline", at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private int nml$fadeSignOutline(int outlineColor) {
        return TranslucentSignFading.fade(outlineColor);
    }
}
