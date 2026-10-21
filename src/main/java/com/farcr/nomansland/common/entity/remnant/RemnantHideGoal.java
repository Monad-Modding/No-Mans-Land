package com.farcr.nomansland.common.entity.remnant;

import com.farcr.nomansland.common.registry.NMLSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class RemnantHideGoal extends Goal {

    private enum HideStage { SEEK, ENTER, SETTLED, PEEK, HIDDEN, STALK, RESURFACE, BURROW, TUNNEL }

    private final Remnant remnant;
    private HideStage stage = HideStage.SEEK;
    private int stageTicks;
    private BlockPos perchFeet;
    private Direction perchFacing;
    private int breathTimer;
    private int wanderTimer;
    private int retryCooldown;
    private int stalkTimer;
    private int exposeTimer;
    private RemnantPhasing.PerchSpot stalkSpot;
    private Vec3 resurfaceTarget;
    private boolean pendingResurface;
    private boolean pendingBurrow;
    private double buryY;
    private int retryTimer;
    private BlockPos tunnelTarget;

    public RemnantHideGoal(Remnant remnant) {
        this.remnant = remnant;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public boolean canUse() {
        this.pendingResurface = false;
        this.pendingBurrow = false;
        if (this.remnant.getTarget() != null || this.remnant.isWandering()) return false;
        if (this.adoptCurrentPerch()) return true;
        if (this.retryCooldown > 0) {
            this.retryCooldown--;
            return false;
        }
        this.retryCooldown = 12;
        RemnantPhasing.PerchSpot spot = RemnantPhasing.findPerchAround(this.remnant.level(), this.remnant.blockPosition(), this.remnant.getRandom(), 12, 8, this.remnant.avoidsSky());
        if (spot == null) {
            if (this.remnant.avoidsSky() && !this.remnant.isPhased()
                    && RemnantPhasing.isSkyExposed(this.remnant.level(), this.remnant.blockPosition())
                    && RemnantPhasing.canPhaseThrough(this.remnant.level(), this.remnant.blockPosition().below())) {
                this.pendingBurrow = true;
                return true;
            }
            if (this.remnant.isPhased()) {
                if (this.remnant.avoidsSky() && RemnantPhasing.isNearOpenSky(this.remnant.level(), this.remnant.blockPosition())) {
                    this.pendingBurrow = true;
                    return true;
                }
                this.resurfaceTarget = this.findEscape();
                if (this.resurfaceTarget != null) {
                    this.pendingResurface = true;
                    return true;
                }
            }
            return false;
        }
        this.perchFeet = spot.feet();
        this.perchFacing = spot.facing();
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.remnant.getTarget() != null || this.remnant.isWandering()) return false;
        if (this.stage == HideStage.RESURFACE) return this.stageTicks < 100 && this.remnant.isPhased();
        if (this.stage == HideStage.BURROW) return true;
        if (this.stage == HideStage.TUNNEL) return this.stageTicks < 260;
        return this.perchFeet != null;
    }

    @Override
    public void start() {
        if (this.pendingBurrow) {
            this.pendingBurrow = false;
            this.perchFeet = null;
            boolean wasPhased = this.remnant.isPhased();
            this.buryY = wasPhased ? this.remnant.getY() : this.remnant.getY() - 1.7;
            this.retryTimer = 20;
            this.remnant.getNavigation().stop();
            this.remnant.setScriptedPhase(true);
            this.setStage(HideStage.BURROW);
            this.remnant.setRemnantState(Remnant.RemnantState.DIVE);
            if (!wasPhased) this.remnant.playSound(NMLSounds.REMNANT_DIVE.get(), 0.9F, 1.05F);
            return;
        }
        if (this.pendingResurface) {
            this.pendingResurface = false;
            this.perchFeet = null;
            this.setStage(HideStage.RESURFACE);
            this.remnant.setRemnantState(Remnant.RemnantState.PHASE_SWIM);
            return;
        }
        boolean alreadyThere = this.remnant.isPhased() && this.remnant.blockPosition().equals(this.perchFeet);
        this.setStage(alreadyThere ? HideStage.SETTLED : HideStage.SEEK);
        if (alreadyThere) this.remnant.setScriptedPhase(true);
        this.breathTimer = 400 + this.remnant.getRandom().nextInt(300);
        this.wanderTimer = 20 * (240 + this.remnant.getRandom().nextInt(360));
        this.exposeTimer = 0;
    }

    @Override
    public void stop() {
        this.remnant.getNavigation().stop();
        if (this.remnant.getTarget() == null) {
            this.remnant.setScriptedPhase(false);
        }
        if (!this.remnant.isPhased()) {
            this.remnant.setRemnantState(Remnant.RemnantState.IDLE);
        }
    }

    @Override
    public void tick() {
        this.stageTicks++;
        boolean perchless = this.stage == HideStage.RESURFACE || this.stage == HideStage.BURROW || this.stage == HideStage.TUNNEL;
        if (!perchless && (this.perchFeet == null || this.perchFacing == null)) {
            this.perchFeet = null;
            return;
        }
        switch (this.stage) {
            case SEEK -> this.seek();
            case ENTER -> this.enterWall();
            case SETTLED -> this.settled();
            case PEEK -> this.peek();
            case HIDDEN -> this.hidden();
            case STALK -> this.stalk();
            case RESURFACE -> this.resurface();
            case BURROW -> this.burrow();
            case TUNNEL -> this.tunnel();
        }
    }

    private void burrow() {
        this.remnant.setScriptedPhase(true);
        if (this.remnant.getY() > this.buryY + 0.15) {
            this.remnant.setRemnantState(Remnant.RemnantState.DIVE);
            this.remnant.getMoveControl().setWantedPosition(this.remnant.getX(), this.buryY, this.remnant.getZ(), 1.2);
        } else {
            this.remnant.setRemnantState(Remnant.RemnantState.WALL_HIDDEN);
            this.holdAt(new Vec3(this.remnant.getX(), this.buryY, this.remnant.getZ()));
        }
        if (--this.retryTimer > 0) return;
        this.retryTimer = 80 + this.remnant.getRandom().nextInt(60);
        RemnantPhasing.PerchSpot spot = RemnantPhasing.findPerchAround(this.remnant.level(), this.remnant.blockPosition(), this.remnant.getRandom(), 12, 8, this.remnant.avoidsSky());
        if (spot != null) {
            this.perchFeet = spot.feet();
            this.perchFacing = spot.facing();
            this.remnant.setScriptedPhase(false);
            this.setStage(HideStage.SEEK);
            return;
        }
        BlockPos cave = RemnantPhasing.findCaveSpot(this.remnant.level(), this.remnant, 16);
        if (cave != null && this.remnant.getNavigation().moveTo(cave.getX() + 0.5, cave.getY(), cave.getZ() + 0.5, 1.0)) {
            this.tunnelTarget = cave;
            this.remnant.setScriptedPhase(false);
            this.setStage(HideStage.TUNNEL);
        }
    }

    private void tunnel() {
        this.remnant.setRemnantState(this.remnant.isPhased() ? Remnant.RemnantState.PHASE_SWIM : Remnant.RemnantState.IDLE);
        if (this.stageTicks % 20 == 0) {
            this.remnant.getNavigation().moveTo(this.tunnelTarget.getX() + 0.5, this.tunnelTarget.getY(), this.tunnelTarget.getZ() + 0.5, 1.0);
        }
        boolean arrived = this.remnant.position().distanceToSqr(Vec3.atBottomCenterOf(this.tunnelTarget)) < 4.0;
        boolean failed = this.stageTicks > 15 && this.remnant.getNavigation().isDone() && !arrived;
        if (arrived || failed || this.stageTicks > 240) {
            this.remnant.getNavigation().stop();
            RemnantPhasing.PerchSpot spot = RemnantPhasing.findPerchAround(this.remnant.level(), this.remnant.blockPosition(), this.remnant.getRandom(), 12, 8, this.remnant.avoidsSky());
            if (spot != null) {
                this.perchFeet = spot.feet();
                this.perchFacing = spot.facing();
                this.setStage(HideStage.SEEK);
                return;
            }
            if (this.remnant.avoidsSky() && this.remnant.isPhased() && RemnantPhasing.isNearOpenSky(this.remnant.level(), this.remnant.blockPosition())) {
                this.buryY = this.remnant.getY();
                this.retryTimer = 100;
                this.setStage(HideStage.BURROW);
                return;
            }
            this.perchFeet = null;
            this.setStage(HideStage.SEEK);
        }
    }

    private void resurface() {
        this.remnant.getMoveControl().setWantedPosition(this.resurfaceTarget.x, this.resurfaceTarget.y, this.resurfaceTarget.z, 1.2);
    }

    @Nullable
    private Vec3 findEscape() {
        BlockPos.MutableBlockPos cursor = this.remnant.blockPosition().mutable();
        for (int i = 0; i < 10; i++) {
            cursor.move(0, 1, 0);
            if (RemnantPhasing.isOpenColumn(this.remnant.level(), cursor)) {
                return Vec3.atBottomCenterOf(cursor);
            }
        }
        for (int i = 0; i < 12; i++) {
            BlockPos probe = this.remnant.blockPosition().offset(
                    this.remnant.getRandom().nextInt(9) - 4,
                    this.remnant.getRandom().nextInt(3) - 1,
                    this.remnant.getRandom().nextInt(9) - 4);
            if (RemnantPhasing.isOpenColumn(this.remnant.level(), probe)) {
                return Vec3.atBottomCenterOf(probe);
            }
        }
        return null;
    }

    private void setStage(HideStage stage) {
        this.stage = stage;
        this.stageTicks = 0;
    }

    private boolean adoptCurrentPerch() {
        if (!this.remnant.isPhased()) return false;
        BlockPos current = this.remnant.blockPosition();
        if (!RemnantPhasing.isWallColumn(this.remnant.level(), current)) return false;
        Direction facing = RemnantPhasing.findPerchFacing(this.remnant.level(), current, this.remnant.getRandom());
        if (facing == null) return false;
        if (this.remnant.avoidsSky() && RemnantPhasing.isSkyExposed(this.remnant.level(), current.relative(facing))) return false;
        this.perchFeet = current;
        this.perchFacing = facing;
        return true;
    }

    private void seek() {
        this.remnant.setRemnantState(this.remnant.isPhased() ? Remnant.RemnantState.PHASE_SWIM : Remnant.RemnantState.IDLE);
        Vec3 front = RemnantPhasing.frontPosition(this.perchFeet, this.perchFacing);
        if (this.stageTicks % 20 == 1) {
            this.remnant.getNavigation().moveTo(front.x, front.y, front.z, 1.0);
        }
        if (this.remnant.position().closerThan(front, 1.3)) {
            this.remnant.getNavigation().stop();
            this.remnant.setScriptedPhase(true);
            this.remnant.playSound(NMLSounds.REMNANT_DIVE.get(), 0.7F, 1.3F);
            this.setStage(HideStage.ENTER);
            return;
        }
        if (this.stageTicks > 240 || (this.stageTicks > 30 && this.remnant.getNavigation().isDone())) {
            this.perchFeet = null;
        }
    }

    private void enterWall() {
        this.remnant.setRemnantState(Remnant.RemnantState.PHASE_SWIM);
        Vec3 halfOut = RemnantPhasing.halfOutPosition(this.perchFeet, this.perchFacing);
        this.remnant.getMoveControl().setWantedPosition(halfOut.x, halfOut.y, halfOut.z, 1.0);
        if (this.remnant.position().distanceToSqr(halfOut) < 0.04 || this.stageTicks > 40) {
            this.settle();
        }
    }

    private void settle() {
        this.setStage(HideStage.SETTLED);
        this.remnant.setRemnantState(Remnant.RemnantState.WALL_IDLE);
        this.breathTimer = 400 + this.remnant.getRandom().nextInt(300);
    }

    private void settled() {
        this.remnant.setRemnantState(Remnant.RemnantState.WALL_IDLE);
        this.holdAt(RemnantPhasing.halfOutPosition(this.perchFeet, this.perchFacing));
        this.faceOutward();
        Player watcher = this.findNearbyPlayer(15.0);
        if (watcher != null) {
            this.vanish();
            return;
        }
        if (--this.breathTimer <= 0) {
            this.setStage(HideStage.PEEK);
            this.remnant.setRemnantState(Remnant.RemnantState.PEEK);
            this.remnant.playSound(NMLSounds.REMNANT_BREATHE.get(), 0.7F, 1.0F);
            return;
        }
        if (--this.wanderTimer <= 0) {
            this.remnant.startWandering(2400 + this.remnant.getRandom().nextInt(2400));
            this.remnant.setScriptedPhase(false);
            Vec3 front = RemnantPhasing.frontPosition(this.perchFeet, this.perchFacing);
            this.remnant.getMoveControl().setWantedPosition(front.x, front.y, front.z, 1.0);
            this.perchFeet = null;
        }
    }

    private void peek() {
        this.holdAt(RemnantPhasing.halfOutPosition(this.perchFeet, this.perchFacing));
        this.faceOutward();
        Player watcher = this.findNearbyPlayer(15.0);
        if (watcher != null) {
            this.vanish();
            return;
        }
        if (this.stageTicks > 45) this.settle();
    }

    private void vanish() {
        this.setStage(HideStage.HIDDEN);
        this.remnant.setRemnantState(Remnant.RemnantState.WALL_HIDDEN);
        this.stalkTimer = 60 + this.remnant.getRandom().nextInt(80);
        this.exposeTimer = 0;
    }

    private void hidden() {
        this.remnant.setRemnantState(Remnant.RemnantState.WALL_HIDDEN);
        this.holdAt(RemnantPhasing.hiddenPosition(this.perchFeet));
        Player player = this.findNearbyPlayer(16.0);
        if (player == null) {
            if (++this.exposeTimer > 140) this.settle();
            return;
        }
        this.exposeTimer = 0;
        this.remnant.getLookControl().setLookAt(player, 40.0F, 40.0F);
        if (--this.stalkTimer <= 0) {
            if (this.remnant.distanceTo(player) > 5.0 && this.startStalk(player)) return;
            this.stalkTimer = 100 + this.remnant.getRandom().nextInt(60);
        }
    }

    private boolean startStalk(Player player) {
        RemnantPhasing.PerchSpot spot = RemnantPhasing.findAmbushPerch(this.remnant.level(), this.remnant, player, player.position(), true, 4.0, 9.0);
        if (spot == null) return false;
        BlockPos feet = spot.feet();
        this.remnant.setScriptedPhase(false);
        if (!this.remnant.getNavigation().moveTo(feet.getX() + 0.5, feet.getY(), feet.getZ() + 0.5, 1.0)) {
            this.remnant.setScriptedPhase(true);
            return false;
        }
        this.stalkSpot = spot;
        this.setStage(HideStage.STALK);
        return true;
    }

    private void stalk() {
        this.remnant.setRemnantState(this.remnant.isPhased() ? Remnant.RemnantState.PHASE_SWIM : Remnant.RemnantState.IDLE);
        BlockPos feet = this.stalkSpot.feet();
        if (this.stageTicks % 20 == 0) {
            this.remnant.getNavigation().moveTo(feet.getX() + 0.5, feet.getY(), feet.getZ() + 0.5, 1.0);
        }
        if (this.remnant.position().distanceToSqr(Vec3.atBottomCenterOf(feet)) < 2.0) {
            this.perchFeet = feet;
            this.perchFacing = this.stalkSpot.facing();
            this.remnant.getNavigation().stop();
            this.remnant.setScriptedPhase(true);
            this.vanish();
            this.stalkTimer = 120 + this.remnant.getRandom().nextInt(120);
            return;
        }
        if (this.stageTicks > 140 || (this.stageTicks > 15 && this.remnant.getNavigation().isDone())) {
            this.remnant.getNavigation().stop();
            if (this.adoptCurrentPerch()) {
                this.remnant.setScriptedPhase(true);
                this.vanish();
            } else {
                this.setStage(HideStage.SEEK);
            }
        }
    }

    private void holdAt(Vec3 position) {
        if (this.remnant.position().distanceToSqr(position) > 0.09) {
            this.remnant.getMoveControl().setWantedPosition(position.x, position.y, position.z, 1.0);
        }
    }

    private void faceOutward() {
        if (this.perchFacing == null) return;
        this.remnant.setYRot(this.perchFacing.toYRot());
        this.remnant.yBodyRot = this.remnant.getYRot();
        this.remnant.setYHeadRot(this.remnant.getYRot());
    }

    @Nullable
    private Player findNearbyPlayer(double range) {
        return this.remnant.level().getNearestPlayer(this.remnant.getX(), this.remnant.getY(), this.remnant.getZ(), range, EntitySelector.NO_CREATIVE_OR_SPECTATOR);
    }
}
