package com.farcr.nomansland.client.renderer.entity;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.NMLModelLayers;
import com.farcr.nomansland.client.model.remnant.RemnantModel;
import com.farcr.nomansland.common.entity.remnant.Remnant;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class RemnantRenderer extends MobRenderer<Remnant, RemnantModel<Remnant>> {

    private static final ResourceLocation TEXTURE = NoMansLand.location("textures/entity/remnant/remnant.png");

    public RemnantRenderer(EntityRendererProvider.Context context) {
        super(context, new RemnantModel<>(context.bakeLayer(NMLModelLayers.REMNANT_LAYER)), 0.55F);
    }

    @Override
    public void render(Remnant remnant, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        this.shadowRadius = remnant.isPhased() ? 0.0F : 0.55F;
        super.render(remnant, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(Remnant remnant) {
        return TEXTURE;
    }
}
