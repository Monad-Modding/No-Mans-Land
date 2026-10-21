package com.farcr.nomansland.client.renderer.effect;

import com.mojang.blaze3d.pipeline.TextureTarget;
import net.minecraft.client.Minecraft;

public class RenderTargetUnclear extends TextureTarget {
    public RenderTargetUnclear(int width, int height, boolean useDepth, boolean clearError) {
        super(width, height, useDepth, clearError);
    }

    public void resizeIfEligible(Minecraft minecraft) {
        if (width != minecraft.getWindow().getWidth()
        || height != minecraft.getWindow().getHeight()) {
            resize(
                minecraft.getWindow().getWidth(),
                minecraft.getWindow().getHeight(),
                false
            );
        }
    }

    @Override public void clear(boolean clearError) {}
    public void forceClear(boolean clearError) {
        super.clear(clearError);
    }
}
