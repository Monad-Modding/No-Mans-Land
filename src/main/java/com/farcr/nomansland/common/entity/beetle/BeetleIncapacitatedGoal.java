package com.farcr.nomansland.common.entity.beetle;

import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class BeetleIncapacitatedGoal extends Goal {
    private final Beetle beetle;

    public BeetleIncapacitatedGoal(Beetle beetle) {
        this.beetle = beetle;
        setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP, Flag.LOOK, Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        return beetle.isIncapacitated();
    }

    @Override
    public boolean canContinueToUse() {
        return beetle.isIncapacitated();
    }

    @Override
    public void start() {
        beetle.getNavigation().stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (beetle.isFlipped()) {
            beetle.getNavigation().stop();
        }
    }
}
