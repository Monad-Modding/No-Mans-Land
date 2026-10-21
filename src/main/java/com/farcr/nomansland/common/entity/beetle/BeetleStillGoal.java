package com.farcr.nomansland.common.entity.beetle;

import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class BeetleStillGoal extends Goal {
    private final Beetle beetle;

    public BeetleStillGoal(Beetle beetle) {
        this.beetle = beetle;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (beetle.getState() != Beetle.STATE_IDLE || beetle.isSpooked()) {
            return false;
        }
        return beetle.isFrozenStill() || beetle.isSensePaused();
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void start() {
        beetle.getNavigation().stop();
    }

    @Override
    public void tick() {
        beetle.getNavigation().stop();
        beetle.setXxa(0.0F);
        beetle.setZza(0.0F);
    }
}
