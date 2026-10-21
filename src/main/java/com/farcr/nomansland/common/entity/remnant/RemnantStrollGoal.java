package com.farcr.nomansland.common.entity.remnant;

import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;

public class RemnantStrollGoal extends WaterAvoidingRandomStrollGoal {

    private final Remnant remnant;

    public RemnantStrollGoal(Remnant remnant) {
        super(remnant, 0.8);
        this.remnant = remnant;
        this.setInterval(50);
    }

    @Override
    public boolean canUse() {
        if (!this.remnant.isWandering() || this.remnant.isPhased()) return false;
        if (this.remnant.avoidsSky() && this.remnant.level().canSeeSky(this.remnant.blockPosition())) return false;
        return super.canUse();
    }

    @Override
    public void start() {
        this.remnant.setRemnantState(Remnant.RemnantState.IDLE);
        super.start();
    }

    @Override
    public boolean canContinueToUse() {
        return this.remnant.isWandering() && super.canContinueToUse();
    }
}
