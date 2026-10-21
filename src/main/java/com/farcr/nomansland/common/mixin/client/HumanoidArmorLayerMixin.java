package com.farcr.nomansland.common.mixin.client;

import com.farcr.nomansland.client.model.armor.GasMaskModel;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.farcr.nomansland.client.TranslucentArmorVisibility;
import com.farcr.nomansland.common.registry.items.NMLDataComponents;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidArmorLayer.class)
public abstract class HumanoidArmorLayerMixin {

    @Unique
    private float nml$translucentAlpha = -1.0F;

    @Inject(
            method = "renderArmorPiece(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/model/HumanoidModel;FFFFFF)V",
            at = @At("HEAD")
    )
    private void nml$setupTranslucentAlpha(PoseStack poseStack, MultiBufferSource bufferSource, LivingEntity entity, EquipmentSlot slot, int packedLight, HumanoidModel model, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        ItemStack stack = entity.getItemBySlot(slot);
        if (stack.has(NMLDataComponents.TRANSLUCENT.get())) {
            float alpha = TranslucentArmorVisibility.getAlpha(entity);
            this.nml$translucentAlpha = alpha < 0.99F ? alpha : -1.0F;
        } else {
            this.nml$translucentAlpha = -1.0F;
        }
    }

    @WrapOperation(
            method = "renderModel(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/model/Model;ILnet/minecraft/resources/ResourceLocation;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderType;armorCutoutNoCull(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;")
    )
    private RenderType nml$gasMaskTranslucent(ResourceLocation texture, Operation<RenderType> original, @Local(argsOnly = true) Model model) {
        if (model instanceof GasMaskModel) return RenderType.entityTranslucent(texture);
        return original.call(texture);
    }

    @Inject(
            method = "renderModel(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/model/Model;ILnet/minecraft/resources/ResourceLocation;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void nml$fadeTranslucentArmor(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, Model model, int color, ResourceLocation texture, CallbackInfo ci) {
        float alpha = this.nml$translucentAlpha;
        if (alpha < 0.0F) return;
        if (alpha <= 0.01F) {
            ci.cancel();
            return;
        }
        int a = (int) (alpha * 255.0F);
        int newColor = (a << 24) | (color & 0x00FFFFFF);
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(texture));
        model.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY, newColor);
        ci.cancel();
    }
}
