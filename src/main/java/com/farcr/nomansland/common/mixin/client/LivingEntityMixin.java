package com.farcr.nomansland.common.mixin.client;

import com.farcr.nomansland.common.extension.LivingEntityExtension;
import com.farcr.nomansland.common.item.AncestralOathSwordItem;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.TransientEntitySectionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements LivingEntityExtension {
    public LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    // player animation stuff for ancestral oath sword
    @Unique private float nml$armShakeAnimationTime = 0;
    @Override public float nml$getShakeAnimationTime() {
        return nml$armShakeAnimationTime;
    }
    @Override public void nml$setShakeAnimationTime(float newTime) {
        this.nml$armShakeAnimationTime = Math.max(newTime, 0f);
    }
    @Override public void nml$shakeArmAnimation() {
        this.nml$setShakeAnimationTime(AncestralOathSwordItem.MAX_ANIMATE_TIME);
    }

    @Unique private float[] nml$armParryAnimationTime = {0f, 0f};
    @Override public void nml$updateParryAnimationTime(float partialTick) {
        for (int i = 0; i < nml$armParryAnimationTime.length; i++)
            nml$armParryAnimationTime[i] = Math.max(nml$armParryAnimationTime[i] - partialTick, 0f);
    }
    @Override public float nml$getParryAnimationTime(HumanoidArm arm) {
        return nml$armParryAnimationTime[Math.min(arm.getId(), 1)];
    }
    @Override public void nml$parryArmAnimation(HumanoidArm arm) {
        nml$armParryAnimationTime[arm.getId()] = AncestralOathSwordItem.MAX_PARRY_ANIMATE_TIME;
    }

    // https://bugs-legacy.mojang.com/browse/MC-273361
    // literally fixed in the first snapshot after 1.21.1
    // i am crying

    // client bound move entity packet calls this
    // this only has an effect if the entity is ticking clientside
    // if the entity is reallly far away then it will never *move* close enough to start ticking
    @Inject(method = "lerpTo", at = @At("HEAD"))
    private void nml$fixMC273361(double x, double y, double z, float yRot, float xRot, int steps, CallbackInfo ci) {
        if (this.levelCallback instanceof TransientEntitySectionManager.Callback manager) {
            if (!manager.currentSection.getStatus().isTicking()) {
                this.setPos(x, y, z);
                this.setRot(yRot, xRot);
            }
        }
    }
}
