package com.farcr.nomansland.common.entity.cervidae.moose;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;


/**
 * Behavior similar to {@link net.minecraft.world.entity.ai.goal.AvoidEntityGoal}
 * Avoids a specific entity from a certain radius.
 * Triggered after the Moose stomps.
 */
public class MooseAggroStanceGoal extends Goal {

    private final TargetingConditions targetingConditions;

    protected final Moose moose;
    protected final double speedModifier;
    protected final float aggroDistance;
    protected final PathNavigation pathNav;

    @Nullable
    protected Path path;
    protected Entity avoidedTarget;

    public MooseAggroStanceGoal(Moose moose, double speedModifier, float aggroDistance) {
        this.moose = moose;
        this.speedModifier = speedModifier;
        this.aggroDistance = aggroDistance;
        this.pathNav = moose.getNavigation();
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        targetingConditions = TargetingConditions.forNonCombat().range(aggroDistance);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    public boolean shouldAvoid(Entity entity) {
        if (moose.isPacified()) {
            return false;
        }
        if (entity instanceof Player) {
            return moose.hasStompedRecently(Moose.AGGRO_STANCE_DURATION);
        }
        return false;
    }

    @Override
    public boolean canUse() {
        if (moose.isVehicle()) {
            return false;
        }
        var introvertArea = moose.getBoundingBox().inflate(aggroDistance, 3.0, aggroDistance);
        var level = moose.level();
        var avoided = level.getEntitiesOfClass(LivingEntity.class, introvertArea, EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(this::shouldAvoid));

        var avoidedTarget = level.getNearestEntity(
                avoided, targetingConditions,
                moose, moose.getX(), moose.getY(), moose.getZ());

        if (avoidedTarget == null) {
            return false;
        }
        Vec3 escapePos = DefaultRandomPos.getPosAway(moose, Mth.floor(aggroDistance), 6, avoidedTarget.position());
        if (escapePos == null) {
            return false;
        }
        if (avoidedTarget.distanceToSqr(escapePos.x, escapePos.y, escapePos.z) < avoidedTarget.distanceToSqr(moose)) {
            return false;
        }
        path = pathNav.createPath(escapePos.x, escapePos.y, escapePos.z, 0);
        this.avoidedTarget = avoidedTarget;
        return path != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (moose.targetMemory.isUpsetAt(avoidedTarget)) {
            return false;
        }
        return !pathNav.isDone();
    }

    @Override
    public void start() {
        pathNav.moveTo(path, speedModifier);
        pathNav.setSpeedModifier(speedModifier);
    }

    @Override
    public void tick() {
        moose.getNavigation().setSpeedModifier(moose.getStompAdjustedMovementSpeed((float) speedModifier));
        moose.lookAtAndFaceTarget(avoidedTarget);
    }
}