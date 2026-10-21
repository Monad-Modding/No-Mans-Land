package com.farcr.nomansland.common.entity.clod;

import com.farcr.nomansland.common.entity.clod.ai.ClodAvoidSunGoal;
import com.farcr.nomansland.common.entity.clod.ai.ClodAvoidThreatGoal;
import com.farcr.nomansland.common.entity.clod.ai.ClodFreezeGoal;
import com.farcr.nomansland.common.entity.clod.ai.ClodGroupGoal;
import com.farcr.nomansland.common.entity.clod.ai.ClodInvestigateGoal;
import com.farcr.nomansland.common.registry.NMLSounds;
import com.farcr.nomansland.common.registry.NMLTags;
import com.farcr.nomansland.common.registry.entities.NMLEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RestrictSunGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NonTameRandomTargetGoal;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Clod extends Animal {

    private static final EntityDataAccessor<Float> DATA_OPACITY = SynchedEntityData.defineId(Clod.class, EntityDataSerializers.FLOAT);

    private static final float FADE_OUT_STEP = 0.05F;
    private static final float FADE_IN_STEP = 0.2F;
    private static final double MOVE_THRESHOLD_SQR = 2.5E-5;
    private static final double THREAT_RANGE = 10.0;
    private static final double LEAVE_RANGE = 15.0;
    private static final double ALARM_RANGE = 20.0;
    private static final int ALERT_TICKS = 300;
    private static final int INVESTIGATE_TICKS = 100;

    private int fleeTicks;
    private boolean hiding;
    private int alertTicks;
    private int investigateTicks;
    @Nullable
    private Vec3 lastThreatPos;
    @Nullable
    private LivingEntity watchedThreat;

    public Clod(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.MOVEMENT_SPEED, 0.22)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.0)
                .add(Attributes.STEP_HEIGHT, 0.6);
    }

    public static boolean checkClodSpawnRules(EntityType<? extends Animal> type, LevelReader level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return level.getBlockState(pos.below()).isSolidRender(level, pos.below()) && level.getBlockState(pos).isAir();
    }

    public static void registerClodRelatedGoals(Mob mob) {
        boolean predator = mob instanceof Fox || mob instanceof Wolf || mob instanceof PolarBear || mob instanceof Cat || mob instanceof Ocelot;
        if (!predator) return;

        mob.goalSelector.addGoal(4, new MeleeAttackGoal((PathfinderMob) mob, 1.2, true));
        if (mob instanceof TamableAnimal tamable) {
            mob.targetSelector.addGoal(5, new NonTameRandomTargetGoal<>(tamable, Clod.class, false, target -> true));
        } else {
            mob.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(mob, Clod.class, false));
        }
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new TemptGoal(this, 1.0, stack -> stack.is(NMLTags.CLOD_FOOD), false));
        this.goalSelector.addGoal(2, new ClodInvestigateGoal(this, 1.0));
        this.goalSelector.addGoal(2, new ClodFreezeGoal(this));
        this.goalSelector.addGoal(3, new ClodAvoidThreatGoal(this, 1.5));
        this.goalSelector.addGoal(4, new ClodAvoidSunGoal(this, 1.2));
        this.goalSelector.addGoal(4, new RestrictSunGoal(this));
        this.goalSelector.addGoal(5, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(6, new FollowParentGoal(this, 1.1));
        this.goalSelector.addGoal(7, new ClodGroupGoal(this, 0.9));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_OPACITY, 1.0F);
    }

    public float getOpacity() {
        return this.entityData.get(DATA_OPACITY);
    }

    public void setOpacity(float opacity) {
        this.entityData.set(DATA_OPACITY, Math.max(0.0F, Math.min(1.0F, opacity)));
    }

    public boolean isInvisibleByStillness() {
        return this.getOpacity() <= 0.05F;
    }

    public boolean isHiding() {
        return this.hiding;
    }

    public void setHiding(boolean hiding) {
        this.hiding = hiding;
    }

    public boolean isFleeing() {
        return this.fleeTicks > 0;
    }

    public void startFleeing() {
        this.fleeTicks = 80 + this.random.nextInt(40);
    }

    public void stopFleeing() {
        this.fleeTicks = 0;
    }

    @Nullable
    public LivingEntity findThreat(double range) {
        LivingEntity closest = null;
        double closestDist = Double.MAX_VALUE;
        for (LivingEntity entity : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(range), this::isThreat)) {
            if (!this.canPerceive(entity, range)) continue;
            double dist = this.distanceToSqr(entity);
            if (dist < closestDist) {
                closestDist = dist;
                closest = entity;
            }
        }
        return closest;
    }

    private boolean canPerceive(LivingEntity threat, double range) {
        double visibility = threat.getVisibilityPercent(this);
        double effectiveRange = Math.max(range * visibility, 2.0);
        if (this.distanceToSqr(threat) > effectiveRange * effectiveRange) return false;
        return this.getSensing().hasLineOfSight(threat);
    }

    public boolean isAlerted() {
        return this.alertTicks > 0;
    }

    public void alert() {
        this.alertTicks = ALERT_TICKS;
    }

    public boolean isInvestigating() {
        return this.investigateTicks > 0 && this.lastThreatPos != null;
    }

    public void clearInvestigate() {
        this.investigateTicks = 0;
    }

    @Nullable
    public Vec3 getLastThreatPos() {
        return this.lastThreatPos;
    }

    private boolean isThreat(LivingEntity entity) {
        if (entity == this || !entity.isAlive()) return false;
        if (entity instanceof Clod) return false;
        if (entity instanceof Player player) {
            if (player.isCreative() || player.isSpectator()) return false;
            if (player.getMainHandItem().is(NMLTags.CLOD_FOOD) || player.getOffhandItem().is(NMLTags.CLOD_FOOD)) return false;
            return true;
        }
        if (entity instanceof Monster) return true;
        return entity instanceof Fox || entity instanceof Wolf || entity instanceof PolarBear
                || entity instanceof Cat || entity instanceof Ocelot;
    }

    public double threatRange() {
        return THREAT_RANGE;
    }

    public double leaveRange() {
        return LEAVE_RANGE;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();

        if (this.fleeTicks > 0) this.fleeTicks--;

        LivingEntity threat = this.findThreat(this.leaveRange());
        boolean seeingVisible = threat != null && !threat.isInvisible();
        if (threat != null) {
            this.alertTicks = ALERT_TICKS;
            if (seeingVisible) this.lastThreatPos = threat.position();
            boolean within10 = this.distanceToSqr(threat) <= THREAT_RANGE * THREAT_RANGE;
            if (within10 && !this.isInvisibleByStillness()) {
                this.startFleeing();
            }
        } else if (this.alertTicks > 0) {
            this.alertTicks--;
            if (this.alertTicks == 0) {
                this.lastThreatPos = null;
                this.investigateTicks = 0;
            }
        }

        if (this.watchedThreat != null && this.watchedThreat.isAlive() && this.watchedThreat.isInvisible()
                && this.distanceToSqr(this.watchedThreat) <= LEAVE_RANGE * LEAVE_RANGE && this.lastThreatPos != null) {
            this.investigateTicks = INVESTIGATE_TICKS;
        }
        this.watchedThreat = seeingVisible ? threat : null;
        if (this.investigateTicks > 0) this.investigateTicks--;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            this.updateInvisibility();
        }
    }

    private void updateInvisibility() {
        double dx = this.getX() - this.xOld;
        double dz = this.getZ() - this.zOld;
        boolean moving = (dx * dx + dz * dz) > MOVE_THRESHOLD_SQR;
        float current = this.getOpacity();
        if (moving) {
            this.setOpacity(Math.min(1.0F, current + FADE_IN_STEP));
        } else {
            this.setOpacity(Math.max(0.0F, current - FADE_OUT_STEP));
        }
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData data) {
        if (!this.isBaby() && (spawnType == MobSpawnType.NATURAL || spawnType == MobSpawnType.CHUNK_GENERATION) && this.random.nextFloat() < 0.2F) {
            Clod baby = NMLEntities.CLOD.get().create(level.getLevel());
            if (baby != null) {
                baby.setBaby(true);
                baby.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);
                baby.finalizeSpawn(level, difficulty, MobSpawnType.JOCKEY, null);
                baby.startRiding(this, true);
            }
        }
        return super.finalizeSpawn(level, difficulty, spawnType, data);
    }

    public void alarmGroup() {
        Vec3 source = this.position();
        for (Clod clod : this.level().getEntitiesOfClass(Clod.class, this.getBoundingBox().inflate(ALARM_RANGE))) {
            if (clod == this) continue;
            clod.lastThreatPos = source;
            clod.alert();
            clod.startFleeing();
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean result = super.hurt(source, amount);
        if (result && !this.level().isClientSide) {
            this.setOpacity(1.0F);
            this.lastThreatPos = source.getEntity() != null ? source.getEntity().position() : this.position();
            this.alert();
            this.startFleeing();
            this.alarmGroup();
        }
        return result;
    }

    @Override
    public void knockback(double strength, double x, double z) {
        super.knockback(strength * 3.0, x, z);
    }

    @Override
    public double getVisibilityPercent(@Nullable Entity lookingEntity) {
        return super.getVisibilityPercent(lookingEntity) * this.getOpacity();
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effectInstance) {
        if (effectInstance.getEffect().is(MobEffects.POISON.unwrapKey().orElseThrow())) {
            return false;
        }
        return super.canBeAffected(effectInstance);
    }

    @Override
    protected int calculateFallDamage(float fallDistance, float damageMultiplier) {
        return (int) Math.ceil(super.calculateFallDamage(fallDistance, damageMultiplier) * 0.5F);
    }

    @Override
    public boolean canStandOnFluid(FluidState fluidState) {
        return fluidState.is(FluidTags.WATER);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        this.floatClod();
    }

    private void floatClod() {
        if (this.isInWater()) {
            this.setDeltaMovement(this.getDeltaMovement().scale(0.5).add(0.0, 0.02, 0.0));
        }
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(NMLTags.CLOD_FOOD);
    }

    @Override
    @Nullable
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return NMLEntities.CLOD.get().create(level);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putFloat("Opacity", this.getOpacity());
        compound.putInt("FleeTicks", this.fleeTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setOpacity(compound.getFloat("Opacity"));
        this.fleeTicks = compound.getInt("FleeTicks");
    }

    @Override
    protected float getSoundVolume() {
        return 0.4F;
    }

    @Override
    @Nullable
    protected SoundEvent getAmbientSound() {
        return NMLSounds.CLOD_AMBIENT.get();
    }

    @Override
    @Nullable
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return NMLSounds.CLOD_HURT.get();
    }

    @Override
    @Nullable
    protected SoundEvent getDeathSound() {
        return NMLSounds.CLOD_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, net.minecraft.world.level.block.state.BlockState block) {
        this.playSound(NMLSounds.CLOD_STEP.get(), 0.12F, 1.0F);
    }

    public boolean isGroupLeader() {
        for (Clod other : this.level().getEntitiesOfClass(Clod.class, this.getBoundingBox().inflate(8.0), c -> !c.isBaby())) {
            if (other != this && other.getId() < this.getId()) return false;
        }
        return true;
    }

    @Nullable
    public Clod findGroupCenter() {
        List<Clod> group = this.level().getEntitiesOfClass(Clod.class, this.getBoundingBox().inflate(12.0), c -> c != this && !c.isBaby());
        Clod leader = null;
        int lowestId = this.getId();
        for (Clod clod : group) {
            if (clod.getId() < lowestId) {
                lowestId = clod.getId();
                leader = clod;
            }
        }
        return leader;
    }

    @Override
    public Vec3 getPassengerRidingPosition(Entity entity) {
        return super.getPassengerRidingPosition(entity).add(0.0, 0.1, 0.0);
    }
}
