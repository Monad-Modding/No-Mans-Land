package com.farcr.nomansland.common.mixin.client;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.registry.entities.NMLEffects;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Gui.class)
public class CorrosionHeartMixin {
    @WrapOperation(
            method = "renderHeart",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui$HeartType;getSprite(ZZZ)Lnet/minecraft/resources/ResourceLocation;")
    )
    private ResourceLocation nml$corrodedHeart(Gui.HeartType heartType, boolean hardcore, boolean halfHeart, boolean blinking, Operation<ResourceLocation> original) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (heartType == Gui.HeartType.NORMAL && player != null && player.hasEffect(NMLEffects.CORROSION)) {
            String name = "corroded" + (hardcore ? "_hardcore" : "") + (halfHeart ? "_half" : "_full") + (blinking ? "_blinking" : "");
            return NoMansLand.location("hud/heart/" + name);
        }
        return original.call(heartType, hardcore, halfHeart, blinking);
    }
}
