package com.farcr.nomansland.common.entity.beetle;

import com.farcr.nomansland.common.registry.entities.NMLEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

public class BeetlePushDungGoal extends Goal {
    private final Beetle beetle;
    private final double speedModifier;
    private DungBall ball;
    private int repathTimer;

    public BeetlePushDungGoal(Beetle beetle, double speedModifier) {
        this.beetle = beetle;
        this.speedModifier = speedModifier;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (beetle.isIncapacitated()) return false;
        DungBall current = beetle.getDungBall();
        if (current != null) {
            ball = current;
            return true;
        }
        DungBall reclaimable = findReclaimable();
        if (reclaimable != null) {
            reclaimable.setOwner(beetle);
            beetle.setDungBall(reclaimable);
            ball = reclaimable;
            return true;
        }
        if (beetle.readyForDungBall() && beetle.getRandom().nextInt(80) == 0) {
            ball = spawnBall();
            return ball != null;
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return ball != null && ball.isAlive() && !beetle.isIncapacitated() && beetle.getDungBall() == ball;
    }

    @Override
    public void start() {
        beetle.setState(Beetle.STATE_PUSHING);
        repathTimer = 0;
    }

    @Override
    public void stop() {
        ball = null;
        beetle.resetBotherTimer();
        if (beetle.getState() == Beetle.STATE_PUSHING) {
            beetle.setState(Beetle.STATE_IDLE);
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (ball == null) return;

        if (beetle.getRandom().nextInt(800) == 0
                || (ball.getSize() >= DungBall.MAX_SIZE && beetle.getRandom().nextInt(200) == 0)) {
            beetle.abandonDungBall();
            return;
        }

        if (--repathTimer <= 0 || beetle.getNavigation().isDone()) {
            repathTimer = 70 + beetle.getRandom().nextInt(60);
            Vec3 wander = DefaultRandomPos.getPos(beetle, 9, 7);
            if (wander != null) {
                beetle.getNavigation().moveTo(wander.x, wander.y, wander.z, speedModifier);
            }
        }

        Vec3 forward = Vec3.directionFromRotation(0.0F, beetle.yBodyRot);
        double distance = beetle.getBbWidth() / 2.0 + ball.getRadius() + 0.1;
        Vec3 desired = beetle.position().add(forward.x * distance, 0.0, forward.z * distance);
        ball.setPushTarget(desired);
        ball.markPushed();
        beetle.getLookControl().setLookAt(ball.getX(), ball.getEyeY(), ball.getZ());
    }

    private DungBall findReclaimable() {
        List<DungBall> balls = beetle.level().getEntitiesOfClass(DungBall.class, beetle.getBoundingBox().inflate(8.0));
        DungBall best = null;
        double bestDistance = Double.MAX_VALUE;
        for (DungBall candidate : balls) {
            if (candidate.isAlive() && candidate.isReclaimable()) {
                double distance = beetle.distanceToSqr(candidate);
                if (distance < bestDistance) {
                    bestDistance = distance;
                    best = candidate;
                }
            }
        }
        return best;
    }

    private DungBall spawnBall() {
        Vec3 forward = Vec3.directionFromRotation(0.0F, beetle.yBodyRot);
        double distance = beetle.getBbWidth() / 2.0 + 0.4;
        BlockPos spawnPos = BlockPos.containing(beetle.getX() + forward.x * distance, beetle.getY(), beetle.getZ() + forward.z * distance);
        if (!beetle.level().getBlockState(spawnPos).getCollisionShape(beetle.level(), spawnPos).isEmpty()) {
            spawnPos = beetle.blockPosition();
        }
        DungBall newBall = NMLEntities.DUNG_BALL.get().create(beetle.level());
        if (newBall == null) return null;
        newBall.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, beetle.getYRot(), 0.0F);
        newBall.setOwner(beetle);
        beetle.level().addFreshEntity(newBall);
        beetle.setDungBall(newBall);
        return newBall;
    }
}
