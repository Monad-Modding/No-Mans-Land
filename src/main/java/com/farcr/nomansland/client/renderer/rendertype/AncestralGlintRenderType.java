package com.farcr.nomansland.client.renderer.rendertype;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.extensions.AncestralOathSwordClientExtensions;
import com.farcr.nomansland.client.renderer.FriendMoonRenderer;
import com.farcr.nomansland.common.friend.condition.MoonlightOfferingConditions;
import com.farcr.nomansland.common.friend.dialogue.DialoguePool;
import com.farcr.nomansland.common.friend.dialogue.DialogueUtil;
import com.farcr.nomansland.common.registry.items.NMLItems;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class AncestralGlintRenderType {
    public static ShaderInstance ANCESTRAL_GLINT_SHADER;
    public static RenderType ancestralGlint(boolean entity, float opacity) {
        return RenderType.create(
            "ancestral_glint", DefaultVertexFormat.POSITION_TEX_COLOR,
            VertexFormat.Mode.QUADS, 1536, RenderType.CompositeState.builder()
                .setShaderState(new RenderStateShard.ShaderStateShard(() -> ANCESTRAL_GLINT_SHADER))
                .setTextureState(
                    new RenderStateShard.TextureStateShard(
                        NoMansLand.location("textures/misc/ancestral_glint.png"),
                        true, false
                    )
                )
                .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                .setCullState(RenderStateShard.NO_CULL)
                .setDepthTestState(RenderStateShard.EQUAL_DEPTH_TEST)
                .setTransparencyState(
                    new RenderStateShard.TransparencyStateShard("ancestral_glint_opacity", () -> {
                        RenderSystem.enableBlend();
                        /* for once i am happy this method exists as this makes this less error prone and deliberate
                        * since this context has a consistent value that it's set to there's no way to really "pollute" it
                        * like setting a color context would, for example, and so I dont need to track anything fancy I can just
                        * reset it to the setting value that already tracks what it should actually be which is decently convenient */
                        RenderSystem.setShaderGlintAlpha(opacity * Minecraft.getInstance().options.glintStrength().get());
                        // because i cant dynamically make a rendertype for an item glint
                        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
                    }, () -> {
                        RenderSystem.disableBlend();
                        RenderSystem.setShaderGlintAlpha(Minecraft.getInstance().options.glintStrength().get());
                        RenderSystem.defaultBlendFunc();
                    })
                )
                .setTexturingState(entity ? RenderStateShard.ENTITY_GLINT_TEXTURING : RenderStateShard.GLINT_TEXTURING)
                .createCompositeState(false)
        );
    }
    public static final RenderType DEFAULT_ANCESTRAL_GLINT = ancestralGlint(false, 1f);

    public static void addGlint(Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder> map) {
        if (!map.containsKey(DEFAULT_ANCESTRAL_GLINT))
            map.put(DEFAULT_ANCESTRAL_GLINT, new ByteBufferBuilder(DEFAULT_ANCESTRAL_GLINT.bufferSize()));
    }

    public static @Nullable VertexConsumer getConsumer(
        MultiBufferSource bufferSource, ItemStack itemContext
    ) {
        if (itemContext != null) {
            if (IClientItemExtensions.of(itemContext) instanceof AncestralOathSwordClientExtensions extensions)
                return bufferSource.getBuffer(DEFAULT_ANCESTRAL_GLINT);
        }
        return null;
    }
}
