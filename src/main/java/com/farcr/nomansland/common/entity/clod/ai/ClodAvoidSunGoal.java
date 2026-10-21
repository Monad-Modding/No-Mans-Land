package com.farcr.nomansland.common.entity.clod.ai;

import com.farcr.nomansland.common.entity.clod.Clod;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class ClodAvoidSunGoal extends Goal {
    private final Clod clod;
    private final double speedModifier;
    private double wantedX;
    private double wantedY;
    private double wantedZ;

    public ClodAvoidSunGoal(Clod clod, double speedModifier) {
        this.clod = clod;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.clod.isFleeing() || this.clod.isHiding()) return false;
        Level level = this.clod.level();
        if (!level.isDay() || !level.canSeeSky(this.clod.blockPosition())) return false;
        return this.findShade();
    }

    @Override
    public boolean canContinueToUse() {
        return !this.clod.getNavigation().isDone() && !this.clod.isFleeing() && !this.clod.isHiding();
    }

    @Override
    public void start() {
        this.clod.getNavigation().moveTo(this.wantedX, this.wantedY, this.wantedZ, this.speedModifier);
    }

    private boolean findShade() {
        RandomSource random = this.clod.getRandom();
        BlockPos origin = this.clod.blockPosition();
        for (int i = 0; i < 12; i++) {
            BlockPos pos = origin.offset(random.nextInt(20) - 10, random.nextInt(6) - 3, random.nextInt(20) - 10);
            if (!this.clod.level().canSeeSky(pos)) {
                Vec3 target = Vec3.atBottomCenterOf(pos);
                this.wantedX = target.x;
                this.wantedY = target.y;
                this.wantedZ = target.z;
                return true;
            }
        }
        return false;
    }
}
