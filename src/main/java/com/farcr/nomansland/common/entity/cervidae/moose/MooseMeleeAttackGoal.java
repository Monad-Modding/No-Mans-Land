package com.farcr.nomansland.common.entity.cervidae.moose;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

public class MooseMeleeAttackGoal extends MeleeAttackGoal {

    private final Moose moose;
    protected final PathNavigation pathNav;

    private LivingEntity cachedTarget;
    private boolean isReadyingAttack;
    private int attackDelay;

    public MooseMeleeAttackGoal(Moose moose, double speedModifier) {
        super(moose, speedModifier, false);
        this.moose = moose;
        this.pathNav = moose.getNavigation();
    }

    @Override
    public boolean canUse() {
        var target = moose.getTarget();
        if (target == null) {
            return false;
        }
        if (!target.isAlive()) {
            return false;
        }
        if (!moose.targetMemory.isUpsetAt(target)) {
            return false;
        }
        long time = moose.level().getGameTime();

        if (isReadyingAttack || time - lastCanUseCheck > 4L) {
            lastCanUseCheck = time;
            path = pathNav.createPath(target, 0);
            return path != null;
        } else {
            return false;
        }
    }

    @Override
    public boolean canContinueToUse() {
        if (isReadyingAttack) {
            return true;
        }
        var target = moose.getTarget();
        if (target == null) {
            return false;
        }
        if (!target.isAlive()) {
            return false;
        }
        if (!moose.targetMemory.isUpsetAt(target)) {
            return false;
        }
        if (moose.distanceTo(target) > Moose.ACTIVE_AGGRO_DISTANCE) {
            return false;
        }
        if (followingTargetEvenIfNotSeen) {
            if (moose.isWithinRestriction(target.blockPosition())) {
                if (target.isSpectator()) {
                    return false;
                }
                if (target instanceof Player player) {
                    return !player.isCreative();
                }
                return true;
            }
            return false;
        } else {
            return !pathNav.isDone();
        }
    }

    @Override
    public void tick() {
        if (isReadyingAttack) {
            if (attackDelay > 0) {
                attackDelay--;
                if (attackDelay == 0) {
                    isReadyingAttack = false;
                    if (cachedTarget == null || cachedTarget.isDeadOrDying()) {
                        cachedTarget = null;
                    }
                    if (cachedTarget != null) {
                        if (canDamageCachedTarget(cachedTarget)) {
                            moose.doHurtTarget(cachedTarget);
                            resetAttackCooldown();
                            cachedTarget = null;
                        }
                    }
                }
            }
        }
        super.tick();
        moose.lookAtAndFaceTarget(moose.getTarget());
    }

    @Override
    protected void checkAndPerformAttack(LivingEntity target) {
        if (isReadyingAttack) {
            return;
        }
        if (canPerformAttack(target)) {
            moose.level().broadcastEntityEvent(moose, Moose.ATTACK_EVENT);
            cachedTarget = target;
            isReadyingAttack = true;
            attackDelay = 16;
        }
    }

    @Override
    protected void resetAttackCooldown() {
        this.ticksUntilNextAttack = this.adjustedTickDelay(60);
    }

    @Override
    protected boolean canPerformAttack(LivingEntity target) {
        if (moose.isVehicle()) {
            return false;
        }
        if (!isTimeToAttack()) {
            return false;
        }
        if (!moose.getSensing().hasLineOfSight(target)) {
            return false;
        }
        var vehicle = target.getVehicle();
        if (vehicle != null) {
            var aabb1 = vehicle.getBoundingBox();
            var aabb2 = target.getBoundingBox();
            var area = new AABB(
                    Math.min(aabb2.minX, aabb1.minX),
                    aabb2.minY,
                    Math.min(aabb2.minZ, aabb1.minZ),
                    Math.max(aabb2.maxX, aabb1.maxX),
                    aabb2.maxY,
                    Math.max(aabb2.maxZ, aabb1.maxZ));
            if (area.intersects(moose.getHitbox())) {
                return true;
            }
        }
        return moose.isWithinMeleeAttackRange(target);
    }

    protected boolean canDamageCachedTarget(LivingEntity cachedTarget) {
        if (!isTimeToAttack()) {
            return false;
        }
        if (!moose.getSensing().hasLineOfSight(cachedTarget)) {
            return false;
        }
        return cachedTarget.distanceTo(moose) < 6f || moose.isWithinMeleeAttackRange(cachedTarget);
    }
}