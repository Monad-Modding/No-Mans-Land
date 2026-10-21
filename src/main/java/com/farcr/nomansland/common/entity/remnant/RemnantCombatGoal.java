package com.farcr.nomansland.common.entity.remnant;

import com.farcr.nomansland.common.registry.NMLSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class RemnantCombatGoal extends Goal {

    private enum Stage {
        CHOOSE, APPROACH, RETREAT, WALL_WAIT, RELOCATE,
        TIP_WINDUP, TIP_RUSH, TIP_RECOVER, SWAT,
        LUNGE_TELEGRAPH, LUNGE_FLY, EXPOSED, GET_UP,
        POKE_OUT, POKE_BACK,
        DIVE, FLOOR_SWIM, ERUPT, CEILING_DROP, STUNNED, WHIRLPOOL, EMERGE, COMBAT_PEEK
    }

    private final Remnant remnant;
    private Stage stage = Stage.CHOOSE;
    private int stageTicks;
    private int tipCooldown;
    private int swatCooldown;
    private int lungeCooldown;
    private int pokeCooldown;
    private int ambushCooldown;
    private int relocateCooldown;
    private int waitDuration;
    private boolean attackHit;
    private boolean preferStone;
    private double whirlSinkY;
    private BlockPos embedFeet;
    private Direction embedFacing;
    private Vec3 embedPos;
    private Vec3 retreatFront;
    private RemnantPhasing.PerchSpot retreatSpot;
    private RemnantPhasing.PerchSpot relocateSpot;
    private Vec3 rushDirection = Vec3.ZERO;
    private Vec3 pokePoint = Vec3.ZERO;
    private Vec3 whirlCenter = Vec3.ZERO;
    private float whirlAngle;
    private double whirlY;
    private Vec3 emergeTarget = Vec3.ZERO;
    private double diveTargetY;
    private BlockPos ceilingSpot;
    private boolean ceilingApproach;
    private int stunDuration = 22;
    private Vec3 lastKnownPos = Vec3.ZERO;
    private int lastKnownTick;
    private Vec3 swimDestination = Vec3.ZERO;
    private Vec3 peekAnchor = Vec3.ZERO;
    private Vec3 peekPos = Vec3.ZERO;
    private boolean peekFromSwim;
    private boolean peekAcquired;
    private int combatPeekCooldown;

    public RemnantCombatGoal(Remnant remnant) {
        this.remnant = remnant;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.remnant.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = this.remnant.getTarget();
        if (target == null || !target.isAlive()) return false;
        if (target instanceof Player player && (player.isCreative() || player.isSpectator())) {
            this.remnant.setTarget(null);
            return false;
        }
        if (this.remnant.distanceToSqr(target) > 2500.0) {
            this.remnant.setTarget(null);
            return false;
        }
        return true;
    }

    @Override
    public void start() {
        this.enter(Stage.CHOOSE);
        this.tipCooldown = 8;
        this.swatCooldown = 0;
        this.lungeCooldown = 12;
        this.pokeCooldown = 0;
        this.ambushCooldown = 60;
        this.relocateCooldown = 0;
        this.combatPeekCooldown = 0;
        this.embedFeet = null;
        this.embedFacing = null;
        this.embedPos = null;
        LivingEntity target = this.remnant.getTarget();
        if (target != null) {
            this.lastKnownPos = target.position();
            this.lastKnownTick = this.remnant.tickCount;
            this.swimDestination = this.lastKnownPos;
        }
    }

    @Override
    public void stop() {
        this.remnant.getNavigation().stop();
        this.remnant.setScriptedPhase(false);
        this.remnant.setScriptedMotion(false);
        this.remnant.releaseSunkVictim();
        this.remnant.consumeRetreatImpulse();
        this.remnant.consumeAmbushPoke();
        this.preferStone = false;
        this.remnant.setRemnantState(this.remnant.isPhased() ? Remnant.RemnantState.WALL_HIDDEN : Remnant.RemnantState.IDLE);
    }

    @Override
    public void tick() {
        LivingEntity target = this.remnant.getTarget();
        if (target == null) return;
        this.stageTicks++;
        if (this.tipCooldown > 0) this.tipCooldown--;
        if (this.swatCooldown > 0) this.swatCooldown--;
        if (this.lungeCooldown > 0) this.lungeCooldown--;
        if (this.pokeCooldown > 0) this.pokeCooldown--;
        if (this.ambushCooldown > 0) this.ambushCooldown--;
        if (this.relocateCooldown > 0) this.relocateCooldown--;
        if (this.combatPeekCooldown > 0) this.combatPeekCooldown--;
        this.updateMemory(target);
        switch (this.stage) {
            case CHOOSE -> this.choose(target);
            case APPROACH -> this.approach(target);
            case RETREAT -> this.retreat(target);
            case WALL_WAIT -> this.wallWait(target);
            case RELOCATE -> this.relocate(target);
            case TIP_WINDUP -> this.tipWindup(target);
            case TIP_RUSH -> this.tipRush(target);
            case TIP_RECOVER -> this.tipRecover(target);
            case SWAT -> this.swat(target);
            case LUNGE_TELEGRAPH -> this.lungeTelegraph(target);
            case LUNGE_FLY -> this.lungeFly(target);
            case EXPOSED -> this.exposed();
            case GET_UP -> this.getUp();
            case POKE_OUT -> this.pokeOut(target);
            case POKE_BACK -> this.pokeBack(target);
            case DIVE -> this.dive(target);
            case FLOOR_SWIM -> this.floorSwim(target);
            case ERUPT -> this.erupt(target);
            case CEILING_DROP -> this.ceilingDrop(target);
            case COMBAT_PEEK -> this.combatPeek(target);
            case STUNNED -> this.stunned();
            case WHIRLPOOL -> this.whirlpool(target);
            case EMERGE -> this.emerge();
        }
    }

    private void enter(Stage stage) {
        this.stage = stage;
        this.stageTicks = 0;
        this.attackHit = false;
    }

    private void updateMemory(LivingEntity target) {
        if (!this.remnant.isPhased() && this.remnant.getSensing().hasLineOfSight(target)) {
            this.lastKnownPos = target.position();
            this.lastKnownTick = this.remnant.tickCount;
        } else if (this.remnant.distanceTo(target) < 2.5) {
            this.lastKnownPos = target.position();
            this.lastKnownTick = this.remnant.tickCount;
        }
    }

    private boolean memoryFresh(int maxAge) {
        return this.remnant.tickCount - this.lastKnownTick <= maxAge;
    }

    private double aimDistance() {
        return this.remnant.position().distanceTo(this.lastKnownPos);
    }

    private void facePosition(Vec3 pos) {
        this.remnant.getLookControl().setLookAt(pos.x, pos.y + 1.0, pos.z);
        this.faceDirection(pos.subtract(this.remnant.position()));
    }

    private void choose(LivingEntity target) {
        double dist = this.remnant.distanceTo(target);
        if (this.remnant.consumeAmbushPoke() && this.remnant.isPhased() && dist < 3.5) {
            this.anchorEmbed();
            this.startPoke(target);
            return;
        }
        if (this.remnant.isPhased()) {
            if (this.embedPos == null) this.anchorEmbed();
            if (dist <= 2.4 && this.pokeCooldown <= 0) {
                this.startPoke(target);
                return;
            }
            double aimDist = this.aimDistance();
            if (this.lungeCooldown <= 0 && this.canLungeAt(this.lastKnownPos, aimDist)) {
                if (this.memoryFresh(30)) {
                    this.startLunge();
                    return;
                }
                if (this.combatPeekCooldown <= 0) {
                    this.startCombatPeek(false);
                    return;
                }
            }
            if (aimDist > 18.0 && this.ambushCooldown <= 0) {
                this.startFloorSwim(target);
                return;
            }
            if (this.relocateCooldown <= 0 && this.remnant.getRandom().nextFloat() < 0.65F && this.startRelocate(target)) return;
            this.startWallWait(20 + this.remnant.getRandom().nextInt(40));
            return;
        }
        boolean canDive = this.ambushCooldown <= 0 && this.canDiveHere();
        boolean exposed = this.remnant.avoidsSky() && RemnantPhasing.isSkyExposed(this.remnant.level(), this.remnant.blockPosition());
        if (this.remnant.consumeRetreatImpulse() && this.startRetreat(target)) return;
        if (dist <= 2.1 && this.swatCooldown <= 0) {
            this.startSwat();
            return;
        }
        if ((this.preferStone || exposed) && dist > 2.5) {
            this.preferStone = false;
            if (this.startRetreat(target)) return;
            if (this.relocateCooldown <= 0 && this.startRelocate(target)) return;
            if (canDive) {
                this.startDive();
                return;
            }
        }
        this.preferStone = false;
        if (dist >= 1.2 && dist <= 5.5 && this.tipCooldown <= 0 && this.remnant.onGround()) {
            this.startTip();
            return;
        }
        if (dist > 6.0) {
            if (this.relocateCooldown <= 0 && this.startRelocate(target)) return;
            if (canDive) {
                this.startDive();
                return;
            }
        }
        this.enter(Stage.APPROACH);
    }

    private void approach(LivingEntity target) {
        this.remnant.setRemnantState(this.remnant.isPhased() ? Remnant.RemnantState.PHASE_SWIM : Remnant.RemnantState.IDLE);
        this.remnant.getLookControl().setLookAt(target, 30.0F, 30.0F);
        if (this.stageTicks % 8 == 1) {
            this.remnant.getNavigation().moveTo(target, 1.0);
        }
        double dist = this.remnant.distanceTo(target);
        if (this.remnant.consumeRetreatImpulse() && this.startRetreat(target)) return;
        if (dist <= 2.1 && this.swatCooldown <= 0) {
            this.startSwat();
            return;
        }
        if (dist >= 1.2 && dist <= 5.0 && this.tipCooldown <= 0 && this.remnant.onGround()) {
            this.startTip();
            return;
        }
        boolean canDive = this.ambushCooldown <= 0 && this.canDiveHere();
        if (dist > 7.0) {
            if (this.relocateCooldown <= 0 && this.startRelocate(target)) return;
            if (canDive) {
                this.startDive();
                return;
            }
        }
        if (this.stageTicks % 30 == 29) {
            this.enter(Stage.CHOOSE);
            return;
        }
        if (this.stageTicks > 20 && this.remnant.getNavigation().isDone() && dist > 2.5) {
            if (this.relocateCooldown <= 0 && this.startRelocate(target)) return;
            if (canDive) {
                this.startDive();
                return;
            }
            this.enter(Stage.CHOOSE);
        }
    }

    private boolean startRetreat(LivingEntity target) {
        RemnantPhasing.PerchSpot spot = RemnantPhasing.findRetreatPerch(this.remnant.level(), this.remnant, this.lastKnownPos);
        if (spot == null) return false;
        Vec3 front = RemnantPhasing.frontPosition(spot.feet(), spot.facing());
        Vec3 origin = this.remnant.position().add(0.0, 0.9, 0.0);
        Vec3 destination = front.add(0.0, 0.9, 0.0);
        HitResult hit = this.remnant.level().clip(new ClipContext(origin, destination, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this.remnant));
        if (hit.getType() != HitResult.Type.MISS) return false;
        this.retreatSpot = spot;
        this.retreatFront = front;
        this.remnant.getNavigation().stop();
        this.enter(Stage.RETREAT);
        this.remnant.setRemnantState(Remnant.RemnantState.RETREAT);
        return true;
    }

    private void retreat(LivingEntity target) {
        this.remnant.setFaceLocked(true);
        this.faceTarget(target);
        if (this.remnant.distanceTo(target) <= 2.1 && this.swatCooldown <= 0) {
            this.startSwat();
            return;
        }
        Vec3 toSpot = this.retreatFront.subtract(this.remnant.position());
        if (toSpot.horizontalDistance() < 0.45) {
            this.embedFeet = this.retreatSpot.feet();
            this.embedFacing = this.retreatSpot.facing();
            this.embedPos = RemnantPhasing.hiddenPosition(this.embedFeet);
            this.remnant.setScriptedPhase(true);
            this.remnant.playSound(NMLSounds.REMNANT_DIVE.get(), 0.8F, 1.2F);
            this.startWallWait(15 + this.remnant.getRandom().nextInt(40));
            return;
        }
        Vec3 step = toSpot.normalize().scale(0.14);
        this.remnant.setDeltaMovement(step.x, this.remnant.getDeltaMovement().y, step.z);
        if (this.stageTicks > 70) this.enter(Stage.CHOOSE);
    }

    private void startWallWait(int duration) {
        this.waitDuration = duration;
        this.enter(Stage.WALL_WAIT);
        this.remnant.setRemnantState(Remnant.RemnantState.WALL_HIDDEN);
    }

    private void wallWait(LivingEntity target) {
        if (this.embedPos == null) this.anchorEmbed();
        this.remnant.setFaceLocked(true);
        this.holdEmbed();
        this.facePosition(this.lastKnownPos);
        double dist = this.remnant.distanceTo(target);
        if (dist <= 2.4 && this.pokeCooldown <= 0) {
            this.startPoke(target);
            return;
        }
        if (dist <= 2.3 && this.swatCooldown <= 0) {
            this.startSwat();
            return;
        }
        double aimDist = this.aimDistance();
        if (this.lungeCooldown <= 0 && this.canLungeAt(this.lastKnownPos, aimDist)) {
            if (this.memoryFresh(30)) {
                if (this.isLookingAway(target) || this.stageTicks > this.waitDuration / 2) {
                    this.startLunge();
                    return;
                }
            } else if (this.combatPeekCooldown <= 0 && this.stageTicks > 8) {
                this.startCombatPeek(false);
                return;
            }
        }
        if (aimDist > 18.0 && this.ambushCooldown <= 0) {
            this.startFloorSwim(target);
            return;
        }
        if (this.stageTicks > this.waitDuration) {
            if (this.relocateCooldown <= 0 && this.remnant.getRandom().nextFloat() < 0.6F && this.startRelocate(target)) return;
            if (this.lungeCooldown <= 0 && this.memoryFresh(30) && this.canLungeAt(this.lastKnownPos, aimDist)) {
                this.startLunge();
                return;
            }
            if (this.combatPeekCooldown <= 0 && !this.memoryFresh(40)) {
                this.startCombatPeek(false);
                return;
            }
            this.startEmergeNear(this.embedPos != null ? this.embedPos : this.remnant.position());
        }
    }

    private boolean startRelocate(LivingEntity target) {
        RemnantPhasing.PerchSpot spot = RemnantPhasing.findAmbushPerch(this.remnant.level(), this.remnant, target, this.lastKnownPos, this.memoryFresh(30), 4.0, 9.0);
        if (spot == null) {
            this.relocateCooldown = 60;
            return false;
        }
        BlockPos feet = spot.feet();
        if (!this.remnant.getNavigation().moveTo(feet.getX() + 0.5, feet.getY(), feet.getZ() + 0.5, 1.0)) {
            this.relocateCooldown = 60;
            return false;
        }
        this.relocateSpot = spot;
        this.remnant.setScriptedPhase(false);
        this.enter(Stage.RELOCATE);
        return true;
    }

    private void relocate(LivingEntity target) {
        this.remnant.setRemnantState(this.remnant.isPhased() ? Remnant.RemnantState.PHASE_SWIM : Remnant.RemnantState.IDLE);
        double dist = this.remnant.distanceTo(target);
        if (dist <= 2.1 && this.swatCooldown <= 0) {
            this.startSwat();
            return;
        }
        if (!this.remnant.isPhased() && dist < 9.0 && this.memoryFresh(30)) {
            this.remnant.setFaceLocked(true);
            this.faceTarget(target);
            Path path = this.remnant.getNavigation().getPath();
            if (path != null && !path.isDone()) {
                Vec3 next = path.getNextEntityPos(this.remnant);
                Vec3 dir = new Vec3(next.x - this.remnant.getX(), 0.0, next.z - this.remnant.getZ());
                if (dir.lengthSqr() > 1.0E-4) {
                    dir = dir.normalize().scale(0.14);
                    this.remnant.setDeltaMovement(dir.x, this.remnant.getDeltaMovement().y, dir.z);
                }
            }
        }
        BlockPos feet = this.relocateSpot.feet();
        if (this.stageTicks % 20 == 0) {
            this.remnant.getNavigation().moveTo(feet.getX() + 0.5, feet.getY(), feet.getZ() + 0.5, 1.0);
        }
        if (this.remnant.position().distanceToSqr(Vec3.atBottomCenterOf(feet)) < 2.0) {
            this.embedFeet = feet;
            this.embedFacing = this.relocateSpot.facing();
            this.embedPos = RemnantPhasing.hiddenPosition(feet);
            this.remnant.getNavigation().stop();
            this.remnant.setScriptedPhase(true);
            this.startWallWait(10 + this.remnant.getRandom().nextInt(30));
            return;
        }
        if (this.stageTicks > 120 || (this.stageTicks > 15 && this.remnant.getNavigation().isDone())) {
            this.relocateCooldown = 80;
            this.enter(Stage.CHOOSE);
        }
    }

    private void startTip() {
        this.remnant.getNavigation().stop();
        this.enter(Stage.TIP_WINDUP);
        this.remnant.setRemnantState(Remnant.RemnantState.TIP_WINDUP);
        this.remnant.playSound(NMLSounds.REMNANT_GRIND.get(), 0.9F, 0.65F);
    }

    private void tipWindup(LivingEntity target) {
        this.faceTarget(target);
        this.remnant.setDeltaMovement(this.remnant.getDeltaMovement().multiply(0.6, 1.0, 0.6));
        if (this.stageTicks >= 14) {
            this.enter(Stage.TIP_RUSH);
            this.remnant.setRemnantState(Remnant.RemnantState.TIP_RUSH);
            Vec3 to = target.position().subtract(this.remnant.position());
            this.rushDirection = new Vec3(to.x, 0.0, to.z).normalize();
        }
    }

    private void tipRush(LivingEntity target) {
        double speed = Math.min(0.30 + 0.05 * this.stageTicks, 0.62);
        this.remnant.setDeltaMovement(this.rushDirection.x * speed, this.remnant.getDeltaMovement().y, this.rushDirection.z * speed);
        this.faceDirection(this.rushDirection);
        if (!this.attackHit && this.intersects(target, 0.3)) {
            this.attackHit = true;
            this.remnant.doHurtTarget(target);
            this.knock(target, 0.5, 0.3);
            this.remnant.playSound(NMLSounds.REMNANT_ATTACK.get(), 1.0F, 1.0F);
            this.startTipRecover();
            return;
        }
        if (this.stageTicks >= 12) this.startTipRecover();
    }

    private void startTipRecover() {
        this.tipCooldown = 50;
        this.enter(Stage.TIP_RECOVER);
        this.remnant.setRemnantState(Remnant.RemnantState.TIP_RECOVER);
    }

    private void tipRecover(LivingEntity target) {
        this.remnant.setDeltaMovement(this.remnant.getDeltaMovement().multiply(0.6, 1.0, 0.6));
        this.remnant.getLookControl().setLookAt(target, 30.0F, 30.0F);
        if (this.stageTicks >= 20 && this.swatCooldown <= 0 && this.remnant.distanceTo(target) <= 2.1) {
            this.startSwat();
            return;
        }
        if (this.stageTicks >= 32) {
            this.preferStone = this.remnant.getRandom().nextFloat() < 0.7F;
            this.enter(Stage.CHOOSE);
        }
    }

    private void startSwat() {
        this.remnant.getNavigation().stop();
        this.enter(Stage.SWAT);
        this.remnant.setRemnantState(Remnant.RemnantState.SWAT);
    }

    private void swat(LivingEntity target) {
        this.faceTarget(target);
        if (this.stageTicks == 5 && this.remnant.distanceTo(target) <= 2.3) {
            this.remnant.doHurtTarget(target);
            this.knock(target, 0.4, 0.25);
            this.remnant.playSound(NMLSounds.REMNANT_ATTACK.get(), 1.0F, 1.1F);
        }
        if (this.stageTicks >= 14) {
            this.swatCooldown = 24;
            this.preferStone = this.remnant.getRandom().nextFloat() < 0.7F;
            this.enter(Stage.CHOOSE);
        }
    }

    private boolean canLungeAt(Vec3 aim, double dist) {
        if (dist < 2.5 || dist > 9.5) return false;
        if (Math.abs(aim.y - this.remnant.getY()) > 2.5) return false;
        Vec3 horizontal = new Vec3(aim.x - this.remnant.getX(), 0.0, aim.z - this.remnant.getZ());
        if (horizontal.lengthSqr() < 1.0E-4) return false;
        Vec3 origin = this.remnant.position().add(horizontal.normalize().scale(1.1)).add(0.0, 1.8, 0.0);
        HitResult hit = this.remnant.level().clip(new ClipContext(origin, aim.add(0.0, 1.5, 0.0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this.remnant));
        return hit.getType() == HitResult.Type.MISS;
    }

    private void startLunge() {
        if (this.embedPos == null) this.anchorEmbed();
        this.remnant.getNavigation().stop();
        this.enter(Stage.LUNGE_TELEGRAPH);
        this.remnant.setRemnantState(Remnant.RemnantState.LUNGE_TELEGRAPH);
        this.remnant.playSound(NMLSounds.REMNANT_GRIND.get(), 1.0F, 0.55F);
    }

    private void lungeTelegraph(LivingEntity target) {
        this.remnant.setFaceLocked(true);
        this.holdEmbed();
        this.facePosition(this.lastKnownPos);
        if (this.stageTicks % 3 == 0 && this.embedFeet != null) {
            this.burstParticles(this.remnant.position().add(0.0, 1.0, 0.0), this.remnant.level().getBlockState(this.embedFeet), 3);
        }
        if (this.stageTicks >= 18) {
            this.enter(Stage.LUNGE_FLY);
            this.remnant.setRemnantState(Remnant.RemnantState.LUNGE_FLY);
            this.remnant.setScriptedMotion(true);
            this.remnant.setScriptedPhase(false);
            this.lungeCooldown = 55;
            Vec3 to = this.lastKnownPos.add(0.0, 0.1, 0.0).subtract(this.remnant.position());
            Vec3 horizontal = new Vec3(to.x, 0.0, to.z);
            double launch = Mth.clamp(0.5 + horizontal.length() * 0.06, 0.6, 1.05);
            Vec3 dir = horizontal.normalize();
            this.remnant.setDeltaMovement(dir.x * launch, 0.28, dir.z * launch);
            this.remnant.playSound(NMLSounds.REMNANT_EMERGE.get(), 1.0F, 1.15F);
            this.burstParticles(this.remnant.position().add(0.0, 1.2, 0.0), this.remnant.level().getBlockState(this.embedFeet != null ? this.embedFeet : this.remnant.blockPosition()), 14);
        }
    }

    private void lungeFly(LivingEntity target) {
        this.faceDirection(this.remnant.getDeltaMovement());
        if (!this.attackHit && this.intersects(target, 0.4)) {
            this.attackHit = true;
            target.hurt(this.remnant.damageSources().mobAttack(this.remnant), 9.0F);
            this.knock(target, 0.7, 0.3);
            this.remnant.playSound(NMLSounds.REMNANT_ATTACK.get(), 1.0F, 0.9F);
        }
        if ((this.stageTicks > 4 && this.remnant.onGround()) || this.remnant.isInLiquid() || this.stageTicks > 40) {
            this.remnant.setScriptedMotion(false);
            this.remnant.playSound(NMLSounds.REMNANT_CRASH.get(), 0.7F, 1.25F);
            this.enter(Stage.EXPOSED);
            this.remnant.setRemnantState(Remnant.RemnantState.EXPOSED);
        }
    }

    private void exposed() {
        this.remnant.setDeltaMovement(this.remnant.getDeltaMovement().multiply(0.5, 1.0, 0.5));
        if (this.stageTicks >= 40) {
            this.enter(Stage.GET_UP);
            this.remnant.setRemnantState(Remnant.RemnantState.GET_UP);
        }
    }

    private void getUp() {
        if (this.stageTicks >= 14) {
            this.preferStone = this.remnant.getRandom().nextFloat() < 0.85F;
            this.enter(Stage.CHOOSE);
        }
    }

    private void startPoke(LivingEntity target) {
        if (this.embedPos == null) this.anchorEmbed();
        Vec3 to = target.position().subtract(this.embedPos);
        Vec3 horizontal = new Vec3(to.x, 0.0, to.z);
        Vec3 dir = horizontal.lengthSqr() < 1.0E-4 ? Vec3.atLowerCornerOf(this.embedFacing == null ? Direction.NORTH.getNormal() : this.embedFacing.getNormal()) : horizontal.normalize();
        this.pokePoint = this.embedPos.add(dir.scale(1.2));
        this.remnant.setScriptedPhase(true);
        this.remnant.getNavigation().stop();
        this.enter(Stage.POKE_OUT);
        this.remnant.setRemnantState(Remnant.RemnantState.POKE);
        this.remnant.playSound(NMLSounds.REMNANT_GRIND.get(), 0.7F, 1.3F);
    }

    private void pokeOut(LivingEntity target) {
        this.remnant.setFaceLocked(true);
        this.remnant.getMoveControl().setWantedPosition(this.pokePoint.x, this.pokePoint.y, this.pokePoint.z, 2.4);
        this.faceTarget(target);
        if (this.stageTicks == 4 && this.remnant.distanceTo(target) <= 2.6) {
            this.remnant.doHurtTarget(target);
            this.knock(target, 0.4, 0.2);
            this.remnant.playSound(NMLSounds.REMNANT_ATTACK.get(), 1.0F, 1.2F);
        }
        if (this.stageTicks >= 6) this.enter(Stage.POKE_BACK);
    }

    private void pokeBack(LivingEntity target) {
        this.remnant.setFaceLocked(true);
        this.remnant.setRemnantState(Remnant.RemnantState.POKE);
        this.remnant.getMoveControl().setWantedPosition(this.embedPos.x, this.embedPos.y, this.embedPos.z, 2.4);
        this.faceTarget(target);
        if (this.stageTicks >= 8 || this.remnant.position().distanceToSqr(this.embedPos) < 0.04) {
            this.pokeCooldown = 22;
            this.startWallWait(14 + this.remnant.getRandom().nextInt(30));
        }
    }

    private boolean canDiveHere() {
        return this.remnant.onGround() && RemnantPhasing.canPhaseThrough(this.remnant.level(), this.remnant.blockPosition().below());
    }

    private void startDive() {
        this.remnant.getNavigation().stop();
        this.enter(Stage.DIVE);
        this.remnant.setRemnantState(Remnant.RemnantState.DIVE);
        this.remnant.setScriptedPhase(true);
        this.diveTargetY = this.remnant.getY() - 2.6;
        this.remnant.playSound(NMLSounds.REMNANT_DIVE.get(), 1.0F, 1.0F);
        this.burstParticles(this.remnant.position(), this.remnant.level().getBlockState(this.remnant.blockPosition().below()), 14);
    }

    private void dive(LivingEntity target) {
        this.remnant.getMoveControl().setWantedPosition(this.remnant.getX(), this.diveTargetY, this.remnant.getZ(), 1.3);
        boolean submerged = !this.remnant.level().getBlockState(BlockPos.containing(this.remnant.getX(), this.remnant.getEyeY(), this.remnant.getZ())).isAir();
        if ((submerged && this.remnant.isPhased() && this.remnant.getY() < this.diveTargetY + 1.0) || this.stageTicks > 30) {
            this.startFloorSwim(target);
        }
    }

    private void startFloorSwim(LivingEntity target) {
        this.remnant.setScriptedPhase(false);
        this.swimDestination = this.lastKnownPos;
        boolean floorOk = RemnantPhasing.isDiveable(this.remnant.level().getBlockState(BlockPos.containing(this.swimDestination.x, this.swimDestination.y - 0.5, this.swimDestination.z)));
        this.ceilingSpot = RemnantPhasing.findCeilingSpot(this.remnant.level(), this.swimDestination, !floorOk);
        this.ceilingApproach = this.ceilingSpot != null && (!floorOk || this.remnant.getRandom().nextFloat() < 0.45F);
        this.enter(Stage.FLOOR_SWIM);
        this.remnant.setRemnantState(Remnant.RemnantState.PHASE_SWIM);
    }

    private void floorSwim(LivingEntity target) {
        if (!this.remnant.isPhased()) {
            this.enter(Stage.CHOOSE);
            return;
        }
        if (this.memoryFresh(10)) {
            this.swimDestination = this.lastKnownPos;
        }
        this.remnant.getLookControl().setLookAt(this.swimDestination.x, this.swimDestination.y + 1.0, this.swimDestination.z);
        if (this.stageTicks % 10 == 1) {
            if (this.ceilingApproach) {
                BlockPos refreshed = RemnantPhasing.findCeilingSpot(this.remnant.level(), this.swimDestination, false);
                if (refreshed != null) this.ceilingSpot = refreshed;
                if (!this.remnant.getNavigation().moveTo(this.ceilingSpot.getX() + 0.5, this.ceilingSpot.getY(), this.ceilingSpot.getZ() + 0.5, 1.0)) {
                    this.ceilingApproach = false;
                }
            }
            if (!this.ceilingApproach) {
                this.remnant.getNavigation().moveTo(this.swimDestination.x, this.swimDestination.y - 2.6, this.swimDestination.z, 1.0);
            }
        }
        double dx = this.swimDestination.x - this.remnant.getX();
        double dz = this.swimDestination.z - this.remnant.getZ();
        double horizontalSqr = dx * dx + dz * dz;
        double dy = this.swimDestination.y - this.remnant.getY();
        if (this.ceilingApproach) {
            if (this.remnant.position().distanceToSqr(Vec3.atBottomCenterOf(this.ceilingSpot)) < 2.5) {
                this.startCeilingDrop(target);
                return;
            }
        } else if (horizontalSqr < 6.8 && dy > 0.3 && dy < 4.8) {
            if (this.memoryFresh(25)) {
                this.chooseEmergence(target);
                return;
            }
            if (this.combatPeekCooldown <= 0) {
                this.startCombatPeek(true);
                return;
            }
            this.startEmergeNear(this.swimDestination);
            return;
        }
        if (this.stageTicks > 160 || (this.stageTicks > 30 && this.remnant.getNavigation().isDone() && horizontalSqr > 49.0)) {
            this.ambushCooldown = Math.max(this.ambushCooldown, 80);
            this.startEmergeNear(this.remnant.position());
        }
    }

    private void startCombatPeek(boolean fromSwim) {
        this.peekFromSwim = fromSwim;
        this.peekAcquired = false;
        this.peekAnchor = this.remnant.position();
        if (fromSwim) {
            this.peekPos = this.peekAnchor.add(0.0, 1.15, 0.0);
        } else {
            Vec3 toward = new Vec3(this.lastKnownPos.x - this.peekAnchor.x, 0.0, this.lastKnownPos.z - this.peekAnchor.z);
            Direction fallback = this.embedFacing == null ? this.remnant.getDirection() : this.embedFacing;
            toward = toward.lengthSqr() < 1.0E-4 ? Vec3.atLowerCornerOf(fallback.getNormal()) : toward.normalize();
            this.peekPos = this.peekAnchor.add(toward.scale(0.8));
        }
        this.remnant.getNavigation().stop();
        this.remnant.setScriptedPhase(true);
        this.enter(Stage.COMBAT_PEEK);
        this.remnant.setRemnantState(Remnant.RemnantState.PEEK);
        this.remnant.playSound(NMLSounds.REMNANT_PHASE.get(), 0.4F, 0.9F);
    }

    private void combatPeek(LivingEntity target) {
        this.remnant.setFaceLocked(true);
        this.facePosition(this.lastKnownPos);
        double dist = this.remnant.distanceTo(target);
        if (dist <= 2.4 && this.pokeCooldown <= 0) {
            this.startPoke(target);
            return;
        }
        if (dist <= 2.3 && this.swatCooldown <= 0) {
            this.startSwat();
            return;
        }
        if (this.stageTicks <= 12 && !this.peekAcquired) {
            this.remnant.getMoveControl().setWantedPosition(this.peekPos.x, this.peekPos.y, this.peekPos.z, 1.6);
            if (this.stageTicks >= 3 && dist < 30.0 && this.remnant.hasLineOfSight(target)) {
                this.peekAcquired = true;
                this.lastKnownPos = target.position();
                this.lastKnownTick = this.remnant.tickCount;
            }
        } else {
            this.remnant.getMoveControl().setWantedPosition(this.peekAnchor.x, this.peekAnchor.y, this.peekAnchor.z, 1.6);
        }
        if (this.stageTicks >= (this.peekAcquired ? 16 : 22)) {
            if (this.peekFromSwim) {
                if (this.peekAcquired) {
                    this.swimDestination = this.lastKnownPos;
                    this.remnant.setScriptedPhase(false);
                    this.enter(Stage.FLOOR_SWIM);
                    this.remnant.setRemnantState(Remnant.RemnantState.PHASE_SWIM);
                } else {
                    this.combatPeekCooldown = 60;
                    this.startEmergeNear(this.swimDestination);
                }
            } else {
                if (!this.peekAcquired) this.combatPeekCooldown = 80;
                this.startWallWait(8 + this.remnant.getRandom().nextInt(16));
            }
        }
    }

    private void startCeilingDrop(LivingEntity target) {
        this.remnant.getNavigation().stop();
        this.enter(Stage.CEILING_DROP);
        this.remnant.setRemnantState(Remnant.RemnantState.ERUPT);
        this.remnant.setScriptedMotion(true);
        this.remnant.setScriptedPhase(true);
        this.ambushCooldown = 90;
        this.remnant.playSound(NMLSounds.REMNANT_GRIND.get(), 0.9F, 0.5F);
    }

    private void ceilingDrop(LivingEntity target) {
        if (this.stageTicks < 6) {
            this.remnant.setDeltaMovement(Vec3.ZERO);
            this.remnant.getLookControl().setLookAt(target, 40.0F, 40.0F);
            if (this.stageTicks % 2 == 0) {
                this.burstParticles(this.remnant.position().add(0.0, -0.8, 0.0), this.remnant.level().getBlockState(this.remnant.blockPosition()), 3);
            }
            return;
        }
        if (this.stageTicks == 6) {
            this.remnant.setScriptedPhase(false);
            Vec3 to = this.swimDestination.subtract(this.remnant.position());
            this.remnant.setDeltaMovement(Mth.clamp(to.x * 0.08, -0.2, 0.2), -0.32, Mth.clamp(to.z * 0.08, -0.2, 0.2));
            this.remnant.playSound(NMLSounds.REMNANT_EMERGE.get(), 1.0F, 1.2F);
        }
        if (this.stageTicks > 6 && this.remnant.isPhased()) {
            Vec3 delta = this.remnant.getDeltaMovement();
            this.remnant.setDeltaMovement(delta.x, -0.32, delta.z);
        }
        if (!this.attackHit && this.stageTicks > 6 && this.intersects(target, 0.4)) {
            this.attackHit = true;
            target.hurt(this.remnant.damageSources().mobAttack(this.remnant), 7.0F);
            this.knock(target, 0.5, 0.2);
            this.remnant.playSound(NMLSounds.REMNANT_ATTACK.get(), 1.0F, 0.85F);
        }
        if ((this.stageTicks > 9 && this.remnant.onGround()) || this.stageTicks > 50) {
            this.remnant.setScriptedMotion(false);
            this.remnant.playSound(NMLSounds.REMNANT_CRASH.get(), 0.9F, 0.9F);
            this.burstParticles(this.remnant.position(), this.remnant.level().getBlockState(this.remnant.blockPosition().below()), 12);
            this.stunDuration = 10;
            this.enter(Stage.STUNNED);
            this.remnant.setRemnantState(Remnant.RemnantState.STUNNED);
        }
    }

    private void chooseEmergence(LivingEntity target) {
        boolean floorStrikeable = target.onGround()
                && RemnantPhasing.isDiveable(this.remnant.level().getBlockState(target.blockPosition().below()));
        if (!floorStrikeable) {
            this.startFeint(target);
            return;
        }
        boolean moving = target.getDeltaMovement().horizontalDistanceSqr() > 0.01;
        float roll = this.remnant.getRandom().nextFloat();
        if (moving) {
            if (roll < 0.40F) this.startWhirlpool(target);
            else if (roll < 0.70F) this.startFeint(target);
            else this.startErupt(target);
        } else {
            if (roll < 0.55F) this.startErupt(target);
            else if (roll < 0.80F) this.startWhirlpool(target);
            else this.startFeint(target);
        }
    }

    private void startErupt(LivingEntity target) {
        BlockPos under = target.blockPosition().below(2);
        if (!RemnantPhasing.canPhaseThrough(this.remnant.level(), under)) {
            this.startFeint(target);
            return;
        }
        this.remnant.getNavigation().stop();
        this.remnant.setPos(target.getX(), target.getY() - 1.9, target.getZ());
        this.enter(Stage.ERUPT);
        this.remnant.setRemnantState(Remnant.RemnantState.ERUPT);
        this.remnant.setScriptedMotion(true);
        this.remnant.setScriptedPhase(false);
        this.ambushCooldown = 110;
        this.remnant.setDeltaMovement(Vec3.ZERO);
        this.remnant.playSound(NMLSounds.REMNANT_GRIND.get(), 0.9F, 0.5F);
        this.burstParticles(new Vec3(target.getX(), target.getY(), target.getZ()), this.remnant.level().getBlockState(target.blockPosition().below()), 10);
    }

    private void erupt(LivingEntity target) {
        if (this.stageTicks < 4) {
            this.remnant.setDeltaMovement(Vec3.ZERO);
            return;
        }
        if (this.stageTicks == 4) {
            double driftX = Mth.clamp(target.getDeltaMovement().x * 1.5, -0.15, 0.15);
            double driftZ = Mth.clamp(target.getDeltaMovement().z * 1.5, -0.15, 0.15);
            this.remnant.setDeltaMovement(driftX, 0.62, driftZ);
            this.remnant.playSound(NMLSounds.REMNANT_EMERGE.get(), 1.2F, 1.0F);
            this.burstParticles(this.remnant.position().add(0.0, 1.9, 0.0), this.remnant.level().getBlockState(this.remnant.blockPosition().below()), 20);
        }
        if (!this.attackHit && this.stageTicks > 4 && this.intersects(target, 0.45)) {
            this.attackHit = true;
            target.hurt(this.remnant.damageSources().mobAttack(this.remnant), 7.0F);
            this.knock(target, 0.5, 0.5);
            this.remnant.playSound(NMLSounds.REMNANT_ATTACK.get(), 1.0F, 0.85F);
        }
        if ((this.stageTicks > 9 && this.remnant.onGround()) || this.stageTicks > 54) {
            this.remnant.setScriptedMotion(false);
            this.remnant.playSound(NMLSounds.REMNANT_CRASH.get(), 1.0F, 0.8F);
            this.burstParticles(this.remnant.position(), this.remnant.level().getBlockState(this.remnant.blockPosition().below()), 16);
            this.stunDuration = 22;
            this.enter(Stage.STUNNED);
            this.remnant.setRemnantState(Remnant.RemnantState.STUNNED);
        }
    }

    private void stunned() {
        this.remnant.setDeltaMovement(this.remnant.getDeltaMovement().multiply(0.5, 1.0, 0.5));
        if (this.stageTicks >= this.stunDuration) {
            this.preferStone = this.remnant.getRandom().nextFloat() < 0.85F;
            this.enter(Stage.CHOOSE);
        }
    }

    private void startWhirlpool(LivingEntity target) {
        this.remnant.getNavigation().stop();
        this.enter(Stage.WHIRLPOOL);
        this.remnant.setRemnantState(Remnant.RemnantState.WHIRLPOOL);
        this.remnant.setScriptedMotion(true);
        this.remnant.setScriptedPhase(true);
        this.whirlCenter = new Vec3(target.getX(), 0.0, target.getZ());
        this.whirlY = Math.min(this.remnant.getY(), target.getY() - 1.7);
        this.whirlAngle = (float) Mth.atan2(this.remnant.getZ() - target.getZ(), this.remnant.getX() - target.getX());
        this.whirlSinkY = target.getY() - 1.0;
        this.ambushCooldown = 130;
        this.remnant.playSound(NMLSounds.REMNANT_GRIND.get(), 0.8F, 0.45F);
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 70, 1), this.remnant);
    }

    private void whirlpool(LivingEntity target) {
        this.whirlCenter = new Vec3(
                Mth.lerp(0.08, this.whirlCenter.x, target.getX()),
                0.0,
                Mth.lerp(0.08, this.whirlCenter.z, target.getZ()));
        this.whirlAngle += 0.38F;
        Vec3 next = new Vec3(
                this.whirlCenter.x + Math.cos(this.whirlAngle) * 1.35,
                this.whirlY,
                this.whirlCenter.z + Math.sin(this.whirlAngle) * 1.35);
        Vec3 velocity = next.subtract(this.remnant.position());
        if (velocity.length() > 0.9) velocity = velocity.normalize().scale(0.9);
        this.remnant.setDeltaMovement(velocity);
        double pullX = this.whirlCenter.x - target.getX();
        double pullZ = this.whirlCenter.z - target.getZ();
        double pullDist = Math.sqrt(pullX * pullX + pullZ * pullZ);
        boolean inGrip = pullDist < 3.4 && target.getY() - this.whirlY < 4.5 && target.getY() >= this.whirlY - 0.5;
        if (inGrip) {
            double scale = pullDist > 1.0E-2 ? 0.05 / pullDist : 0.0;
            target.push(pullX * scale, -0.055, pullZ * scale);
            target.hurtMarked = true;
        }
        if (inGrip && pullDist < 2.6) {
            if (target.getY() > this.whirlSinkY + 0.02
                    && RemnantPhasing.isDiveable(this.remnant.level().getBlockState(target.blockPosition().below()))) {
                this.remnant.sinkVictim(target, Math.max(this.whirlSinkY, target.getY() - 0.18));
            }
            if (target.getDeltaMovement().y > 0.0) {
                target.setDeltaMovement(target.getDeltaMovement().multiply(1.0, 0.2, 1.0));
                target.hurtMarked = true;
            }
        }
        if (this.stageTicks % 3 == 0) {
            this.remnant.playSound(NMLSounds.REMNANT_PHASE.get(), 0.6F, 0.8F + this.remnant.getRandom().nextFloat() * 0.4F);
        }
        if (this.stageTicks == 35) {
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1), this.remnant);
        }
        if (this.stageTicks >= 60) {
            this.remnant.setScriptedMotion(false);
            this.remnant.releaseSunkVictim();
            this.startEmergeNear(this.remnant.position());
        }
    }

    private void startFeint(LivingEntity target) {
        Vec3 view = target.getViewVector(1.0F);
        Vec3 behind = new Vec3(-view.x, 0.0, -view.z);
        behind = behind.lengthSqr() < 1.0E-4 ? new Vec3(1.0, 0.0, 0.0) : behind.normalize();
        double baseAngle = Mth.atan2(behind.z, behind.x);
        for (int i = 0; i < 10; i++) {
            double angle = baseAngle + (this.remnant.getRandom().nextDouble() - 0.5) * (0.4 + i * 0.35);
            double radius = 1.6 + this.remnant.getRandom().nextDouble() * 1.2;
            BlockPos probe = BlockPos.containing(
                    target.getX() + Math.cos(angle) * radius,
                    target.getY() + 1.0,
                    target.getZ() + Math.sin(angle) * radius);
            BlockPos ground = RemnantPhasing.findStandableNear(this.remnant.level(), probe, 3);
            if (ground != null && this.remnant.position().distanceToSqr(Vec3.atBottomCenterOf(ground)) < 64.0) {
                this.emergeTarget = Vec3.atBottomCenterOf(ground);
                this.startEmerge();
                return;
            }
        }
        this.startEmergeNear(this.remnant.position());
    }

    private void startEmergeNear(Vec3 center) {
        BlockPos base = BlockPos.containing(center.x, center.y, center.z);
        for (int i = 0; i < 12; i++) {
            BlockPos probe = base.offset(
                    this.remnant.getRandom().nextInt(5) - 2,
                    this.remnant.getRandom().nextInt(4),
                    this.remnant.getRandom().nextInt(5) - 2);
            BlockPos ground = RemnantPhasing.findStandableNear(this.remnant.level(), probe, 2);
            if (ground != null) {
                this.emergeTarget = Vec3.atBottomCenterOf(ground);
                this.startEmerge();
                return;
            }
        }
        BlockPos.MutableBlockPos cursor = base.mutable();
        for (int i = 0; i < 6; i++) {
            if (this.remnant.level().getBlockState(cursor).isAir()) {
                this.emergeTarget = Vec3.atBottomCenterOf(cursor);
                this.startEmerge();
                return;
            }
            cursor.move(0, 1, 0);
        }
        this.emergeTarget = this.remnant.position();
        this.startEmerge();
    }

    private void startEmerge() {
        this.remnant.getNavigation().stop();
        this.remnant.setScriptedPhase(true);
        this.remnant.setScriptedMotion(false);
        this.enter(Stage.EMERGE);
        this.remnant.setRemnantState(Remnant.RemnantState.EMERGE);
        this.remnant.playSound(NMLSounds.REMNANT_EMERGE.get(), 0.8F, 1.25F);
    }

    private void emerge() {
        this.remnant.getMoveControl().setWantedPosition(this.emergeTarget.x, this.emergeTarget.y, this.emergeTarget.z, 2.0);
        boolean arrived = this.remnant.position().distanceToSqr(this.emergeTarget) < 0.09;
        if (arrived || this.stageTicks > 30 || (this.stageTicks > 4 && !this.remnant.isIntersectingDiveable())) {
            this.remnant.setScriptedPhase(false);
            this.enter(Stage.CHOOSE);
        }
    }

    private void anchorEmbed() {
        this.embedFeet = this.remnant.blockPosition();
        this.embedFacing = RemnantPhasing.findPerchFacing(this.remnant.level(), this.embedFeet, this.remnant.getRandom());
        if (this.embedFacing != null && RemnantPhasing.isWallColumn(this.remnant.level(), this.embedFeet)) {
            this.embedPos = RemnantPhasing.hiddenPosition(this.embedFeet);
        } else {
            this.embedFacing = this.remnant.getDirection();
            this.embedPos = this.remnant.position();
        }
        this.remnant.setScriptedPhase(true);
    }

    private void holdEmbed() {
        if (this.embedPos == null) return;
        if (this.remnant.position().distanceToSqr(this.embedPos) > 0.09) {
            this.remnant.getMoveControl().setWantedPosition(this.embedPos.x, this.embedPos.y, this.embedPos.z, 1.0);
        }
    }

    private boolean isLookingAway(LivingEntity target) {
        Vec3 view = target.getViewVector(1.0F);
        Vec3 toRemnant = this.remnant.position().subtract(target.position());
        if (toRemnant.lengthSqr() < 1.0E-4) return false;
        return view.dot(toRemnant.normalize()) < 0.0;
    }

    private boolean intersects(LivingEntity target, double inflate) {
        return this.remnant.getBoundingBox().inflate(inflate).intersects(target.getBoundingBox());
    }

    private void knock(LivingEntity target, double horizontal, double up) {
        Vec3 dir = new Vec3(target.getX() - this.remnant.getX(), 0.0, target.getZ() - this.remnant.getZ());
        if (dir.lengthSqr() > 1.0E-4) dir = dir.normalize();
        target.push(dir.x * horizontal, up, dir.z * horizontal);
        target.hurtMarked = true;
    }

    private void faceTarget(LivingEntity target) {
        this.remnant.getLookControl().setLookAt(target, 30.0F, 30.0F);
        this.faceDirection(target.position().subtract(this.remnant.position()));
    }

    private void faceDirection(Vec3 direction) {
        if (direction.horizontalDistanceSqr() < 1.0E-4) return;
        float yaw = (float) (Mth.atan2(direction.z, direction.x) * (180.0F / (float) Math.PI)) - 90.0F;
        this.remnant.setYRot(yaw);
        this.remnant.yBodyRot = yaw;
    }

    private void burstParticles(Vec3 pos, BlockState state, int count) {
        if (state.isAir()) return;
        if (this.remnant.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state), pos.x, pos.y, pos.z, count, 0.3, 0.25, 0.3, 0.08);
        }
    }
}
