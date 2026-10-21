package com.farcr.nomansland.common.entity.ai;

import com.farcr.nomansland.common.block.ThermalVentBlock;
import com.farcr.nomansland.common.block.VentBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;

import java.util.EnumSet;
import java.util.Map;

public class SoakInThermalWaterGoal extends Goal {
    private static final int SEARCH_RANGE = 10;
    private static final int VERTICAL_SEARCH_RANGE = 4;

    private final PathfinderMob mob;
    private final double speedModifier;
    private BlockPos targetPos;
    private int soakTicks;
    private int cooldown;

    public SoakInThermalWaterGoal(PathfinderMob mob, double speedModifier) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (cooldown > 0) {
            cooldown--;
            return false;
        }
        cooldown = reducedTickDelay(300 + mob.getRandom().nextInt(300));
        if (mob.isPassenger() || mob.isLeashed()) return false;

        Level level = mob.level();
        BlockPos vent = BlockPos.findClosestMatch(mob.blockPosition(), SEARCH_RANGE, VERTICAL_SEARCH_RANGE,
                pos -> level.getBlockState(pos).getBlock() instanceof ThermalVentBlock).orElse(null);
        if (vent == null) return false;

        Map<BlockPos, Integer> affected = VentBlock.computeAffectedWater(level, vent, level.getBlockState(vent).getValue(VentBlock.FACING));
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        for (BlockPos cell : affected.keySet()) {
            if (VentBlock.isWater(level, cell.above())) continue;
            if (VentBlock.isWater(level, cell.below()) && VentBlock.isWater(level, cell.below(2))) continue;
            double distance = cell.distToCenterSqr(mob.position());
            if (distance < bestDistance) {
                bestDistance = distance;
                best = cell;
            }
        }
        if (best == null) return false;
        targetPos = best;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return targetPos != null && soakTicks > 0 && VentBlock.isWater(mob.level(), targetPos);
    }

    @Override
    public void start() {
        soakTicks = adjustedTickDelay(400 + mob.getRandom().nextInt(400));
        mob.getNavigation().moveTo(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5, speedModifier);
    }

    @Override
    public void stop() {
        targetPos = null;
        mob.getNavigation().stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        boolean soaking = mob.isInWater() && mob.blockPosition().closerThan(targetPos, 2.5);
        if (soaking) {
            soakTicks--;
            mob.getNavigation().stop();
        } else {
            soakTicks--;
            if (mob.getNavigation().isDone()) {
                mob.getNavigation().moveTo(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5, speedModifier);
            }
        }

        if (soakTicks % 100 == 0 && !VentBlock.isAffectedBy(mob.level(), targetPos, ThermalVentBlock.class)) {
            soakTicks = 0;
        }
    }
}
