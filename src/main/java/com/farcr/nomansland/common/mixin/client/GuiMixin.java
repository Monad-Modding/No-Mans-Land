package com.farcr.nomansland.common.mixin.client;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.NMLMooseChargeAttackHandler;
import com.farcr.nomansland.client.renderer.DialogueRenderer;
import com.farcr.nomansland.client.renderer.dreams.ClientDreamRenderer;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.InBedChatScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.neoforged.neoforge.client.gui.GuiLayerManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(Gui.class)
public class GuiMixin {
    @SuppressWarnings("UnstableApiUsage")
    @Shadow @Final
    private GuiLayerManager layerManager;

    @Inject(method = "renderSleepOverlay", at = @At("HEAD"), cancellable = true)
    private void nml$renderSleepOverlay(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        ClientDreamRenderer clientRenderer = ClientDreamRenderer.getInstance();
        if (clientRenderer.clientIsDreaming()) {
            ClientDreamRenderer.renderOverlay(guiGraphics, deltaTracker);
            ci.cancel();
        }
    }

    @WrapOperation(method = "renderJumpMeter", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getJumpRidingScale()F"))
    private float nml$renderMooseAttackOverlay(LocalPlayer instance, Operation<Float> original, PlayerRideableJumping rideable) {
        Optional<Float> optional = NMLMooseChargeAttackHandler.replaceVanillaGuiValue(rideable);
        return optional.orElseGet(() -> original.call(instance));
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        ClientDreamRenderer clientRenderer = ClientDreamRenderer.getInstance();
        if (clientRenderer.getDream() != null && clientRenderer.dreamShouldRender() && clientRenderer.getDream().hideHUD()) {
            // manually render regardless !!!
            if (!(Minecraft.getInstance().screen instanceof InBedChatScreen))
                ClientDreamRenderer.renderOverlay(guiGraphics, deltaTracker);
            ci.cancel();
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    @Inject(method = "<init>", at = @At("TAIL"))
    private void nml$injectDialogueRenderer(Minecraft minecraft, CallbackInfo ci) {
        layerManager.add(NoMansLand.location("dialogue"), DialogueRenderer::render);
        layerManager.add(NoMansLand.location("dream_overlay"), ClientDreamRenderer::renderOverlay);
    }
}
