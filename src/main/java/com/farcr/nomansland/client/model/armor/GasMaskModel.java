package com.farcr.nomansland.client.model.armor;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;

public class GasMaskModel extends LodestoneArmorModel {
    public GasMaskModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        return createArmorModel((mesh, root, head, body, right_arm, left_arm, leggings, right_legging, left_legging, right_foot, left_foot) -> {
            head.addOrReplaceChild("gas_mask",
                    CubeListBuilder.create()
                            .texOffs(0, 0).addBox(-4.5F, -8.5F, -4.5F, 9.0F, 9.0F, 9.0F, new CubeDeformation(0.01F))
                            .texOffs(27, 0).addBox(-4.5F, -4.5F, -5.5F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.01F))
                            .texOffs(35, 0).addBox(1.5F, -5.5F, -5.5F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.01F))
                            .texOffs(27, 4).addBox(-4.5F, -4.5F, -5.0F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.01F))
                            .texOffs(35, 4).addBox(1.5F, -5.5F, -5.0F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.01F)),
                    PartPose.offset(0.0F, 0.0F, 0.0F));
            head.addOrReplaceChild("gas_mask_filter",
                    CubeListBuilder.create()
                            .texOffs(0, 18).addBox(-1.5F, 0.0F, -4.0F, 4.0F, 4.0F, 6.0F, new CubeDeformation(-0.01F)),
                    PartPose.offsetAndRotation(-0.5F, -1.5F, -4.5F, (float) Math.toRadians(30.0), 0.0F, 0.0F));
            return LayerDefinition.create(mesh, 64, 64);
        });
    }
}
