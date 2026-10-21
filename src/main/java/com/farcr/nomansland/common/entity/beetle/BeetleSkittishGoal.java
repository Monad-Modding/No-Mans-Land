package com.farcr.nomansland.common.entity.beetle;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class BeetleSkittishGoal extends Goal {
    private final Beetle beetle;
    private final double speedModifier;
    private final PathNavigation navigation;
    private Path path;

    public BeetleSkittishGoal(Beetle beetle, double speedModifier) {
        this.beetle = beetle;
        this.speedModifier = speedModifier;
        this.navigation = beetle.getNavigation();
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!beetle.isSpooked() || beetle.getState() != Beetle.STATE_IDLE) {
            return false;
        }
        Vec3 away = awayPos();
        if (away == null) {
            return false;
        }
        this.path = this.navigation.createPath(away.x, away.y, away.z, 0);
        return this.path != null;
    }

    @Override
    public boolean canContinueToUse() {
        return beetle.isSpooked() && beetle.getState() == Beetle.STATE_IDLE && !this.navigation.isDone();
    }

    @Override
    public void start() {
        this.navigation.moveTo(this.path, this.speedModifier);
    }

    @Override
    public void stop() {
        this.path = null;
    }

    @Override
    public void tick() {
        if (this.navigation.isDone()) {
            Vec3 away = awayPos();
            if (away != null) {
                this.navigation.moveTo(away.x, away.y, away.z, this.speedModifier);
            }
        }
    }

    @Nullable
    private Vec3 awayPos() {
        Vec3 from = new Vec3(beetle.getLastThreatX(), beetle.getY(), beetle.getLastThreatZ());
        Vec3 away = DefaultRandomPos.getPosAway(beetle, 12, 6, from);
        if (away == null || from.distanceToSqr(away) < from.distanceToSqr(beetle.position())) {
            return null;
        }
        return away;
    }
}
