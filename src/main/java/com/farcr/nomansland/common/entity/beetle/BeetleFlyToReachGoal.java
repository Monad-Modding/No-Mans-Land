package com.farcr.nomansland.common.entity.beetle;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class BeetleFlyToReachGoal extends Goal {
    private final Beetle beetle;
    private boolean seeking;
    private double targetX;
    private double targetY;
    private double targetZ;
    private BlockPos seekPos;

    public BeetleFlyToReachGoal(Beetle beetle) {
        this.beetle = beetle;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (beetle.getState() != Beetle.STATE_IDLE) return false;
        if (beetle.getFlightCooldown() > 0) return false;
        if (beetle.getDungBall() != null) return false;
        if (beetle.getRandom().nextInt(160) != 0) return false;
        BlockPos gap = findUnreachableSpot();
        if (gap == null) return false;
        if (beetle.hasFlightHeadroom()) {
            targetX = gap.getX() + 0.5;
            targetY = gap.getY();
            targetZ = gap.getZ() + 0.5;
            seeking = false;
            return true;
        }
        BlockPos open = beetle.findOpenSkySpot();
        if (open != null) {
            seekPos = open;
            seeking = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return seeking && seekPos != null && !beetle.getNavigation().isDone()
                && beetle.getState() == Beetle.STATE_IDLE && !beetle.hasFlightHeadroom();
    }

    @Override
    public void start() {
        if (seeking) {
            beetle.getNavigation().moveTo(seekPos.getX() + 0.5, seekPos.getY(), seekPos.getZ() + 0.5, 1.0);
        } else {
            beetle.startFlight(targetX, targetY, targetZ);
        }
    }

    @Override
    public void stop() {
        seeking = false;
        seekPos = null;
    }

    @Nullable
    private BlockPos findUnreachableSpot() {
        for (int attempt = 0; attempt < 6; attempt++) {
            int dx = beetle.getRandom().nextInt(33) - 16;
            int dz = beetle.getRandom().nextInt(33) - 16;
            if (Math.abs(dx) < 6 && Math.abs(dz) < 6) continue;
            BlockPos stand = findStandable(beetle.blockPosition().offset(dx, 0, dz));
            if (stand == null) continue;
            Path path = beetle.getNavigation().createPath(stand, 0);
            if (path == null || !path.canReach()) {
                return stand;
            }
        }
        return null;
    }

    @Nullable
    private BlockPos findStandable(BlockPos around) {
        Level level = beetle.level();
        for (int y = 4; y >= -4; y--) {
            BlockPos pos = around.offset(0, y, 0);
            BlockPos below = pos.below();
            if (level.getBlockState(below).isSolidRender(level, below)
                    && level.getBlockState(pos).isAir()
                    && level.getBlockState(pos.above()).isAir()) {
                return pos;
            }
        }
        return null;
    }
}
