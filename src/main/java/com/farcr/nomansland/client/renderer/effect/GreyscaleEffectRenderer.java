package com.farcr.nomansland.client.renderer.effect;

import com.farcr.nomansland.NoMansLand;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;

public class GreyscaleEffectRenderer {
    public static GreyscaleEffectRenderer INSTANCE = new GreyscaleEffectRenderer();
    public static GreyscaleEffectRenderer getInstance() { return INSTANCE; }

    public static ResourceLocation GREYSCALE_SHADER = NoMansLand.location("shaders/post/greyscale.json");
    public PostChain postChain;
    public void setupPostChain() throws IOException {
        Minecraft minecraft = Minecraft.getInstance();
        postChain = new PostChain(minecraft.getTextureManager(), minecraft.getResourceManager(),
            minecraft.getMainRenderTarget(), GREYSCALE_SHADER);
        postChain.resize(minecraft.getWindow().getWidth(), minecraft.getWindow().getHeight());
    }

    public void render(Minecraft minecraft, float partialTicks, float intensity) {
        if (postChain != null && intensity > 0.01f) {
            postChain.setUniform("DesaturateAmount", intensity);
            postChain.resize(minecraft.getWindow().getWidth(), minecraft.getWindow().getHeight());
            postChain.process(partialTicks);
        }
    }
}
