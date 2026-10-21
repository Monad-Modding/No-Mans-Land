package com.farcr.nomansland.common.entity;

import com.farcr.nomansland.NMLConfig;
import com.farcr.nomansland.common.registry.entities.NMLEntities;
import com.farcr.nomansland.common.registry.items.NMLItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import com.farcr.nomansland.common.registry.entities.NMLEffects;
import com.farcr.nomansland.common.registry.NMLCriteriaTriggers;
import com.farcr.nomansland.common.effect.FlammableEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;

public class IncendiaryArrow extends AbstractArrow {
    public IncendiaryArrow(EntityType<? extends IncendiaryArrow> entityType, Level level) {
        super(entityType, level);
    }

    public IncendiaryArrow(Level level, double x, double y, double z, ItemStack pickupItemStack, @Nullable ItemStack firedFromWeapon) {
        super(NMLEntities.INCENDIARY_ARROW.get(), x, y, z, level, pickupItemStack, firedFromWeapon);
        setRemainingFireTicks(600);
    }

    public IncendiaryArrow(LivingEntity owner, Level level, ItemStack pickupItemStack, @Nullable ItemStack firedFromWeapon) {
        super(NMLEntities.INCENDIARY_ARROW.get(), owner, level, pickupItemStack, firedFromWeapon);
        setRemainingFireTicks(600);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        Entity entity = result.getEntity();
        entity.igniteForSeconds(30);

        if (!level().isClientSide() && entity instanceof LivingEntity living && living.hasEffect(NMLEffects.FLAMMABLE)) {
            if (getOwner() instanceof ServerPlayer serverPlayer)
                NMLCriteriaTriggers.IGNITE_FLAMMABLE_ENTITY.get().trigger(serverPlayer, living, damageSources().onFire());
            FlammableEffect.igniteFlammable(living);
        }
    }

    @Override
    public boolean displayFireAnimation() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide && isOnFire()) {
            level().addParticle(ParticleTypes.FLAME,
                    this.getX(),
                    this.getY() + 0.1,
                    this.getZ(),
                    (random.nextFloat() - 0.5) * 0.02,
                    -0.01,
                    (random.nextFloat() - 0.5) * 0.02
            );
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);

        Direction direction = result.getDirection();
        BlockPos pos = result.getBlockPos();
        Level level = level();
        BlockState state = level.getBlockState(pos);
        BlockPos neighbourPos = pos.relative(direction);

        if (level instanceof ServerLevel serverLevel) {
            boolean ignitedFire = false;

            if (isOnFire() && NMLConfig.INCENDIARY_ARROW_PLACES_FIRE.get()) {
                if (level.getBlockState(neighbourPos).is(Blocks.FIRE)) {
                    BlockPos.withinManhattan(neighbourPos, 1, 0, 1).forEach(firePos -> {
                        if ((BaseFireBlock.canBePlacedAt(level, firePos, direction)
                                || level.getBlockState(firePos).isFlammable(level, firePos, direction))
                                && level.random.nextFloat() < 0.4F) {
                            level.setBlockAndUpdate(firePos, BaseFireBlock.getState(level, firePos));
                        }
                    });
                    ignitedFire = true;
                } else if (BaseFireBlock.canBePlacedAt(level, neighbourPos, direction)
                        || state.isFlammable(level, pos, direction)
                        || direction == Direction.UP) {
                    level.setBlockAndUpdate(neighbourPos, BaseFireBlock.getState(level, neighbourPos));
                    ignitedFire = true;
                }
            }

            if (ignitedFire) {
                level.playSound(null, blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.0F, 0.9F + level.random.nextFloat() * 0.2F);
                level.playSound(null, blockPosition(), SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS, 0.6F, 0.7F + level.random.nextFloat() * 0.3F);

                for (int i = 0; i < 10; i++) {
                    double velX = (random.nextDouble() - 0.5) * 0.3;
                    double velY = 0.05 + random.nextDouble() * 0.15;
                    double velZ = (random.nextDouble() - 0.5) * 0.3;

                    serverLevel.sendParticles(ParticleTypes.FLAME, position().x, position().y, position().z, 1, velX, velY, velZ, 0.1);
                    serverLevel.sendParticles(ParticleTypes.SMOKE, position().x, position().y, position().z, 1, velX * 0.5, velY * 0.5, velZ * 0.5, 0.2);
                    serverLevel.sendParticles(ParticleTypes.LAVA, position().x, position().y, position().z, 1, velX, 0.05, velZ, 0.3);
                }
            } else if (isOnFire()) {
                serverLevel.sendParticles(ParticleTypes.SMOKE, position().x, position().y, position().z, 8, 0, 0.05, 0, 0.01);
                serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, position().x, position().y, position().z, 4, 0, 0, 0, 0.01);
                level.playSound(null, blockPosition(), SoundEvents.FLINTANDSTEEL_USE, SoundSource.PLAYERS, 0.6F, 1.2F);
                serverLevel.sendParticles(ParticleTypes.LAVA, position().x, position().y, position().z, 4, 0, -0.05, 0, 0.01);
                if (NMLConfig.INCENDIARY_ARROW_PLACES_FIRE.get()) {
                    Ember ember = new Ember(level, getX(), getY(), getZ());
                    level.addFreshEntity(ember);
                }
                clearFire();
            }

            if (isOnFire()) {
                discard();
                serverLevel.sendParticles(ParticleTypes.SMOKE, position().x, position().y, position().z, 5, 0, 0, 0, 0.01);
            }
        }
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return NMLItems.INCENDIARY_ARROW.stack();
    }

    @Override
    protected ItemStack getPickupItem() {
        return isOnFire() ? getDefaultPickupItem() : new ItemStack(Items.ARROW);
    }
}
