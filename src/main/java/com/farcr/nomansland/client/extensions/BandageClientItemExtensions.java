package com.farcr.nomansland.client.extensions;

import com.farcr.nomansland.client.animation.BandageAnimations;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

public class BandageClientItemExtensions implements IClientItemExtensions {

    @Override
    public boolean applyForgeHandTransform(@NotNull PoseStack poseStack, LocalPlayer player, @NotNull HumanoidArm arm, @NotNull ItemStack itemInHand, float partialTick, float equipProcess, float swingProcess) {
        if (!player.isUsingItem() || player.getUseItemRemainingTicks() <= 0) return false;
        if (BandageAnimations.isHealingOther(player)) {
            BandageAnimations.renderFirstPersonOther(poseStack, player, arm, itemInHand, partialTick);
        } else {
            BandageAnimations.renderFirstPersonSelf(poseStack, player, arm, itemInHand, partialTick);
        }
        return true;
    }
}
