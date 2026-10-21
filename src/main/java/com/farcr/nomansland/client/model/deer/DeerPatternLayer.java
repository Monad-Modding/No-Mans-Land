package com.farcr.nomansland.client.model.deer;

import com.farcr.nomansland.common.entity.cervidae.deer.Deer;
import com.farcr.nomansland.common.entity.variant_action.SetPatternLayer;
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
public class DeerPatternLayer extends RenderLayer<Deer, DeerModel<Deer>> {

    public DeerPatternLayer(RenderLayerParent<Deer, DeerModel<Deer>> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, Deer deer, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        SetPatternLayer setPatternLayer = VariantUtil.findAction(deer, SetPatternLayer.class);

        if (setPatternLayer != null) {
            int overlay = LivingEntityRenderer.getOverlayCoords(deer, 0.0F);
            getParentModel().prepareMobModel(deer, limbSwing, limbSwingAmount, partialTicks);
            VertexConsumer vertexconsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(deer.isBaby() ? setPatternLayer.babyTexture : setPatternLayer.texture));
            getParentModel().renderToBuffer(poseStack, vertexconsumer, packedLight, overlay);
        }
    }
}
