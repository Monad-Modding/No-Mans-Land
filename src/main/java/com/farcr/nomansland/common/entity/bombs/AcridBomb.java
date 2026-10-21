package com.farcr.nomansland.common.entity.bombs;

import com.farcr.nomansland.common.block.ToxicGasBlock;
import com.farcr.nomansland.common.registry.NMLParticleTypes;
import com.farcr.nomansland.common.registry.NMLSounds;
import com.farcr.nomansland.common.registry.entities.NMLEffects;
import com.farcr.nomansland.common.registry.entities.NMLEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class AcridBomb extends ThrowableBombEntity {

    private static final float VERTICAL_RESTITUTION = 0.3F;
    private static final float HORIZONTAL_RESTITUTION = 0.4F;
    private static final int EFFECT_RADIUS = 4;
    private static final int GAS_RADIUS = 2;
    private static final int MAX_CORROSION_DURATION = 200;

    public AcridBomb(EntityType<? extends ThrowableBombEntity> entityType, Level level) {
        super(entityType, level);
    }

    public AcridBomb(LivingEntity livingEntity, Level level) {
        super(NMLEntities.ACRID_BOMB.get(), livingEntity, level);
    }

    public AcridBomb(Level level, double x, double y, double z) {
        super(NMLEntities.ACRID_BOMB.get(), x, y, z, level);
    }

    @Override
    public void handleEntityEvent(byte b) {
        if (b == 0) {
            for (int i = 0; i < 200; i++) {
                double theta = random.nextFloat() * 2 * Math.PI;
                double alpha = random.nextFloat() * 2 * Math.PI;
                double cos = Math.cos(alpha);
                double xVelocity = Math.sin(theta) * cos * (random.nextFloat() * 0.3 + 0.7);
                double yVelocity = cos * Math.cos(theta) * (random.nextFloat() * 0.3 + 0.7);
                double zVelocity = Math.sin(alpha) * (random.nextFloat() * 0.3 + 0.7);
                level().addParticle(NMLParticleTypes.TOXIC_GAS.get(), getX(), getY(), getZ(), xVelocity * 0.5, yVelocity * 0.5, zVelocity * 0.5);
            }
        } else {
            super.handleEntityEvent(b);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (isOnFire()) explode();
    }

    @Override
    protected void explode() {
        Level level = level();
        if (!level.isClientSide()) {
            Vec3 center = position();

            AABB area = getBoundingBox().inflate(EFFECT_RADIUS);
            for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, area)) {
                double distance = Math.sqrt(entity.distanceToSqr(center));
                if (distance > EFFECT_RADIUS) continue;
                int duration = (int) (MAX_CORROSION_DURATION * (1.0 - distance / EFFECT_RADIUS));
                if (duration > 0) entity.addEffect(new MobEffectInstance(NMLEffects.CORROSION, duration, 0, false, false, true));
            }

            BlockPos origin = blockPosition();
            int gasRadiusSq = GAS_RADIUS * GAS_RADIUS;
            for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-GAS_RADIUS, -GAS_RADIUS, -GAS_RADIUS), origin.offset(GAS_RADIUS, GAS_RADIUS, GAS_RADIUS))) {
                if (pos.distSqr(origin) <= gasRadiusSq && level.getBlockState(pos).isAir()) {
                    ToxicGasBlock.place(level, pos.immutable());
                }
            }

            level.explode(this, getX(), getY(0.0625), getZ(), 0.0F, Level.ExplosionInteraction.NONE);
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, getX(), getY(0.5), getZ(), 1, 0.0, 0.0, 0.0, 0.0);
            }
        }
        level.broadcastEntityEvent(this, (byte) 0);
        discard();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!level().isClientSide()) explode();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);

        Vec3 motion = getDeltaMovement();
        if (motion.lengthSqr() < 0.1) {
            setDeltaMovement(Vec3.ZERO);
            setOnGround(true);
            return;
        }

        Direction direction = result.getDirection();
        switch (direction.getAxis()) {
            case X -> setDeltaMovement(-motion.x() * HORIZONTAL_RESTITUTION, motion.y(), motion.z());
            case Y -> setDeltaMovement(motion.x() * VERTICAL_RESTITUTION, -motion.y() * VERTICAL_RESTITUTION, motion.z() * VERTICAL_RESTITUTION);
            case Z -> setDeltaMovement(motion.x(), motion.y(), -motion.z() * HORIZONTAL_RESTITUTION);
        }
        if (!shouldFuse()) startFuse(30);
    }

    @Override
    protected ParticleOptions getParticle(LevelAccessor levelAccessor) {
        return ParticleTypes.SMOKE;
    }

    @Override
    public void startFuse(int maxFuse) {
        super.startFuse(maxFuse);
        level().playSound(null, getX(), getY(), getZ(), NMLSounds.BOMB_FUSED.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
    }
}
