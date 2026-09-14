package com.farcr.nomansland.client.model.armor;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

import java.util.Collections;

/**
 * A simple implementation of an armor tied humanoid model.
 * Designing these in blockbench should be as simple as possible:
 * Start with armor_boilerplate.bbmodel
 * Do not change the pivot points of any bone already present in the boilerplate.
 * Once you're done, Export the model as a java file and paste the geometry into {@link LodestoneArmorModel#createArmorModel}.
 * Remove the generated part definitions of the Root* ModelParts
 * Done
 */
public class LodestoneArmorModel extends HumanoidModel<LivingEntity> {
    public EquipmentSlot slot;
    public ModelPart root, head, body, leftArm, rightArm, leggings, leftLegging, rightLegging, leftFoot, rightFoot;

    public LodestoneArmorModel(ModelPart root) {
        super(root);
        this.root = root;
        this.head = getPart("head");
        this.body = getPart("body");
        this.leggings = getPart("leggings");
        this.leftArm = getPart("left_arm");
        this.rightArm = getPart("right_arm");
        this.leftLegging = getPart("left_legging");
        this.rightLegging = getPart("right_legging");
        this.leftFoot = getPart("left_foot");
        this.rightFoot = getPart("right_foot");
    }

    public ModelPart getPart(String name) {
        return getPart(root, name);
    }

    public static PartDefinition createHumanoidAlias(MeshDefinition mesh) {
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", new CubeListBuilder(), PartPose.ZERO);
        root.addOrReplaceChild("body", new CubeListBuilder(), PartPose.ZERO);
        root.addOrReplaceChild("right_arm", new CubeListBuilder(), PartPose.ZERO);
        root.addOrReplaceChild("left_arm", new CubeListBuilder(), PartPose.ZERO);
        root.addOrReplaceChild("leggings", new CubeListBuilder(), PartPose.ZERO);
        root.addOrReplaceChild("right_legging", new CubeListBuilder(), PartPose.ZERO);
        root.addOrReplaceChild("left_legging", new CubeListBuilder(), PartPose.ZERO);
        root.addOrReplaceChild("right_foot", new CubeListBuilder(), PartPose.ZERO);
        root.addOrReplaceChild("left_foot", new CubeListBuilder(), PartPose.ZERO);
        return root;
    }

    public static LayerDefinition createArmorModel(ILodestoneArmorModelBuilder modelBuilder) {
        MeshDefinition mesh = HumanoidModel.createMesh(new CubeDeformation(0), 0);
        PartDefinition root = createHumanoidAlias(mesh);
        PartDefinition head = root.getChild("head");
        PartDefinition body = root.getChild("body");
        PartDefinition right_arm = root.getChild("right_arm");
        PartDefinition left_arm = root.getChild("left_arm");
        PartDefinition leggings = root.getChild("leggings");
        PartDefinition right_legging = root.getChild("right_legging");
        PartDefinition left_legging = root.getChild("left_legging");
        PartDefinition right_foot = root.getChild("right_foot");
        PartDefinition left_foot = root.getChild("left_foot");
        return modelBuilder.createArmorLayer(mesh, root, head, body, right_arm, left_arm, leggings, right_legging, left_legging, right_foot, left_foot);
    }

    @Override
    protected Iterable<ModelPart> headParts() {
        return slot == EquipmentSlot.HEAD ? ImmutableList.of(head) : ImmutableList.of();
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        if (slot == EquipmentSlot.CHEST) {
            return ImmutableList.of(body, leftArm, rightArm);
        } else if (slot == EquipmentSlot.LEGS) {
            return ImmutableList.of(leftLegging, rightLegging, leggings);
        } else if (slot == EquipmentSlot.FEET) {
            return ImmutableList.of(leftFoot, rightFoot);
        } else return ImmutableList.of();
    }

    @Override
    public void renderToBuffer(PoseStack matrixStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        if (slot == EquipmentSlot.LEGS) {  //I don't know why this is needed, but it is.
            this.leggings.copyFrom(this.body);
            this.leftLegging.copyFrom(this.leftLeg);
            this.rightLegging.copyFrom(this.rightLeg);
        }
        super.renderToBuffer(matrixStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    public void copyFromDefault(HumanoidModel model) {
        leggings.copyFrom(model.body);
        body.copyFrom(model.body);
        head.copyFrom(model.head);
        leftArm.copyFrom(model.leftArm);
        rightArm.copyFrom(model.rightArm);
        leftLegging.copyFrom(leftLeg);
        rightLegging.copyFrom(rightLeg);
        leftFoot.copyFrom(leftLeg);
        rightFoot.copyFrom(rightLeg);
    }


    public static ModelPart getPart(ModelPart root, String name) {
        try {
            return root.getChild(name);
        } catch (Exception ignored) {
            return new ModelPart(Collections.emptyList(), Collections.emptyMap());
        }
    }

    public interface ILodestoneArmorModelBuilder {
        LayerDefinition createArmorLayer(MeshDefinition mesh, PartDefinition root, PartDefinition head, PartDefinition body, PartDefinition right_arm, PartDefinition left_arm, PartDefinition leggings, PartDefinition right_legging, PartDefinition left_legging, PartDefinition right_foot, PartDefinition left_foot);
    }
}