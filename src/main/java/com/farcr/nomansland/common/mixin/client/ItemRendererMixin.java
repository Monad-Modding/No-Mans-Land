package com.farcr.nomansland.common.mixin.client;

import com.farcr.nomansland.client.extensions.AncestralOathSwordClientExtensions;
import com.farcr.nomansland.client.model.utils.BakedModelOpacityWrapper;
import com.farcr.nomansland.client.renderer.rendertype.AncestralGlintRenderType;
import com.farcr.nomansland.client.renderer.rendertype.GlintConsumer;
import com.farcr.nomansland.client.renderer.rendertype.MoonlightGlowRenderType;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
    @Shadow
    public abstract void renderQuadList(PoseStack poseStack, VertexConsumer buffer, List<BakedQuad> quads, ItemStack itemStack, int combinedLight, int combinedOverlay);

    @Inject(method = "render(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/client/resources/model/BakedModel;)V", at = @At("HEAD"))
    private void setGlowItemContext(
        ItemStack itemStack, ItemDisplayContext displayContext,
        boolean leftHand, PoseStack poseStack,
        MultiBufferSource bufferSource,
        int combinedLight, int combinedOverlay,
        BakedModel model, CallbackInfo callbackInfo
    ) {
        MoonlightGlowRenderType.setContext(itemStack);
    }

    @Unique private static Map<BakedModel, BakedModelOpacityWrapper> MAP_WRAPPER = new HashMap<>();
    @Unique private BakedModelOpacityWrapper nml$BAKED_MODEL_OPACITY_CONTEXT;

    @Inject(
        method = "renderStatic(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/level/Level;III)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;render(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/client/resources/model/BakedModel;)V"
        )
    )
    private void nml$renderOathGlint(
        LivingEntity entity, ItemStack itemStack, ItemDisplayContext displayContext,
        boolean leftHand, PoseStack poseStack, MultiBufferSource bufferSource,
        Level level, int combinedLight, int combinedOverlay, int seed, CallbackInfo ci,
        @Local BakedModel bakedModel
    ) {
        VertexConsumer consumer = AncestralGlintRenderType.getConsumer(bufferSource, itemStack);
        if (consumer == null || entity == null) return;
        if (!(IClientItemExtensions.of(itemStack) instanceof AncestralOathSwordClientExtensions extensions)) return;

        MAP_WRAPPER.putIfAbsent(bakedModel, new BakedModelOpacityWrapper(bakedModel));
        nml$BAKED_MODEL_OPACITY_CONTEXT = MAP_WRAPPER.get(bakedModel);
        nml$BAKED_MODEL_OPACITY_CONTEXT.setAlphaValue(extensions.getGlintOpacity(itemStack, entity));
    }

    @WrapOperation(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;renderModelLists(Lnet/minecraft/client/resources/model/BakedModel;Lnet/minecraft/world/item/ItemStack;IILcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;)V"
        )
    )
    private void nml$renderOathGlint(
        ItemRenderer instance, BakedModel model, ItemStack stack,
        int combinedLight, int combinedOverlay, PoseStack poseStack,
        VertexConsumer buffer, Operation<Void> original,
        @Local(argsOnly = true) MultiBufferSource bufferSource
    ) {
        original.call(instance, model, stack, combinedLight, combinedOverlay, poseStack, buffer);

        if (nml$BAKED_MODEL_OPACITY_CONTEXT == null) return;
        VertexConsumer consumer = AncestralGlintRenderType.getConsumer(bufferSource, stack);
        if (consumer == null) return;

        original.call(instance, MAP_WRAPPER.get(model), stack, combinedLight, combinedOverlay, poseStack, consumer);
    }

    @Inject(
        method = "render",
        at = @At("TAIL")
    )
    private void nml$renderTail(
        ItemStack itemStack, ItemDisplayContext displayContext,
        boolean leftHand, PoseStack poseStack, MultiBufferSource bufferSource,
        int combinedLight, int combinedOverlay, BakedModel model, CallbackInfo ci
    ) {
        nml$BAKED_MODEL_OPACITY_CONTEXT = null;
    }

    @Inject(method = "getFoilBuffer", at = @At("RETURN"), cancellable = true)
    private static void glowBufferInject(
        MultiBufferSource bufferSource, RenderType renderType,
        boolean isItem, boolean glint, CallbackInfoReturnable<VertexConsumer> cir
    ) {
        cir.setReturnValue(GlintConsumer.consume(bufferSource, cir.getReturnValue()));
    }

    @Inject(method = "getFoilBufferDirect", at = @At("RETURN"), cancellable = true)
    private static void glowBufferInjectDirect(
        MultiBufferSource bufferSource, RenderType renderType,
        boolean isItem, boolean glint, CallbackInfoReturnable<VertexConsumer> cir
    ) {
        cir.setReturnValue(GlintConsumer.consume(bufferSource, cir.getReturnValue()));
    }
}
