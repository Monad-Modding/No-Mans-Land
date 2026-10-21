package com.farcr.nomansland.client.model.clod;

import com.farcr.nomansland.common.entity.clod.Clod;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

public class ClodModel<T extends Clod> extends HierarchicalModel<T> {

    public float alpha = 1.0F;
    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public ClodModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition parts = mesh.getRoot();

        PartDefinition body = parts.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.5F, -7.125F, -4.375F, 9, 7, 9)
                        .texOffs(0, 20).mirror().addBox(3.5F, -4.125F, -5.375F, 2, 3, 2).mirror(false)
                        .texOffs(8, 20).addBox(-5.5F, -4.125F, -5.375F, 2, 3, 2)
                        .texOffs(0, 16).addBox(-3.5F, -0.125F, -4.375F, 7, 2, 2)
                        .texOffs(27, 0).addBox(-3.5F, -0.125F, -0.875F, 7, 3, 5)
                        .texOffs(0, 32).addBox(-4.5F, -7.125F, -4.375F, 9, 14, 9, new CubeDeformation(0.25F)),
                PartPose.offset(0.0F, 15.125F, -0.125F));

        body.addOrReplaceChild("mouth", CubeListBuilder.create()
                        .texOffs(36, 8).addBox(-1.5F, 0.0F, 0.0F, 3, 4, 2),
                PartPose.offsetAndRotation(0.0F, -6.125F, -4.375F, -0.4363F, 0.0F, 0.0F));

        parts.addOrReplaceChild("right_leg", CubeListBuilder.create()
                        .texOffs(0, 25).addBox(-1.0F, 0.0F, -0.75F, 2, 5, 2)
                        .texOffs(18, 16).addBox(-1.5F, 5.0F, -1.75F, 3, 1, 3),
                PartPose.offset(2.5F, 18.0F, 1.25F));

        parts.addOrReplaceChild("left_leg", CubeListBuilder.create()
                        .texOffs(0, 25).addBox(-1.0F, 0.0F, -0.75F, 2, 5, 2)
                        .texOffs(18, 16).addBox(-1.5F, 5.0F, -1.75F, 3, 1, 3),
                PartPose.offset(-2.5F, 18.0F, 1.25F));

        parts.addOrReplaceChild("left_fin", CubeListBuilder.create()
                        .texOffs(51, 0).addBox(0.0F, 0.0F, -2.5F, 0, 4, 5),
                PartPose.offsetAndRotation(-3.5F, 16.0F, 1.5F, 0.0F, 0.0F, -0.4363F));

        parts.addOrReplaceChild("right_fin", CubeListBuilder.create()
                        .texOffs(51, 0).mirror().addBox(0.0F, 0.0F, -2.5F, 0, 4, 5).mirror(false),
                PartPose.offsetAndRotation(3.5F, 16.0F, 1.5F, 0.0F, 0.0F, 0.4363F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(T clod, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root.getAllParts().forEach(ModelPart::resetPose);

        this.body.yRot = netHeadYaw * ((float) Math.PI / 180F) * 0.3F;
        this.body.xRot = headPitch * ((float) Math.PI / 180F) * 0.2F;

        float swing = Mth.cos(limbSwing * 0.9F) * 1.4F * limbSwingAmount;
        this.rightLeg.xRot = swing;
        this.leftLeg.xRot = Mth.cos(limbSwing * 0.9F + (float) Math.PI) * 1.4F * limbSwingAmount;

        this.body.y = 15.125F + Mth.cos(limbSwing * 0.9F) * limbSwingAmount * 0.6F;

        if (clod.isBaby()) {
            this.body.x += (clod.getRandom().nextFloat() - 0.5F) * 0.12F;
            this.body.z += (clod.getRandom().nextFloat() - 0.5F) * 0.12F;
            this.body.zRot += (clod.getRandom().nextFloat() - 0.5F) * 0.1F;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        int newAlpha = (int) (Mth.clamp(this.alpha, 0.0F, 1.0F) * 255.0F);
        color = FastColor.ARGB32.color(newAlpha, FastColor.ARGB32.red(color), FastColor.ARGB32.green(color), FastColor.ARGB32.blue(color));
        super.renderToBuffer(poseStack, buffer, packedLight, packedOverlay, color);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}
