package com.farcr.nomansland.common.entity.beetle;

import com.farcr.nomansland.common.entity.variant_action.SetGrubBug;
import dev.tazer.mixed_litter.VariantUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Grub extends Animal {
    private int adultGrowthTicks = -1;
    @Nullable
    private ResourceLocation pendingVariant;

    public Grub(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 4)
                .add(Attributes.MOVEMENT_SPEED, 0.12)
                .add(Attributes.STEP_HEIGHT, 0.5)
                .add(Attributes.JUMP_STRENGTH, 0.0);
    }

    public static boolean checkGrubSpawnRules(EntityType<? extends Animal> type, LevelReader level, MobSpawnType spawnType, BlockPos pos, net.minecraft.util.RandomSource random) {
        return level.getBlockState(pos.below()).isSolidRender(level, pos.below()) && level.getBlockState(pos).isAir();
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new PanicGoal(this, 1.4));
        goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.9));
        goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 5.0F));
        goalSelector.addGoal(6, new RandomLookAroundGoal(this));
    }

    public void setPendingVariant(ResourceLocation variant) {
        pendingVariant = variant;
    }

    @Nullable
    public EntityType<?> resolveGrowsInto() {
        SetGrubBug action = VariantUtil.findAction(this, SetGrubBug.class);
        return action == null ? null : action.getBug();
    }

    @Override
    public void jumpFromGround() {
    }

    @Override
    protected float getJumpPower() {
        return 0.0F;
    }

    @Override
    protected void customServerAiStep() {
        applyPendingVariant();
        if (isBaby()) {
            checkSquished();
        } else {
            tickAdultGrowth();
        }
        if (!isRemoved()) {
            super.customServerAiStep();
        }
    }

    private void applyPendingVariant() {
        if (pendingVariant != null && level() instanceof ServerLevel serverLevel) {
            VariantUtil.setVariants(this, VariantUtil.lookupVariantIds(List.of(pendingVariant), serverLevel.registryAccess()));
            pendingVariant = null;
        }
    }

    private void tickAdultGrowth() {
        EntityType<?> growsInto = resolveGrowsInto();
        if (growsInto == null) return;
        if (adultGrowthTicks < 0) {
            adultGrowthTicks = 24000 + random.nextInt(2400);
        } else {
            adultGrowthTicks--;
            if (adultGrowthTicks <= 0) {
                growInto(growsInto);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void growInto(EntityType<?> growsInto) {
        if (!(level() instanceof ServerLevel)) return;
        Mob result = this.convertTo((EntityType<? extends Mob>) growsInto, false);
        if (result != null) {
            result.setBaby(false);
            playSound(SoundEvents.SLIME_SQUISH, 0.6F, 1.2F);
        }
    }

    private void checkSquished() {
        for (LivingEntity living : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(0.15, 0.35, 0.15))) {
            if (living != this
                    && living.getBoundingBox().minY >= getBoundingBox().maxY - 0.25
                    && living.fallDistance > 1.5F) {
                playSound(SoundEvents.SLIME_SQUISH, 0.7F, 0.9F);
                kill();
                break;
            }
        }
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        if (spawnType != MobSpawnType.BREEDING && spawnType != MobSpawnType.CONVERSION && random.nextFloat() < 0.2F) {
            setBaby(true);
        }
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob otherParent) {
        return null;
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return false;
    }

    @Override
    protected float getSoundVolume() {
        return 0.3F;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SLIME_SQUISH_SMALL;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.SLIME_HURT_SMALL;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SLIME_DEATH_SMALL;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("AdultGrowthTicks", adultGrowthTicks);
        if (pendingVariant != null) compound.putString("PendingVariant", pendingVariant.toString());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        adultGrowthTicks = compound.contains("AdultGrowthTicks") ? compound.getInt("AdultGrowthTicks") : -1;
        pendingVariant = compound.contains("PendingVariant") ? ResourceLocation.tryParse(compound.getString("PendingVariant")) : null;
    }
}
