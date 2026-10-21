package com.farcr.nomansland.common.entity.remnant;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;

public class RemnantTargetGoal extends TargetGoal {

    private static final double RETENTION_RANGE = 40.0;

    private final Remnant remnant;
    private final TargetingConditions acquireConditions = TargetingConditions.forCombat().range(15.0);
    private Player pendingTarget;

    public RemnantTargetGoal(Remnant remnant) {
        super(remnant, false);
        this.remnant = remnant;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (this.remnant.isPhased()) return false;
        this.pendingTarget = this.remnant.level().getNearestPlayer(this.acquireConditions, this.remnant);
        return this.pendingTarget != null;
    }

    @Override
    public void start() {
        this.remnant.setTarget(this.pendingTarget);
        super.start();
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = this.remnant.getTarget();
        if (target == null || !target.isAlive()) return false;
        if (target instanceof Player player && (player.isSpectator() || player.isCreative())) return false;
        return this.remnant.distanceToSqr(target) <= RETENTION_RANGE * RETENTION_RANGE;
    }
}
