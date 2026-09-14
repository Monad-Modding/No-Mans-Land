package com.farcr.nomansland.common.mixin.client;

import com.farcr.nomansland.client.renderer.dreams.ClientDreamRenderer;
import com.farcr.nomansland.common.registry.entities.NMLEntityDataAttachments;
import com.farcr.nomansland.common.registry.items.NMLItems;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {

    @Inject(method = "itemUsed", at = @At("HEAD"), cancellable = true)
    private void nml$cancelItemUsed(InteractionHand hand, CallbackInfo ci) {
        assert Minecraft.getInstance().player != null;
        ItemStack itemStack = Minecraft.getInstance().player.getItemInHand(hand);
        if (itemStack.is(NMLItems.ANCESTRAL_OATH_SWORD)) ci.cancel();
    }

    @ModifyVariable(method = "renderHandsWithItems", at = @At("HEAD"), argsOnly = true)
    private float nml$modifyStasisPartialTick(float partialTick) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && player.hasData(NMLEntityDataAttachments.STASIS_TICK_MULTIPLIER))
            return partialTick * player.getData(NMLEntityDataAttachments.STASIS_TICK_MULTIPLIER);
        return partialTick;
    }

    @ModifyVariable(method = "renderArmWithItem", at = @At("HEAD"), argsOnly = true)
    private ItemStack nml$hideItemDuringDream(ItemStack stack) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && ClientDreamRenderer.getInstance().dreamShouldRender())
            return ItemStack.EMPTY;
        return stack;
    }
}
