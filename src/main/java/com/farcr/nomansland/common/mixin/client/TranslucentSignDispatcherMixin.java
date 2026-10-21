package com.farcr.nomansland.common.mixin.client;

import com.farcr.nomansland.client.TranslucentSignFading;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntityRenderDispatcher.class)
public class TranslucentSignDispatcherMixin {

    @Inject(method = "render", at = @At("HEAD"))
    private <E extends BlockEntity> void nml$beginSignFade(E blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, CallbackInfo ci) {
        TranslucentSignFading.begin(blockEntity);
    }

    @Inject(method = "render", at = @At("RETURN"))
    private <E extends BlockEntity> void nml$endSignFade(E blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, CallbackInfo ci) {
        TranslucentSignFading.end();
    }
}
