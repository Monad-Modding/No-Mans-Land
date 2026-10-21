package com.farcr.nomansland.common.entity.cave_carp.ai;

import com.farcr.nomansland.common.entity.cave_carp.CaveCarp;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;

import java.util.EnumSet;

public class CaveCarpSeekLightGoal extends Goal {

    private static final int HORIZONTAL_RANGE = 8;
    private static final int VERTICAL_RANGE = 4;

    private final CaveCarp carp;
    private final double speedModifier;
    private BlockPos target;
    private int recalcCooldown;

    public CaveCarpSeekLightGoal(CaveCarp carp, double speedModifier) {
        this.carp = carp;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!this.carp.isInWater()) return false;
        BlockPos brightest = this.findBrightestStillWater();
        if (brightest == null) return false;
        Level level = this.carp.level();
        if (level.getMaxLocalRawBrightness(brightest) <= level.getMaxLocalRawBrightness(this.carp.blockPosition())) return false;
        if (this.carp.getNavigation().createPath(brightest, 1) == null) return false;
        this.target = brightest;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return this.target != null && !this.carp.getNavigation().isDone() && this.carp.isInWater();
    }

    @Override
    public void start() {
        this.carp.getNavigation().moveTo(this.target.getX() + 0.5, this.target.getY() + 0.5, this.target.getZ() + 0.5, this.speedModifier);
        this.recalcCooldown = this.adjustedTickDelay(40);
    }

    @Override
    public void stop() {
        this.target = null;
        this.carp.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (--this.recalcCooldown <= 0) {
            this.recalcCooldown = this.adjustedTickDelay(40);
            if (this.target != null) {
                this.carp.getNavigation().moveTo(this.target.getX() + 0.5, this.target.getY() + 0.5, this.target.getZ() + 0.5, this.speedModifier);
            }
        }
    }

    private BlockPos findBrightestStillWater() {
        Level level = this.carp.level();
        BlockPos origin = this.carp.blockPosition();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        BlockPos best = null;
        int bestLight = -1;
        for (int i = 0; i < 48; i++) {
            int dx = this.carp.getRandom().nextInt(HORIZONTAL_RANGE * 2 + 1) - HORIZONTAL_RANGE;
            int dy = this.carp.getRandom().nextInt(VERTICAL_RANGE * 2 + 1) - VERTICAL_RANGE;
            int dz = this.carp.getRandom().nextInt(HORIZONTAL_RANGE * 2 + 1) - HORIZONTAL_RANGE;
            cursor.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
            if (!this.carp.isStillWater(cursor)) continue;
            int light = level.getMaxLocalRawBrightness(cursor);
            if (light > bestLight) {
                bestLight = light;
                best = cursor.immutable();
            }
        }
        return best;
    }
}
