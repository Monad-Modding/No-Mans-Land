package com.farcr.nomansland.common.mixin.client;

import com.farcr.nomansland.client.extensions.AncestralOathSwordClientExtensions;
import com.farcr.nomansland.common.entity.buddy.Buddy;
import com.farcr.nomansland.common.extension.LivingEntityExtension;
import com.farcr.nomansland.common.registry.entities.NMLEffects;
import com.farcr.nomansland.common.registry.items.NMLItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(HumanoidModel.class)
public abstract class HumanoidModelMixin<T extends LivingEntity> extends AgeableListModel<T> implements ArmedModel, HeadedModel {
    @Shadow @Final public ModelPart head;
    @Shadow @Final public ModelPart hat;
    @Shadow @Final public ModelPart leftArm;
    @Shadow @Final public ModelPart rightArm;
    @Shadow @Final public ModelPart rightLeg;
    @Shadow @Final public ModelPart leftLeg;
    @Shadow public HumanoidModel.ArmPose leftArmPose;
    @Shadow public HumanoidModel.ArmPose rightArmPose;

    @Unique @Nullable private static ItemStack itemShieldContext = null;
    @Unique private void nml$setHandContext(LivingEntity livingEntity, InteractionHand interactionHand) {
        itemShieldContext = livingEntity.getItemInHand(interactionHand);
    }

    @Inject(
        method = "poseRightArm",
        at = @At(
            value = "INVOKE",
            shift = At.Shift.BEFORE,
            target = "Lnet/minecraft/client/model/HumanoidModel;poseBlockingArm(Lnet/minecraft/client/model/geom/ModelPart;Z)V"
        )
    )
    private void nml$swordBlockAnimationRight(T livingEntity, CallbackInfo ci) {
        InteractionHand mainHand = (livingEntity.getMainArm() == HumanoidArm.RIGHT)
            ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        nml$setHandContext(livingEntity, mainHand);
    }

    @Inject(
        method = "poseLeftArm",
        at = @At(
            value = "INVOKE",
            shift = At.Shift.BEFORE,
            target = "Lnet/minecraft/client/model/HumanoidModel;poseBlockingArm(Lnet/minecraft/client/model/geom/ModelPart;Z)V"
        )
    )
    private void nml$swordBlockAnimationLeft(T livingEntity, CallbackInfo ci) {
        InteractionHand mainHand = (livingEntity.getMainArm() == HumanoidArm.LEFT)
            ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        nml$setHandContext(livingEntity, mainHand);
    }

    @Inject(method = "poseBlockingArm", at = @At("HEAD"), cancellable = true)
    private void nml$swordBlockAnimation(ModelPart arm, boolean isRightArm, CallbackInfo ci) {
        if (itemShieldContext != null && itemShieldContext.is(NMLItems.ANCESTRAL_OATH_SWORD)) {
            // from minecraft 1.8.9
            arm.xRot = arm.xRot * 0.5F - ((float)Math.PI / 10F) * 3f;
            arm.yRot = (isRightArm ? -30.0F : 30.0F)  * ((float)Math.PI / 180F);
            itemShieldContext = null;
            ci.cancel();
        }
    }

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At(value = "TAIL"))
    private void nml$setupAnims(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (entity.getItemBySlot(EquipmentSlot.CHEST).is(NMLItems.TORTOISE_SHELL.get()) && this.attackTime <= 0) {
            boolean flag = entity.getFallFlyingTicks() > 4;
            float f = 1.0F;
            if (flag) {
                f = (float) entity.getDeltaMovement().lengthSqr();
                f /= 0.2F;
                f *= f * f;
            }

            if (f < 1.0F) {
                f = 1.0F;
            }
            float movementRight = (Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 2.0F * limbSwingAmount * 0.5F / f);
            float movementLeft = (Mth.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F / f);
            boolean rightArmPosed = rightArmPose != HumanoidModel.ArmPose.EMPTY && rightArmPose != HumanoidModel.ArmPose.ITEM;
            boolean leftArmPosed = leftArmPose != HumanoidModel.ArmPose.EMPTY && leftArmPose != HumanoidModel.ArmPose.ITEM;
            if (!rightArmPosed && !leftArmPose.isTwoHanded()) {
                if (movementRight <= 0) {
                    rightArm.xRot = -Math.abs(movementRight);
                } else {
                    rightArm.xRot = 0.0F;
                }
            }
            if (!leftArmPosed && !rightArmPose.isTwoHanded()) {
                if (movementRight >= 0) { // So that the left arm does not perfectly coordinate with the right arm
                    leftArm.xRot = -Math.abs(movementLeft);
                } else {
                    leftArm.xRot = 0.0F;
                }
            }
        }

        if (entity.hasEffect(NMLEffects.HAPPINESS)) {
            Buddy.setupAnimationHappy(
                entity, head, hat,
                leftArm, rightArm,
                leftLeg, rightLeg,
                (ageInTicks / Buddy.DIVIDE_TIME_CONSTANT)
            );
        }

        // it really irks me how hardcoded arms are in minecraft
        LivingEntityExtension entityExtension = (LivingEntityExtension) entity;
        if (entity.getMainArm() == HumanoidArm.RIGHT) {
            this.rightArm.yRot += AncestralOathSwordClientExtensions.getShakePosition(
                entityExtension.nml$getShakeAnimationTime()) / 160f;
        } else if (entity.getMainArm() == HumanoidArm.LEFT) {
            this.leftArm.yRot -= AncestralOathSwordClientExtensions.getShakePosition(
                entityExtension.nml$getShakeAnimationTime()) / 160f;
        }

        this.leftArm.yRot -= AncestralOathSwordClientExtensions.swordRotationTransform(
            entityExtension.nml$getParryAnimationTime(HumanoidArm.LEFT)) / 50f;
        this.rightArm.yRot += AncestralOathSwordClientExtensions.swordRotationTransform(
            entityExtension.nml$getParryAnimationTime(HumanoidArm.RIGHT)) / 50f;

        entityExtension.nml$updateParryAnimationTime(
            Minecraft.getInstance().getTimer().getGameTimeDeltaTicks()
        );
    }
}
