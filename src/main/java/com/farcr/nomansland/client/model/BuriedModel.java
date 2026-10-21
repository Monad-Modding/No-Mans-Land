package com.farcr.nomansland.client.model;

import com.farcr.nomansland.common.entity.Buried;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class BuriedModel extends HierarchicalModel<Buried> {
    private static final float DEG_TO_RAD = (float) Math.PI / 180;

    private final ModelPart root;
    private final ModelPart head;

    public BuriedModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("buried").getChild("Body").getChild("Head");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition buried = root.addOrReplaceChild("buried",
                CubeListBuilder.create(),
                PartPose.offset(0, 21.72757F, 0.33068F));

        PartDefinition Body = buried.addOrReplaceChild("Body",
                CubeListBuilder.create()
                        .texOffs(0, 16).addBox(-4, -12.04149F, -2.04967F, 8, 12, 4),
                PartPose.offsetAndRotation(0, 0.27243F, 5.66932F, 1.48353F, 0, 0));

        Body.addOrReplaceChild("RightArm",
                CubeListBuilder.create()
                        .texOffs(2, 0).mirror().addBox(-1, 5, -1, 2, 0, 2, new CubeDeformation(0.01F)).mirror(false)
                        .texOffs(40, 16).addBox(-1, -1, -1, 2, 12, 2),
                PartPose.offsetAndRotation(5, -11.04149F, -0.04967F, -2.83616F, 0, 0.34907F));

        Body.addOrReplaceChild("LeftArm",
                CubeListBuilder.create()
                        .texOffs(40, 0).mirror().addBox(-1, -1, -1, 2, 12, 2).mirror(false)
                        .texOffs(-2, 0).mirror().addBox(-1, 5, -1, 2, 0, 2, new CubeDeformation(0.01F)).mirror(false),
                PartPose.offsetAndRotation(-5, -11.04149F, -0.04967F, -2.83616F, 0, -0.34907F));

        Body.addOrReplaceChild("Head",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4, -6, -8, 8, 8, 8),
                PartPose.offsetAndRotation(0, -12.04149F, -0.04967F, -1.48353F, 0, 0));

        buried.addOrReplaceChild("LeftLeg",
                CubeListBuilder.create()
                        .texOffs(32, 0).mirror().addBox(-1, -0.8087F, -1.91225F, 2, 12, 2).mirror(false)
                        .texOffs(-2, 2).mirror().addBox(-1, 5.1913F, -1.91225F, 2, 0, 2, new CubeDeformation(0.01F)).mirror(false),
                PartPose.offsetAndRotation(-2, -0.47757F, 6.37366F, 1.39626F, 0, 0));

        buried.addOrReplaceChild("RightLeg",
                CubeListBuilder.create()
                        .texOffs(32, 16).addBox(-1, -1, -1.96231F, 2, 12, 2)
                        .texOffs(2, 2).mirror().addBox(-1, 5, -1.96231F, 2, 0, 2, new CubeDeformation(0.01F)).mirror(false),
                PartPose.offsetAndRotation(2, -0.59074F, 6.66932F, 1.39626F, 0, 0));

        return LayerDefinition.create(mesh, 64, 32);
    }

    public static LayerDefinition createArmorLayer(CubeDeformation deformation) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition buried = root.addOrReplaceChild("buried",
                CubeListBuilder.create(),
                PartPose.offset(0, 21.72757F, 0.33068F));

        PartDefinition Body = buried.addOrReplaceChild("Body",
                CubeListBuilder.create(),
                PartPose.offsetAndRotation(0, 0.27243F, 5.66932F, 1.48353F, 0, 0));

        Body.addOrReplaceChild("BodyArmor",
                CubeListBuilder.create().texOffs(16, 16).addBox(-4, -12.04149F, -2.04967F, 8, 12, 4, deformation),
                PartPose.ZERO);

        Body.addOrReplaceChild("RightArm",
                CubeListBuilder.create().texOffs(40, 16).addBox(-2, -1, -2, 4, 12, 4, deformation),
                PartPose.offsetAndRotation(5, -11.04149F, -0.04967F, -2.83616F, 0, 0.34907F));

        Body.addOrReplaceChild("LeftArm",
                CubeListBuilder.create().texOffs(40, 16).mirror().addBox(-2, -1, -2, 4, 12, 4, deformation),
                PartPose.offsetAndRotation(-5, -11.04149F, -0.04967F, -2.83616F, 0, -0.34907F));

        Body.addOrReplaceChild("Head",
                CubeListBuilder.create().texOffs(0, 0).addBox(-4, -6, -8, 8, 8, 8, deformation),
                PartPose.offsetAndRotation(0, -12.04149F, -0.04967F, -1.48353F, 0, 0));

        buried.addOrReplaceChild("LeftLeg",
                CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2, -0.8087F, -2.91225F, 4, 12, 4, deformation),
                PartPose.offsetAndRotation(-2, -0.47757F, 6.37366F, 1.39626F, 0, 0));

        buried.addOrReplaceChild("RightLeg",
                CubeListBuilder.create().texOffs(0, 16).addBox(-2, -1, -2.96231F, 4, 12, 4, deformation),
                PartPose.offsetAndRotation(2, -0.59074F, 6.66932F, 1.39626F, 0, 0));

        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(Buried entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root.getAllParts().forEach(ModelPart::resetPose);

        this.animate(entity.emergeAnimationState, BuriedAnimation.EMERGE, ageInTicks);
        this.animate(entity.lungeStartAnimationState, BuriedAnimation.LUNGE_START, ageInTicks);
        this.animate(entity.lungeAirborneAnimationState, BuriedAnimation.LUNGE_AIRBORNE, ageInTicks);

        if (entity.getAnimState() == Buried.ANIM_NONE) {
            if (entity.isAggressive()) {
                this.animateWalk(BuriedAnimation.CHASING, limbSwing, limbSwingAmount, 2.0F, 2.5F);
            } else {
                this.animateWalk(BuriedAnimation.CRAWL, limbSwing, limbSwingAmount, 2.0F, 2.5F);
            }
        }

        this.head.xRot += headPitch * DEG_TO_RAD;
        this.head.zRot -= netHeadYaw * DEG_TO_RAD;
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}
