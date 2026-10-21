package com.farcr.nomansland.client.renderer.entity;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.NMLModelLayers;
import com.farcr.nomansland.client.model.clod.ClodModel;
import com.farcr.nomansland.common.entity.clod.Clod;
import com.farcr.nomansland.common.integration.LambDynLightsIntegration;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

public class ClodRenderer extends MobRenderer<Clod, ClodModel<Clod>> {

    private static final ResourceLocation TEXTURE = NoMansLand.location("textures/entity/clod/clod.png");
    private static final float MIN_CLOSE_OPACITY = 0.25F;

    public static ShaderInstance CLOD_SHADER;

    private static final RenderType CLOD_DITHER = RenderType.create(
            "nomansland:clod_dither",
            DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS,
            1536,
            true,
            false,
            RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(() -> CLOD_SHADER))
                    .setTextureState(new RenderStateShard.TextureStateShard(TEXTURE, false, false))
                    .setTransparencyState(RenderStateShard.NO_TRANSPARENCY)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setOverlayState(RenderStateShard.OVERLAY)
                    .createCompositeState(true)
    );

    private static final RenderType CLOD_FADE_FALLBACK = RenderType.create(
            "nomansland:clod_fade",
            DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS,
            1536,
            true,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(TEXTURE, false, false))
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setOverlayState(RenderStateShard.OVERLAY)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .createCompositeState(true)
    );

    public ClodRenderer(EntityRendererProvider.Context context) {
        super(context, new ClodModel<>(context.bakeLayer(NMLModelLayers.CLOD_LAYER)), 0.3F);
    }

    @Override
    public void render(Clod clod, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        float alpha = computeAlpha(clod);
        this.getModel().alpha = alpha;
        this.shadowStrength = alpha;
        super.render(clod, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private static final double CLOSE_RANGE = 2.0;

    private float computeAlpha(Clod clod) {
        float opacity = clod.getOpacity();
        if (opacity >= MIN_CLOSE_OPACITY) return Mth.clamp(opacity, 0.0F, 1.0F);

        Player player = Minecraft.getInstance().player;
        if (player == null) return opacity;

        double distSqr = clod.distanceToSqr(player);
        if (distSqr >= CLOSE_RANGE * CLOSE_RANGE) return opacity;

        int light = clod.level().getMaxLocalRawBrightness(clod.blockPosition());
        boolean dynamicReveal = LambDynLightsIntegration.isActive() && LambDynLightsIntegration.getDynamicLightLevel(clod.blockPosition()) > 0.0;
        boolean nightVision = player.hasEffect(MobEffects.NIGHT_VISION);
        if (light <= 0 && !dynamicReveal && !nightVision) return opacity;

        float proximity = 1.0F - (float) (Math.sqrt(distSqr) / CLOSE_RANGE);
        float boost = MIN_CLOSE_OPACITY * Mth.clamp(proximity, 0.0F, 1.0F);
        return Math.max(opacity, boost);
    }

    @Override
    protected RenderType getRenderType(Clod clod, boolean bodyVisible, boolean translucent, boolean glowing) {
        float alpha = this.getModel().alpha;
        if (alpha >= 0.99F) return RenderType.entityCutoutNoCull(TEXTURE);
        return CLOD_SHADER != null ? CLOD_DITHER : CLOD_FADE_FALLBACK;
    }

    @Override
    public ResourceLocation getTextureLocation(Clod clod) {
        return TEXTURE;
    }
}
