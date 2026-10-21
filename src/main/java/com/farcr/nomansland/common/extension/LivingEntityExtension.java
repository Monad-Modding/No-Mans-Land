package com.farcr.nomansland.common.extension;

import net.minecraft.world.entity.HumanoidArm;
import org.apache.commons.lang3.NotImplementedException;

public interface LivingEntityExtension {
    default void nml$skipDroppingDeathLoot() throws NotImplementedException {
        throw new NotImplementedException();
    }

    default void nml$setBeingResurrected() throws NotImplementedException {
        throw new NotImplementedException();
    }

    default boolean nml$isBeingResurrected() {
        return false;
    }

    default void nml$beginBellParalysis() {};
    default int nml$getBellParalysis() {
        return 0;
    };

    default void nml$shakeArmAnimation() {}
    default void nml$setShakeAnimationTime(float newTime) {}
    default float nml$getShakeAnimationTime() { return 0.0f; }

    default void nml$parryArmAnimation(HumanoidArm arm) {}
    default void nml$updateParryAnimationTime(float partialTick) {}
    default float nml$getParryAnimationTime(HumanoidArm arm) { return 0.0f; }
}
