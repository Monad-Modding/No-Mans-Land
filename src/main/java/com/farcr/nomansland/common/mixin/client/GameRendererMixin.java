package com.farcr.nomansland.common.mixin.client;

import com.farcr.nomansland.client.extensions.AncestralOathSwordClientExtensions;
import com.farcr.nomansland.client.handler.InvertedBellClientHandler;
import com.farcr.nomansland.client.renderer.effect.AccumulateZoomRenderer;
import com.farcr.nomansland.client.renderer.effect.GreyscaleEffectRenderer;
import com.farcr.nomansland.common.registry.entities.NMLEntityDataAttachments;
import com.farcr.nomansland.common.registry.items.NMLItems;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Shadow @Final Minecraft minecraft;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getMainRenderTarget()Lcom/mojang/blaze3d/pipeline/RenderTarget;"))
    private void applyInvertedBellPost(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
        InvertedBellClientHandler.instance.render(this.minecraft, deltaTracker.getRealtimeDeltaTicks());
        AccumulateZoomRenderer.getInstance().render(this.minecraft, deltaTracker.getRealtimeDeltaTicks());
        if (this.minecraft.player != null) {
            if (this.minecraft.player.isHolding(NMLItems.ANCESTRAL_OATH_SWORD.get()))
                AncestralOathSwordClientExtensions.render(deltaTracker.getRealtimeDeltaTicks());
        }
        Entity cameraEntity = Minecraft.getInstance().cameraEntity;
        if (cameraEntity != null && cameraEntity.hasData(NMLEntityDataAttachments.STASIS_TICK_MULTIPLIER)) {
            GreyscaleEffectRenderer.getInstance().render(
                this.minecraft, deltaTracker.getRealtimeDeltaTicks(),
                1f - cameraEntity.getData(NMLEntityDataAttachments.STASIS_TICK_MULTIPLIER)
            );
        }
    }
}
