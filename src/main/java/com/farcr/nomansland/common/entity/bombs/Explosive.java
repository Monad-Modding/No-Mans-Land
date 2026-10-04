package com.farcr.nomansland.common.entity.bombs;

import com.farcr.nomansland.common.block.torches.Lightables;

import com.farcr.nomansland.NMLConfig;
import com.farcr.nomansland.common.block.torches.Lightables;
import com.farcr.nomansland.common.registry.NMLSounds;
import com.farcr.nomansland.common.registry.entities.NMLEntities;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;


public class Explosive extends ThrowableBombEntity {

    private BlockPos hitPos;

    public Explosive(EntityType<? extends ThrowableProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public Explosive(Level level, double x, double y, double z) {
        super(NMLEntities.EXPLOSIVE.get(), x, y, z, level);
    }

    private void spawnParticles(ParticleOptions particle, int amount) {
        for (int i = 0; i < amount; i++) {
            double theta = random.nextFloat() * 2 * Math.PI;
            double alpha = random.nextFloat() * 2 * Math.PI;
            double cos = Math.cos(alpha);
            double xVelocity = Math.sin(theta) * cos * (random.nextFloat() * 0.3 + 0.7);
            double yVelocity = cos * Math.cos(theta) * (random.nextFloat() * 0.3 + 0.7);
            double zVelocity = Math.sin(alpha) * (random.nextFloat() * 0.3 + 0.7);
            level().addParticle(particle, getX(), getY(), getZ(), xVelocity * 0.6, yVelocity * 0.6, zVelocity * 0.6);
        }
    }

    @Override
    public void handleEntityEvent(byte b) {
        if (b == 0) {
            spawnParticles(ParticleTypes.SMOKE, 320);

            for (int i = 0; i < 40; i++) {
                double theta = random.nextFloat() * 2 * Math.PI;
                double alpha = random.nextFloat() * 2 * Math.PI;
                double cos = Math.cos(alpha);
                double xVelocity = Math.sin(theta) * cos * (random.nextFloat() * 0.3 + 0.7);
                double yVelocity = cos * Math.cos(theta) * (random.nextFloat() * 0.3 + 0.7);
                double zVelocity = Math.sin(alpha) * (random.nextFloat() * 0.3 + 0.7);
                level().addParticle(ParticleTypes.FLAME, false, getX(), getY(), getZ(), xVelocity * 0.1, yVelocity * 0.1, zVelocity * 0.1);
            }
        } else if (b == 1) {
            spawnParticles(ParticleTypes.SMOKE, 400);
        } else {
            super.handleEntityEvent(b);
        }
    }

    @Override
    protected void explode() {
        Level level = level();

        level.explode(this, getX(), getY(0.0625), getZ(), NMLConfig.EXPLOSIVE_STRENGTH.get().floatValue(), Level.ExplosionInteraction.TNT);

        // Light nearby campfires on fire
        BlockPos.withinManhattan(blockPosition(), 6, 4, 6).forEach(pos -> {
            BlockState state = level.getBlockState(pos);
            Lightables.light(level, pos, state, this);
            if (state.is(Blocks.TNT)) {
                TntBlock.explode(level, pos);
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 11);
            }

        });
        level.broadcastEntityEvent(this, (byte) (isInWater() ? 1 : 0));
        discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        Vec3 pos = position();
        Vec3 resultPos = result.getLocation();
        Vec3 dir;

        SubLevelAccess subLevel = SableCompanion.INSTANCE.getContaining(this.level(), result.getBlockPos());
        if (subLevel != null) {
            dir = subLevel.logicalPose().transformPositionInverse(pos).vectorTo(resultPos).normalize();
        } else {
            dir = pos.vectorTo(resultPos).normalize();
        }

        setDeltaMovement(Vec3.ZERO);
        setPos(new Vec3(resultPos.x - dir.x * getBbWidth() * 0.01, resultPos.y - dir.y * getBbHeight() * 0.01, resultPos.z - dir.z * getBbWidth() * 0.01));
        setNoGravity(true);
        if (!shouldFuse()) {
            startFuse(100);
        }
        hitPos = result.getBlockPos();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        setDeltaMovement(getDeltaMovement().scale(-0.1));
        if (!shouldFuse()) {
            startFuse(100);
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (hitPos != null && level().getBlockState(hitPos).getCollisionShape(level(), hitPos).isEmpty()) {
            hitPos = null;
            setNoGravity(false);
        }

        if (isOnFire()) explode();
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
