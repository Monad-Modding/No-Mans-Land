package com.farcr.nomansland.common.entity.cervidae.moose;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.joml.Vector3d;

/**
 * Copy of Malum's CultistMoveControl, which is a copy of Cappin's MoveControl.
 * Offers better movement interpolation and more precise body look control.
 */
public class MooseMoveControl extends MoveControl {

    public enum BodyDirection {
        DEFAULT,
        FACE_TARGET,
        DISABLED
    }

    public final Moose moose;

    public BodyDirection direction = BodyDirection.DEFAULT;
    public Entity target;
    public int strafeAdjustmentLimiter;

    public MooseMoveControl(Moose moose) {
        super(moose);
        this.moose = moose;
    }

    public void replaceBodyDirection(BodyDirection direction) {
        this.direction = direction;
    }

    @Override
    public void tick() {
        var movementData = moose.getMovementData();
        var movementDirection = movementData.getMovementDirection();
        double baseSpeed = moose.getAttributeValue(Attributes.MOVEMENT_SPEED);
        float speed = (float) (speedModifier * baseSpeed);
        float mooseSpeed = movementData.getMotionLength();
        float delta = mooseSpeed / speed;
        float driftRemoval = delta * 0.5f;
        float acceleration = Mth.clamp(0.05f + driftRemoval, 0, 1) * speed;

        movementDirection.zero();
        switch (operation) {
            case MOVE_TO -> {
                var wanted = getWantedPosition();
                double x = wanted.x - moose.getX();
                double y = wanted.y - moose.getY();
                double z = wanted.z - moose.getZ();
                var trajectory = getMovementTrajectory(x, y, z);
                var direction = trajectory.normalize();
                movementDirection.set(direction);
                tryJump(x, y, z);
                rotateBody();
            }
            case STRAFE -> {
                float forward = strafeForwards;
                float right = strafeRight;

                if (forward == 0 && right == 0) {
                    operation = Operation.WAIT;
                    break;
                }
                var forwards = new Vec3(
                        Math.cos(moose.yBodyRot * Mth.DEG_TO_RAD + Mth.HALF_PI), 0,
                        Math.sin(moose.yBodyRot * Mth.DEG_TO_RAD + Mth.HALF_PI)
                );
                var side = forwards.cross(new Vec3(0, 1, 0)).normalize();
                var xForward = new MutableDouble(forwards.x * forward);
                var zForward = new MutableDouble(forwards.z * forward);
                var xSide = new MutableDouble(side.x * right);
                var zSide = new MutableDouble(side.z * right);
                adjustStrafing(xForward, zForward, xSide, zSide);
                double x = xForward.getValue() + xSide.getValue();
                double z = zForward.getValue() + zSide.getValue();
                movementDirection.set(x, 0, z);
            }
            case WAIT -> {
                acceleration = 0.1f;
                moose.setXxa(0.0F);
                moose.setZza(0.0F);
            }
        }

        movementData.interpolate(speed, acceleration);
        resetValues();
    }

    public Vector3d getWantedPosition() {
        return new Vector3d(wantedX, wantedY, wantedZ);
    }

    public Vector3d getMovementTrajectory(double x, double y, double z) {
        return new Vector3d(x, 0, z);
    }

    public void resetValues() {
        if (operation != Operation.JUMPING || mob.onGround()) {
            operation = Operation.WAIT;
        }
        if (operation != Operation.STRAFE) {
            strafeForwards = 0;
            strafeRight = 0;
        }
        if (strafeAdjustmentLimiter < 0) {
            strafeAdjustmentLimiter++;
        }
        direction = BodyDirection.DEFAULT;
    }

    //The Moose doesn't strafe but still
    public void adjustStrafing(MutableDouble xForward, MutableDouble zForward, MutableDouble xSide, MutableDouble zSide) {
        if (strafeAdjustmentLimiter > 40) {
            strafeAdjustmentLimiter = -20;
        }
        if (strafeAdjustmentLimiter < 0) {
            return;
        }
        var fx = xForward.getValue();
        var fz = zForward.getValue();
        var sx = xSide.getValue();
        var sz = zSide.getValue();

        if (!isWalkable(fx, 0)) {
            xForward.setValue(0);
            strafeForwards *= -1;
            strafeAdjustmentLimiter++;
        }
        if (!isWalkable(0, fz)) {
            zForward.setValue(0);
            strafeForwards *= -1;
            strafeAdjustmentLimiter++;
        }
        if (!isWalkable(sx, 0)) {
            xSide.setValue(0);
            strafeRight *= -1;
            strafeAdjustmentLimiter++;
        }
        if (!isWalkable(0, sz)) {
            zSide.setValue(0);
            strafeRight *= -1;
            strafeAdjustmentLimiter++;
        }
    }

    public void rotateBody() {
        if (direction.equals(BodyDirection.DISABLED)) {
            return;
        }
        var target = this.target != null ? this.target : moose.getTarget();
        if (direction.equals(BodyDirection.FACE_TARGET) && target != null) {
            moose.faceTarget(target);
            return;
        }
        double xDiff = wantedX - moose.getX();
        double zDiff = wantedZ - moose.getZ();
        if ((xDiff * xDiff + zDiff * zDiff) > 0.05f) {
            float movementAngle = (float) (Mth.atan2(zDiff, xDiff) * 180.0F / (float) Math.PI) - 90.0F;
            moose.setYRot(rotlerp(moose.getYRot(), movementAngle, 15.0F));
        }
    }

    public void tryJump(double xDiff, double yDiff, double zDiff) {
        var blockpos = moose.blockPosition();
        var blockstate = moose.level().getBlockState(blockpos);
        var voxelshape = blockstate.getCollisionShape(moose.level(), blockpos);
        if (yDiff > (double) moose.maxUpStep() && xDiff * xDiff + zDiff * zDiff < (double) Math.max(1.0F, moose.getBbWidth())
                || !voxelshape.isEmpty()
                && moose.getY() < voxelshape.max(Direction.Axis.Y) + (double) blockpos.getY()
                && !blockstate.is(BlockTags.DOORS)
                && !blockstate.is(BlockTags.FENCES)) {
            moose.getJumpControl().jump();
            operation = Operation.JUMPING;
        }
    }

    @Override
    public float rotlerp(float sourceAngle, float targetAngle, float maximumChange) {
        return super.rotlerp(sourceAngle, targetAngle, maximumChange);
    }

    public boolean isWalkable(double relativeX, double relativeZ) {
        var navigation = moose.getNavigation();
        var node = navigation.getNodeEvaluator();
        var pathType = node.getPathType(moose, BlockPos.containing(
                moose.getX() + relativeX,
                moose.getBlockY(),
                moose.getZ() + relativeZ)
        );
        return pathType == PathType.WALKABLE;
    }
}