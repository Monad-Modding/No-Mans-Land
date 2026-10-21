package com.farcr.nomansland.common.mixin.client;

import com.farcr.nomansland.common.item.BandageHud;
import com.farcr.nomansland.common.mixin.plugin.annotation.IfModPresent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import squeek.appleskin.client.HUDOverlayHandler;

@IfModPresent("appleskin")
@Mixin(HUDOverlayHandler.HealthOverlay.class)
public class AppleSkinHealthMixin {

    @Inject(method = "shouldRenderOverlay", at = @At("RETURN"), cancellable = true)
    private void nml$bandageShouldRender(Minecraft minecraft, Player player, GuiGraphics guiGraphics, int guiTicks, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() && BandageHud.shouldShow(player)) cir.setReturnValue(true);
    }
}
