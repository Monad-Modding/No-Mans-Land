package com.farcr.nomansland.common.entity.centipede;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class CentipedeShrivelGoal extends Goal {
    private final Centipede centipede;

    public CentipedeShrivelGoal(Centipede centipede) {
        this.centipede = centipede;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return this.centipede.isInShrivelState();
    }

    @Override
    public boolean canContinueToUse() {
        return this.centipede.isInShrivelState();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        this.centipede.getNavigation().stop();
    }

    @Override
    public void tick() {
        this.centipede.getNavigation().stop();
        if (this.centipede.onGround() && this.centipede.getRandom().nextInt(4) == 0) {
            Vec3 movement = this.centipede.getDeltaMovement();
            double jitterX = (this.centipede.getRandom().nextDouble() - 0.5) * 0.2;
            double jitterZ = (this.centipede.getRandom().nextDouble() - 0.5) * 0.2;
            this.centipede.setDeltaMovement(jitterX, movement.y, jitterZ);
        }
        this.centipede.setYRot(this.centipede.getYRot() + (this.centipede.getRandom().nextFloat() - 0.5F) * 12.0F);
    }
}
