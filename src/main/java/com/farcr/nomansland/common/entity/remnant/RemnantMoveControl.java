package com.farcr.nomansland.common.entity.remnant;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.phys.Vec3;

public class RemnantMoveControl extends MoveControl {

    private static final double PHASED_SPEED = 0.2;

    private final Remnant remnant;

    public RemnantMoveControl(Remnant remnant) {
        super(remnant);
        this.remnant = remnant;
    }

    @Override
    public void tick() {
        if (!this.remnant.isPhased()) {
            if (!this.remnant.isFaceLocked()) {
                super.tick();
            }
            return;
        }
        if (this.remnant.isScriptedMotion()) return;
        if (this.operation == Operation.MOVE_TO) {
            Vec3 delta = new Vec3(this.wantedX - this.remnant.getX(), this.wantedY - this.remnant.getY(), this.wantedZ - this.remnant.getZ());
            double distance = delta.length();
            if (distance < 0.05) {
                this.operation = Operation.WAIT;
                this.remnant.setDeltaMovement(this.remnant.getDeltaMovement().scale(0.4));
                return;
            }
            double speed = PHASED_SPEED * this.speedModifier;
            Vec3 desired = delta.scale(Math.min(speed, distance) / distance);
            this.remnant.setDeltaMovement(this.remnant.getDeltaMovement().scale(0.35).add(desired.scale(0.65)));
            if (desired.horizontalDistanceSqr() > 1.0E-6 && !this.remnant.isFaceLocked()) {
                float yaw = (float) (Mth.atan2(desired.z, desired.x) * (180.0F / (float) Math.PI)) - 90.0F;
                this.remnant.setYRot(this.rotlerp(this.remnant.getYRot(), yaw, 35.0F));
                this.remnant.yBodyRot = this.remnant.getYRot();
            }
        } else {
            this.remnant.setDeltaMovement(this.remnant.getDeltaMovement().scale(0.55));
        }
    }
}
