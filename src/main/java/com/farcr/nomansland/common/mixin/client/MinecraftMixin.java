package com.farcr.nomansland.common.mixin.client;

import com.farcr.nomansland.client.handler.InvertedBellClientHandler;
import com.farcr.nomansland.client.renderer.dreams.ClientDreamRenderer;
import com.farcr.nomansland.common.extension.LivingEntityExtension;
import com.farcr.nomansland.common.extension.SoundInstanceExtension;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.farcr.nomansland.common.item.AncestralOathSwordItem;
import com.farcr.nomansland.common.networking.alchemist_tools.ServerboundOathSwordAnimate;
import com.farcr.nomansland.common.registry.entities.NMLEntityDataAttachments;
import com.farcr.nomansland.common.registry.items.NMLItems;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import net.minecraft.client.sounds.SoundManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;


@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow
    @Final
    private SoundManager soundManager;

    @Shadow
    public abstract DeltaTracker getTimer();

    @Shadow
    @Nullable
    public LocalPlayer player;

    @Shadow
    @Nullable
    public HitResult hitResult;

    @Unique private boolean nml$onStasisCancel() {
        return (this.player != null && this.player.getExistingData(NMLEntityDataAttachments.STASIS_TICK_MULTIPLIER).orElse(1f) <= 0.01f);
    }

    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void nml$cancelAttackStasis(CallbackInfoReturnable<Boolean> cir) {
        if (this.nml$onStasisCancel()) cir.setReturnValue(false);
    }

    @Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
    private void nml$cancelContinueAttackStasis(boolean leftClick, CallbackInfo ci) {
        if (this.nml$onStasisCancel()) ci.cancel();
    }

    @Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
    private void nml$cancelUseItem(CallbackInfo ci) {
        if (this.nml$onStasisCancel()) ci.cancel();
    }

    @WrapOperation(
        method = "startAttack",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;attack(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;)V"
        )
    )
    private void nml$redirectAttackIfOathSword(
        MultiPlayerGameMode instance, Player player,
        Entity targetEntity, Operation<Void> original
    ) {
        if (player.getMainHandItem().is(NMLItems.ANCESTRAL_OATH_SWORD)
        && !AncestralOathSwordItem.canHurtUnderOath(targetEntity)) {
            // just send the info manually its easier that way
            instance.ensureHasSentCarriedItem();
            instance.connection.send(ServerboundInteractPacket.createAttackPacket(targetEntity, player.isShiftKeyDown()));
            return;
        }
        original.call(instance, player, targetEntity);
    }

    @WrapOperation(
        method = "startAttack",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/player/LocalPlayer;swing(Lnet/minecraft/world/InteractionHand;)V"
        )
    )
    private void nml$wrapSwing(LocalPlayer player, InteractionHand hand, Operation<Void> original) {
        if (player.getMainHandItem().is(NMLItems.ANCESTRAL_OATH_SWORD)
        && this.hitResult instanceof EntityHitResult entityHitResult
        && !AncestralOathSwordItem.canHurtUnderOath(entityHitResult.getEntity())) {
            ((LivingEntityExtension) player).nml$shakeArmAnimation();
            PacketDistributor.sendToServer(new ServerboundOathSwordAnimate());
            return;
        }
        original.call(player, hand);
    }

    @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sounds/SoundManager;updateSource(Lnet/minecraft/client/Camera;)V"))
    private void deafenBell(boolean renderLevel, CallbackInfo ci) {
        float intensity = InvertedBellClientHandler.instance.getIntensity(this.getTimer().getRealtimeDeltaTicks());
        SoundEngineAccessor accessor = (SoundEngineAccessor) this.soundManager.soundEngine;
        if (intensity > 0) {
            accessor.getInstanceToChannel().forEach((instance, channel) -> {
                if (!((SoundInstanceExtension) instance).nml$getBypassDeafening()) {
                    float f = accessor.invokeCalculateVolume(instance) * (1 - intensity) * (1 - intensity);
                    channel.execute(sound -> {
                        sound.setVolume(f);
                    });
                }
            });
        }
    }

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void nml$OverrideSetScreenDream(Screen guiScreen, CallbackInfo ci) {
        ClientDreamRenderer renderer = ClientDreamRenderer.getInstance();
        if (renderer.clientIsDreaming() && renderer.dreamShouldRender()
        && ClientDreamRenderer.isBlacklistedScreen(guiScreen))
            ci.cancel();
    }

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void nml$skipBellDimensionScreen(Screen guiScreen, CallbackInfo ci) {
        if (InvertedBellClientHandler.instance.isActive()
                && (guiScreen instanceof ReceivingLevelScreen || guiScreen instanceof ProgressScreen)) {
            ci.cancel();
        }
    }

    @WrapOperation(method = "updateScreenAndTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sounds/SoundManager;stop()V"))
    private void nml$keepSoundsThroughTransitions(SoundManager soundManager, Operation<Void> original) {
        if (InvertedBellClientHandler.instance.isActive()) return;
        ClientDreamRenderer renderer = ClientDreamRenderer.getInstance();
        if (renderer.clientIsDreaming()) return;
        original.call(soundManager);
    }
}
