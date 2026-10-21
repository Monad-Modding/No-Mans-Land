package com.farcr.nomansland.client.renderer.entity;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.NMLModelLayers;
import com.farcr.nomansland.client.model.beetle.BeetleModel;
import com.farcr.nomansland.common.entity.beetle.Beetle;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BeetleRenderer extends MobRenderer<Beetle, BeetleModel<Beetle>> {
    public BeetleRenderer(EntityRendererProvider.Context context) {
        super(context, new BeetleModel<>(context.bakeLayer(NMLModelLayers.BEETLE_LAYER)), 0.4F);
    }

    @Override
    public ResourceLocation getTextureLocation(Beetle beetle) {
        return NoMansLand.location("textures/entity/beetle/crown.png");
    }

    @Override
    protected void setupRotations(Beetle beetle, PoseStack poseStack, float bob, float yBodyRot, float partialTick, float scale) {
        super.setupRotations(beetle, poseStack, bob, yBodyRot, partialTick, scale);
        float flip = beetle.getFlipAmount(partialTick);
        if (flip > 0.0F) {
            poseStack.translate(0.0F, 0.35F * flip, 0.0F);
            poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F * flip));
        }
    }
}
