package com.farcr.nomansland.common.mixin.client;

import com.farcr.nomansland.client.BarrelCarrying;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public abstract class BarrelCarryPoseMixin {
    @Unique
    private static final float nml$REACH = (float) Math.toRadians(-80.0);
    @Unique
    private static final float nml$SPREAD = (float) Math.toRadians(15.0);

    @Shadow
    @Final
    public ModelPart rightArm;

    @Shadow
    @Final
    public ModelPart leftArm;

    @Shadow
    @Final
    public ModelPart head;

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("RETURN"))
    private void nml$carryBarrel(LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (!(entity instanceof AbstractClientPlayer player) || !BarrelCarrying.isCarrying(player)) return;
        this.leftArm.zRot = 0.0F;
        this.rightArm.zRot = 0.0F;
        this.leftArm.xRot = nml$REACH + this.head.xRot;
        this.rightArm.xRot = nml$REACH + this.head.xRot;
        this.rightArm.yRot = -nml$SPREAD;
        this.leftArm.yRot = nml$SPREAD;
    }
}
