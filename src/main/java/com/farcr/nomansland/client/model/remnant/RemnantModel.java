package com.farcr.nomansland.client.model.remnant;

import com.farcr.nomansland.common.entity.remnant.Remnant;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public class RemnantModel<T extends Remnant> extends HierarchicalModel<T> {

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public RemnantModel(ModelPart root) {
        this.root = root.getChild("root");
        this.head = this.root.getChild("head");
        this.body = this.root.getChild("body");
        this.rightArm = this.root.getChild("right_arm");
        this.leftArm = this.root.getChild("left_arm");
        this.rightLeg = this.root.getChild("right_leg");
        this.leftLeg = this.root.getChild("left_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition parts = mesh.getRoot();

        PartDefinition root = parts.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        root.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8),
                PartPose.offset(0.0F, -29.0F, 0.0F));

        root.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(0, 16).addBox(-4.0F, -5.0F, -2.0F, 8, 14, 4)
                        .texOffs(0, 34).addBox(-4.0F, 9.0F, -2.0F, 8, 14, 4),
                PartPose.offset(0.0F, -24.0F, 0.0F));

        root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                        .texOffs(32, 18).addBox(-2.0F, -2.0F, -1.5F, 3, 15, 3),
                PartPose.offset(-5.0F, -27.0F, 0.0F));

        root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                        .texOffs(32, 0).mirror().addBox(-1.0F, -2.0F, -1.5F, 3, 15, 3).mirror(false),
                PartPose.offset(5.0F, -27.0F, 0.0F));

        root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                        .texOffs(52, 18).addBox(-1.6F, 0.0F, -1.5F, 3, 15, 3),
                PartPose.offset(-1.9F, -15.0F, 0.0F));

        root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                        .texOffs(52, 0).mirror().addBox(-1.4F, 0.0F, -1.5F, 3, 15, 3).mirror(false),
                PartPose.offset(1.9F, -15.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    private static float ease(float t) {
        t = Mth.clamp(t, 0.0F, 1.0F);
        return t * t * (3.0F - 2.0F * t);
    }

    @Override
    public void setupAnim(T remnant, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root.getAllParts().forEach(ModelPart::resetPose);
        Remnant.RemnantState state = remnant.getRemnantState();
        float st = remnant.getStateAnimTime(ageInTicks);
        float partialTick = ageInTicks - remnant.tickCount;

        boolean headLook = switch (state) {
            case IDLE, WALL_IDLE, WALL_HIDDEN, PEEK, RETREAT, TIP_WINDUP, SWAT, LUNGE_TELEGRAPH, POKE, EMERGE -> true;
            default -> false;
        };
        if (headLook) {
            this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
            this.head.xRot = headPitch * ((float) Math.PI / 180F);
        }

        float swing = Mth.cos(limbSwing * 0.5F) * 0.8F * limbSwingAmount;
        float swingOpposite = Mth.cos(limbSwing * 0.5F + (float) Math.PI) * 0.8F * limbSwingAmount;
        if (state == Remnant.RemnantState.IDLE || state == Remnant.RemnantState.RETREAT || state == Remnant.RemnantState.EMERGE) {
            this.rightLeg.xRot = swing;
            this.leftLeg.xRot = swingOpposite;
            this.rightArm.xRot = swingOpposite * 0.6F;
            this.leftArm.xRot = swing * 0.6F;
            this.root.zRot = Mth.cos(limbSwing * 0.25F) * 0.06F * limbSwingAmount;
        }

        float breath = Mth.sin(ageInTicks * 0.045F) * 0.03F;

        switch (state) {
            case IDLE -> this.body.xRot = breath * 0.5F;
            case WALL_IDLE, WALL_HIDDEN -> this.applyMeditation(breath);
            case PEEK -> {
                this.applyMeditation(breath * 2.0F);
                float peek = Math.min(ease(st / 8.0F), ease((45.0F - st) / 8.0F));
                this.root.z -= 6.5F * peek;
                this.root.xRot -= 0.1F * peek;
                this.head.yRot += Mth.sin(st * 0.12F) * 0.5F * peek;
                this.head.xRot -= 0.15F * peek;
            }
            case RETREAT -> {
                this.rightArm.xRot = -0.7F + swingOpposite * 0.2F;
                this.leftArm.xRot = -0.7F + swing * 0.2F;
            }
            case TIP_WINDUP -> {
                float k = ease(st / 14.0F);
                this.root.xRot = -0.3F * k;
                this.rightArm.xRot = -0.55F * k;
                this.leftArm.xRot = -0.55F * k;
                this.head.xRot += 0.2F * k;
            }
            case TIP_RUSH -> {
                float k = ease(st / 3.0F);
                this.root.xRot = -0.3F + 0.95F * k;
                this.rightArm.xRot = -1.5F * k;
                this.leftArm.xRot = -1.5F * k;
                this.rightArm.zRot = 0.2F * k;
                this.leftArm.zRot = -0.2F * k;
                this.head.xRot = -0.5F * k;
                this.rightLeg.xRot = 0.3F * k;
                this.leftLeg.xRot = 0.3F * k;
            }
            case TIP_RECOVER -> {
                float settle = ease(st / 6.0F);
                float rise = ease((st - 8.0F) / 24.0F);
                float remain = 1.0F - rise;
                this.root.xRot = Mth.lerp(settle, 0.65F, 0.85F) * remain;
                this.rightArm.xRot = (-0.4F + Mth.sin(ageInTicks * 0.3F) * 0.1F) * remain;
                this.leftArm.xRot = (-0.4F - Mth.sin(ageInTicks * 0.3F) * 0.1F) * remain;
                this.head.xRot = 0.55F * remain;
            }
            case SWAT -> {
                float armAngle;
                float twist;
                if (st < 5.0F) {
                    float k = ease(st / 5.0F);
                    armAngle = -2.5F * k;
                    twist = 0.25F * k;
                } else if (st < 9.0F) {
                    float k = ease((st - 5.0F) / 4.0F);
                    armAngle = Mth.lerp(k, -2.5F, -0.7F);
                    twist = Mth.lerp(k, 0.25F, -0.4F);
                } else {
                    float k = ease((st - 9.0F) / 5.0F);
                    armAngle = Mth.lerp(k, -0.7F, 0.0F);
                    twist = Mth.lerp(k, -0.4F, 0.0F);
                }
                this.rightArm.xRot = armAngle;
                this.rightArm.yRot = -0.3F;
                this.body.yRot = twist;
            }
            case LUNGE_TELEGRAPH -> {
                float k = ease(st / 6.0F);
                this.root.xRot = -0.18F * k;
                this.rightArm.xRot = 0.5F * k;
                this.leftArm.xRot = 0.5F * k;
                this.root.x += Mth.sin(st * 2.4F) * 0.35F * k;
            }
            case LUNGE_FLY -> {
                float k = ease(st / 4.0F);
                this.root.xRot = 1.15F * k;
                this.rightArm.xRot = -2.9F * k;
                this.leftArm.xRot = -2.9F * k;
                this.rightArm.zRot = 0.25F * k;
                this.leftArm.zRot = -0.25F * k;
                this.head.xRot = -0.7F * k;
                this.rightLeg.xRot = 0.15F * k;
                this.leftLeg.xRot = 0.15F * k;
            }
            case EXPOSED -> {
                float k = ease(st / 5.0F);
                this.root.xRot = Mth.lerp(k, 1.15F, 1.5F);
                this.rightArm.xRot = -2.9F + Mth.sin(ageInTicks * 0.18F) * 0.25F * k;
                this.leftArm.xRot = -2.9F + Mth.sin(ageInTicks * 0.18F + 1.5F) * 0.25F * k;
                this.head.xRot = (-0.45F + Mth.sin(ageInTicks * 0.1F) * 0.2F) * k;
            }
            case GET_UP -> {
                float k = 1.0F - ease(st / 14.0F);
                this.root.xRot = 1.5F * k;
                this.rightArm.xRot = -2.9F * k;
                this.leftArm.xRot = -2.9F * k;
                this.head.xRot = -0.45F * k;
            }
            case POKE -> {
                float jab = ease(st / 3.0F) * ease((11.0F - st) / 4.0F);
                this.rightArm.xRot = -1.9F * jab;
                this.leftArm.xRot = -1.9F * jab;
                this.body.xRot = 0.15F * jab;
            }
            case DIVE -> {
                float k = ease(st / 12.0F);
                this.root.xRot = 0.85F * k;
                this.rightArm.xRot = -2.6F * k;
                this.leftArm.xRot = -2.6F * k;
                this.head.xRot = -0.4F * k;
            }
            case PHASE_SWIM, WHIRLPOOL -> {
                float pitch = remnant.getSwimPitch(partialTick);
                this.root.xRot = state == Remnant.RemnantState.WHIRLPOOL ? 1.1F : pitch;
                float ph = ageInTicks * 0.3F;
                this.rightArm.xRot = -1.1F + Mth.sin(ph) * 0.8F;
                this.leftArm.xRot = -1.1F + Mth.sin(ph) * 0.8F;
                this.rightArm.yRot = 0.45F + Mth.cos(ph) * 0.4F;
                this.leftArm.yRot = -0.45F - Mth.cos(ph) * 0.4F;
                this.rightLeg.xRot = Mth.sin(ph + (float) Math.PI) * 0.3F;
                this.leftLeg.xRot = Mth.sin(ph + (float) Math.PI) * 0.3F;
                this.head.xRot = -pitch * 0.5F;
            }
            case ERUPT -> {
                float k = ease(st / 4.0F);
                this.rightArm.xRot = -3.0F * k;
                this.leftArm.xRot = -3.0F * k;
                this.head.xRot = -0.5F * k;
                this.rightLeg.xRot = 0.1F * k;
                this.leftLeg.xRot = 0.1F * k;
            }
            case STUNNED -> {
                float k = ease(st / 6.0F);
                this.root.y += 7.0F * k;
                this.root.xRot = 0.45F * k;
                this.rightLeg.xRot = -1.4F * k;
                this.leftLeg.xRot = -1.4F * k;
                this.rightArm.xRot = (-0.5F + Mth.sin(ageInTicks * 0.25F) * 0.08F) * k;
                this.leftArm.xRot = (-0.5F - Mth.sin(ageInTicks * 0.25F) * 0.08F) * k;
                this.head.xRot = 0.65F * k;
            }
            case EMERGE -> {
                float k = 1.0F - ease(st / 10.0F);
                this.root.xRot = -0.3F * k;
                this.rightArm.xRot += 0.7F * k;
                this.leftArm.xRot += 0.7F * k;
            }
        }
    }

    private void applyMeditation(float breath) {
        this.rightArm.xRot = -0.95F + breath;
        this.leftArm.xRot = -0.95F + breath;
        this.rightArm.yRot = -0.45F;
        this.leftArm.yRot = 0.45F;
        this.head.xRot += 0.3F;
        this.body.xRot = breath;
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}
