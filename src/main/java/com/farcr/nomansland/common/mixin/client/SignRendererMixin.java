package com.farcr.nomansland.common.mixin.client;

import com.farcr.nomansland.common.extension.TranslucentSign;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.mojang.blaze3d.vertex.PoseStack;

@Mixin(SignRenderer.class)
public class SignRendererMixin {

    @Unique
    private float nml$signAlpha = -1.0F;

    @Inject(method = "renderSignText", at = @At("HEAD"), cancellable = true)
    private void nml$computeSignAlpha(BlockPos pos, SignText text, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int lineHeight, int maxWidth, boolean isFrontText, CallbackInfo ci) {
        this.nml$signAlpha = -1.0F;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return;
        BlockEntity blockEntity = minecraft.level.getBlockEntity(pos);
        if (!(blockEntity instanceof TranslucentSign sign) || !sign.nml$isTranslucent()) return;

        Vec3 camera = minecraft.gameRenderer.getMainCamera().getPosition();
        double distance = Math.sqrt(camera.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
        float alpha = (float) Mth.clamp(1.0 - (distance - 1.0) / 4.0, 0.0, 1.0);
        if (alpha <= 0.05F) {
            ci.cancel();
            return;
        }
        this.nml$signAlpha = alpha;
    }

    @ModifyArg(
            method = "renderSignText",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;drawInBatch(Lnet/minecraft/util/FormattedCharSequence;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)I"),
            index = 3
    )
    private int nml$fadeText(int color) {
        return nml$applyAlpha(color);
    }

    @ModifyArg(
            method = "renderSignText",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;drawInBatch8xOutline(Lnet/minecraft/util/FormattedCharSequence;FFIILorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"),
            index = 3
    )
    private int nml$fadeOutlineText(int color) {
        return nml$applyAlpha(color);
    }

    @Unique
    private int nml$applyAlpha(int color) {
        if (this.nml$signAlpha < 0.0F) return color;
        int a = Mth.clamp((int) (this.nml$signAlpha * 255.0F), 16, 255);
        return (a << 24) | (color & 0x00FFFFFF);
    }
}
