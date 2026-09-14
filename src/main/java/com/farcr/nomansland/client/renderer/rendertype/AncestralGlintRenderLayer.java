package com.farcr.nomansland.client.renderer.rendertype;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.extension.EntityExtension;
import com.farcr.nomansland.common.registry.entities.NMLEffects;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL30;

/*
* Heavily based on:
* https://github.com/baguchi/EnchantWithMob-Forge/blob/1.21.1/src/main/java/baguchi/enchantwithmob/client/render/layer/EnchantLayer.java
*
* I love you enchantwithmob
*/
public class AncestralGlintRenderLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    public AncestralGlintRenderLayer(RenderLayerParent<T, M> renderer) {
        super(renderer);
    }

    @SuppressWarnings("unchecked")
    public static void addLayers(final EntityRenderersEvent.AddLayers event) {
        event.getContext().getEntityRenderDispatcher().getSkinMap().forEach((model, playerRenderer) -> {
            if (event.getSkin(model) != null && playerRenderer instanceof LivingEntityRenderer livingEntityRenderer)
                livingEntityRenderer.addLayer(new AncestralGlintRenderLayer(event.getSkin(model)));
        });
        event.getEntityTypes().forEach(entityType -> {
            if (event.getRenderer(entityType) instanceof LivingEntityRenderer r)
                r.addLayer(new AncestralGlintRenderLayer(r));
        });
    }

    @Override
    public void render(
        @NotNull PoseStack poseStack, @NotNull MultiBufferSource multiBufferSource,
        int packedLight, @NotNull T livingEntity, float limbSwing, float limbSwingAmount,
        float partialTicks, float ageInTicks, float headYaw, float headPitch
    ) {
        if (!livingEntity.hasEffect(NMLEffects.STASIS)) return;

        EntityModel<T> entityModel = this.getParentModel();
        entityModel.prepareMobModel(livingEntity, limbSwing, limbSwingAmount, partialTicks);
        this.getParentModel().copyPropertiesTo(entityModel);

        float intensity = 1f - ((EntityExtension) livingEntity).nml$getVisualTickMultiplier();
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(AncestralGlintRenderType.ancestralGlint(true, intensity));
        entityModel.setupAnim(livingEntity, limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch);
        entityModel.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY,
            FastColor.ARGB32.colorFromFloat(intensity, intensity, intensity, intensity));
    }
}
