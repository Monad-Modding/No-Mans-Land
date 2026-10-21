package com.farcr.nomansland.common.entity.bombs;

import net.minecraft.world.level.gameevent.GameEvent;
import com.farcr.nomansland.common.entity.InkCloud;
import com.farcr.nomansland.common.registry.NMLSounds;
import com.farcr.nomansland.common.registry.NMLTags;
import com.farcr.nomansland.common.registry.entities.NMLEntities;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class InkBomb extends ThrowableBombEntity {

    private static final float VERTICAL_RESTITUTION = 0.3F;
    private static final float HORIZONTAL_RESTITUTION = 0.4F;

    public InkBomb(EntityType<? extends ThrowableBombEntity> entityType, Level level) {
        super(entityType, level);
    }

    public InkBomb(LivingEntity livingEntity, Level level) {
        super(NMLEntities.INK_BOMB.get(), livingEntity, level);
    }

    public InkBomb(Level level, double x, double y, double z) {
        super(NMLEntities.INK_BOMB.get(), x, y, z, level);
    }

    @Override
    public void tick() {
        super.tick();

        if (isOnFire()) explode();
    }

    @Override
    protected void explode() {
        Level level = level();

        level.playSound(null, blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 4, (1 + (random.nextFloat() - random.nextFloat()) * 0.2F) * 0.7F);
        level.gameEvent(this, GameEvent.EXPLODE, this.position());
        level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(3.5F)).forEach(entity -> {
            entity.hurt(Explosion.getDefaultDamageSource(level, this), 4);

            boolean immune = false;
            for (ItemStack stack : entity.getArmorSlots()) {
                if (stack.is(NMLTags.INK_IMMUNE)) {
                    immune = true;
                    break;
                }
            }

            if (!immune) {
                entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200));
                entity.removeEffect(MobEffects.NIGHT_VISION);
            }

            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 1));
            entity.removeEffect(MobEffects.INVISIBILITY);
        });

        InkCloud inkCloud = new InkCloud(level(), getX(), getY() - 1, getZ());
        Entity owner = getOwner();
        if (owner instanceof LivingEntity livingentity) {
            inkCloud.setOwner(livingentity);
        }

        inkCloud.setRadius(3);
        inkCloud.setWaitTime(1);
        level().addFreshEntity(inkCloud);
        level.broadcastEntityEvent(this, (byte) 0);
        discard();
    }

    @Override
    public void handleEntityEvent(byte b) {
        if (b == 0) {
            for (int i = 0; i < 40; i++) {
                double theta = random.nextFloat() * 2 * Math.PI;
                double alpha = random.nextFloat() * 2 * Math.PI;
                double cos = Math.cos(alpha);
                double xVelocity = Math.sin(theta) * cos * (random.nextFloat() * 0.3 + 0.7);
                double yVelocity = cos * Math.cos(theta) * (random.nextFloat() * 0.3 + 0.7);
                double zVelocity = Math.sin(alpha) * (random.nextFloat() * 0.3 + 0.7);
                level().addParticle(getParticle(level()), false, getX(), getY(), getZ(), xVelocity * 0.1, yVelocity * 0.1, zVelocity * 0.1);
            }
        } else {
            super.handleEntityEvent(b);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (!level().isClientSide()) {
            explode();
        }
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
            case X -> setDeltaMovement(
                    -motion.x() * HORIZONTAL_RESTITUTION,
                    motion.y(),
                    motion.z()
            );
            case Y ->
                    setDeltaMovement(motion.x() * VERTICAL_RESTITUTION, -motion.y() * VERTICAL_RESTITUTION, motion.z() * VERTICAL_RESTITUTION);
            case Z -> setDeltaMovement(
                    motion.x(),
                    motion.y(),
                    -motion.z() * HORIZONTAL_RESTITUTION
            );
        }
        if (!shouldFuse()) {
            startFuse(30);
        }
    }

    @Override
    protected ParticleOptions getParticle(LevelAccessor levelAccessor) {
        return switch (levelAccessor.getRandom().nextInt(4)) {
            case 0 -> ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0x0b0a09);
            case 1 -> ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0x131110);
            case 2 -> ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0x111819);
            default -> ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0x1a1c1b);
        };
    }

    @Override
    public void startFuse(int maxFuse) {
        super.startFuse(maxFuse);
        level().playSound(null, getX(), getY(), getZ(), NMLSounds.BOMB_FUSED.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
    }
}
