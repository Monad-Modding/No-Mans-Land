package com.farcr.nomansland.client.model.beetle;

import com.farcr.nomansland.common.entity.beetle.Grub;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GrubModel<T extends Grub> extends HierarchicalModel<T> {
    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart tail;

    public GrubModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.head = body.getChild("head");
        this.tail = body.getChild("tail");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, -3.0F, -3.5F, 5.0F, 3.0F, 7.0F),
                PartPose.offset(0.0F, 22.0F, 0.0F));

        body.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 10).addBox(-2.0F, -2.5F, -3.0F, 4.0F, 3.0F, 3.0F),
                PartPose.offset(0.0F, 0.0F, -3.5F));

        body.addOrReplaceChild("tail",
                CubeListBuilder.create().texOffs(0, 17).addBox(-1.5F, -2.0F, 0.0F, 3.0F, 2.0F, 3.0F),
                PartPose.offset(0.0F, -0.5F, 3.5F));

        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void setupAnim(T grub, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        root().getAllParts().forEach(ModelPart::resetPose);

        float crawl = Math.max(limbSwingAmount, 0.12F);
        body.yRot = Mth.cos(limbSwing * 0.6F) * 0.15F * crawl;
        body.y = 22.0F + Mth.cos(limbSwing * 1.2F) * 0.5F * crawl;

        head.yRot = Mth.cos(limbSwing * 0.6F + 1.0F) * 0.35F * crawl + netHeadYaw * ((float) Math.PI / 180F) * 0.3F;
        head.xRot = headPitch * ((float) Math.PI / 180F) * 0.3F;
        tail.yRot = Mth.cos(limbSwing * 0.6F - 1.0F) * 0.35F * crawl;
    }

    @Override
    public ModelPart root() {
        return root;
    }
}
