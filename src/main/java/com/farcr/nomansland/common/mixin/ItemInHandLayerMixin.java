package com.farcr.nomansland.common.mixin;

import com.farcr.nomansland.client.handler.CarvingClientHandler;
import com.farcr.nomansland.common.item.ChiselItem;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemInHandLayer.class)
public class ItemInHandLayerMixin {

    @Unique
    private static final DataComponentMap nomansland$COMPONENT_MAP = DataComponentMap.builder()
            .set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(1))
            .build();

    @WrapOperation(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getMainHandItem()Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack nomansland$getMainHand(LivingEntity instance, Operation<ItemStack> original) {
        ItemStack item = original.call(instance);
        if(Minecraft.getInstance().player == instance && CarvingClientHandler.instance.isChiseling() && !(item.getItem() instanceof ChiselItem)) {
            ItemStack copy = instance.getOffhandItem().copy();
            copy.applyComponents(nomansland$COMPONENT_MAP);
            return copy;
        }

        return item;
    }

    @WrapOperation(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getOffhandItem()Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack nomansland$getOffhandItem(LivingEntity instance, Operation<ItemStack> original) {
        ItemStack item = original.call(instance);
        if(Minecraft.getInstance().player == instance && CarvingClientHandler.instance.isChiseling() && !(item.getItem() instanceof ChiselItem)) {
            ItemStack copy = instance.getMainHandItem().copy();
            copy.applyComponents(nomansland$COMPONENT_MAP);
            return copy;
        }

        return item;
    }



}
