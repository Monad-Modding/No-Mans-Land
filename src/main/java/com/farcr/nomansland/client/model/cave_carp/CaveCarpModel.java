package com.farcr.nomansland.client.model.cave_carp;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CaveCarpModel<T extends Entity> extends HierarchicalModel<T> {

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart tail;
    private final ModelPart leftWhisker;
    private final ModelPart rightWhisker;

    public CaveCarpModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.tail = this.body.getChild("tail");
        this.leftWhisker = this.body.getChild("left_whisker");
        this.rightWhisker = this.body.getChild("right_whisker");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 2).addBox(-1.5F, -3.0F, -4.0F, 3, 6, 8)
                .texOffs(14, 5).addBox(-1.0F, 0.0F, -6.0F, 2, 3, 2), PartPose.offset(0, 17, 0));

        body.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(14, 0).addBox(-1.0F, -1.5F, 0.0F, 2, 3, 2)
                .texOffs(0, -2).addBox(0.0F, -2.5F, 2.0F, 0, 5, 4), PartPose.offset(0, -0.5F, 4));

        body.addOrReplaceChild("dorsal_fin", CubeListBuilder.create()
                .texOffs(0, -7).addBox(0.0F, -2.0F, -3.5F, 0, 2, 7), PartPose.offset(0, -3, 0.5F));

        body.addOrReplaceChild("left_fin", CubeListBuilder.create()
                .texOffs(0, 5).mirror().addBox(0.0F, 0.0F, -1.0F, 0, 2, 2).mirror(false),
                PartPose.offsetAndRotation(1.5F, 2, -2, 0, 0, -0.5236F));

        body.addOrReplaceChild("right_fin", CubeListBuilder.create()
                .texOffs(0, 5).addBox(0.0F, 0.0F, -1.0F, 0, 2, 2),
                PartPose.offsetAndRotation(-1.5F, 2, -2, 0, 0, 0.5236F));

        body.addOrReplaceChild("anal_fin", CubeListBuilder.create()
                .texOffs(0, 7).addBox(0.0F, 0.0F, -1.0F, 0, 1, 2), PartPose.offset(0, 3, 2));

        body.addOrReplaceChild("left_whisker", CubeListBuilder.create()
                .texOffs(4, 5).addBox(0.0F, 0.0F, -0.5F, 0, 3, 2), PartPose.offset(1, 3, -4.5F));

        body.addOrReplaceChild("right_whisker", CubeListBuilder.create()
                .texOffs(4, 5).addBox(0.0F, 0.0F, -0.5F, 0, 3, 2), PartPose.offset(-1, 3, -4.5F));

        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float speed = entity.isInWater() ? 1.0F : 1.5F;

        this.tail.yRot = -speed * 0.45F * Mth.sin(0.6F * ageInTicks);

        float whiskerSway = 0.12F * Mth.sin(0.15F * ageInTicks);
        this.leftWhisker.xRot = whiskerSway;
        this.rightWhisker.xRot = whiskerSway;
        this.leftWhisker.zRot = whiskerSway;
        this.rightWhisker.zRot = -whiskerSway;
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}
