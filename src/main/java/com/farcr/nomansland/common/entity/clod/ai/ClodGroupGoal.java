package com.farcr.nomansland.common.entity.clod.ai;

import com.farcr.nomansland.common.entity.clod.Clod;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class ClodGroupGoal extends Goal {
    private final Clod clod;
    private final double speedModifier;
    private Clod leader;
    private int recalcTicks;

    public ClodGroupGoal(Clod clod, double speedModifier) {
        this.clod = clod;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.clod.isFleeing() || this.clod.isHiding()) return false;
        if (this.clod.isGroupLeader()) return false;
        this.leader = this.clod.findGroupCenter();
        return this.leader != null && this.clod.distanceToSqr(this.leader) > 128;
    }

    @Override
    public boolean canContinueToUse() {
        return this.leader != null && this.leader.isAlive() && !this.clod.isFleeing() && this.clod.distanceToSqr(this.leader) > 12;
    }

    @Override
    public void start() {
        this.recalcTicks = 0;
    }

    @Override
    public void stop() {
        this.leader = null;
        this.clod.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (this.leader == null) return;
        if (--this.recalcTicks <= 0) {
            this.recalcTicks = this.adjustedTickDelay(10);
            this.clod.getNavigation().moveTo(this.leader, this.speedModifier);
        }
    }
}
