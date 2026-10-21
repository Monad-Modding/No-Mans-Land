package com.farcr.nomansland.common.entity.centipede;

import com.farcr.nomansland.common.registry.NMLSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class CentipedeBurrowGoal extends Goal {
    private enum Phase { SUBMERGING, WATCHING }

    private static final int MIN_BURROW_SEGMENTS = 6;
    private static final double SEE_RANGE = 14.0;
    private static final double LUNGE_RANGE = 5.0;
    private static final double DRIVE_SPEED = 0.25;
    private static final double ARRIVE = 0.03;
    private static final double EMERGE_REACH = 0.7;

    private final Centipede centipede;
    private Phase phase;
    private SurfacePathfinder.Cell startCell;
    private SurfacePathfinder.Cell emergeCell;
    private final Vec3[] waypoints = new Vec3[3];
    private int wpIndex;
    private Vec3 watchPos;
    private int watchTicks;
    @Nullable
    private LivingEntity pounceTarget;

    public CentipedeBurrowGoal(Centipede centipede) {
        this.centipede = centipede;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.centipede.getTarget() != null) return false;
        if (!this.centipede.canBurrowNow()) return false;
        if (this.centipede.getSegments() < MIN_BURROW_SEGMENTS) return false;
        if (this.centipede.isShriveling() || this.centipede.isBurrowed()) return false;
        if (this.centipede.hurtTime > 0) return false;
        if (this.centipede.getRandom().nextInt(160) != 0) return false;
        return plan();
    }

    @Override
    public boolean canContinueToUse() {
        return this.phase != null && !this.centipede.isShriveling();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    private boolean plan() {
        SurfacePathfinder.Cell start = this.centipede.currentCell();
        if (start == null) return false;
        SurfacePathfinder pf = new SurfacePathfinder(this.centipede.level());
        if (!pf.fullyBuried(start)) return false;
        SurfacePathfinder.Cell emerge = pf.pickReachable(start, 3, 8, 400, this.centipede.getRandom(), c -> pf.tunnelBuried(start, c));
        if (emerge == null) return false;
        this.startCell = start;
        this.emergeCell = emerge;
        Direction face = emerge.face();
        Vec3 normal = new Vec3(face.getStepX(), face.getStepY(), face.getStepZ());
        double hh = this.centipede.getBbHeight() * 0.5;
        this.watchPos = this.centipede.surfaceAnchor(emerge).add(normal.scale(EMERGE_REACH)).subtract(0.0, hh, 0.0);
        this.waypoints[0] = Vec3.atCenterOf(start.block());
        this.waypoints[1] = Vec3.atCenterOf(emerge.block());
        this.waypoints[2] = this.watchPos;
        return true;
    }

    @Override
    public void start() {
        this.phase = Phase.SUBMERGING;
        this.wpIndex = 0;
        this.watchTicks = 0;
        this.pounceTarget = null;
        this.centipede.setBurrowed(true);
        this.centipede.setBurrowPhysics(true);
        if (this.startCell != null) {
            burstEffects(this.centipede.position().add(0.0, this.centipede.getBbHeight() * 0.5, 0.0), this.startCell.block());
        }
    }

    @Override
    public void tick() {
        if (this.centipede.hurtTime > 0) {
            emerge(false);
            return;
        }
        if (this.phase == Phase.SUBMERGING) {
            Vec3 wp = this.waypoints[this.wpIndex];
            faceTravel(wp.subtract(this.centipede.position()));
            drive(wp);
            digEffects();
            if (this.centipede.position().distanceToSqr(wp) <= ARRIVE) {
                this.wpIndex++;
                if (this.wpIndex >= this.waypoints.length) {
                    this.phase = Phase.WATCHING;
                    burstEffects(this.watchPos.add(0.0, this.centipede.getBbHeight() * 0.5, 0.0), this.emergeCell.block());
                } else if (this.wpIndex == 1) {
                    this.centipede.attachToCell(this.emergeCell);
                }
            }
            return;
        }

        this.centipede.setPos(this.watchPos.x, this.watchPos.y, this.watchPos.z);
        this.centipede.setDeltaMovement(Vec3.ZERO);
        this.watchTicks++;
        LivingEntity seen = visibleTarget();
        int patience = 900 + this.centipede.getSegments() * 120;
        if (seen != null && !this.centipede.frightenedByHeld(seen)) {
            float bodyYaw = this.centipede.yBodyRot;
            float dy = Mth.wrapDegrees(this.centipede.yHeadRot - bodyYaw);
            this.centipede.yHeadRot = bodyYaw + Mth.clamp(dy, -75.0F, 75.0F);
            this.centipede.setXRot(Mth.clamp(this.centipede.getXRot(), -60.0F, 60.0F));
            this.centipede.getLookControl().setLookAt(seen, 40.0F, 40.0F);
            if (this.watchPos.distanceToSqr(seen.getX(), seen.getY(), seen.getZ()) <= LUNGE_RANGE * LUNGE_RANGE) {
                this.pounceTarget = seen;
                emerge(true);
            }
        } else if (this.watchTicks >= patience) {
            emerge(false);
        }
    }

    private void emerge(boolean pounce) {
        this.centipede.setPos(this.watchPos.x, this.watchPos.y, this.watchPos.z);
        this.centipede.setBurrowPhysics(false);
        this.centipede.setBurrowed(false);
        this.centipede.attachToCell(this.emergeCell);
        if (pounce && this.pounceTarget != null && this.pounceTarget.isAlive()) {
            burstEffects(this.centipede.position().add(0.0, this.centipede.getBbHeight() * 0.5, 0.0), this.emergeCell.block());
            this.centipede.setTarget(this.pounceTarget);
            double dx = this.pounceTarget.getX() - this.centipede.getX();
            double dy = this.pounceTarget.getY() - this.centipede.getY();
            double dz = this.pounceTarget.getZ() - this.centipede.getZ();
            this.centipede.requestLunge(dx, dy, dz);
        }
        this.phase = null;
    }

    private void digEffects() {
        if (!(this.centipede.level() instanceof ServerLevel server)) return;
        Vec3 p = this.centipede.position();
        double cy = p.y + this.centipede.getBbHeight() * 0.5;
        BlockPos head = BlockPos.containing(p.x, cy, p.z);
        BlockState state = server.getBlockState(head);
        if (state.getCollisionShape(server, head).isEmpty()) return;
        server.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state), p.x, cy, p.z, 5, 0.25, 0.25, 0.25, 0.02);
        if (this.centipede.tickCount % 4 == 0) {
            SoundType type = state.getSoundType();
            server.playSound(null, head, type.getHitSound(), SoundSource.HOSTILE, 0.7F, type.getPitch() * 0.75F);
        }
    }

    private void burstEffects(Vec3 at, BlockPos solid) {
        if (this.centipede.level() instanceof ServerLevel server) {
            BlockState state = server.getBlockState(solid);
            if (!state.getCollisionShape(server, solid).isEmpty()) {
                server.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state), at.x, at.y, at.z, 26, 0.3, 0.3, 0.3, 0.1);
            }
        }
        this.centipede.playSound(NMLSounds.CENTIPEDE_HISS.get(), 0.8F, 0.7F + this.centipede.getRandom().nextFloat() * 0.2F);
    }

    private void drive(Vec3 target) {
        Vec3 cur = this.centipede.position();
        Vec3 d = target.subtract(cur);
        double dist = d.length();
        if (dist > 1.0E-4) {
            double stepLen = Math.min(DRIVE_SPEED, dist);
            Vec3 next = cur.add(d.scale(stepLen / dist));
            this.centipede.setPos(next.x, next.y, next.z);
        }
        this.centipede.setDeltaMovement(Vec3.ZERO);
    }

    private void faceTravel(Vec3 v) {
        if (v.x * v.x + v.z * v.z < 1.0E-6) return;
        float yaw = (float) (Mth.atan2(-v.x, v.z) * Mth.RAD_TO_DEG);
        this.centipede.setYRot(yaw);
        this.centipede.yBodyRot = yaw;
        this.centipede.yHeadRot = yaw;
    }

    @Nullable
    private LivingEntity visibleTarget() {
        LivingEntity target = this.centipede.getTarget();
        if (target != null && target.isAlive() && this.centipede.hasLineOfSight(target)) {
            return target;
        }
        LivingEntity best = null;
        double bestSqr = Double.MAX_VALUE;
        Player player = this.centipede.level().getNearestPlayer(this.watchPos.x, this.watchPos.y, this.watchPos.z, SEE_RANGE, false);
        if (player != null && Centipede.canTargetPlayer(player) && this.centipede.hasLineOfSight(player)) {
            best = player;
            bestSqr = this.watchPos.distanceToSqr(player.getX(), player.getY(), player.getZ());
        }
        AABB area = new AABB(this.watchPos.x - SEE_RANGE, this.watchPos.y - SEE_RANGE, this.watchPos.z - SEE_RANGE,
                this.watchPos.x + SEE_RANGE, this.watchPos.y + SEE_RANGE, this.watchPos.z + SEE_RANGE);
        for (LivingEntity entity : this.centipede.level().getEntitiesOfClass(LivingEntity.class, area, Centipede::isPrey)) {
            double d = this.watchPos.distanceToSqr(entity.getX(), entity.getY(), entity.getZ());
            if (d < bestSqr && d <= SEE_RANGE * SEE_RANGE && this.centipede.hasLineOfSight(entity)) {
                best = entity;
                bestSqr = d;
            }
        }
        return best;
    }

    @Override
    public void stop() {
        this.centipede.setBurrowPhysics(false);
        this.centipede.setBurrowed(false);
        this.centipede.resetBurrowCooldown();
        this.phase = null;
        this.pounceTarget = null;
    }
}
