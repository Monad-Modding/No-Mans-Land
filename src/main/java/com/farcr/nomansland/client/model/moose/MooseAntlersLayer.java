package com.farcr.nomansland.client.model.moose;

import com.farcr.nomansland.common.entity.cervidae.moose.Moose;
import com.farcr.nomansland.common.entity.variant_action.SetAntlerLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.tazer.mixed_litter.VariantUtil;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MooseAntlersLayer extends RenderLayer<Moose, MooseModel<Moose>> {

    public MooseAntlersLayer(RenderLayerParent<Moose, MooseModel<Moose>> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, Moose moose, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (moose.hasAntlers()) {
            SetAntlerLayer setAntlerLayer = VariantUtil.findAction(moose, SetAntlerLayer.class);

            if (setAntlerLayer != null) {
                int overlay = LivingEntityRenderer.getOverlayCoords(moose, 0.0F);
                getParentModel().prepareMobModel(moose, limbSwing, limbSwingAmount, partialTicks);
                VertexConsumer vertexconsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(setAntlerLayer.texture));
                getParentModel().renderToBuffer(poseStack, vertexconsumer, packedLight, overlay);
            }
        }
    }
}