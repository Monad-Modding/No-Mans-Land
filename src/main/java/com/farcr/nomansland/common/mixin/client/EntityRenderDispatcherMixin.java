package com.farcr.nomansland.common.mixin.client;

import com.farcr.nomansland.client.renderer.context.StasisEntityContext;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin<T extends Entity> {

    @ModifyVariable(method = "render", at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private float nml$affectPartialTick(float partialTick, @Local(argsOnly = true) Entity entity) {
        return (partialTick * entity.nml$getVisualTickMultiplier());
    }

    @WrapOperation(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;render(Lnet/minecraft/world/entity/Entity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"
        )
    )
    private void nml$wrapRenderer(
        EntityRenderer instance, T entity,
        float entityYaw, float partialTick,
        PoseStack poseStack, MultiBufferSource bufferSource,
        int packedLight, Operation<Void> original
    ) {
        StasisEntityContext.setEntityContext(entity);
        original.call(instance, StasisEntityContext.getEntitySnapshot(entity), entityYaw, partialTick, poseStack, bufferSource, packedLight);
        StasisEntityContext.clearEntityContext();
    }
}
