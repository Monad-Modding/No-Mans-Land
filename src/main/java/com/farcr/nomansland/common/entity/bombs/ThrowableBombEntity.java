package com.farcr.nomansland.common.entity.bombs;

import net.minecraft.world.level.gameevent.GameEvent;
import com.farcr.nomansland.common.registry.NMLTags;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;

public abstract class ThrowableBombEntity extends ThrowableProjectile {

    private static final EntityDataAccessor<Boolean> DATA_SHOULD_FUSE_ID = SynchedEntityData.defineId(ThrowableBombEntity.class, EntityDataSerializers.BOOLEAN);

    protected float oRoll;
    protected float roll;
    protected int oFuse;
    protected int fuse;
    protected int maxFuse = -1;

    protected ThrowableBombEntity(EntityType<? extends ThrowableProjectile> entityType, Level level) {
        super(entityType, level);
    }

    protected ThrowableBombEntity(EntityType<? extends ThrowableProjectile> entityType, double d, double e, double f, Level level) {
        super(entityType, d, e, f, level);
    }

    protected ThrowableBombEntity(EntityType<? extends ThrowableProjectile> entityType, LivingEntity livingEntity, Level level) {
        super(entityType, livingEntity, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_SHOULD_FUSE_ID, false);
    }

    @Override
    protected void updateRotation() {
        Vec3 deltaMovement = getDeltaMovement();
        double distanceSq = deltaMovement.horizontalDistanceSqr();
        double distance = Math.sqrt(distanceSq);
        setXRot(lerpRotation(xRotO, (float) (Mth.atan2(deltaMovement.y, distance) * 180.0F / (float) Math.PI)));
        if (distanceSq > 0.01) {
            setYRot(lerpRotation(yRotO, (float) (Mth.atan2(deltaMovement.x, deltaMovement.z) * 180.0F / (float) Math.PI)));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("Fuse", fuse);
        compound.putInt("MaxFuse", maxFuse);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        fuse = compound.getInt("Fuse");
        maxFuse = compound.getInt("MaxFuse");
        entityData.set(DATA_SHOULD_FUSE_ID, maxFuse >= 0);
    }

    @Override
    public void tick() {
        super.tick();

        Level level = level();
        if (level.isClientSide()) {
            oFuse = fuse;
            oRoll = roll;

            if (shouldFuse()) {
                fuse++;
                if (fuse >= maxFuse) {
                    fuse = maxFuse;
                }
            }

            double distance = getDeltaMovement().lengthSqr();
            if (distance > 0.01) {
                roll += Math.sqrt(distance) * 45;
            }

            if (!onGround()) {
                level.addParticle(getParticle(level), getX(), getY() + getBbHeight(), getZ(), 0, 0, 0);
            }
        } else {
            if (shouldFuse()) {
                fuse++;
                if (fuse >= maxFuse) {
                    explode();
                }
            }

            if (level.getBlockState(blockPosition()).is(NMLTags.BOMB_EXPLODE)) {
                explode();
            }
        }
    }

    public boolean shouldFuse() {
        return entityData.get(DATA_SHOULD_FUSE_ID);
    }

    public int getMaxFuse() {
        return maxFuse;
    }

    public void startFuse(int maxFuse) {
        this.maxFuse = maxFuse;
        entityData.set(DATA_SHOULD_FUSE_ID, maxFuse >= 0);
        if (maxFuse >= 0 && !this.level().isClientSide) {
            this.gameEvent(GameEvent.PRIME_FUSE, this.getOwner());
        }
    }

    protected abstract void explode();

    protected abstract ParticleOptions getParticle(LevelAccessor levelAccessor);

    public float getRoll(float partialTicks) {
        return Mth.lerp(partialTicks, oRoll, roll);
    }

    public float getSwelling(float partialTicks) {
        return Mth.lerp(partialTicks, oFuse, fuse) / (float) (getMaxFuse() - 2);
    }
}