package com.farcr.nomansland.common.entity.living_pot;

import com.farcr.nomansland.common.block.pots.*;
import com.farcr.nomansland.common.blockentity.PotBlockEntity;
import com.farcr.nomansland.common.registry.NMLRegistries;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import com.farcr.nomansland.common.registry.entities.NMLEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ThrowablePotionItem;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.ContainerSingleItem;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class LivingPot extends PathfinderMob implements NeutralMob, ContainerSingleItem {

    private static final EntityDataAccessor<Integer> DATA_REMAINING_ANGER_TIME = SynchedEntityData.defineId(LivingPot.class, EntityDataSerializers.INT);
    private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(290, 310);
    public static final EntityDataAccessor<String> VARIANT = SynchedEntityData.defineId(LivingPot.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<BlockState> BLOCKSTATE = SynchedEntityData.defineId(LivingPot.class, EntityDataSerializers.BLOCK_STATE);
    public static final EntityDataAccessor<Boolean> DATA_IS_SLEEPING = SynchedEntityData.defineId(LivingPot.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Integer> DATA_LAST_ATTACK_TICK = SynchedEntityData.defineId(LivingPot.class, EntityDataSerializers.INT);

    @Nullable private UUID persistentAngerTarget;

    private static final String ITEM_KEY = "StoredItem";

    private final PotContents contents = new PotContents();
    @Nullable
    private PotVariant cachedVariant;
    @Nullable
    private String cachedVariantKey;

    @Nullable private BlockPos homePos;
    private int returnTimer = 12000;

    private static final int MELEE_IMPACT_DELAY = 11;
    private static final int MELEE_COOLDOWN = 20;
    @Nullable private LivingEntity pendingMeleeTarget = null;
    private int pendingMeleeTickAt = -1;
    public int meleeCooldownTicks = 0;

    @Nullable private Vec3 lastKnownTargetPos = null;
    private int lastKnownTargetAge = 0;
    private static final int LAST_KNOWN_TARGET_EXPIRY = 400;
    @Nullable private LivingEntity bumpTarget = null;
    private int bumpTargetTicks = 0;
    public boolean isDashing = false;
    public boolean hasWokenLargePot = false;
    private int wakeUpCooldownTicks = 0;
    private static final int WAKE_UP_COOLDOWN = 6000;
    private int wakeUpTicks = 0;
    public boolean droppedSelf;
    private transient boolean inLootTableDrop = false;

    public final AnimationState dashStartAnimState = new AnimationState();
    public final AnimationState dashLoopAnimState = new AnimationState();
    public final AnimationState dashEndAnimState = new AnimationState();
    public final AnimationState wakeUpAnimState = new AnimationState();
    public final AnimationState sleepAnimState = new AnimationState();
    public final AnimationState attackAnimState = new AnimationState();

    private int lastKnownAttackTick = -1;

    public LivingPot(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    @Nullable
    public PotVariant getVariant() {
        String key = this.entityData.get(VARIANT);
        if (key.isEmpty()) return null;
        if (!key.equals(this.cachedVariantKey) || this.cachedVariant == null) {
            this.cachedVariantKey = key;
            this.cachedVariant = this.level().registryAccess().registryOrThrow(NMLRegistries.POT_VARIANT_KEY).getOptional(ResourceKey.create(NMLRegistries.POT_VARIANT_KEY, ResourceLocation.parse(key))).orElse(null);
        }
        return this.cachedVariant;
    }

    public PotContents getContents() {
        return this.contents;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (VARIANT.equals(key)) {
            this.cachedVariant = null;
            this.refreshDimensions();
        }
    }

    public BlockState getBlockState() {
        return entityData.get(BLOCKSTATE);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_REMAINING_ANGER_TIME, 0);
        builder.define(VARIANT, "");
        builder.define(BLOCKSTATE, NMLBlocks.ANCIENT_POT.get().defaultBlockState());
        builder.define(DATA_IS_SLEEPING, false);
        builder.define(DATA_LAST_ATTACK_TICK, 0);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 30)
                .add(Attributes.MOVEMENT_SPEED, 0.20)
                .add(Attributes.ATTACK_DAMAGE, 6)
                .add(Attributes.FOLLOW_RANGE, 20)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new LivingPotFindHelpGoal(this));
        goalSelector.addGoal(1, new LivingPotDashGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false) {
            @Override public boolean canUse() { return isLarge() && meleeCooldownTicks <= 0 && super.canUse() && getTarget() != null && getTarget().canBeSeenAsEnemy() && (!getTarget().isInvisible() || getTarget() == bumpTarget); }
            @Override public boolean canContinueToUse() { return isLarge() && meleeCooldownTicks <= 0 && super.canContinueToUse() && getTarget() != null && getTarget().canBeSeenAsEnemy() && (!getTarget().isInvisible() || getTarget() == bumpTarget); }
        });
        goalSelector.addGoal(2, new LivingPotFleeGoal(this, 1.4));
        goalSelector.addGoal(4, new LivingPotReturnHomeGoal(this));
        goalSelector.addGoal(5, new LivingPotWanderGoal(this, 1.0));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 12.0F) {
            @Override public boolean canUse() { return super.canUse() && lookAt instanceof Player p && p.canBeSeenAsEnemy() && !p.isInvisible(); }
            @Override public boolean canContinueToUse() { return super.canContinueToUse() && lookAt instanceof Player p && p.canBeSeenAsEnemy() && !p.isInvisible(); }
        });
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                living -> living instanceof Player p && p.canBeSeenAsEnemy() && !p.isInvisible() && isAngryAt(p)));
        targetSelector.addGoal(2, new HurtByTargetGoal(this) {
            @Override
            public void start() {
                super.start();
                if (getTarget() instanceof Player p) {
                    if (!p.canBeSeenAsEnemy() || (p.isInvisible() && p != bumpTarget)) {
                        setTarget(null);
                    }
                }
            }
        });
        targetSelector.addGoal(5, new ResetUniversalAngerTargetGoal<>(this, true));
    }

    private static final float LEG_HEIGHT = 5.0F / 16.0F;

    @Override
    protected EntityDimensions getDefaultDimensions(Pose pose) {
        PotVariant variant = getVariant();
        if (variant != null) {
            AABB bounds = variant.shape().bounds();
            float width = (float) Math.max(bounds.getXsize(), bounds.getZsize());
            float height = (float) bounds.getYsize() + LEG_HEIGHT;
            return EntityDimensions.fixed(width - 0.05F, height);
        }
        if (isLarge()) return EntityDimensions.fixed(0.95F, 1.5625F + LEG_HEIGHT);
        return EntityDimensions.fixed(0.75F, 1.0F + LEG_HEIGHT);
    }

    public static LivingPot fromPot(PotBlockEntity be) {
        Level level = be.getLevel();
        BlockPos pos = be.getBlockPos();
        LivingPot pot = new LivingPot(NMLEntities.LIVING_POT.get(), level);
        pot.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        float yaw = be.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING).toYRot();
        pot.setYRot(yaw);
        pot.yRotO = yaw;
        pot.setYBodyRot(yaw);
        pot.setYHeadRot(yaw);
        pot.setVariant(be.variant, be.getBlockState());
        pot.contents.copyFrom(be.getContents());
        pot.setHomePos(pos);
        return pot;
    }

    public void setVariant(PotVariant variant, BlockState state) {
        ResourceLocation location = level().registryAccess().registryOrThrow(NMLRegistries.POT_VARIANT_KEY).getKey(variant);
        if (location == null) return;
        entityData.set(VARIANT, location.toString());
        entityData.set(BLOCKSTATE, state);
        if (isSmall()) {
            Objects.requireNonNull(getAttribute(Attributes.MAX_HEALTH)).setBaseValue(10);
            Objects.requireNonNull(getAttribute(Attributes.MOVEMENT_SPEED)).setBaseValue(0.16);
            Objects.requireNonNull(getAttribute(Attributes.KNOCKBACK_RESISTANCE)).setBaseValue(0.0);
        } else {
            Objects.requireNonNull(getAttribute(Attributes.MAX_HEALTH)).setBaseValue(30);
            Objects.requireNonNull(getAttribute(Attributes.MOVEMENT_SPEED)).setBaseValue(0.1);
            Objects.requireNonNull(getAttribute(Attributes.ATTACK_DAMAGE)).setBaseValue(6);
            Objects.requireNonNull(getAttribute(Attributes.KNOCKBACK_RESISTANCE)).setBaseValue(1.0);
        }
        if (variant.traits().contains(PotTrait.BRITTLE)) {
            double halfHealth = Objects.requireNonNull(getAttribute(Attributes.MAX_HEALTH)).getBaseValue() / 2.0;
            Objects.requireNonNull(getAttribute(Attributes.MAX_HEALTH)).setBaseValue(halfHealth);
        }
        setHealth(getMaxHealth());
        refreshDimensions();
    }

    @Override
    public boolean isPushable() {
        return isSmall();
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    protected void doPush(Entity other) {
        if (isLarge() && other instanceof Player player) {
            double dx = player.getX() - getX();
            double dz = player.getZ() - getZ();
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist > 0.01) {
                double strength = 0.4;
                player.push(dx / dist * strength, 0, dz / dist * strength);
            }
        } else {
            super.doPush(other);
        }
    }

    @Override
    public void playerTouch(Player player) {
        super.playerTouch(player);
        if (!level().isClientSide && player.canBeSeenAsEnemy() && isAngryAt(player) && player.isInvisible()) {
            if (isLarge() && bumpTarget == null) {
                bumpTarget = player;
                bumpTargetTicks = 40;
                setTarget(player);
            }
            if (isSmall()) {
                lastKnownTargetPos = player.position();
                lastKnownTargetAge = 0;
            }
        }
    }

    @Override
    public boolean fireImmune() {
        PotVariant variant = getVariant();
        return variant == null || !variant.traits().contains(PotTrait.FLAMMABLE);
    }

    @Override
    public boolean canDrownInFluidType(FluidType type) {
        return false;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        if (fallDistance > 1.0F && !level().isClientSide) {
            int count = (int) Math.min(fallDistance * (isLarge() ? 4 : 2), isLarge() ? 20 : 10);
            spawnShatterParticles(count, isLarge() ? 0.3 : 0.15);
            level().playSound(null, getX(), getY(), getZ(),
                    SoundEvents.DECORATED_POT_HIT, SoundSource.HOSTILE,
                    Math.min(fallDistance * 0.2F, 1.2F), 0.7F + random.nextFloat() * 0.3F);
        }
        return super.causeFallDamage(fallDistance, multiplier, source);
    }

    @Override
    protected int getBaseExperienceReward() {
        return random.nextInt(2, 6);
    }

    @Override
    public boolean shouldDropExperience() {
        PotVariant variant = getVariant();
        return variant != null && variant.traits().contains(PotTrait.DROPS_EXPERIENCE);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypeTags.IS_FALL) || source.is(DamageTypeTags.IS_EXPLOSION)) {
            amount *= 3.0F;
        }
        if (isLarge() && isDashing) {
            amount *= 1.5F;
        }
        boolean hurt = super.hurt(source, amount);
        if (hurt && !level().isClientSide) {
            spawnShatterParticles(isLarge() ? 12 : 3, isLarge() ? 0.3 : 0.15);
            Entity attacker = source.getEntity();
            if (attacker instanceof Player player && player.canBeSeenAsEnemy()) {
                angerAt(player);
                for (LivingPot other : level().getEntitiesOfClass(LivingPot.class, getBoundingBox().inflate(20),
                        p -> p != this && p.isAlive())) {
                    if (!other.isAngryAt(player)) {
                        other.startPersistentAngerTimer();
                        other.setPersistentAngerTarget(player.getUUID());
                    }
                    other.aimAt(player);
                }
                if (isSmall() && hasWokenLargePot) {
                    hasWokenLargePot = false;
                    wakeUpCooldownTicks = 0;
                }
            }
        }
        return hurt;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (!level().isClientSide && target instanceof LivingEntity living) {
            getLookControl().setLookAt(target, 30.0F, 30.0F);
            pendingMeleeTarget = living;
            pendingMeleeTickAt = tickCount + MELEE_IMPACT_DELAY;
            meleeCooldownTicks = MELEE_COOLDOWN;
            setLastAttackTick(tickCount);
            if (bumpTarget != null && target == bumpTarget) {
                bumpTarget = null;
                bumpTargetTicks = 0;
            }
            level().playSound(null, getX(), getY(), getZ(),
                    SoundEvents.DECORATED_POT_HIT, SoundSource.HOSTILE,
                    1.0F, 0.6F + random.nextFloat() * 0.2F);
        }
        return true;
    }

    public void setLastAttackTick(int tick) {
        entityData.set(DATA_LAST_ATTACK_TICK, tick);
    }

    private static final byte WAKE_UP_EVENT = 72;
    private static final byte DASH_START_EVENT = 76;
    private static final byte DASH_LOOP_EVENT = 81;
    private static final byte DASH_END_EVENT = 85;
    private static final byte DASH_STOP_EVENT = 89;

    public void startWakeUp() {
        this.wakeUpTicks = 30;
        setDeltaMovement(Vec3.ZERO);
        level().broadcastEntityEvent(this, WAKE_UP_EVENT);
    }

    public void startDashStart() {
        level().broadcastEntityEvent(this, DASH_START_EVENT);
    }

    public void startDashLoop() {
        level().broadcastEntityEvent(this, DASH_LOOP_EVENT);
    }

    public void startDashEnd() {
        level().broadcastEntityEvent(this, DASH_END_EVENT);
    }

    public void stopDashAnims() {
        level().broadcastEntityEvent(this, DASH_STOP_EVENT);
    }

    @Override
    public void handleEntityEvent(byte id) {
        switch (id) {
            case WAKE_UP_EVENT -> wakeUpAnimState.start(tickCount);
            case DASH_START_EVENT -> {
                dashStartAnimState.start(tickCount);
                dashLoopAnimState.stop();
                dashEndAnimState.stop();
            }
            case DASH_LOOP_EVENT -> {
                dashStartAnimState.stop();
                dashLoopAnimState.start(tickCount);
            }
            case DASH_END_EVENT -> {
                dashStartAnimState.stop();
                dashLoopAnimState.stop();
                dashEndAnimState.start(tickCount);
            }
            case DASH_STOP_EVENT -> {
                dashStartAnimState.stop();
                dashLoopAnimState.stop();
                dashEndAnimState.stop();
            }
            default -> super.handleEntityEvent(id);
        }
    }

    public Set<PotModifier> getModifiers() {
        return this.contents.getModifiers();
    }

    public boolean hasModifier(PotModifier modifier) {
        return this.contents.hasModifier(modifier);
    }

    public void setModifiers(Set<PotModifier> modifiers) {
        this.contents.setModifiers(modifiers);
    }

    public void removeModifier(PotModifier modifier) {
        this.contents.getModifiers().remove(modifier);
    }

    public PotionContents getStoredPotion() {
        return this.contents.getPotion(this.level());
    }

    public void setStoredPotion(PotionContents potion) {
        this.contents.setPotion(potion);
    }

    public void setPotionTable(ResourceLocation potionTableId, long seed) {
        this.contents.setPotionTable(potionTableId, seed);
    }

    public void placeAsBlock(BlockPos pos) {
        PotVariant variant = getVariant();
        if (variant == null || level().isClientSide) return;

        level().setBlockAndUpdate(pos, getBlockState());
        if (level().getBlockEntity(pos) instanceof PotBlockEntity be) {
            be.variant = variant;
            be.getContents().copyFrom(this.contents);
            be.setChanged();
        }
        if (hasModifier(PotModifier.TRAPPED) && level() instanceof ServerLevel serverLevel) {
            BlockState placed = level().getBlockState(pos);
            level().setBlock(pos, placed.setValue(BlockStateProperties.POWERED, true), 2);
            level().updateNeighborsAt(pos, placed.getBlock());
            level().scheduleTick(pos, placed.getBlock(), 4);
            for (int i = 0; i < 6; i++) {
                double x = pos.getX() + 0.25 + random.nextDouble() * 0.5;
                double y = pos.getY() + 0.5 + random.nextDouble() * 0.5;
                double z = pos.getZ() + 0.25 + random.nextDouble() * 0.5;
                serverLevel.sendParticles(DustParticleOptions.REDSTONE, x, y, z, 1, 0, 0, 0, 0);
            }
        }
        discard();
    }

    public void setSleeping(boolean sleeping) {
        entityData.set(DATA_IS_SLEEPING, sleeping);
    }


    @Override
    public boolean isNoAi() {
        if (level().isClientSide) return super.isNoAi();
        return super.isNoAi() || wakeUpTicks > 0;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide) {
            if (wakeUpTicks > 0) {
                wakeUpTicks--;
                if (wakeUpTicks == 0) {
                    setDeltaMovement(Vec3.ZERO);
                }
            }
            if (meleeCooldownTicks > 0) {
                meleeCooldownTicks--;
            }
            if (pendingMeleeTarget != null && tickCount >= pendingMeleeTickAt) {
                if (pendingMeleeTarget.isAlive() && distanceToSqr(pendingMeleeTarget) <= 6.0) {
                    float damage = (float) getAttributeValue(Attributes.ATTACK_DAMAGE);
                    pendingMeleeTarget.hurt(damageSources().mobAttack(this), damage);
                }
                pendingMeleeTarget = null;
                pendingMeleeTickAt = -1;
            }
            if (isDashing && level() instanceof ServerLevel serverLevel) {
                BlockPos below = BlockPos.containing(getX(), getY() - 0.1, getZ());
                BlockState groundState = level().getBlockState(below);
                if (!groundState.isAir()) {
                    serverLevel.sendParticles(
                            new BlockParticleOption(ParticleTypes.BLOCK, groundState),
                            getX(), getY(), getZ(),
                            2, 0.2, 0.0, 0.2, 0.1);
                }
            }
            if (bumpTarget != null && (bumpTargetTicks <= 0 || !bumpTarget.isAlive())) {
                bumpTarget = null;
                bumpTargetTicks = 0;
            }
            if (bumpTargetTicks > 0) {
                bumpTargetTicks--;
            }
        }
        if (level().isClientSide) {

            if (entityData.get(DATA_IS_SLEEPING)) {
                sleepAnimState.startIfStopped(tickCount);
            } else {
                sleepAnimState.stop();
            }

            int serverAttackTick = entityData.get(DATA_LAST_ATTACK_TICK);
            if (serverAttackTick != lastKnownAttackTick && serverAttackTick > 0) {
                attackAnimState.start(tickCount);
                lastKnownAttackTick = serverAttackTick;
            }
        }
    }

    @Override
    protected void customServerAiStep() {
        updatePersistentAnger((ServerLevel) level(), false);

        if (getTarget() instanceof Player p && p.isInvisible()) {
            setTarget(null);
        }

        if (lastKnownTargetPos != null) {
            lastKnownTargetAge++;
            if (lastKnownTargetAge >= LAST_KNOWN_TARGET_EXPIRY) {
                setLastKnownTargetPos(null);
            }
        }

        if (homePos == null) {
            homePos = blockPosition();
        }

        if (isIdle() && returnTimer > 0) {
            returnTimer--;
        }
        if (isSmall() && hasWokenLargePot) {
            wakeUpCooldownTicks++;
            if (wakeUpCooldownTicks >= WAKE_UP_COOLDOWN) {
                hasWokenLargePot = false;
                wakeUpCooldownTicks = 0;
            }
        }
        super.customServerAiStep();
    }

    public void spawnShatterParticles(int count, double spread) {
        PotVariant variant = getVariant();
        if (level() instanceof ServerLevel serverLevel && variant != null) {
            serverLevel.sendParticles(
                    new PotShatterParticleOption(variant.model()),
                    getX(), getY() + (isLarge() ? 0.8 : 0.5), getZ(),
                    count, spread, 0.3, spread, 0.15
            );
        }
    }

    @Override
    public void die(DamageSource damageSource) {
        if (!level().isClientSide) {
            spawnShatterParticles(isLarge() ? 40 : 14, isLarge() ? 0.5 : 0.25);
            float pitch = isLarge() ? 0.5F + random.nextFloat() * 0.2F : 0.8F + random.nextFloat() * 0.4F;
            level().playSound(null, getX(), getY(), getZ(),
                    SoundEvents.DECORATED_POT_SHATTER, SoundSource.HOSTILE,
                    1.2F, pitch);
        }

        super.die(damageSource);

        if (!level().isClientSide) {
            PotBlock.applyBreakEffects((ServerLevel) level(), position(),
                    getBlockState(), getVariant(), getModifiers(),
                    getStoredPotion(), isLarge(), true);
            discard();
        }
    }

    @Override
    protected void dropFromLootTable(DamageSource damageSource, boolean hitByPlayer) {
        inLootTableDrop = true;
        try {
            super.dropFromLootTable(damageSource, hitByPlayer);
        } finally {
            inLootTableDrop = false;
        }
    }

    @Override
    @Nullable
    public ItemEntity spawnAtLocation(ItemStack stack, float yOffset) {
        if (inLootTableDrop && !stack.isEmpty() && stack.getItem() instanceof BlockItem bi && bi.getBlock() instanceof PotBlock) {
            droppedSelf = true;
        }
        return super.spawnAtLocation(stack, yOffset);
    }

    @Override
    @Nullable
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.DECORATED_POT_HIT;
    }

    @Override
    @Nullable
    protected SoundEvent getDeathSound() {
        return SoundEvents.DECORATED_POT_BREAK;
    }

    public boolean isIdle() {
        return getTarget() == null && !isAngry();
    }

    public boolean isSmall() {
        PotVariant variant = getVariant();
        return variant != null && variant.size() == PotSize.SMALL;
    }

    public boolean isLarge() {
        PotVariant variant = getVariant();
        return variant != null && variant.size() == PotSize.LARGE;
    }

    public void setLastKnownTargetPos(@Nullable Vec3 pos) {
        this.lastKnownTargetPos = pos;
        this.lastKnownTargetAge = 0;
    }

    @Nullable
    public Vec3 getLastKnownTargetPos() {
        return lastKnownTargetPos;
    }

    public void setHomePos(BlockPos pos) {
        this.homePos = pos;
    }

    @Nullable
    public BlockPos getHomePos() {
        return homePos;
    }

    public int getReturnTimer() {
        return returnTimer;
    }

    public void setStoredItem(ItemStack item) {
        this.contents.setItem(item.copy());
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        this.unpackLootTable(damageSource.getEntity() instanceof Player player ? player : null);
        if (!this.contents.getItem().isEmpty()) {
            this.spawnOrThrowPotion(level, this.contents.getItem());
            this.contents.setItem(ItemStack.EMPTY);
        }
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    private void spawnOrThrowPotion(ServerLevel level, ItemStack stack) {
        if (stack.getItem() instanceof ThrowablePotionItem potionItem) {
            Projectile projectile = potionItem.asProjectile(level, new Vec3(getX(), getY() + 0.5, getZ()), stack, Direction.UP);
            projectile.setDeltaMovement(0, 0.05, 0);
            level.addFreshEntity(projectile);
        } else {
            spawnAtLocation(stack);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        readPersistentAngerSaveData(level(), tag);

        if (tag.contains("Variant") && tag.contains("BlockState")) {
            PotVariant v = level().registryAccess().registryOrThrow(NMLRegistries.POT_VARIANT_KEY)
                    .getOptional(ResourceKey.create(NMLRegistries.POT_VARIANT_KEY, ResourceLocation.parse(tag.getString("Variant"))))
                    .orElse(null);
            if (v != null) setVariant(v, NbtUtils.readBlockState(level().holderLookup(Registries.BLOCK), tag.getCompound("BlockState")));
        }
        NbtUtils.readBlockPos(tag, "HomePos").ifPresent(pos -> homePos = pos);
        if (tag.contains("ReturnTimer")) {
            returnTimer = tag.getInt("ReturnTimer");
        }
        this.contents.load(tag, this.level().registryAccess(), ITEM_KEY);
        hasWokenLargePot = tag.getBoolean("HasWokenLargePot");
        if (tag.contains("WakeUpCooldown")) {
            wakeUpCooldownTicks = tag.getInt("WakeUpCooldown");
        }
        if (tag.contains("WakeUpTicks")) {
            wakeUpTicks = tag.getInt("WakeUpTicks");
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        addPersistentAngerSaveData(tag);

        PotVariant variant = getVariant();
        if (variant != null) {
            Optional.ofNullable(level().registryAccess().registryOrThrow(NMLRegistries.POT_VARIANT_KEY).getKey(variant))
                    .ifPresent(key -> tag.putString("Variant", key.toString()));
            tag.put("BlockState", NbtUtils.writeBlockState(getBlockState()));
        }
        if (homePos != null) {
            tag.put("HomePos", NbtUtils.writeBlockPos(homePos));
        }
        tag.putInt("ReturnTimer", returnTimer);
        this.contents.save(tag, this.level().registryAccess(), ITEM_KEY);
        tag.putBoolean("HasWokenLargePot", hasWokenLargePot);
        tag.putInt("WakeUpCooldown", wakeUpCooldownTicks);
        tag.putInt("WakeUpTicks", wakeUpTicks);
    }

    public void setLootTable(@Nullable ResourceKey<LootTable> lootTable) {
        this.contents.setLootTable(lootTable);
    }

    public void setLootTableSeed(long seed) {
        this.contents.setLootTableSeed(seed);
    }

    @Nullable
    public ResourceKey<LootTable> getPotLootTable() {
        return this.contents.getLootTable();
    }

    public long getLootTableSeed() {
        return this.contents.getLootTableSeed();
    }

    public void unpackLootTable(@Nullable Player player) {
        ResourceKey<LootTable> key = this.contents.getLootTable();
        if (key == null || this.level().getServer() == null) return;
        LootTable table = this.level().getServer().reloadableRegistries().getLootTable(key);
        this.contents.setLootTable(null);
        LootParams.Builder builder = new LootParams.Builder((ServerLevel) this.level()).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(this.blockPosition()));
        if (player != null) {
            builder.withLuck(player.getLuck()).withParameter(LootContextParams.THIS_ENTITY, player);
        }
        table.fill(this, builder.create(LootContextParamSets.CHEST), this.contents.getLootTableSeed());
    }

    @Override
    public ItemStack getTheItem() {
        this.unpackLootTable(null);
        return this.contents.getItem();
    }

    @Override
    public ItemStack splitTheItem(int amount) {
        this.unpackLootTable(null);
        ItemStack split = this.contents.getItem().split(amount);
        if (this.contents.getItem().isEmpty()) {
            this.contents.setItem(ItemStack.EMPTY);
        }
        return split;
    }

    @Override
    public void setTheItem(ItemStack item) {
        this.unpackLootTable(null);
        this.contents.setItem(item);
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        this.contents.setItem(ItemStack.EMPTY);
    }

    @Override
    public int getRemainingPersistentAngerTime() { return entityData.get(DATA_REMAINING_ANGER_TIME); }

    @Override
    public void setRemainingPersistentAngerTime(int time) { entityData.set(DATA_REMAINING_ANGER_TIME, time); }

    @Nullable @Override
    public UUID getPersistentAngerTarget() { return persistentAngerTarget; }

    @Override
    public void setPersistentAngerTarget(@Nullable UUID target) { persistentAngerTarget = target; }

    @Override
    public void startPersistentAngerTimer() { setRemainingPersistentAngerTime(PERSISTENT_ANGER_TIME.sample(random)); }

    public void angerAt(Player player) {
        startPersistentAngerTimer();
        setPersistentAngerTarget(player.getUUID());
        aimAt(player);
    }

    private void aimAt(Player player) {
        if (!player.isInvisible()) {
            setTarget(player);
        } else {
            setLastKnownTargetPos(player.position());
        }
    }
}
