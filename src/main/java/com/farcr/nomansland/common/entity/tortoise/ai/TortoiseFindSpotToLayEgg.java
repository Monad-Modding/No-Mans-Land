package com.farcr.nomansland.common.entity.tortoise.ai;

import com.farcr.nomansland.common.entity.tortoise.Tortoise;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class TortoiseFindSpotToLayEgg extends Goal {
    protected final Tortoise tortoise;
    private final double speedModifier;
    private final Level level;
    private boolean failedAttempt = false;
    private int ticksGoalRan;
    private int nextStartTick;
    private Path path;

    public TortoiseFindSpotToLayEgg(Tortoise mob, double speedModifier) {
        this.tortoise = mob;
        this.speedModifier = speedModifier;
        this.level = mob.level();
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!this.tortoise.hasEgg() || this.tortoise.inShell() || this.tortoise.isLayingEgg())
            return false;
        if (this.nextStartTick > 0) {
            this.nextStartTick--;
            return false;
        }
        if (this.getHomePos() != null)
            return true;
        this.nextStartTick = this.adjustedTickDelay(200 + this.tortoise.getRandom().nextInt(200));
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return this.tortoise.hasEgg() && !this.tortoise.inShell() && !this.failedAttempt && this.path != null && !this.tortoise.isLayingEgg();
    }

    @Override
    public void start() {
        this.failedAttempt = false;
        this.ticksGoalRan = 0;
    }

    @Override
    public void tick() {
        if (this.path == null)
            return;
        this.ticksGoalRan++;
        BlockPos blockToGo = this.path.getTarget();
        this.tortoise.getNavigation().moveTo(this.path, this.speedModifier);
        this.tortoise.getLookControl().setLookAt(Vec3.atCenterOf(blockToGo));
        // Shut down the goal if the blockToGo isn't a valid home or if the goal's gone on for too long and the tortoise still hasn't reached it's destination
        if (!tortoise.isValidHome(blockToGo) || ticksGoalRan >= 600 && !blockToGo.closerToCenterThan(this.tortoise.position(), 1.5D)
                || !path.canReach() && blockToGo.closerToCenterThan(this.tortoise.position(), 4.5D)) {
            this.tortoise.getNavigation().stop();
            this.failedAttempt = true;
        } else if (blockToGo.closerToCenterThan(this.tortoise.position(), 1.5D) || this.tortoise.isValidHome(this.tortoise.blockPosition())) {
            this.tortoise.setLayingEgg(true);
        }
    }

    @Override
    public void stop() {
        super.stop();
        if (this.failedAttempt)
            this.tortoise.setHasEgg(false);
        this.path = null;
    }

    @Nullable
    protected BlockPos getHomePos() {
        if (!tortoise.hasEgg())
            return null;
        // Borrowed from TryToFindWaterGoal, modified to have a longer range and accommodate the Tortoise's larger hitbox
        Iterable<BlockPos> iterable = BlockPos.betweenClosed(Mth.floor(tortoise.getX() - 12), Mth.floor(tortoise.getY() - 6), Mth.floor(tortoise.getZ() - 12), Mth.floor(tortoise.getX() + 12), Mth.floor(tortoise.getY() + 6), Mth.floor(tortoise.getZ() + 12));
        BlockPos blockToGo = null;
        for (BlockPos newPos : iterable) {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos horizontalPos = newPos.relative(direction);
                if (level.isEmptyBlock(horizontalPos.above()) &&
                        level.getBlockState(horizontalPos).isPathfindable(PathComputationType.LAND)
                        && tortoise.isValidHome(horizontalPos) &&
                        BlockPos.squareOutSouthEast(horizontalPos).allMatch(tortoise::isValidHome)) {
                    blockToGo = horizontalPos;
                    break;
                }
            }
            if (blockToGo != null) {
                this.path = tortoise.getNavigation().createPath(blockToGo, 0);
                return blockToGo;
            }
        }
        return null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
