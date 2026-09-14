package com.farcr.nomansland.common.mixin;

import com.farcr.nomansland.common.extension.EntityExtension;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import com.farcr.nomansland.common.registry.entities.NMLEffects;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.Set;

@Mixin(Entity.class)
public abstract class EntityMixin implements EntityExtension {

    @Shadow public abstract void playSound(SoundEvent sound, float volume, float pitch);

    @Shadow public abstract double getY();

    @Shadow public abstract double getY(double scale);

    @Shadow public abstract double getZ();

    @Shadow public abstract double getX();

    @Shadow public abstract Level level();

    @Shadow public abstract EntityType<?> getType();

    @Shadow @Final protected RandomSource random;

    @Shadow public abstract BlockPos blockPosition();

    @Shadow public abstract Set<String> getTags();

    @Shadow public abstract BlockPos getOnPos();

    @Shadow protected abstract BlockPos getOnPos(float yOffset);

    @Shadow public abstract BlockState getBlockStateOn();

    @Shadow public float fallDistance;

    @Shadow public abstract Vec3 position();

    @Shadow public abstract boolean onGround();

    @Shadow public abstract boolean isInWater();

    /*
    * Offering Injection
    */

    @Shadow public abstract float getYRot();

    @Shadow public abstract float getXRot();

    @Shadow
    public abstract void setDeltaMovement(Vec3 deltaMovement);

    @Shadow
    public abstract Vec3 getDeltaMovement();

    @Unique private boolean NML$offering = false;
    @Unique private boolean NML$previouslyInspected = false;
    @Unique private float NML$inspectionFade = 0f;
    @Unique private float NML$prevInspectionFade = 0f;
    public void NML$setInspectionState(boolean isInspecting) {
        NML$offering = isInspecting;
        if (isInspecting)
            NML$previouslyInspected = true;
    }

    @ModifyVariable(method = "move", at = @At("HEAD"), argsOnly = true)
    private Vec3 nml$setDeltaMovement(Vec3 deltaMovement) {
        return deltaMovement.multiply(nml$getVisualTickMultiplier(), nml$getVisualTickMultiplier(), nml$getVisualTickMultiplier());
    }

    @Inject(method = "getGravity", at = @At("RETURN"), cancellable = true)
    private void nml$getGravity(CallbackInfoReturnable<Double> cir) {
        if (nml$getVisualTickMultiplier() < 1f) cir.setReturnValue(cir.getReturnValue() * (double) nml$getVisualTickMultiplier());
    }

    public boolean NML$isBeingInspected() {
        return NML$offering;
    }

    public boolean NML$wasPreviouslyInspected() {
        return NML$previouslyInspected;
    }

    public float NML$getInspectionFade(float partialTick) {
        return Mth.lerp(partialTick, NML$prevInspectionFade, NML$inspectionFade);
    }

    @Inject(method = "getGravity", at = @At("RETURN"), cancellable = true)
    private void NML$getGravity(CallbackInfoReturnable<Double> cir) {
        if (NML$isBeingInspected()) cir.setReturnValue(0.0d);
    }

    @ModifyVariable(method = "turn", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private double nml$rotX(double rotX) {
        return rotX * nml$getVisualTickMultiplier();
    }

    @ModifyVariable(method = "turn", at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private double nml$rotY(double rotY) {
        return rotY * nml$getVisualTickMultiplier();
    }


    @Inject(method = "isNoGravity", at = @At("RETURN"), cancellable = true)
    private void NML$isNoGravity(CallbackInfoReturnable<Boolean> cir) {
        if (NML$isBeingInspected()) cir.setReturnValue(true);
    }

    @ModifyVariable(method = "setXRot", at = @At("HEAD"), argsOnly = true)
    private float NML$slowInspectionSpin(float xRot) {
        if (NML$isBeingInspected()) {
            float current = getXRot();
            return current + (xRot - current) * 0.3f;
        }
        return xRot;
    }

    @Unique @Nullable
    private Vec3 startingToFallPosition;

    @Inject(method = "getOnPosLegacy", at = @At("RETURN"), cancellable = true)
    private void getOnPosLegacy(CallbackInfoReturnable<BlockPos> cir) {
        cir.setReturnValue(getBlockStateOn().is(NMLBlocks.SPIKE_TRAP.block()) ? getOnPos() : getOnPos(0.2F));
    }

    @Inject(method = "resetFallDistance", at = @At("HEAD"))
    private void resetFallDistance(CallbackInfo ci) {
        if (((Entity) (Object) this) instanceof LivingEntity livingEntity && livingEntity.getHealth() > 0 && startingToFallPosition != null && !livingEntity.getPassengers().isEmpty()) {
            livingEntity.getPassengers().forEach(entity -> {
                if (entity instanceof ServerPlayer player && player.getHealth() > 0) {
                    CriteriaTriggers.FALL_FROM_HEIGHT.trigger(player, this.startingToFallPosition);
                }
            });
        }

        startingToFallPosition = null;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tick(CallbackInfo ci) {
        trackStartFallingPosition();
        NML$updateInspectionFade();
    }

    @Unique
    private void NML$updateInspectionFade() {
        if (!level().isClientSide()) return;
        NML$prevInspectionFade = NML$inspectionFade;
        float target = NML$offering ? 1f : 0f;
        NML$inspectionFade = Mth.lerp(0.1f, NML$inspectionFade, target);
    }

    @Unique
    private void trackStartFallingPosition() {
        if (fallDistance > 0.0F && startingToFallPosition == null) {
            startingToFallPosition = position();
        }
    }

    @Unique private Entity nml$Self = (Entity) (Object) this;

    @Inject(method = "push(DDD)V", at = @At("HEAD"), cancellable = true)
    private void nml$stasisAvoidPush(double x, double y, double z, CallbackInfo ci) {
        if (nml$Self instanceof LivingEntity livingEntity
        && livingEntity.hasEffect(NMLEffects.STASIS)) ci.cancel();
    }
}
