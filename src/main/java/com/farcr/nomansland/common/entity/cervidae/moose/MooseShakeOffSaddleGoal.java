package com.farcr.nomansland.common.entity.cervidae.moose;

import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;


/**
 * Behavior similar to {@link net.minecraft.world.entity.ai.goal.AvoidEntityGoal}
 * Avoids specified entities from a certain radius. Occasionally Stomps if any are found within a certain radius.
 */
public class MooseShakeOffSaddleGoal extends Goal {

    protected final Moose moose;

    public int saddleShakeOffTimer;

    public MooseShakeOffSaddleGoal(Moose moose) {
        this.moose = moose;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!moose.isSaddled()) {
            return false;
        }
        if (moose.isPacified()) {
            return false;
        }
        saddleShakeOffTimer++;
        return saddleShakeOffTimer >= Moose.SADDLE_SHAKEOFF_DELAY;
    }

    @Override
    public boolean canContinueToUse() {
        return moose.isStomping;
    }

    @Override
    public void start() {
        moose.shakeOffSaddle();
        saddleShakeOffTimer = 0;
    }
}