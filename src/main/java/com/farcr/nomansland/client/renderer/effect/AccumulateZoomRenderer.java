package com.farcr.nomansland.client.renderer.effect;

import com.farcr.nomansland.NoMansLand;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;

public class AccumulateZoomRenderer {
    public static AccumulateZoomRenderer INSTANCE = new AccumulateZoomRenderer();
    public static AccumulateZoomRenderer getInstance() { return INSTANCE; }

    public static ResourceLocation ACCUMULATE_ZOOM_SHADER = NoMansLand.location("shaders/post/accumulate_zoom.json");
    public PostChain postChain;
    public void setupPostChain(RenderTarget renderTarget, String shader) throws IOException {
        Minecraft minecraft = Minecraft.getInstance();
        PostChain postChain = new PostChain(minecraft.getTextureManager(), minecraft.getResourceManager(), renderTarget, ACCUMULATE_ZOOM_SHADER);
        RenderTarget swapTarget = postChain.getTempTarget("swap");
        PostPass pass = postChain.addPass(shader, swapTarget, persistentTarget, false);
        pass.getEffect().setSampler("DiffuseSampler", swapTarget::getColorTextureId);
        pass.getEffect().setSampler("PreviousSampler", persistentTarget::getColorTextureId);

        postChain.addPass("blit", persistentTarget, renderTarget, false);
        postChain.resize(minecraft.getWindow().getWidth(), minecraft.getWindow().getHeight());
        this.postChain = postChain;
    }

    public final RenderTargetUnclear persistentTarget = new RenderTargetUnclear(100, 100, false, false);

    public float zoomOut = 0.985f;
    public float fadeOut = 0.7f;

    public float maxTicks = 0.0f;
    public float ticks = 0.0f;
    public void setEffectForTicks(int ticks) {
        this.maxTicks = ticks;
        this.ticks = ticks;
    }

    public void renderConstant(Minecraft minecraft, float partialTicks, float tempZoom, float tempFade) {
        if (postChain != null && !minecraft.isPaused()) {
            persistentTarget.resizeIfEligible(minecraft);
            postChain.setUniform("zoomOut", tempZoom);
            postChain.setUniform("fadeOut", Math.clamp(tempFade, 0.0f, 1.0f));
            postChain.resize(minecraft.getWindow().getWidth(), minecraft.getWindow().getHeight());
            postChain.process(partialTicks);
        }
    }

    public void render(Minecraft minecraft, float partialTicks) {
        ticks = Math.max(0, ticks - partialTicks);
        float tempZoom = zoomOut * (ticks / maxTicks);
        float tempFade = fadeOut * (ticks / maxTicks);
        if (tempZoom > 0.0f || tempFade > 0.0f) renderConstant(minecraft, partialTicks, tempZoom, tempFade);
    }
}
