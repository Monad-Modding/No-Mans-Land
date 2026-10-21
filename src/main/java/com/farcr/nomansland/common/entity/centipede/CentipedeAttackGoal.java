package com.farcr.nomansland.common.entity.centipede;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class CentipedeAttackGoal extends Goal {
    private final Centipede centipede;
    private int attackCooldown;
    private int lungeCooldown;

    public CentipedeAttackGoal(Centipede centipede) {
        this.centipede = centipede;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.centipede.getTarget();
        return target != null && target.isAlive() && !this.centipede.isBurrowed() && !this.centipede.isShriveling();
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = this.centipede.getTarget();
        if (target == null || this.centipede.isFleeing()) return;

        this.centipede.getLookControl().setLookAt(target, 30.0F, 30.0F);
        if (this.attackCooldown > 0) this.attackCooldown--;
        if (this.lungeCooldown > 0) this.lungeCooldown--;

        double distSqr = this.centipede.distanceToSqr(target);
        double reach = this.centipede.getBbWidth() * 2.0 * this.centipede.getBbWidth() * 2.0 + target.getBbWidth();

        if (distSqr <= reach) {
            if (this.attackCooldown <= 0) {
                this.attackCooldown = 20;
                this.centipede.doHurtTarget(target);
            }
            return;
        }

        if (this.lungeCooldown <= 0 && this.centipede.onGround() && this.centipede.getRandom().nextInt(50) == 0) {
            if (lungeTowards(target)) {
                this.lungeCooldown = 80;
            }
        }
    }

    private boolean lungeTowards(LivingEntity target) {
        Vec3 vel = target.getKnownMovement();
        Vec3 lead = target.position().add(vel.x * 6.0, 0.0, vel.z * 6.0);
        double dx = lead.x - this.centipede.getX();
        double dz = lead.z - this.centipede.getZ();
        double h = Math.sqrt(dx * dx + dz * dz);
        if (h < 1.0E-4) return false;
        double ahead = Math.min(3.0, h);
        double lx = this.centipede.getX() + dx / h * ahead;
        double lz = this.centipede.getZ() + dz / h * ahead;
        if (!hasGroundBelow(lx, this.centipede.getY() + 0.5, lz)) return false;
        return this.centipede.requestLunge(dx, target.getY() - this.centipede.getY(), dz);
    }

    private boolean hasGroundBelow(double x, double y, double z) {
        Level level = this.centipede.level();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(Mth.floor(x), Mth.floor(y), Mth.floor(z));
        for (int d = 0; d < 4; d++) {
            if (!level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()) return true;
            pos.move(0, -1, 0);
        }
        return false;
    }
}
