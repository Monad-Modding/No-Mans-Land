package com.farcr.nomansland.common.entity.beetle;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class DungBall extends Entity {
    public static final int MAX_SIZE = 2;
    private static final EntityDataAccessor<Integer> DATA_SIZE = SynchedEntityData.defineId(DungBall.class, EntityDataSerializers.INT);
    private static final int[] GROW_THRESHOLD = {500, 1300};
    private static final int RECLAIM_DELAY = 100;
    private static final int ABANDON_DESPAWN = 1400;

    @Nullable
    private UUID ownerUUID;
    private int pushedTicks;
    private int abandonedTicks;
    @Nullable
    private Vec3 pushTarget;
    public float roll;
    public float rollO;

    public DungBall(EntityType<? extends DungBall> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_SIZE, 0);
    }

    public int getSize() {
        return entityData.get(DATA_SIZE);
    }

    public void setSize(int size) {
        entityData.set(DATA_SIZE, Mth.clamp(size, 0, MAX_SIZE));
        refreshDimensions();
    }

    private static float widthForSize(int size) {
        return switch (size) {
            case 0 -> 0.4F;
            case 1 -> 0.6F;
            default -> 0.85F;
        };
    }

    public float getRadius() {
        return getBbWidth() / 2.0F;
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        float width = widthForSize(getSize());
        return EntityDimensions.scalable(width, width);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        if (DATA_SIZE.equals(key)) {
            refreshDimensions();
        }
        super.onSyncedDataUpdated(key);
    }

    public void setOwner(@Nullable Beetle beetle) {
        ownerUUID = beetle == null ? null : beetle.getUUID();
        if (beetle != null) abandonedTicks = 0;
    }

    @Nullable
    public Beetle getOwner() {
        if (ownerUUID == null || !(level() instanceof ServerLevel serverLevel)) return null;
        return serverLevel.getEntity(ownerUUID) instanceof Beetle beetle ? beetle : null;
    }

    public boolean isReclaimable() {
        return getOwner() == null && abandonedTicks > RECLAIM_DELAY;
    }

    public boolean isOwnedBy(Beetle beetle) {
        return ownerUUID != null && ownerUUID.equals(beetle.getUUID());
    }

    public void setPushTarget(Vec3 target) {
        pushTarget = target;
    }

    public void markPushed() {
        pushedTicks++;
        int size = getSize();
        if (size < MAX_SIZE && pushedTicks >= GROW_THRESHOLD[size]) {
            setSize(size + 1);
            playSound(SoundEvents.ROOTED_DIRT_PLACE, 0.7F, 0.7F + random.nextFloat() * 0.2F);
        }
    }

    @Override
    public void tick() {
        super.tick();

        rollO = roll;
        roll += (float) (getDeltaMovement().horizontalDistance() / Math.max(0.2F, getRadius()) * 0.9);

        Vec3 motion = getDeltaMovement();
        double vy = motion.y - 0.04;
        double vx;
        double vz;
        if (pushTarget != null) {
            Vec3 delta = pushTarget.subtract(position());
            vx = Mth.clamp(delta.x * 0.4, -0.28, 0.28);
            vz = Mth.clamp(delta.z * 0.4, -0.28, 0.28);
        } else {
            float friction = onGround() ? 0.6F : 0.96F;
            vx = motion.x * friction;
            vz = motion.z * friction;
        }
        setDeltaMovement(vx, vy, vz);
        move(MoverType.SELF, getDeltaMovement());
        setDeltaMovement(getDeltaMovement().multiply(1.0, 0.98, 1.0));
        pushTarget = null;

        if (!level().isClientSide) {
            if (getOwner() == null) {
                abandonedTicks++;
                if (abandonedTicks > ABANDON_DESPAWN) {
                    discard();
                }
            } else {
                abandonedTicks = 0;
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (level().isClientSide || isRemoved()) return false;
        if (source.is(DamageTypes.IN_FIRE) || source.is(DamageTypes.ON_FIRE)) return false;
        markHurt();
        dropTrash();
        scareOwner();
        playSound(SoundEvents.MUD_BREAK, 0.8F, 0.8F + random.nextFloat() * 0.2F);
        discard();
        return true;
    }

    private void dropTrash() {
        if (!(level() instanceof ServerLevel)) return;
        int count = 1 + getSize() + random.nextInt(3);
        for (int i = 0; i < count; i++) {
            spawnAtLocation(Items.STICK.asItem());
        }
    }

    private void scareOwner() {
        Beetle owner = getOwner();
        setOwner(null);
        if (owner != null) {
            owner.setDungBall(null);
            owner.flee(getX(), getZ());
        }
    }

    @Override
    public boolean isPickable() {
        return !isRemoved();
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        setSize(compound.getInt("Size"));
        pushedTicks = compound.getInt("PushedTicks");
        abandonedTicks = compound.getInt("AbandonedTicks");
        ownerUUID = compound.hasUUID("Owner") ? compound.getUUID("Owner") : null;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putInt("Size", getSize());
        compound.putInt("PushedTicks", pushedTicks);
        compound.putInt("AbandonedTicks", abandonedTicks);
        if (ownerUUID != null) compound.putUUID("Owner", ownerUUID);
    }
}
