package com.farcr.nomansland.client.renderer.rendertype;

import com.farcr.nomansland.client.renderer.FriendMoonRenderer;
import com.farcr.nomansland.common.friend.condition.MoonlightOfferingConditions;
import com.farcr.nomansland.common.friend.dialogue.DialoguePool;
import com.farcr.nomansland.common.friend.dialogue.DialogueUtil;
import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.vertex.*;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;

public class MoonlightGlowRenderType {
    public static ShaderInstance MOONLIGHT_GLOW_SHADER;
    public static final RenderType MOONLIGHT_GLOW = RenderType.create("moonlight_glow",
        DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS, 1536, false, false,
        RenderType.CompositeState.builder()
            .setShaderState(new RenderStateShard.ShaderStateShard(
                () -> {
                    AbstractUniform elapsedTime = MOONLIGHT_GLOW_SHADER.safeGetUniform("ElapsedTime");
                    float totalTime = (float) ((double) Util.getMillis() * Minecraft.getInstance().options.glintSpeed().get() / 2.0);
                    elapsedTime.set(totalTime);

                    AbstractUniform glintAlpha = MOONLIGHT_GLOW_SHADER.safeGetUniform("GlintOpacity");
                    float alpha = FriendMoonRenderer.getInstance().getFriendMoonOpacity();
                    glintAlpha.set(alpha * (float) ((double) Minecraft.getInstance().options.glintStrength().get()));

                    MOONLIGHT_GLOW_SHADER.apply();
                    return MOONLIGHT_GLOW_SHADER;
                }
            ))
            .setTextureState(RenderStateShard.NO_TEXTURE)
            .setWriteMaskState(RenderStateShard.COLOR_WRITE)
            .setCullState(RenderStateShard.NO_CULL)
            .setDepthTestState(RenderStateShard.EQUAL_DEPTH_TEST)
            .setTransparencyState(RenderStateShard.GLINT_TRANSPARENCY)
            .createCompositeState(false)
    );

    public static ItemStack itemContext;
    public static void setContext(ItemStack newContext) {
        itemContext = newContext;
    }

    public static boolean itemCanBeOffered(Item item) {
        Minecraft instance = Minecraft.getInstance();
        if (instance.player != null && FriendMoonRenderer.getInstance().getFriendMoonOpacity() > 0) {
            if (instance.level != null) {
                ArrayList<DialoguePool> list = new ArrayList<>();
                DialogueUtil.appendTags(
                    item, instance.level.registryAccess(), Registries.ITEM,
                    MoonlightOfferingConditions.ItemOfferingConditional.COMPILED_MAP,
                    MoonlightOfferingConditions.ItemOfferingConditional.KEY_MAP,
                    list
                );
                return !list.isEmpty();
            }
        }
        return false;
    }

    public static boolean shouldRenderGlow() {
        if (MOONLIGHT_GLOW_SHADER == null) {
            itemContext = null;
            return false;
        }
        if (itemContext != null) {
            boolean validItem = (FriendMoonRenderer.getInstance().getFriendMoonOpacity() > 0)
                && itemCanBeOffered(itemContext.getItem());
            itemContext = null;
            return validItem;
        }
        return false;
    }

    public static void addGlint(Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder> map) {
        if (!map.containsKey(MOONLIGHT_GLOW))
            map.put(MOONLIGHT_GLOW, new ByteBufferBuilder(MOONLIGHT_GLOW.bufferSize()));
    }

    public static VertexConsumer getConsumer(
        MultiBufferSource bufferSource, VertexConsumer originalConsumer
    ) {
        if (shouldRenderGlow()) {
            return VertexMultiConsumer.create(
                bufferSource.getBuffer(MOONLIGHT_GLOW),
                originalConsumer
            );
        }
        return originalConsumer;
    }
}
