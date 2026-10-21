package com.farcr.nomansland.common.mixin.client;

import com.farcr.nomansland.client.renderer.rendertype.AncestralGlintRenderType;
import com.farcr.nomansland.client.renderer.rendertype.MoonlightGlowRenderType;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// https://github.com/VazkiiMods/Quark/blob/master/src/main/java/org/violetmoon/quark/mixin/mixins/client/RenderBuffersMixin.java#L22
@Mixin(RenderBuffers.class)
public class RenderBuffersMixin {

    @Inject(method = "lambda$new$0", at = @At("TAIL"))
    private static void nml$addGlintTypes(
        Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder> mapBuildersIn,
        RenderType type, CallbackInfo ci
    ) {
        MoonlightGlowRenderType.addGlint(mapBuildersIn);
        AncestralGlintRenderType.addGlint(mapBuildersIn);
    }
}