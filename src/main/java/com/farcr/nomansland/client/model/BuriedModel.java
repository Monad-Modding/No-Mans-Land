package com.farcr.nomansland.client.model;// Made with Blockbench 4.9.3, Made by Farcr

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.Entity;

public class BuriedModel<T extends Entity> extends EntityModel<T> {
    private final ModelPart buried;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart legs;
    private final ModelPart arms;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;
    private final ModelPart rightArm;
    private final ModelPart leftArm;

    public BuriedModel(ModelPart root) {
        this.buried = root.getChild("buried");
        this.head = buried.getChild("Head");
        this.body = buried.getChild("Body");
        this.legs = buried.getChild("Body").getChild("Legs");
        this.arms = buried.getChild("Body").getChild("Arms");
        this.leftLeg = buried.getChild("Body").getChild("Legs").getChild("LeftLeg");
        this.rightLeg = buried.getChild("Body").getChild("Legs").getChild("RightLeg");
        this.rightArm = buried.getChild("Body").getChild("Arms").getChild("RightArm");
        this.leftArm = buried.getChild("Body").getChild("Arms").getChild("LeftArm");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition buried = partdefinition.addOrReplaceChild("buried", CubeListBuilder.create(), PartPose.offset(0.0F, 34.0F, -6.0F));

        PartDefinition Head = buried.addOrReplaceChild("Head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -6.0F, -8.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -13.0F, 0.0F, 0.0436F, 0.0F, -0.1309F));

        PartDefinition Body = buried.addOrReplaceChild("Body", CubeListBuilder.create().texOffs(0, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -13.0F, 0.0F, 1.4835F, 0.0F, 0.0F));

        PartDefinition Legs = Body.addOrReplaceChild("Legs", CubeListBuilder.create(), PartPose.offset(4.0F, 1.0F, 0.0F));

        PartDefinition LeftLeg = Legs.addOrReplaceChild("LeftLeg", CubeListBuilder.create().texOffs(32, 0).mirror().addBox(-1.0F, 0.0F, -1.0F, 2.0F, 12.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false).texOffs(-2, 2).mirror().addBox(-1.0F, 6.0F, -1.0F, 2.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-2.0F, 11.0F, 0.0F, -0.0444F, -0.0852F, -0.112F));

        PartDefinition RightLeg = Legs.addOrReplaceChild("RightLeg", CubeListBuilder.create().texOffs(32, 16).addBox(-1.0F, -1.0F, -2.0F, 2.0F, 12.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(2, 2).mirror().addBox(-1.0F, 5.0F, -2.0F, 2.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-6.0F, 12.0F, 1.0F, 0.0F, 0.0F, 0.2182F));

        PartDefinition Arms = Body.addOrReplaceChild("Arms", CubeListBuilder.create(), PartPose.offset(-2.0F, 13.0F, 1.0F));

        PartDefinition LeftArm = Arms.addOrReplaceChild("LeftArm", CubeListBuilder.create().texOffs(40, 0).mirror().addBox(-1.0F, -1.0F, -1.0F, 2.0F, 12.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false).texOffs(-2, 0).mirror().addBox(-1.0F, 5.0F, -1.0F, 2.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(7.0F, -12.0F, -1.0F, -2.7213F, -0.0735F, 0.4305F));

        PartDefinition RightArm = Arms.addOrReplaceChild("RightArm", CubeListBuilder.create().texOffs(2, 0).mirror().addBox(-1.0F, 5.0F, -1.0F, 2.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false).texOffs(40, 16).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 12.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -12.0F, -1.0F, -2.7242F, 0.4157F, -0.1073F));

        return LayerDefinition.create(meshdefinition, 64, 32);
    }


    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

        if (head.yRot != 0) {
            body.yRot = (head.yRot / 10);
            body.zRot = (head.yRot / 10);
            legs.xRot = (-head.yRot / 8);
            arms.xRot = (head.yRot / 8);
        }

        if (head != null) {
            head.xRot = (headPitch * ((float) Math.PI / 180F));
            head.yRot = (netHeadYaw * ((float) Math.PI / 180F));
        }

    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        buried.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}