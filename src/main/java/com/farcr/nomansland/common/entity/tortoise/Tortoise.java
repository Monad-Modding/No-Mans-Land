package com.farcr.nomansland.common.entity.tortoise;

import com.farcr.nomansland.common.entity.tortoise.ai.*;
import com.farcr.nomansland.common.registry.NMLSounds;
import com.farcr.nomansland.common.registry.NMLTags;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import com.farcr.nomansland.common.registry.entities.NMLEntities;
import com.farcr.nomansland.common.registry.items.NMLItems;
import com.google.common.base.Suppliers;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.function.Supplier;

public class Tortoise extends Animal {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final EntityDataAccessor<Boolean> LAYING_EGG = SynchedEntityData.defineId(Tortoise.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IN_SHELL = SynchedEntityData.defineId(Tortoise.class, EntityDataSerializers.BOOLEAN);
    @Nullable
    private UUID lastHurtByUUID;
    @Nullable
    private BlockPos homePos;
    private boolean hasEgg;
    private boolean goingHome;
    private boolean searching;
    private long hurtWhen;
    private static final float BABY_SCALE = 0.3F;
    private static final Supplier<EntityDimensions> BABY_DIMENSIONS = Suppliers.memoize(() -> NMLEntities.TORTOISE.get().getDimensions()
            .withAttachments(EntityAttachments.builder()
                    .attach(EntityAttachment.PASSENGER, 0.0F, NMLEntities.TORTOISE.get().getHeight(), -0.25F))
            .scale(BABY_SCALE));
    private int layEggCounter;
    private int timesFedWhenBaby;
    public final AnimationState hidingAnimationState = new AnimationState();
    public final AnimationState emergingAnimationState = new AnimationState();
    public final AnimationState layingEggAnimationState = new AnimationState();

    public Tortoise(EntityType<? extends Tortoise> entityType, Level level) {
        super(entityType, level);
        this.lookControl = new LookControl(this) {
            @Override
            public void tick() {
                if (Tortoise.this.inShell() && !Tortoise.this.isSearching())
                    return;
                super.tick();
            }
        };
        this.navigation = new GroundPathNavigation(this, level) {
            @Override
            public boolean isDone() {
                return super.isDone() || Tortoise.this.inShell();
            }
        };
    }

    public static boolean checkTortoiseSpawnRules(EntityType<Tortoise> tortoise, LevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return isBrightEnoughToSpawn(level, pos);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new TortoiseSearchForDangerGoal(this));
        this.goalSelector.addGoal(0, new TortoiseStayAroundHomeGoal(this, 0.75F));
        this.goalSelector.addGoal(1, new TortoiseSleepAndWakeUpGoal(this));
        this.goalSelector.addGoal(2, new TortoiseBreedGoal(this, 0.5F));
        this.goalSelector.addGoal(3, new TortoiseLayEggGoal(this));
        this.goalSelector.addGoal(3, new TortoiseFindSpotToLayEgg(this, 0.85F));
        this.goalSelector.addGoal(3, new TemptGoal(this, 0.5F, itemStack -> itemStack.is(NMLTags.TORTOISE_FOOD), false));
        this.goalSelector.addGoal(4, new FollowParentGoal(this, 0.5));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.5F));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30.0F)
                .add(Attributes.MOVEMENT_SPEED, 0.25F)
                .add(Attributes.STEP_HEIGHT, 1.0F)
                .add(Attributes.ARMOR, 15.0F);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(Tortoise.LAYING_EGG, false);
        builder.define(Tortoise.IN_SHELL, false);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        if (key.equals(LAYING_EGG)) {
            resetAnimations();
            if (isLayingEgg()) layingEggAnimationState.startIfStopped(tickCount);
            refreshDimensions();
        }

        if (key.equals(IN_SHELL)) {
            resetAnimations();
            if (inShell()) hidingAnimationState.startIfStopped(tickCount);
            else emergingAnimationState.startIfStopped(tickCount);
            refreshDimensions();
        }

        super.onSyncedDataUpdated(key);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide && this.lastHurtByUUID != null && this.inShell()
                && this.level().getGameTime() - this.hurtWhen > 600L) {
            this.setSearching(true);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        if (this.getHomePos() != null)
            compound.put("home_pos", NbtUtils.writeBlockPos(this.getHomePos()));
        compound.putBoolean("HasEgg", this.hasEgg());
        compound.putBoolean("Searching", this.isSearching());
        compound.putBoolean("InShell", this.inShell());
        compound.putLong("HurtWhen", this.getHurtWhen());
        compound.putInt("EggCount", this.getLayEggCounter());
        compound.putInt("TimesFed", this.getTimesFedWhenBaby());
        if (this.lastHurtByUUID != null)
            compound.putUUID("HurtByUUID", this.lastHurtByUUID);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        NbtUtils.readBlockPos(compound, "home_pos").ifPresent(this::setHomePos);
        this.setSearching(compound.getBoolean("Searching"));
        this.retreatShell(compound.getBoolean("InShell"));
        this.setHasEgg(compound.getBoolean("HasEgg"));
        this.setHurtWhen(compound.getLong("HurtWhen"));
        this.setLayEggCounter(compound.getInt("EggCount"));
        if (compound.hasUUID("HurtByUUID")) this.lastHurtByUUID = compound.getUUID("HurtByUUID");
        this.setTimesFedWhenBaby(compound.getInt("TimesFed"));
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        if (spawnType == MobSpawnType.STRUCTURE)
            this.setHomePos(this.blockPosition());
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (this.isFood(itemstack)) {
            int i = this.getAge();
            if (!this.level().isClientSide && i == 0 && this.canFallInLove()) {
                this.usePlayerItem(player, hand, itemstack);
                this.setInLove(player);
                return InteractionResult.SUCCESS;
            }

            if (this.isBaby() && this.getTimesFedWhenBaby() < 3 && !this.level().isClientSide) {
                for (int particleCount = 0; particleCount < 5; particleCount++) {
                    ((ServerLevel) level()).sendParticles(new ItemParticleOption(ParticleTypes.ITEM, itemstack), this.getX() + this.getLookAngle().x / 2.0,
                            this.getY() + this.getLookAngle().y,
                            this.getZ() + this.getLookAngle().z / 2, 1, 0, 0, 0, 0.15);
                }
                this.usePlayerItem(player, hand, itemstack);
                this.playSound(
                        NMLSounds.TORTOISE_EAT.get(),
                        0.5F + 0.5F * (float) this.random.nextInt(2),
                        (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F
                );
                this.ageUp(1800);
                this.timesFedWhenBaby++;
                return InteractionResult.SUCCESS;
            }

            if (this.level().isClientSide) {
                return InteractionResult.CONSUME;
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public void setBaby(boolean baby) {
        this.setAge(baby ? -432000 : 0);
    }

    @Override
    @Nullable
    protected SoundEvent getAmbientSound() {
        return NMLSounds.TORTOISE_AMBIENT.get();
    }

    @Override
    protected SoundEvent getSwimSound() {
        return NMLSounds.TORTOISE_SWIM.get();
    }

    @Override
    @Nullable
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return this.isBaby() ? NMLSounds.TORTOISE_HURT_BABY.get() : NMLSounds.TORTOISE_HURT.get();
    }

    @Override
    @Nullable
    protected SoundEvent getDeathSound() {
        return this.isBaby() ? NMLSounds.TORTOISE_DEATH_BABY.get() : NMLSounds.TORTOISE_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState block) {
        super.playStepSound(pos, block);
    }

    @Override
    public boolean canFallInLove() {
        return super.canFallInLove() && !this.hasEgg() && !this.inShell();
    }

    @Override
    protected float nextStep() {
        return this.moveDist + 0.15F;
    }

    @Override
    public float getAgeScale() {
        return this.isBaby() ? BABY_SCALE : 1.0F;
    }

    @Override
    @Nullable
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return NMLEntities.TORTOISE.get().create(level);
    }

    @Override
    protected float getKnockback(Entity attacker, DamageSource damageSource) {
        return this.inShell() ? 0.0F : super.getKnockback(attacker, damageSource);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(NMLTags.TORTOISE_FOOD);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getEntity() instanceof LivingEntity) {
            if (!this.inShell()) {
                this.setLastHurtByMob((LivingEntity) source.getEntity());
                this.setHurtWhen(this.level().getGameTime());
                this.retreatShell(true);
            } else {
                if (amount > this.getMaxHealth()) {
                    amount -= this.getMaxHealth();
                    this.playSound(this.getHurtSound(source));
                } else {
                    this.playSound(NMLSounds.TORTOISE_SHELL_DEFLECT.get(), 1.0F, 1.0F);
                    return false;
                }
            }
        }
        return super.hurt(source, amount);
    }

    @Override
    public void setLastHurtByMob(@Nullable LivingEntity livingEntity) {
        super.setLastHurtByMob(livingEntity);
        if (livingEntity != null)
            this.setLastHurtByUUID(livingEntity.getUUID());
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide && this.isAlive() && this.isLayingEgg() && this.layEggCounter >= 1 && this.layEggCounter % 5 == 0) {
            BlockPos blockpos = this.blockPosition();
            BlockState blockstate = this.level().getBlockState(blockpos.below());
            if (blockstate.getRenderShape() != RenderShape.INVISIBLE) {
                for (int i = 0; i < 5; i++) {
                    ((ServerLevel) this.level())
                            .sendParticles(
                                    new BlockParticleOption(ParticleTypes.BLOCK, blockstate),
                                    (double) blockpos.getX() + 0.5,
                                    (double) blockpos.getY() + 0.7,
                                    (double) blockpos.getZ() + 0.5,
                                    3,
                                    ((double) this.getRandom().nextFloat() - 0.5) * 0.08,
                                    ((double) this.getRandom().nextFloat() - 0.5) * 0.08,
                                    ((double) this.getRandom().nextFloat() - 0.5) * 0.08,
                                    0.15F
                            );
                }
                this.playSound(blockstate.getSoundType(level(), blockpos.below(), this).getPlaceSound());
            }
            this.gameEvent(GameEvent.ENTITY_ACTION);
        }
    }


    @Override
    protected void ageBoundaryReached() {
        super.ageBoundaryReached();
        if (!this.isBaby() && this.level().getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
            this.spawnAtLocation(new ItemStack(NMLItems.STURDY_SCUTE.get(), this.random.nextIntBetweenInclusive(1, 2)), 1);
        }
    }

    private void resetAnimations() {
        hidingAnimationState.stop();
        emergingAnimationState.stop();
        layingEggAnimationState.stop();
    }

    @Override
    public float getWalkTargetValue(BlockPos pos, LevelReader level) {
        return 0.0F;
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        return this.isBaby() ? BABY_DIMENSIONS.get() : super.getDefaultDimensions(pose);
    }

    public void setHomePos(@Nullable BlockPos homePos) {
        this.homePos = homePos;
    }

    @Nullable
    public BlockPos getHomePos() {
        return this.homePos;
    }

    public boolean hasEgg() {
        return this.hasEgg;
    }

    public void setHasEgg(boolean hasEgg) {
        this.hasEgg = hasEgg;
    }

    public boolean isLayingEgg() {
        return this.entityData.get(LAYING_EGG);
    }

    public boolean inShell() {
        return this.entityData.get(IN_SHELL);
    }

    public void retreatShell(boolean shouldShell) {
        this.entityData.set(IN_SHELL, shouldShell);
        this.stopInPlace();
    }

    public boolean isSearching() {
        return this.searching;
    }

    public void setSearching(boolean searching) {
        this.searching = searching;
    }

    public void setLayingEgg(boolean isLayingEgg) {
        this.layEggCounter = isLayingEgg ? 1 : 0;
        this.entityData.set(LAYING_EGG, isLayingEgg);
    }

    public boolean isGoingHome() {
        return this.goingHome;
    }

    public void setGoingHome(boolean isGoingHome) {
        this.goingHome = isGoingHome;
    }

    public long getHurtWhen() {
        return this.hurtWhen;
    }

    public void setHurtWhen(long hurtWhen) {
        this.hurtWhen = hurtWhen;
    }

    @Nullable
    public UUID getLastHurtByUUID() {
        return lastHurtByUUID;
    }

    public void setLastHurtByUUID(@Nullable UUID lastHurtByUUID) {
        this.lastHurtByUUID = lastHurtByUUID;
    }

    public int getLayEggCounter() {
        return layEggCounter;
    }

    public void setLayEggCounter(int layEggCounter) {
        this.layEggCounter = layEggCounter;
    }

    /**
     * a Home, to Tortoises, is a location that has 0 Sky light, No direct sky access,
     * a light level lower than 7, and above a block within the correct tag (#suitable_turtle_home).
     */
    public boolean isValidHome(BlockPos pos) {
        return level().getBlockState(pos.below()).is(NMLTags.SUITABLE_TORTOISE_HOME) && (level().getBrightness(LightLayer.BLOCK, pos) < 7 && level().getBrightness(LightLayer.SKY, pos) < 7) && !level().canSeeSky(pos) && !level().getBlockState(pos).is(NMLBlocks.TORTOISE_EGGS);
    }

    public int getTimesFedWhenBaby() {
        return timesFedWhenBaby;
    }

    public void setTimesFedWhenBaby(int timesFedWhenBaby) {
        this.timesFedWhenBaby = timesFedWhenBaby;
    }
}