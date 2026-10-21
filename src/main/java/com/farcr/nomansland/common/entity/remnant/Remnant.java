package com.farcr.nomansland.common.entity.remnant;

import com.farcr.nomansland.common.registry.NMLSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class Remnant extends Monster {

    public enum RemnantState {
        IDLE,
        WALL_IDLE,
        WALL_HIDDEN,
        PEEK,
        RETREAT,
        TIP_WINDUP,
        TIP_RUSH,
        TIP_RECOVER,
        SWAT,
        LUNGE_TELEGRAPH,
        LUNGE_FLY,
        EXPOSED,
        GET_UP,
        POKE,
        DIVE,
        PHASE_SWIM,
        ERUPT,
        STUNNED,
        WHIRLPOOL,
        EMERGE;

        public static final RemnantState[] VALUES = values();
    }

    private static final EntityDataAccessor<Byte> DATA_STATE = SynchedEntityData.defineId(Remnant.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Boolean> DATA_PHASED = SynchedEntityData.defineId(Remnant.class, EntityDataSerializers.BOOLEAN);

    private boolean scriptedPhase;
    private boolean scriptedMotion;
    private boolean faceLocked;
    private boolean retreatImpulse;
    private boolean ambushPoke;
    private long wanderUntil;
    private int clientStateChangeTick;
    private float swimPitch;
    private float swimPitchO;
    @Nullable
    private LivingEntity sunkVictim;

    public Remnant(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new RemnantMoveControl(this);
        this.xpReward = 15;
        this.setPathfindingMalus(PathType.DANGER_FIRE, 0.0F);
        this.setPathfindingMalus(PathType.DAMAGE_FIRE, 0.0F);
        this.setPathfindingMalus(PathType.WATER, 4.0F);
        this.setPathfindingMalus(PathType.WATER_BORDER, 2.0F);
        this.getNavigation().setMaxVisitedNodesMultiplier(2.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 40.0)
                .add(Attributes.MOVEMENT_SPEED, 0.2)
                .add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.ARMOR, 10.0)
                .add(Attributes.ARMOR_TOUGHNESS, 4.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.STEP_HEIGHT, 1.0)
                .add(Attributes.WATER_MOVEMENT_EFFICIENCY, 1.0);
    }

    public static boolean checkRemnantSpawnRules(EntityType<Remnant> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        if (pos.getY() > 20) return false;
        if (level.getLevel().isDay() && level.canSeeSky(pos)) return false;
        float depth = Mth.clamp((20.0F - pos.getY()) / 84.0F, 0.0F, 1.0F);
        if (random.nextFloat() > 0.5F + 0.5F * depth) return false;
        return Monster.checkMonsterSpawnRules(type, level, spawnType, pos, random);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new RemnantCombatGoal(this));
        this.goalSelector.addGoal(2, new RemnantHideGoal(this));
        this.goalSelector.addGoal(3, new RemnantStrollGoal(this));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new RemnantTargetGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_STATE, (byte) 0);
        builder.define(DATA_PHASED, false);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new RemnantNavigation(this, level);
    }

    @Override
    protected BodyRotationControl createBodyControl() {
        return new BodyRotationControl(this);
    }

    public RemnantState getRemnantState() {
        int index = this.entityData.get(DATA_STATE);
        return RemnantState.VALUES[Mth.clamp(index, 0, RemnantState.VALUES.length - 1)];
    }

    public void setRemnantState(RemnantState state) {
        if (this.getRemnantState() != state) {
            this.entityData.set(DATA_STATE, (byte) state.ordinal());
        }
    }

    public boolean isPhased() {
        return this.entityData.get(DATA_PHASED);
    }

    public void setPhased(boolean phased) {
        this.entityData.set(DATA_PHASED, phased);
        this.noPhysics = phased;
        this.setNoGravity(phased);
    }

    public boolean isScriptedMotion() {
        return this.scriptedMotion;
    }

    public void setScriptedMotion(boolean scriptedMotion) {
        this.scriptedMotion = scriptedMotion;
    }

    public void setScriptedPhase(boolean scriptedPhase) {
        this.scriptedPhase = scriptedPhase;
        if (scriptedPhase && !this.isPhased()) {
            this.setPhased(true);
        }
    }

    public boolean isFaceLocked() {
        return this.faceLocked;
    }

    public void setFaceLocked(boolean faceLocked) {
        this.faceLocked = faceLocked;
    }

    public boolean consumeRetreatImpulse() {
        boolean impulse = this.retreatImpulse;
        this.retreatImpulse = false;
        return impulse;
    }

    public boolean consumeAmbushPoke() {
        boolean poke = this.ambushPoke;
        this.ambushPoke = false;
        return poke;
    }

    public boolean isWandering() {
        return this.level().getGameTime() < this.wanderUntil;
    }

    public void startWandering(int ticks) {
        this.wanderUntil = this.level().getGameTime() + ticks;
    }

    public float getStateAnimTime(float ageInTicks) {
        return Math.max(0.0F, ageInTicks - this.clientStateChangeTick);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        if (DATA_STATE.equals(key) && this.level().isClientSide) {
            this.clientStateChangeTick = this.tickCount;
        }
        if (DATA_PHASED.equals(key)) {
            this.noPhysics = this.isPhased();
            this.setNoGravity(this.isPhased());
        }
        super.onSyncedDataUpdated(key);
    }

    @Override
    public void tick() {
        boolean phased = this.isPhased();
        this.noPhysics = phased;
        this.setNoGravity(phased);
        super.tick();
        if (phased) this.resetFallDistance();
        if (this.level().isClientSide) this.clientEffects();
    }

    @Override
    public void aiStep() {
        if (!this.level().isClientSide) {
            this.faceLocked = false;
            this.managePhasing();
            if (this.isInWater() && !this.isPhased() && !this.onGround()) {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.028, 0.0));
            }
            if (this.isPhased() && this.tickCount % 16 == 0 && this.getDeltaMovement().lengthSqr() > 0.0035) {
                this.playSound(NMLSounds.REMNANT_PHASE.get(), 0.35F, 0.45F + this.random.nextFloat() * 0.3F);
            }
        }
        super.aiStep();
    }

    private void managePhasing() {
        boolean intersecting = this.isIntersectingDiveable();
        boolean wants = this.scriptedPhase || intersecting || this.pathWantsPhase();
        if (wants && !this.isPhased()) {
            this.setPhased(true);
        } else if (!wants && this.isPhased() && !intersecting) {
            this.setPhased(false);
            RemnantState state = this.getRemnantState();
            if (state == RemnantState.WALL_IDLE || state == RemnantState.WALL_HIDDEN || state == RemnantState.PEEK
                    || state == RemnantState.PHASE_SWIM || state == RemnantState.DIVE) {
                this.setRemnantState(RemnantState.IDLE);
            }
        }
    }

    private boolean pathWantsPhase() {
        Path path = this.getNavigation().getPath();
        if (path == null || path.isDone()) return false;
        Node node = path.getNextNode();
        if (node.type != PathType.OPEN) return false;
        return node.asBlockPos().closerToCenterThan(this.position(), 3.0);
    }

    public boolean avoidsSky() {
        return this.level().isDay();
    }

    public boolean isIntersectingDiveable() {
        AABB box = this.getBoundingBox().deflate(0.08);
        BlockPos min = BlockPos.containing(box.minX, box.minY, box.minZ);
        BlockPos max = BlockPos.containing(box.maxX, box.maxY, box.maxZ);
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            BlockState state = this.level().getBlockState(pos);
            if (RemnantPhasing.isDiveable(state) && !state.getCollisionShape(this.level(), pos).isEmpty()) return true;
        }
        return false;
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (this.isPhased()) {
            if (this.isControlledByLocalInstance()) {
                this.move(MoverType.SELF, this.getDeltaMovement());
            }
            return;
        }
        super.travel(travelVector);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (this.isPhased() && this.getTarget() == null && this.tickCount % 5 == 0) {
            Player player = this.level().getNearestPlayer(this.getX(), this.getY(), this.getZ(), 2.3, EntitySelector.NO_CREATIVE_OR_SPECTATOR);
            if (player != null && player.isAlive()) {
                this.setTarget(player);
                this.ambushPoke = true;
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean result = super.hurt(source, amount);
        if (result && !this.level().isClientSide && source.getEntity() instanceof LivingEntity && !this.isPhased()) {
            if (this.random.nextFloat() < 0.6F || this.getHealth() < this.getMaxHealth() * 0.4F) {
                this.retreatImpulse = true;
            }
        }
        return result;
    }

    public void sinkVictim(LivingEntity target, double newY) {
        this.teleportVictim(target, newY);
        this.sunkVictim = target;
    }

    public void releaseSunkVictim() {
        if (this.sunkVictim == null || this.level().isClientSide) return;
        LivingEntity victim = this.sunkVictim;
        this.sunkVictim = null;
        BlockPos feet = victim.blockPosition();
        if (RemnantPhasing.isOpenColumn(this.level(), feet)) return;
        BlockPos.MutableBlockPos cursor = feet.mutable();
        for (int i = 0; i < 4; i++) {
            cursor.move(0, 1, 0);
            if (RemnantPhasing.isOpenColumn(this.level(), cursor)) {
                this.teleportVictim(victim, cursor.getY());
                return;
            }
        }
    }

    private void teleportVictim(LivingEntity target, double newY) {
        if (target instanceof ServerPlayer player) {
            player.connection.teleport(target.getX(), newY, target.getZ(), target.getYRot(), target.getXRot());
        } else {
            target.teleportTo(target.getX(), newY, target.getZ());
        }
    }

    @Override
    public void die(DamageSource source) {
        this.releaseSunkVictim();
        super.die(source);
    }

    @Override
    public void remove(RemovalReason reason) {
        this.releaseSunkVictim();
        super.remove(reason);
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        boolean result = super.causeFallDamage(fallDistance, multiplier * 2.0F, source);
        if (result) {
            this.playSound(NMLSounds.REMNANT_CRASH.get(), 1.0F, 0.9F + this.random.nextFloat() * 0.2F);
        }
        return result;
    }

    @Override
    public int getMaxFallDistance() {
        return 2;
    }

    @Override
    protected float getBlockSpeedFactor() {
        return 1.0F;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    public float getSwimPitch(float partialTick) {
        return Mth.lerp(partialTick, this.swimPitchO, this.swimPitch);
    }

    private void clientEffects() {
        Vec3 delta = this.position().subtract(this.xOld, this.yOld, this.zOld);
        float targetPitch = 0.0F;
        if (this.isPhased() && delta.lengthSqr() > 1.0E-5) {
            targetPitch = Mth.clamp((float) -Mth.atan2(delta.y, Math.max(delta.horizontalDistance(), 0.02)), -1.2F, 1.2F);
        }
        this.swimPitchO = this.swimPitch;
        this.swimPitch += (targetPitch - this.swimPitch) * 0.2F;
        RemnantState state = this.getRemnantState();
        boolean moving = this.position().distanceToSqr(this.xOld, this.yOld, this.zOld) > 0.0025;
        if (this.isPhased() && moving && state != RemnantState.WHIRLPOOL) {
            this.spawnBodyDust(3);
        }
        if (state == RemnantState.LUNGE_TELEGRAPH && this.tickCount % 2 == 0) {
            this.spawnBodyDust(2);
        }
        if (state == RemnantState.WHIRLPOOL) {
            this.spawnWhirlpoolDust();
        }
    }

    private void spawnBodyDust(int count) {
        for (int i = 0; i < count; i++) {
            double px = this.getX() + (this.random.nextDouble() - 0.5) * this.getBbWidth() * 1.4;
            double py = this.getY() + this.random.nextDouble() * this.getBbHeight();
            double pz = this.getZ() + (this.random.nextDouble() - 0.5) * this.getBbWidth() * 1.4;
            BlockPos pos = BlockPos.containing(px, py, pz);
            BlockState state = this.level().getBlockState(pos);
            if (!RemnantPhasing.isDiveable(state)) {
                state = this.level().getBlockState(this.blockPosition());
                if (!RemnantPhasing.isDiveable(state)) continue;
            }
            this.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, state), px, py, pz,
                    (this.random.nextDouble() - 0.5) * 0.1, this.random.nextDouble() * 0.05, (this.random.nextDouble() - 0.5) * 0.1);
        }
    }

    private void spawnWhirlpoolDust() {
        int surfaceY = this.findSurfaceAbove();
        if (surfaceY == Integer.MIN_VALUE) return;
        BlockPos below = new BlockPos(this.blockPosition().getX(), surfaceY - 1, this.blockPosition().getZ());
        BlockState state = this.level().getBlockState(below);
        if (state.isAir()) return;
        for (int i = 0; i < 5; i++) {
            float angle = (this.tickCount * 0.5F) + i * ((float) Math.PI * 2.0F / 5.0F);
            double radius = 0.9 + this.random.nextDouble() * 1.1;
            double px = this.getX() + Mth.cos(angle) * radius;
            double pz = this.getZ() + Mth.sin(angle) * radius;
            double py = surfaceY + 0.1;
            this.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, state), px, py, pz,
                    -Mth.sin(angle) * 0.35, 0.12, Mth.cos(angle) * 0.35);
        }
        if (this.tickCount % 4 == 0) {
            this.level().addParticle(ParticleTypes.POOF, this.getX(), surfaceY + 0.2, this.getZ(), 0.0, 0.04, 0.0);
        }
    }

    private int findSurfaceAbove() {
        BlockPos.MutableBlockPos cursor = this.blockPosition().mutable();
        for (int i = 0; i < 6; i++) {
            if (this.level().getBlockState(cursor).isAir()) return cursor.getY();
            cursor.move(0, 1, 0);
        }
        return Integer.MIN_VALUE;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("Phased", this.isPhased());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setPhased(compound.getBoolean("Phased"));
    }

    @Override
    @Nullable
    protected SoundEvent getAmbientSound() {
        return this.getRemnantState() == RemnantState.IDLE ? NMLSounds.REMNANT_AMBIENT.get() : null;
    }

    @Override
    @Nullable
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return NMLSounds.REMNANT_HURT.get();
    }

    @Override
    @Nullable
    protected SoundEvent getDeathSound() {
        return NMLSounds.REMNANT_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState block) {
        if (!this.isPhased()) {
            this.playSound(NMLSounds.REMNANT_STEP.get(), 0.6F, 0.9F + this.random.nextFloat() * 0.2F);
        }
    }

    @Override
    protected float getSoundVolume() {
        return 1.0F;
    }
}
