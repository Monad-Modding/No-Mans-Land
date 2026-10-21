package com.farcr.nomansland.client.model.centipede;

import com.farcr.nomansland.common.entity.centipede.Centipede;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CentipedeSegmentModel {
    public static final float LEG_SPLAY = 20.0F * Mth.DEG_TO_RAD;

    private final ModelPart root;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;

    public CentipedeSegmentModel(ModelPart root) {
        this.root = root;
        ModelPart body = root.getChild("body");
        this.leftLeg = body.getChild("left_leg");
        this.rightLeg = body.getChild("right_leg");
    }

    public static LayerDefinition createLayer(int bodyU, int bodyV, int legU, int legV) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(bodyU, bodyV).addBox(-4.5F, -5.0F, -3.0F, 9, 5, 6),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        body.addOrReplaceChild("left_leg",
                CubeListBuilder.create().texOffs(legU, legV).mirror().addBox(-8.0F, -1.0F, -1.0F, 8, 2, 2).mirror(false),
                PartPose.offsetAndRotation(-4.0F, -2.0F, 0.0F, 0.0F, 0.0F, -LEG_SPLAY));
        body.addOrReplaceChild("right_leg",
                CubeListBuilder.create().texOffs(legU, legV).addBox(0.0F, -1.0F, -1.0F, 8, 2, 2),
                PartPose.offsetAndRotation(4.0F, -2.0F, 0.0F, 0.0F, 0.0F, LEG_SPLAY));

        return LayerDefinition.create(mesh, 64, 64);
    }

    public void setupSegment(int index, float limbSwing, float limbSwingAmount) {
        this.root.getAllParts().forEach(ModelPart::resetPose);
        float phase = limbSwing * Centipede.GAIT_FREQ - index * Centipede.WAVE_LAMBDA;
        float step = Mth.cos(phase) * limbSwingAmount * 0.7F;
        float lift = Math.max(0.0F, -Mth.sin(phase)) * limbSwingAmount * 0.5F;
        this.leftLeg.yRot = step;
        this.rightLeg.yRot = -step;
        this.leftLeg.zRot = -LEG_SPLAY - lift;
        this.rightLeg.zRot = LEG_SPLAY + lift;
    }

    public void render(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay) {
        this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay);
    }
}
