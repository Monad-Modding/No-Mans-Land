package com.farcr.nomansland.common.entity.tortoise.ai;

import com.farcr.nomansland.common.entity.tortoise.Tortoise;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class TortoiseStayAroundHomeGoal extends Goal {
    private final Tortoise tortoise;
    private final double speedModifier;
    private int repathCooldown;
    private int failedAttempts;
    private int giveUpUntil;

    public TortoiseStayAroundHomeGoal(Tortoise tortoise, double speedModifier) {
        this.tortoise = tortoise;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.tortoise.tickCount < this.giveUpUntil)
            return false;
        BlockPos homePos = this.tortoise.getHomePos();
        return homePos != null && !homePos.closerToCenterThan(this.tortoise.position(), 10.0) && !this.tortoise.inShell();
    }

    @Override
    public void start() {
        this.tortoise.setGoingHome(true);
        this.repathCooldown = 0;
        this.failedAttempts = 0;
    }

    @Override
    public void tick() {
        BlockPos blockpos = this.tortoise.getHomePos();
        if (blockpos == null)
            return;
        if (this.repathCooldown > 0) {
            this.repathCooldown--;
            return;
        }
        if (this.tortoise.getNavigation().isDone()) {
            this.repathCooldown = this.adjustedTickDelay(40);
            Vec3 vec3 = Vec3.atBottomCenterOf(blockpos);
            if (this.tortoise.getNavigation().moveTo(vec3.x, vec3.y, vec3.z, this.speedModifier)) {
                this.failedAttempts = 0;
            } else {
                this.failedAttempts++;
                if (this.failedAttempts >= 5) {
                    this.giveUpUntil = this.tortoise.tickCount + this.adjustedTickDelay(400);
                    this.tortoise.getNavigation().stop();
                }
            }
        }
    }

    @Override
    public void stop() {
        this.tortoise.setGoingHome(false);
    }

    @Override
    public boolean canContinueToUse() {
        return this.tortoise.tickCount >= this.giveUpUntil
                && this.tortoise.getHomePos() != null
                && !this.tortoise.getHomePos().closerToCenterThan(this.tortoise.position(), 10)
                && !this.tortoise.inShell();
    }
}
