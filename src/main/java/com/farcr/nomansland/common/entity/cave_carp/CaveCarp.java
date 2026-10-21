package com.farcr.nomansland.common.entity.cave_carp;

import com.farcr.nomansland.common.entity.cave_carp.ai.CaveCarpAvoidMovingPlayerGoal;
import com.farcr.nomansland.common.entity.cave_carp.ai.CaveCarpSeekLightGoal;
import com.farcr.nomansland.common.entity.cave_carp.ai.CaveCarpSwimGoal;
import com.farcr.nomansland.common.registry.entities.NMLEntities;
import com.farcr.nomansland.common.registry.items.NMLItems;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.animal.AbstractSchoolingFish;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.NotNull;

public class CaveCarp extends AbstractSchoolingFish {

    public CaveCarp(EntityType<? extends AbstractSchoolingFish> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 3.0);
    }

    public static boolean checkCaveCarpSpawnRules(EntityType<? extends CaveCarp> type, LevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return level.getFluidState(pos).is(FluidTags.WATER) && level.getFluidState(pos.above()).is(FluidTags.WATER);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.removeAllGoals(goal -> goal instanceof PanicGoal);
        this.goalSelector.removeAllGoals(goal -> goal instanceof AvoidEntityGoal<?>);
        this.goalSelector.removeAllGoals(goal -> goal instanceof RandomSwimmingGoal);
        this.goalSelector.addGoal(1, new CaveCarpAvoidMovingPlayerGoal(this, 7.0F, 1.5, 1.3));
        this.goalSelector.addGoal(3, new CaveCarpSeekLightGoal(this, 1.1));
        this.goalSelector.addGoal(4, new CaveCarpSwimGoal(this));
    }

    public boolean isStillWater(BlockPos pos) {
        FluidState fluid = this.level().getFluidState(pos);
        return fluid.is(FluidTags.WATER) && fluid.isSource();
    }

    @Override
    public ItemStack getBucketItemStack() {
        return NMLItems.CAVE_CARP_BUCKET.stack();
    }

    @Override
    public EntityType<?> getType() {
        return NMLEntities.CAVE_CARP.get();
    }

    @Override
    public @NotNull SoundEvent getFlopSound() {
        return SoundEvents.COD_FLOP;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.COD_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource damageSource) {
        return SoundEvents.COD_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.COD_DEATH;
    }

    @Override
    public int getMaxSchoolSize() {
        return 4;
    }
}
