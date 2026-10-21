package com.farcr.nomansland.client.renderer.entity;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.NMLModelLayers;
import com.farcr.nomansland.client.model.cave_carp.CaveCarpModel;
import com.farcr.nomansland.common.entity.cave_carp.CaveCarp;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class CaveCarpRenderer extends MobRenderer<CaveCarp, CaveCarpModel<CaveCarp>> {

    private static final ResourceLocation TEXTURE = NoMansLand.location("textures/entity/cave_carp/cave_carp.png");

    public CaveCarpRenderer(EntityRendererProvider.Context context) {
        super(context, new CaveCarpModel<>(context.bakeLayer(NMLModelLayers.CAVE_CARP_LAYER)), 0.3F);
    }

    @Override
    protected void setupRotations(CaveCarp caveCarp, PoseStack poseStack, float bob, float yBodyRot, float partialTick, float scale) {
        super.setupRotations(caveCarp, poseStack, bob, yBodyRot, partialTick, scale);
        float f = 4.3F * Mth.sin(0.6F * bob);
        poseStack.mulPose(Axis.YP.rotationDegrees(f));
        if (!caveCarp.isInWater()) {
            poseStack.translate(0.1F, 0.1F, 0.0F);
            poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
        }
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(CaveCarp caveCarp) {
        return TEXTURE;
    }
}
