package com.farcr.nomansland.client.renderer.entity;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.NMLModelLayers;
import com.farcr.nomansland.client.model.beetle.GrubModel;
import com.farcr.nomansland.common.entity.beetle.Grub;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GrubRenderer extends MobRenderer<Grub, GrubModel<Grub>> {
    public GrubRenderer(EntityRendererProvider.Context context) {
        super(context, new GrubModel<>(context.bakeLayer(NMLModelLayers.GRUB_LAYER)), 0.3F);
    }

    @Override
    public ResourceLocation getTextureLocation(Grub grub) {
        return NoMansLand.location("textures/entity/grub/true_grub.png");
    }

    @Override
    protected void scale(Grub grub, PoseStack poseStack, float partialTick) {
        if (grub.isBaby()) {
            poseStack.scale(0.6F, 0.6F, 0.6F);
        }
        super.scale(grub, poseStack, partialTick);
    }
}
