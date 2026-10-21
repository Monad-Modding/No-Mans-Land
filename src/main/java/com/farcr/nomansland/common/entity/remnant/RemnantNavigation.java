package com.farcr.nomansland.common.entity.remnant;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.phys.Vec3;

public class RemnantNavigation extends PathNavigation {

    public RemnantNavigation(Mob mob, Level level) {
        super(mob, level);
    }

    @Override
    protected PathFinder createPathFinder(int maxVisitedNodes) {
        this.nodeEvaluator = new RemnantNodeEvaluator();
        this.nodeEvaluator.setCanPassDoors(true);
        return new PathFinder(this.nodeEvaluator, maxVisitedNodes);
    }

    @Override
    protected boolean canUpdatePath() {
        return this.mob.onGround() || this.mob.isInLiquid() || (this.mob instanceof Remnant remnant && remnant.isPhased());
    }

    @Override
    protected Vec3 getTempMobPos() {
        return this.mob.position();
    }

    @Override
    public boolean isStableDestination(BlockPos pos) {
        return !this.level.getBlockState(pos.below()).isAir() || RemnantPhasing.canPhaseThrough(this.level, pos);
    }
}
