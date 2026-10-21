package com.farcr.nomansland.common.mixin.client.integration;

import com.farcr.nomansland.client.renderer.FriendMoonRenderer;
import com.farcr.nomansland.common.mixin.plugin.annotation.IfModPresent;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.povstalec.stellarview.client.render.level.StellarViewOverworldEffects;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@OnlyIn(Dist.CLIENT)
@IfModPresent("stellarview")
@Mixin(StellarViewOverworldEffects.class)
public class StellarViewOverworldEffectsMixin {

    @Inject(method = "renderSky", at = @At("RETURN"))
    private void nml$renderFriendMoonOverStellarView(
        ClientLevel level, int ticks, float partialTick,
        Matrix4f modelViewMatrix, Camera camera, Matrix4f projectionMatrix,
        boolean isFoggy, Runnable setupFog, CallbackInfoReturnable<Boolean> cir
    ) {
        if (!cir.getReturnValueZ()) return;

        FriendMoonRenderer renderer = FriendMoonRenderer.getInstance();

        RenderSystem.enableBlend();
        RenderSystem.depthMask(false);

        renderer.renderFriendMoonFog(modelViewMatrix, projectionMatrix, new PoseStack());

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        renderer.renderFriendMoon(modelViewMatrix, projectionMatrix, Tesselator.getInstance(), new PoseStack(), partialTick);
        renderer.renderFriendShadow(modelViewMatrix, projectionMatrix, Tesselator.getInstance(), new PoseStack(), partialTick);

        FriendMoonRenderer.renderFinalize(modelViewMatrix, projectionMatrix, Tesselator.getInstance(), partialTick);

        RenderSystem.depthMask(true);
    }
}
