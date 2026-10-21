package com.farcr.nomansland.client.model.beetle;

import com.farcr.nomansland.common.entity.beetle.Beetle;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BeetleModel<T extends Beetle> extends HierarchicalModel<T> {
    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart elytraRight;
    private final ModelPart elytraLeft;
    private final ModelPart legFrontRight;
    private final ModelPart legMiddleRight;
    private final ModelPart legBackRight;
    private final ModelPart legFrontLeft;
    private final ModelPart legMiddleLeft;
    private final ModelPart legBackLeft;

    public BeetleModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.head = body.getChild("head");
        this.elytraRight = body.getChild("elytra_right");
        this.elytraLeft = body.getChild("elytra_left");
        this.legFrontRight = body.getChild("leg_front_right");
        this.legMiddleRight = body.getChild("leg_middle_right");
        this.legBackRight = body.getChild("leg_back_right");
        this.legFrontLeft = body.getChild("leg_front_left");
        this.legMiddleLeft = body.getChild("leg_middle_left");
        this.legBackLeft = body.getChild("leg_back_left");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0).addBox(-4.5F, -7, -5, 9, 7, 10),
                PartPose.offset(0, 23, 0));

        body.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(46, 0).addBox(-2, -4, -3, 5, 4, 4)
                        .texOffs(38, 12).addBox(-2, -8, -3, 5, 4, 2),
                PartPose.offset(-0.5F, -1, -6));

        body.addOrReplaceChild("elytra_right",
                CubeListBuilder.create().texOffs(0, 17).mirror().addBox(-4.65F, -7, -5, 5, 6, 12, new CubeDeformation(0.1F)).mirror(false),
                PartPose.offset(0, 0, 0));

        body.addOrReplaceChild("elytra_left",
                CubeListBuilder.create().texOffs(0, 35).mirror().addBox(-0.35F, -7, -5, 5, 6, 12, new CubeDeformation(0.1F)).mirror(false),
                PartPose.offset(0, 0, 0));

        body.addOrReplaceChild("leg_front_right",
                CubeListBuilder.create().texOffs(38, 0).mirror().addBox(-1, 0, -1, 2, 2, 2, new CubeDeformation(-0.01F)).mirror(false),
                PartPose.offset(-3.5F, -1, -4));

        body.addOrReplaceChild("leg_middle_right",
                CubeListBuilder.create().texOffs(38, 4).mirror().addBox(-1, 0, -1, 2, 2, 2, new CubeDeformation(-0.01F)).mirror(false),
                PartPose.offset(-3.5F, -1, 0));

        body.addOrReplaceChild("leg_back_right",
                CubeListBuilder.create().texOffs(38, 8).mirror().addBox(-1, 0, -1, 2, 2, 2, new CubeDeformation(-0.01F)).mirror(false),
                PartPose.offset(-3.5F, -1, 4));

        body.addOrReplaceChild("leg_front_left",
                CubeListBuilder.create().texOffs(38, 0).addBox(-1, 0, -1, 2, 2, 2, new CubeDeformation(-0.01F)),
                PartPose.offset(3.5F, -1, -4));

        body.addOrReplaceChild("leg_middle_left",
                CubeListBuilder.create().texOffs(38, 4).addBox(-1, 0, -1, 2, 2, 2, new CubeDeformation(-0.01F)),
                PartPose.offset(3.5F, -1, 0));

        body.addOrReplaceChild("leg_back_left",
                CubeListBuilder.create().texOffs(38, 8).addBox(-1, 0, -1, 2, 2, 2, new CubeDeformation(-0.01F)),
                PartPose.offset(3.5F, -1, 4));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(T beetle, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        root().getAllParts().forEach(ModelPart::resetPose);

        head.xRot = headPitch * ((float) Math.PI / 180F) * 0.4F;
        head.yRot = netHeadYaw * ((float) Math.PI / 180F) * 0.4F;

        float openness = beetle.wingAmount;
        float buzz = Mth.cos(ageInTicks * 2.2F) * 0.35F * openness;
        elytraRight.zRot = -openness * 1.15F - buzz;
        elytraLeft.zRot = openness * 1.15F + buzz;
        elytraRight.yRot = -openness * 0.35F;
        elytraLeft.yRot = openness * 0.35F;
        elytraRight.xRot = -openness * 0.25F;
        elytraLeft.xRot = -openness * 0.25F;

        boolean flailing = beetle.isFlipped();
        float phaseA;
        float phaseB;
        if (flailing) {
            phaseA = Mth.cos(ageInTicks * 1.3F) * 0.9F;
            phaseB = Mth.cos(ageInTicks * 1.3F + 1.2F) * 0.9F;
        } else {
            float amount = Math.max(limbSwingAmount, openness * 0.4F);
            phaseA = Mth.cos(limbSwing * 0.9F) * 0.7F * amount;
            phaseB = Mth.cos(limbSwing * 0.9F + (float) Math.PI) * 0.7F * amount;
        }

        legFrontRight.xRot = phaseA;
        legBackRight.xRot = phaseA;
        legMiddleLeft.xRot = phaseA;
        legFrontLeft.xRot = phaseB;
        legBackLeft.xRot = phaseB;
        legMiddleRight.xRot = phaseB;
    }

    @Override
    public ModelPart root() {
        return root;
    }
}
