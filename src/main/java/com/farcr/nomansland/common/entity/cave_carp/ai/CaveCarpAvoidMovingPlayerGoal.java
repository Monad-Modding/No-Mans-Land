package com.farcr.nomansland.common.entity.cave_carp.ai;

import com.farcr.nomansland.common.entity.cave_carp.CaveCarp;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class CaveCarpAvoidMovingPlayerGoal extends Goal {

    private final CaveCarp carp;
    private final float maxDistance;
    private final double walkSpeedModifier;
    private final double sprintSpeedModifier;
    private final PathNavigation navigation;
    private final TargetingConditions conditions;
    private Player threat;
    private Path fleePath;

    public CaveCarpAvoidMovingPlayerGoal(CaveCarp carp, float maxDistance, double walkSpeedModifier, double sprintSpeedModifier) {
        this.carp = carp;
        this.maxDistance = maxDistance;
        this.walkSpeedModifier = walkSpeedModifier;
        this.sprintSpeedModifier = sprintSpeedModifier;
        this.navigation = carp.getNavigation();
        this.conditions = TargetingConditions.forCombat().range(maxDistance).ignoreLineOfSight();
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        Player nearest = this.carp.level().getNearestPlayer(this.conditions, this.carp);
        if (nearest == null) return false;
        if (!nearest.walkAnimation.isMoving()) return false;

        Vec3 away = DefaultRandomPos.getPosAway(this.carp, 12, 7, nearest.position());
        if (away == null) return false;
        if (nearest.distanceToSqr(away.x, away.y, away.z) < nearest.distanceToSqr(this.carp)) return false;

        this.fleePath = this.navigation.createPath(away.x, away.y, away.z, 0);
        this.threat = nearest;
        return this.fleePath != null;
    }

    @Override
    public boolean canContinueToUse() {
        return !this.navigation.isDone();
    }

    @Override
    public void start() {
        this.navigation.moveTo(this.fleePath, this.walkSpeedModifier);
    }

    @Override
    public void stop() {
        this.threat = null;
    }

    @Override
    public void tick() {
        if (this.threat != null && this.carp.distanceToSqr(this.threat) < 49.0) {
            this.carp.getNavigation().setSpeedModifier(this.sprintSpeedModifier);
        } else {
            this.carp.getNavigation().setSpeedModifier(this.walkSpeedModifier);
        }
    }
}
