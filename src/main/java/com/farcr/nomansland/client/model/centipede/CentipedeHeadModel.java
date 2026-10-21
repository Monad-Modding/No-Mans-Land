package com.farcr.nomansland.client.model.centipede;

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
public class CentipedeHeadModel {
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart leftFeeler;
    private final ModelPart rightFeeler;

    public CentipedeHeadModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.leftFeeler = this.head.getChild("left_feeler");
        this.rightFeeler = this.head.getChild("right_feeler");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(32, 11).addBox(-3.5F, -4.0F, -3.0F, 7, 4, 6),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        head.addOrReplaceChild("left_feeler",
                CubeListBuilder.create().texOffs(28, 25).addBox(-12.0F, 0.0F, -4.0F, 12, 0, 4),
                PartPose.offsetAndRotation(-1.5F, -1.0F, -3.0F, 0.0F, 0.3927F, 0.0F));
        head.addOrReplaceChild("right_feeler",
                CubeListBuilder.create().texOffs(28, 25).mirror().addBox(0.0F, 0.0F, -4.0F, 12, 0, 4).mirror(false),
                PartPose.offsetAndRotation(1.5F, -1.0F, -3.0F, 0.0F, -0.3927F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    public void setupHead(float ageInTicks, float netHeadYaw, float headPitch) {
        this.root.getAllParts().forEach(ModelPart::resetPose);
        this.head.yRot = Mth.clamp(netHeadYaw, -50.0F, 50.0F) * Mth.DEG_TO_RAD * 0.6F;
        this.head.xRot = Mth.clamp(headPitch, -40.0F, 40.0F) * Mth.DEG_TO_RAD * 0.5F;
        float wiggle = Mth.cos(ageInTicks * 0.3F) * 0.22F;
        this.leftFeeler.yRot = 0.3927F + wiggle;
        this.rightFeeler.yRot = -0.3927F - wiggle;
    }

    public void render(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay) {
        this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay);
    }
}
