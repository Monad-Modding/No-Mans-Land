package com.farcr.nomansland.common.mixin;

import com.farcr.nomansland.client.handler.CarvingClientHandler;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {

    @Unique
    private static final DataComponentMap nomansland$COMPONENT_MAP = DataComponentMap.builder()
            .set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(1))
            .build();

    @WrapOperation(method = "renderHandsWithItems", at = {
            @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/ItemInHandRenderer$HandRenderSelection;renderOffHand:Z", opcode = Opcodes.GETFIELD),
            @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/ItemInHandRenderer$HandRenderSelection;renderMainHand:Z", opcode = Opcodes.GETFIELD)
    })
    private boolean nomansland$renderOffHand(ItemInHandRenderer.HandRenderSelection instance, Operation<Boolean> original) {
        return original.call(instance) || CarvingClientHandler.instance.isChiseling();
    }

    @WrapOperation(method = "renderHandsWithItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderArmWithItem(Lnet/minecraft/client/player/AbstractClientPlayer;FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"))
    private void nomansland$renderHandsWithItems(ItemInHandRenderer instance, AbstractClientPlayer player, float partialTicks, float pitch, InteractionHand hand, float swingProgress, ItemStack stack, float equippedProgress, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, Operation<Void> original) {
        if(CarvingClientHandler.instance.isChiseling()) {
            InteractionHand chiselHand = CarvingClientHandler.instance.getHand();
            if(chiselHand != hand) {
                stack = player.getItemInHand(chiselHand).copy();
                stack.applyComponents(nomansland$COMPONENT_MAP);
            }
        }

        original.call(instance, player, partialTicks, pitch, hand, swingProgress, stack, equippedProgress, poseStack, buffer, combinedLight);
    }

}
