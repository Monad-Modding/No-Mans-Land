package com.farcr.nomansland.common.entity;

import com.farcr.nomansland.common.registry.entities.NMLEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class Buried extends AbstractSkeleton {
    public static final byte ANIM_NONE = 0;
    public static final byte ANIM_EMERGE = 1;
    public static final byte ANIM_LUNGE_START = 2;
    public static final byte ANIM_LUNGE_AIRBORNE = 3;

    private static final EntityDataAccessor<Byte> DATA_ANIM_STATE = SynchedEntityData.defineId(Buried.class, EntityDataSerializers.BYTE);

    public final AnimationState emergeAnimationState = new AnimationState();
    public final AnimationState lungeStartAnimationState = new AnimationState();
    public final AnimationState lungeAirborneAnimationState = new AnimationState();

    private int animTimer;
    private int leapCooldown;

    public Buried(EntityType<? extends Buried> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ANIM_STATE, ANIM_NONE);
    }

    public byte getAnimState() {
        return this.entityData.get(DATA_ANIM_STATE);
    }

    public void setAnimState(byte state, int durationTicks) {
        this.entityData.set(DATA_ANIM_STATE, state);
        this.animTimer = durationTicks;
    }

    public static void spawnFromRemains(ServerLevel level, double x, double y, double z) {
        Buried buried = NMLEntities.BURIED.get().create(level);
        if (buried == null) return;
        buried.moveTo(x, y, z, level.random.nextFloat() * 360, 0);
        buried.finalizeSpawn(level, level.getCurrentDifficultyAt(BlockPos.containing(x, y, z)), MobSpawnType.SPAWNER, null);
        level.addFreshEntity(buried);
        buried.setAnimState(ANIM_EMERGE, 15);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractSkeleton.createAttributes()
                .add(Attributes.MAX_HEALTH, 15)
                .add(Attributes.ATTACK_DAMAGE, 3)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(3, new BuriedLeapGoal(this));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide) return;
        if (this.leapCooldown > 0) this.leapCooldown--;
        byte state = getAnimState();
        if (state == ANIM_NONE) return;
        this.animTimer--;
        if (state == ANIM_LUNGE_START) {
            if (this.animTimer <= 0 || !this.onGround()) {
                this.entityData.set(DATA_ANIM_STATE, ANIM_LUNGE_AIRBORNE);
                this.animTimer = 30;
            }
        } else if (state == ANIM_LUNGE_AIRBORNE) {
            if (this.onGround() || this.animTimer <= 0) {
                setAnimState(ANIM_NONE, 0);
            }
        } else if (this.animTimer <= 0) {
            setAnimState(ANIM_NONE, 0);
        }
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        if (DATA_ANIM_STATE.equals(key)) {
            this.emergeAnimationState.stop();
            this.lungeStartAnimationState.stop();
            this.lungeAirborneAnimationState.stop();
            switch (getAnimState()) {
                case ANIM_EMERGE -> this.emergeAnimationState.start(this.tickCount);
                case ANIM_LUNGE_START -> this.lungeStartAnimationState.start(this.tickCount);
                case ANIM_LUNGE_AIRBORNE -> this.lungeAirborneAnimationState.start(this.tickCount);
                default -> {
                }
            }
        }
        super.onSyncedDataUpdated(key);
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        if (random.nextFloat() < 0.15F) {
            equipGold(random, EquipmentSlot.HEAD, Items.GOLDEN_HELMET, 0.7F);
            equipGold(random, EquipmentSlot.CHEST, Items.GOLDEN_CHESTPLATE, 0.35F);
            equipGold(random, EquipmentSlot.LEGS, Items.GOLDEN_LEGGINGS, 0.4F);
            equipGold(random, EquipmentSlot.FEET, Items.GOLDEN_BOOTS, 0.4F);
        }
    }

    private void equipGold(RandomSource random, EquipmentSlot slot, Item item, float chance) {
        if (random.nextFloat() < chance) {
            this.setItemSlot(slot, new ItemStack(item));
            this.armorDropChances[slot.getIndex()] = 0.35F;
        }
    }

    @Override
    public void knockback(double strength, double x, double z) {
        super.knockback(strength * 1.75, x, z);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SKELETON_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.SKELETON_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SKELETON_DEATH;
    }

    @Override
    protected SoundEvent getStepSound() {
        return SoundEvents.SKELETON_STEP;
    }

    private static class BuriedLeapGoal extends Goal {
        private final Buried buried;
        private LivingEntity target;

        public BuriedLeapGoal(Buried buried) {
            this.buried = buried;
            this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.buried.getAnimState() != ANIM_NONE || this.buried.leapCooldown > 0 || this.buried.isVehicle() || !this.buried.onGround()) return false;
            this.target = this.buried.getTarget();
            if (this.target == null) return false;
            double dist = this.buried.distanceToSqr(this.target);
            if (dist < 4.0 || dist > 16.0) return false;
            if (this.target.getY() - this.buried.getY() > 1.5) return false;
            return this.buried.getRandom().nextInt(reducedTickDelay(5)) == 0;
        }

        @Override
        public boolean canContinueToUse() {
            return !this.buried.onGround();
        }

        @Override
        public void start() {
            double dx = this.target.getX() - this.buried.getX();
            double dz = this.target.getZ() - this.buried.getZ();
            float yaw = (float) (Mth.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;
            this.buried.setYRot(yaw);
            this.buried.yBodyRot = yaw;
            this.buried.getLookControl().setLookAt(this.target, 30.0F, 30.0F);

            Vec3 velocity = this.buried.getDeltaMovement();
            Vec3 leap = new Vec3(dx, 0.0, dz);
            if (leap.lengthSqr() > 1.0E-7) {
                leap = leap.normalize().scale(0.4).add(velocity.scale(0.2));
            }
            this.buried.setDeltaMovement(leap.x, 0.4, leap.z);
            this.buried.leapCooldown = 40;
            this.buried.setAnimState(ANIM_LUNGE_START, 5);
        }
    }
}
