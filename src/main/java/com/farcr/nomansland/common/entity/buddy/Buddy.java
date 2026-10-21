package com.farcr.nomansland.common.entity.buddy;

import com.farcr.nomansland.common.entity.variant_action.SetBuddyMushroom;
import com.farcr.nomansland.common.registry.NMLRegistries;
import com.farcr.nomansland.common.registry.NMLSounds;
import com.farcr.nomansland.common.registry.entities.NMLEffects;
import com.mojang.serialization.Dynamic;
import dev.tazer.mixed_litter.MLRegistries;
import dev.tazer.mixed_litter.VariantUtil;
import dev.tazer.mixed_litter.variants.Variant;
import dev.tazer.mixed_litter.variants.VariantGroup;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkSource;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class Buddy extends PathfinderMob implements Npc {

    public static final float DIVIDE_TIME_CONSTANT = 1.9333F;
    private static final EntityDataAccessor<Integer> DATA_ASCENSION_TICKS =
        SynchedEntityData.defineId(Buddy.class, EntityDataSerializers.INT);
    private Registry<BuddyFood> buddyFoods;
    private void setBuddyFood(Registry<BuddyFood> registry) {
        this.buddyFoods = registry;
    }

    public Buddy(EntityType<? extends Buddy> entityType, Level level) {
        super(entityType, level);

        level.registryAccess().registry(NMLRegistries.BUDDY_FOOD_KEY)
            .ifPresent(this::setBuddyFood);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ASCENSION_TICKS, -1);
    }

    @Override public boolean shouldStayCloseToLeashHolder() { return false; }

    private static final int SUSPICIOUS_STEW_MULTIPLIER = 10;

    private static final int HOME_RADIUS = 12;
    private static final int KIDNAPPED_DISTANCE = 64;

    private BlockPos anchorPosition;
    public boolean isNaturallySpawned() {
        return (anchorPosition != null);
    }

    public static final List<String> COPY_ON_RESPAWN = List.of(
        "CustomName", "neoforge:attachments"
    );

    public void prepareAnchor(BlockPos anchorPosition) {
        this.anchorPosition = anchorPosition;
        this.restrictTo(anchorPosition, HOME_RADIUS);
    }

    private void keepPathingNearAnchor() {
        if (anchorPosition == null) return;
        if (!anchorPosition.closerToCenterThan(this.position(), KIDNAPPED_DISTANCE)) return;

        Optional<WalkTarget> walkTarget = this.getBrain().getMemory(MemoryModuleType.WALK_TARGET);
        if (walkTarget.isEmpty()) return;

        Vec3 anchorCenter = Vec3.atCenterOf(anchorPosition);
        Vec3 target = Vec3.atCenterOf(walkTarget.get().getTarget().currentBlockPosition());
        double distance = target.distanceTo(anchorCenter);
        if (distance <= HOME_RADIUS) return;

        Vec3 clamped = anchorCenter.add(target.subtract(anchorCenter).scale(HOME_RADIUS / distance));
        this.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(
            BlockPos.containing(clamped), walkTarget.get().getSpeedModifier(), walkTarget.get().getCloseEnoughDist()));
    }

    public int getAscensionTicks() {
        return this.entityData.get(DATA_ASCENSION_TICKS);
    }

    public void setAscensionTicks(int ticks) {
        this.entityData.set(DATA_ASCENSION_TICKS, ticks);
    }

    public boolean isAscending() {
        return getAscensionTicks() >= 0;
    }

    LivingEntity followTarget;
    int followTimer = 0;

    public Block getMushroomBlock() {
        SetBuddyMushroom action = (SetBuddyMushroom) VariantUtil.findAction(this, SetBuddyMushroom.class);
        if (action != null && action.getBlock() != null)
            return action.getBlock();
        return Blocks.RED_MUSHROOM;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        int mushroomCount = 1 + this.random.nextInt(3);
        for (int i = 0; i < mushroomCount; i++)
            this.spawnAtLocation(new ItemStack(getMushroomBlock()));
    }

    public String getVariantName() {
        for (Variant variant : VariantUtil.getVariants(this)) {
            VariantGroup group = VariantUtil.getGroup(this, variant);
            if (group == null)
                continue;
            ResourceLocation groupKey = registryAccess().registryOrThrow(MLRegistries.VARIANT_GROUP_KEY).getKey(group);
            if (groupKey == null || !groupKey.getPath().equals("buddy"))
                continue;
            ResourceLocation variantKey = registryAccess().registryOrThrow(MLRegistries.VARIANT_KEY).getKey(variant);
            if (variantKey == null)
                continue;
            String path = variantKey.getPath();
            int lastSlash = path.lastIndexOf('/');
            return lastSlash >= 0 ? path.substring(lastSlash + 1) : path;
        }
        return "red";
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return NMLSounds.BUDDY_AMBIENT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return NMLSounds.BUDDY_HURT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return NMLSounds.BUDDY_DEATH.get();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        if (anchorPosition != null)
            tag.put("BuddyAnchorPosition", NbtUtils.writeBlockPos(anchorPosition));
        super.addAdditionalSaveData(tag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        NbtUtils.readBlockPos(tag, "BuddyAnchorPosition").ifPresent(this::prepareAnchor);
        super.readAdditionalSaveData(tag);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 50f)
            .add(Attributes.KNOCKBACK_RESISTANCE, -.5f);
    }

    @Override
    public void tick() {
        if (this.isAlive()) {
            if (isAscending()) {
                if (getHealth() <= 1.5f)
                    setHealth(1.5f);
            }
            setHealth(Math.min(getHealth() + (1f / 20f), getMaxHealth()));
        }
        super.tick();

        if (level().isClientSide) {
            crouchTimer = Math.max(0, crouchTimer - 1);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean result = super.hurt(source, amount);
        if (isAscending() && getHealth() <= 1.5f)
            setHealth(1.5f);
        return result;
    }

    @Override
    public boolean isPushable() {
        return !isAscending() && super.isPushable();
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
    }

    @Override
    protected void customServerAiStep() {
        if (isAscending())
            return;
        ServerLevel level = (ServerLevel) this.level();
        getBrain().tick(level, this);
        keepPathingNearAnchor();
        BuddyAI.updateActivity(this);
        super.customServerAiStep();
    }

    private int crouchTimer = 0;
    public void crouch() {
        crouchTimer += 6;
    }

    @Override public boolean isCrouching() {
        return (crouchTimer > 0);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (isAscending())
            return InteractionResult.PASS;
        if (this.isAlive() && buddyFoods != null) {
            ItemStack itemstack = player.getItemInHand(hand);
            Optional<BuddyFood> foodResult = buddyFoods.stream().filter(
                (foodType) -> foodType.item().contains(itemstack.getItemHolder())
            ).findFirst();
            if (foodResult.isPresent()) {
                if (!this.level().isClientSide) {
                    if (!player.hasInfiniteMaterials()) {
                        Optional<ItemStack> resultingItem = Optional.empty();
                        FoodProperties foodProperties = itemstack.getFoodProperties(this);
                        if (foodProperties != null) resultingItem = foodProperties.usingConvertsTo();
                        itemstack.shrink(1);
                        if (resultingItem.isPresent()) {
                            ItemStack result = resultingItem.get().copy();
                            if (itemstack.isEmpty())
                                player.setItemInHand(hand, result);
                            else if (!player.getInventory().add(result))
                                player.drop(result, false);
                        }
                    }

                    this.playSound(
                        SoundEvents.PLAYER_BURP,
                        0.5F + 0.5F * (float) this.random.nextInt(2),
                        (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F
                    );

                    if (itemstack.is(Items.SUSPICIOUS_STEW)) {
                        SuspiciousStewEffects stew = itemstack.getOrDefault(
                            DataComponents.SUSPICIOUS_STEW_EFFECTS, SuspiciousStewEffects.EMPTY);
                        for (SuspiciousStewEffects.Entry effect : stew.effects()) {
                            MobEffectInstance susEffect = effect.createEffectInstance();
                            this.addEffect(
                                new MobEffectInstance(
                                    susEffect.getEffect(),
                                    susEffect.getDuration() * SUSPICIOUS_STEW_MULTIPLIER,
                                    susEffect.getAmplifier(),
                                    false, true
                                )
                            );
                        }
                    }
                }
                if (!this.level().isClientSide) {
                    MobEffectInstance effectInstance = new MobEffectInstance(
                        NMLEffects.HAPPINESS, foodResult.get().happinessTicks(),
                        0, true, false);
                    this.addEffect(effectInstance);
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
        }
        return super.mobInteract(player, hand);
    }

    public static void setupAnimationHappy(
        Entity entity, ModelPart head, ModelPart hat,
        ModelPart leftArm, ModelPart rightArm,
        ModelPart leftLeg, ModelPart rightLeg,
        float time
    ) {
        head.yRot = (float) Math.sin(time * 0.83D);
        head.xRot = (float) Math.sin(time) * 0.8F;
        hat.yRot = head.yRot;
        hat.xRot = head.xRot;

        rightArm.xRot = (float) Math.sin(time * 0.6662D + Math.PI) * 2.0F;
        rightArm.zRot = (float) (Math.sin(time * 0.2312D) + 1.0D);
        leftArm.xRot = (float) Math.sin(time * 0.6662D) * 2.0F;
        leftArm.zRot = (float) (Math.sin(time * 0.2812D) - 1.0D);
        rightLeg.xRot = (float) Math.sin(time * 0.6662D) * 1.4F;
        leftLeg.xRot = (float) Math.sin(time * 0.6662D + Math.PI) * 1.4F;
    }

    public static float getHappinessYDisplacement(float time) {
        return (float)(-Math.abs(Math.sin(time * 0.6662)) * 0.4f);
    }

    @Override
    protected Brain.Provider<Buddy> brainProvider() {
        return BuddyAI.brainProvider();
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return BuddyAI.makeBrain(brainProvider().makeBrain(dynamic));
    }

    @Override
    public Brain<Buddy> getBrain() {
        return (Brain<Buddy>) super.getBrain();
    }

    public static boolean checkBuddySpawnRules(
        EntityType<? extends Buddy> buddy,
        LevelAccessor level, MobSpawnType spawnType,
        BlockPos pos, RandomSource random
    ) {
        return level.getBlockState(pos.above()).isAir();
    }

    @Override public void remove(Entity.RemovalReason reason) {
        if (isNaturallySpawned() && this.level() instanceof ServerLevel serverLevel) {
            if (reason.shouldDestroy() && !isAscending())
                BuddyChunkAnchor.getOrDefault(serverLevel).queryRespawn(anchorPosition, this);
        }
        super.remove(reason);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) { return false; }
}
