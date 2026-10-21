package com.farcr.nomansland.common.entity.clod.ai;

import com.farcr.nomansland.common.entity.clod.Clod;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class ClodFreezeGoal extends Goal {
    private final Clod clod;

    public ClodFreezeGoal(Clod clod) {
        this.clod = clod;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        return !this.clod.isFleeing() && !this.clod.isInvestigating() && this.clod.isAlerted();
    }

    @Override
    public boolean canContinueToUse() {
        return !this.clod.isFleeing() && !this.clod.isInvestigating() && this.clod.isAlerted();
    }

    @Override
    public void start() {
        this.clod.setHiding(true);
        this.clod.getNavigation().stop();
    }

    @Override
    public void stop() {
        this.clod.setHiding(false);
    }

    @Override
    public void tick() {
        this.clod.getNavigation().stop();
        this.clod.setXxa(0.0F);
        this.clod.setZza(0.0F);
        LivingEntity threat = this.clod.findThreat(this.clod.leaveRange());
        if (threat != null) {
            this.clod.getLookControl().setLookAt(threat);
        }
    }
}
