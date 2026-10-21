package com.farcr.nomansland.common.entity.cervidae.moose;

import net.minecraft.world.level.gameevent.GameEvent;
import com.farcr.nomansland.client.NMLMooseChargeAttackHandler;
import com.farcr.nomansland.common.entity.cervidae.IAntlers;
import com.farcr.nomansland.common.entity.cervidae.ShedAntlersGoal;
import com.farcr.nomansland.common.registry.NMLSounds;
import com.farcr.nomansland.common.registry.NMLTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class Moose extends PathfinderMob implements PlayerRideable, PlayerRideableJumping, Saddleable, IAntlers {

    private static final EntityDataAccessor<Boolean> DATA_HAS_ANTLERS = SynchedEntityData.defineId(Moose.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_IS_PACIFIED = SynchedEntityData.defineId(Moose.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_IS_SADDLED = SynchedEntityData.defineId(Moose.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_CAN_CHARGE_ATTACK = SynchedEntityData.defineId(Moose.class, EntityDataSerializers.BOOLEAN);

    public static final byte ADD_WARNING_FEEDBACK_EVENT = 15;
    public static final byte ADD_STOMP_FEEDBACK_EVENT = 14;
    public static final byte START_STOMP_EVENT = 13;

    public static final byte ATTACK_EVENT = 12;
    public static final byte CHARGED_ATTACK_START_EVENT = 11;
    public static final byte CHARGED_ATTACK_END_EVENT = 10;

    public static final byte TAME_EVENT = 9;
    public static final byte EAT_EVENT = 8;
    public static final byte REJECT_FOOD_EVENT = 7;
    public static final byte REJECT_SADDLE_EVENT = 6;

    //Controls how long the cooldown period lasts after a player-initiated charge attack
    protected static final int CHARGE_ATTACK_COOLDOWN = 40;
    //Controls how long the cooldown period lasts after a perfectly-timed player-initiated charge attack
    protected static final int CHARGE_ATTACK_PERFECT_COOLDOWN = 20;
    //Controls the multiplier applied to damage dealt when the moose performs a perfectly-timed player-initiated charge attack
    protected static final float CHARGE_ATTACK_PERFECT_DAMAGE = 1.5f;

    //Controls how long a nearby entity has to stay within the stomp radius in order for the Moose to stomp
    protected static final int STOMP_WINDUP = 30;
    //Adds a degree of randomness to when the moose starts it's stomping sequence
    protected static final float STOMP_CHANCE = 0.4f;
    //Controls how close an entity has to be for the moose to consider stomping
    protected static final float STOMP_DISTANCE = 6f;
    //Controls how long the stomp action is considered to be. Should be equal to the animation length
    protected static final int STOMP_DURATION = 15;
    //Controls how long it takes before the moose can stomp again. Also applies after the moose attacks
    protected static final int STOMP_COOLDOWN = 400;
    //Controls how long certain mobs should be scared of the Moose after it stomps
    protected static final int STOMP_FEAR_DURATION = 300;
    //Controls how long after a stomp the moose should start looking for targets that haven't backed off.
    protected static final int STOMP_AGGRO_DELAY = 40;

    //Controls how long the moose will stay on guard after a stomp, during this time if any entity gets close the moose will charge at it
    protected static final int AGGRO_STANCE_DURATION = 300;
    //Controls how far from the moose will consider a nearby target as a threat, keeping their distance, after a stomp
    protected static final float AGGRO_STANCE_DISTANCE = 12f;
    //Controls how long the moose will ignore nearby entities that it would normally attack after a successful attack
    protected static final int AGGRO_TIMEOUT = 100;
    //Controls how long a nearby entity has to stay within the aggro radius in order for the Moose to start attacking them
    protected static final int AGGRO_WINDUP = 60;
    //Adds a degree of randomness to aggro status accumulation.
    protected static final float AGGRO_PROGRESS_CHANCE = 0.8f;
    //Controls how close an entity has to be after the moose stomps for the moose to charge at it
    protected static final float IRRITATED_AGGRO_DISTANCE = 8f;
    //Controls how close an entity has to remain for the moose to continue charging at it. If the entity re-enters this radius, the moose will resume its charge
    protected static final float ACTIVE_AGGRO_DISTANCE = 16f;

    //Multiplies the movement speed of the moose during the stomp state
    protected static final float ACTIVE_STOMP_SPEED_MULTIPLIER = 0.3f;
    //Multiplies the movement speed of the moose while it is being ridden by a player
    protected static final float RIDDEN_SPEED_MULTIPLIER = 1.3f;
    //Multiplies the movement speed of the moose while it is being commanded to attack by a player
    protected static final float CHARGING_ATTACK_SPEED_MULTIPLIER = 1.1f;

    //Controls how far the moose will try to back away from the nearest target after it attacks
    protected static final float BACK_OFF_DISTANCE = 8f;
    //Controls how long the moose will walk away from a target for after attacking
    protected static final int BACK_OFF_DURATION = 100;

    //Controls how far the moose will look at players from
    protected static final float LOOK_DISTANCE = 18f;

    //Controls how long it takes before an un-pacified moose shakes off it's saddle
    protected static final int SADDLE_SHAKEOFF_DELAY = 10;
    //Controls how many carrots must be fed to the moose before rolling for a successful taming attempt
    protected static final int MINIMUM_TAME_ATTEMPTS = 4;
    //Controls the change for the moose to be pacified after being fed a carrot.
    protected static final float SUCCESSFUL_TAME_CHANCE = 0.333f;

    public AnimationState shakeAnimationState = new AnimationState();
    public AnimationState stompAnimationState = new AnimationState();
    public AnimationState warningAnimationState = new AnimationState();

    public AnimationState attackAnimationState = new AnimationState();
    public AnimationState chargedAttackStartAnimationState = new AnimationState();
    public AnimationState chargedAttackHoldAnimationState = new AnimationState();
    public AnimationState chargedAttackEndAnimationState = new AnimationState();

    protected final MooseTargetManagementMemory targetMemory = new MooseTargetManagementMemory();
    protected final MooseMovementData movementData = new MooseMovementData();

    public int antlerTimer;

    public int inAggroRadius;
    public long mostRecentWarning;
    public long mostRecentAttack;

    public boolean isStomping;
    public int stompTimer;
    public long mostRecentStomp;

    private int pacificationStage;
    private int chargeAttackCooldown;

    private int chargeLoopAnimationDelay;

    public Moose(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        moveControl = new MooseMoveControl(this);
    }

    @Override
    public @NotNull MooseMoveControl getMoveControl() {
        return (MooseMoveControl) moveControl;
    }

    public MooseMovementData getMovementData() {
        return movementData;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);

        builder.define(DATA_HAS_ANTLERS, true);
        builder.define(DATA_IS_PACIFIED, false);
        builder.define(DATA_IS_SADDLED, false);
        builder.define(DATA_CAN_CHARGE_ATTACK, true);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        saveAntlerData(compound);

        compound.put("TargetMemory", targetMemory.serializeNBT());

        compound.putInt("InAggroRadius", inAggroRadius);
        compound.putLong("MostRecentWarning", mostRecentWarning);
        compound.putLong("MostRecentAttack", mostRecentAttack);

        compound.putBoolean("IsInStompState", isStomping);
        compound.putInt("StompTimer", stompTimer);
        compound.putLong("MostRecentStomp", mostRecentStomp);

        compound.putInt("PacificationStage", getPacificationStage());
        compound.putInt("ChargeAttackCooldown", getChargeAttackCooldown());

        compound.putBoolean("IsSaddled", isSaddled());
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        readAntlerData(compound);

        targetMemory.deserializeNBT(compound.getCompound("TargetMemory"));

        inAggroRadius = compound.getInt("InAggroRadius");
        mostRecentWarning = compound.getInt("MostRecentWarning");
        mostRecentAttack = compound.getInt("MostRecentAttack");

        isStomping = compound.getBoolean("IsInStompState");
        stompTimer = compound.getInt("StompTimer");
        mostRecentStomp = compound.getInt("MostRecentStomp");

        setPacificationStage(compound.getInt("PacificationStage"));
        setChargeAttackCooldown(compound.getInt("ChargeAttackCooldown"));
        setIsSaddled(compound.getBoolean("IsSaddled"));
    }

    public static float modifyMooseMovedWronglyThreshold() {
        return 0.25f;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.15D)
                .add(Attributes.FOLLOW_RANGE, 20.0D)
                .add(Attributes.ATTACK_DAMAGE, 10.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.ARMOR, 4.0D)
                .add(Attributes.STEP_HEIGHT, 1);
    }

    public static void registerMooseRelatedGoals(Mob otherMob) {
        if (otherMob instanceof Wolf wolf) {
            wolf.goalSelector.addGoal(3, new AvoidEntityGoal<>(wolf, Moose.class, 12.0F, 1.2, 1.2));
        }

        if (otherMob instanceof Monster monster) {
            monster.goalSelector.addGoal(0,
                    new AvoidEntityGoal<>(monster, Moose.class, Moose::shouldHostilesAvoid, 12.0F, 1.1, 1.1, EntitySelector.NO_CREATIVE_OR_SPECTATOR::test));
        }
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        targetSelector.addGoal(0, new NearestAttackableTargetGoal<>(this, LivingEntity.class, true, (e) -> targetMemory.isUpsetAt(e)));

        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new MooseMeleeAttackGoal(this, 2f));
        goalSelector.addGoal(2, new MooseStompGoal(this, STOMP_DISTANCE));
        goalSelector.addGoal(3, new ShedAntlersGoal(this));
        goalSelector.addGoal(4, new MooseBackOffGoal(this, 0.75f, BACK_OFF_DISTANCE));
        goalSelector.addGoal(5, new MooseAggroStanceGoal(this, 0.25f, AGGRO_STANCE_DISTANCE));
        goalSelector.addGoal(6, new MooseShakeOffSaddleGoal(this));
        goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.4f, 0.0003F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, LOOK_DISTANCE, 0.002f));
    }

    @Override
    public boolean hasAntlers() {
        return entityData.get(DATA_HAS_ANTLERS);
    }

    @Override
    public void setHasAntlers(boolean hasAntlers) {
        entityData.set(DATA_HAS_ANTLERS, hasAntlers);
    }

    @Override
    public int getAntlerTimer() {
        return antlerTimer;
    }

    @Override
    public void setAntlerTimer(int antlersAge) {
        this.antlerTimer = antlersAge;
    }

    @Override
    public void onShedAntlers() {
        playSound(NMLSounds.MOOSE_SHEDS_ANTLERS.get(), 0.6F, 1.0F);
    }

    @Override
    public boolean isSaddleable() {
        return true;
    }

    @Override
    public void equipSaddle(@NotNull ItemStack itemStack, @Nullable SoundSource soundSource) {
        setIsSaddled(true);
        playSound(getSaddleSoundEvent(), 0.5F, 1.0F);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (chargeAttackCooldown > 0) {
            setChargeAttackCooldown(chargeAttackCooldown-1);
        }
        if (chargeLoopAnimationDelay > 0) {
            chargeLoopAnimationDelay--;
            if (chargeLoopAnimationDelay == 0) {
                chargedAttackStartAnimationState.stop();
                chargedAttackHoldAnimationState.start(tickCount);
            }
        }
    }

    @Override
    protected void customServerAiStep() {
        targetMemory.tick();
        movementData.update();
        tickStompState();
        if (!isBaby()) {
            regrowLostAntlers(this);
        }
        super.customServerAiStep();
        move(MoverType.SELF, movementData.getMotionVector());
    }

    protected void tickStompState() {
        if (isStomping) {
            stompTimer++;
            if (stompTimer == STOMP_DURATION) {
                finalizeStomp();
            }
            return;
        }
        if (!hasStompedRecently(AGGRO_STANCE_DURATION)) {
            return;
        }
        if (hasAttackedRecently(AGGRO_TIMEOUT)) {
            return;
        }
        if (hasStompedRecently(STOMP_AGGRO_DELAY)) {
            return;
        }
        takeAggroOnNearbyTargets();
    }

    public void takeAggroOnNearbyTargets() {
        var level = level();
        int interval = 4;
        if (level.getGameTime() % interval == 0) {
            float distance = IRRITATED_AGGRO_DISTANCE;
            var attackArea = getBoundingBox().inflate(distance, 3.0, distance);
            var attackTargets = level.getEntitiesOfClass(LivingEntity.class, attackArea, EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(this::shouldAttackAfterStomp));
            if (attackTargets.isEmpty()) {
                if (inAggroRadius > 0) {
                    inAggroRadius = Math.max(inAggroRadius-interval, 0);
                }
            }
            else {
                if (random.nextFloat() > AGGRO_PROGRESS_CHANCE) {
                    return;
                }
                inAggroRadius += interval;
                if (inAggroRadius >= AGGRO_WINDUP/2) {
                    tryShowWarning();
                    if (inAggroRadius >= AGGRO_WINDUP) {
                        for (LivingEntity target : attackTargets) {
                            targetMemory.addTarget(target, 3600);
                        }
                        inAggroRadius = 0;
                    }
                }
            }
        }
    }

    /**
     * All the on-spawn logic.
     * Handles antlers and their lifetime. Same as the deer
     */
    @SuppressWarnings("deprecation")
    @Override
    public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level, @NotNull DifficultyInstance difficulty, @NotNull MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        if (!isBaby() && random.nextFloat() < 0.8) {
            addAntlersUponSpawning(random);
        }

        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    /**
     * Handles networked entity events, including our particle stuff
     */
    @Override
    public void handleEntityEvent(byte id) {
        switch (id) {
            case ADD_WARNING_FEEDBACK_EVENT -> addWarningFeedback();
            case ADD_STOMP_FEEDBACK_EVENT -> addStompFeedback();
            case START_STOMP_EVENT -> addStompSequenceStartFeedback();
            case ATTACK_EVENT -> addAttackSequenceStartFeedback();
            case CHARGED_ATTACK_START_EVENT -> addChargedAttackStartFeedback();
            case CHARGED_ATTACK_END_EVENT -> addChargedAttackEndFeedback();
            case TAME_EVENT -> spawnTamingParticles(true);
            case EAT_EVENT -> spawnTamingParticles(false);
            case REJECT_FOOD_EVENT -> addRejectedFoodFeedback();
            case REJECT_SADDLE_EVENT -> addRejectedSaddleFeedback();
            default -> super.handleEntityEvent(id);
        }
    }


    /**
     * All the logic tied to the Moose taking damage to others
     */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        var attacker = source.getEntity();
        if (attacker instanceof LivingEntity living) {
            if (living.isVehicle()) {
                for (Entity passenger : living.getPassengers()) {
                    if ((isPacified() && passenger instanceof Player) || !isPacified()) {
                        targetMemory.addTarget(passenger);
                    }
                }
                return hurt;
            }
            if (isPacified() && living instanceof Player) {
                return hurt;
            }
            targetMemory.addTarget(living);
        }
        return hurt;
    }

    /**
     * All the logic tied to the Moose hurting anything through any means is handled here.
     */
    @Override
    public boolean doHurtTarget(Entity target) {
        playSound(NMLSounds.MOOSE_SWINGS.get(), 0.5f, 1.5F);
        return doHurtTarget(target, 1f);
    }

    /**
     * All the logic tied to the Moose hurting anything through any means is handled here.
     */
    public boolean doHurtTarget(Entity target, float attackStrength) {
        var damageSource = damageSources().mobAttack(this);
        float damage = (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * attackStrength;
        if (target.hurt(damageSource, damage)) {
            double knockbackResistance = target instanceof LivingEntity living ? living.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE) : 0;
            double knockup = attackStrength * 0.5F * Math.max(0, 1 - knockbackResistance);
            target.setDeltaMovement(target.getDeltaMovement().add(getDeltaMovement()).add(0, knockup, 0));
            if (target instanceof ServerPlayer player) {
                targetMemory.clearAggression(player);
                player.connection.send(new ClientboundSetEntityMotionPacket(player));
            }
            if (level() instanceof ServerLevel serverLevel) {
                EnchantmentHelper.doPostAttackEffects(serverLevel, target, damageSource);
            }
            mostRecentAttack = level().getGameTime();
            return true;
        }
        return false;
    }

    /**
     * All the right-click logic of the Moose is handled here.
     * Runs once for each hand whenever the mob is right-clicked.
     */
    @SuppressWarnings("OptionalIsPresent")
    @Override
    public @NotNull InteractionResult mobInteract(Player player, @NotNull InteractionHand hand) {
        var heldItem = player.getItemInHand(hand);

        var eatInteraction = tryEatAndHeal(player, heldItem);
        if (eatInteraction.isPresent()) {
            return eatInteraction.get();
        }
        var mountInteraction = tryMount(player);
        if (mountInteraction.isPresent()) {
            return mountInteraction.get();
        }
        var tameInteraction = tryTame(player, heldItem);
        if (tameInteraction.isPresent()) {
            return tameInteraction.get();
        }
        return super.mobInteract(player, hand);
    }

    /**
     * Controls whether hostile mobs should avoid the Moose.
     *
     * @param underAllCircumstancesThisShouldBeTheMooseEntity Unless something has gone horribly wrong, this will be the Moose.
     * @return Whether the moose should be avoided at all cost.
     */
    public static boolean shouldHostilesAvoid(LivingEntity underAllCircumstancesThisShouldBeTheMooseEntity) {
        if (underAllCircumstancesThisShouldBeTheMooseEntity instanceof Moose moose) {
            return moose.hasStompedRecently(Moose.STOMP_FEAR_DURATION);
        }
        return false;
    }

    /**
     * Shakes off the equipped saddle, dropping it on the ground and playing the appropriate animation
     */
    public void shakeOffSaddle() {
        setIsSaddled(false);
        ItemEntity itementity = spawnAtLocation(Items.SADDLE, 1);
        if (itementity != null) {
            float x = (random.nextFloat() - random.nextFloat()) * 0.3F;
            float y = 0.2f + random.nextFloat() * 0.05F;
            float z = (random.nextFloat() - random.nextFloat()) * 0.3F;
            itementity.setDeltaMovement(itementity.getDeltaMovement().add(x, y, z));
        }
        level().broadcastEntityEvent(this, REJECT_SADDLE_EVENT);
        playSound(NMLSounds.MOOSE_REJECTS_SADDLE.get(), 1f, 1f);
    }

    /**
     * Enables a player to mount the Moose if it is pacified and saddled.
     *
     * @param player The soon-to-be rider
     * @return An optional containing the result of the interaction if it was successful
     */
    public Optional<InteractionResult> tryMount(Player player) {
        if (!isPacified()) {
            return Optional.empty();
        }
        if (!isSaddled() || isVehicle()) {
            return Optional.empty();
        }
        var isClientSide = level().isClientSide;
        if (!isClientSide) {
            doPlayerRide(player);
        }
        return Optional.of(InteractionResult.sidedSuccess(isClientSide));
    }

    /**
     * Attempts to eat moose food from a player if the moose is pacified, healing after eating the correct item.
     *
     * @param player The soon-to-be owner of the moose
     * @param stack  The offering.
     * @return An optional containing the result of the interaction if it was successful
     */
    public Optional<InteractionResult> tryEatAndHeal(Player player, ItemStack stack) {
        if (!isPacified()) {
            return Optional.empty();
        }
        var level = level();
        boolean isMooseFood = stack.is(NMLTags.MOOSE_FOOD);
        if (getTarget() != null || (!isMooseFood && stack.getFoodProperties(player) != null)) {
            level.broadcastEntityEvent(this, REJECT_FOOD_EVENT);
            playSound(NMLSounds.MOOSE_REJECTS_FOOD.get(), 1f, 1f);
            return Optional.of(InteractionResult.SUCCESS_NO_ITEM_USED);
        }
        if (isMooseFood && getHealth() < getMaxHealth()) {
            var isClientSide = level.isClientSide;
            if (!isClientSide) {
                stack.shrink(1);
                playSound(NMLSounds.MOOSE_EAT.get(), 1f, 1f);
                this.gameEvent(GameEvent.EAT);
                heal(8);
            }

            return Optional.of(InteractionResult.sidedSuccess(isClientSide));
        }
        return Optional.empty();
    }
    /**
     * Attempts to eat moose food from a player, eventually growing pacified and enabling saddling behavior.
     *
     * @param player The soon-to-be owner of the moose
     * @param stack  The offering.
     * @return An optional containing the result of the interaction if it was successful
     */
    public Optional<InteractionResult> tryTame(Player player, ItemStack stack) {
        if (isPacified()) {
            return Optional.empty();
        }
        if (targetMemory.isUpsetAt(player)) {
            return Optional.empty();
        }
        var level = level();
        boolean isMooseFood = stack.is(NMLTags.MOOSE_FOOD);
        if (getTarget() != null || (!isMooseFood && stack.getFoodProperties(player) != null)) {
            level.broadcastEntityEvent(this, REJECT_FOOD_EVENT);
            playSound(NMLSounds.MOOSE_REJECTS_FOOD.get(), 1f, 1f);
            return Optional.of(InteractionResult.SUCCESS_NO_ITEM_USED);
        }
        if (isMooseFood) {
            var isClientSide = level.isClientSide;
            if (!isClientSide) {
                stack.shrink(1);
                playSound(NMLSounds.MOOSE_EAT.get(), 1f, 1f);
                this.gameEvent(GameEvent.EAT);
                int stage = getPacificationStage();
                if (stage < MINIMUM_TAME_ATTEMPTS) {
                    stage++;
                } else {
                    if (random.nextFloat() < SUCCESSFUL_TAME_CHANCE) {
                        stage++;
                        navigation.stop();
                        setTarget(null);
                        brain.getMemory(MemoryModuleType.ANGRY_AT).ifPresent(target -> {
                            if (player.getUUID().equals(target))
                                brain.eraseMemory(MemoryModuleType.ANGRY_AT);
                        });
                    }
                }
                level.broadcastEntityEvent(this, stage == MINIMUM_TAME_ATTEMPTS ? TAME_EVENT : EAT_EVENT);
                setPacificationStage(stage);
            }

            return Optional.of(InteractionResult.SUCCESS);
        }
        return Optional.empty();
    }

    public float getStompAdjustedMovementSpeed(float speed) {
        if (isStomping) {
            //Actively stomping
            return speed * ACTIVE_STOMP_SPEED_MULTIPLIER;
        }
        return speed;
    }

    public boolean canStartStomp() {
        return !isStomping && !hasStompedRecently(STOMP_COOLDOWN);
    }

    public boolean hasShownWarningRecently(int timeframe) {
        return level().getGameTime() - mostRecentWarning < timeframe;
    }

    public boolean hasAttackedRecently(int timeframe) {
        return level().getGameTime() - mostRecentAttack < timeframe;
    }

    public boolean hasStompedRecently(int timeframe) {
        return level().getGameTime() - mostRecentStomp < timeframe;
    }

    public void tryShowWarning() {
        if (hasShownWarningRecently(60)) {
            return;
        }
        level().broadcastEntityEvent(this, Moose.ADD_WARNING_FEEDBACK_EVENT);
        playSound(NMLSounds.MOOSE_SHOWS_WARNING.get(), 1.5f, 1 + random.nextFloat() * 0.3f);
        mostRecentWarning = level().getGameTime();
    }

    public void startStomping() {
        level().broadcastEntityEvent(this, Moose.START_STOMP_EVENT);
        isStomping = true;
    }

    public void finalizeStomp() {
        playSound(NMLSounds.MOOSE_STOMPS.get(), 1.5f, 1 + random.nextFloat() * 0.3f);
        level().broadcastEntityEvent(this, Moose.ADD_STOMP_FEEDBACK_EVENT);
        isStomping = false;
        stompTimer = 0;
        mostRecentStomp = level().getGameTime();
    }

    public boolean shouldAttackAfterStomp(Entity entity) {
        if (targetMemory.isUpsetAt(entity)) {
            return false;
        }
        if (!isPacified()) {
            if (entity instanceof Player player) {
                return !player.getMainHandItem().is(NMLTags.MOOSE_FOOD) && !player.getOffhandItem().is(NMLTags.MOOSE_FOOD);
            }
        }
        return entity instanceof Monster;
    }

    /**
     * Partially Matches behavior from {@link net.minecraft.world.entity.animal.horse.AbstractHorse}
     * The Horse method disables some animation flags that the Moose doesn't use, besides that the method is equal.
     */
    protected void doPlayerRide(Player player) {
        player.setYRot(getYRot());
        player.setXRot(getXRot());
        player.startRiding(this);
    }

    public void onPlayerStartChargingJump() {
        level().broadcastEntityEvent(this, Moose.CHARGED_ATTACK_START_EVENT);
    }

    @Override
    public boolean canJump() {
        return isPacified() && isSaddled();
    }

    @Override
    public int getJumpCooldown() {
        if (level().isClientSide) {
            return entityData.get(DATA_CAN_CHARGE_ATTACK) ? 0 : 1;
        }
        return chargeAttackCooldown;
    }


    @Override
    public void onPlayerJump(int jumpPower) {
    }

    /**
     * Handles the release of the jump-key by the controlling player, dealing damage to all appropriately found nearby entities.
     */
    @Override
    public void handleStartJump(int jumpPower) {
        float delta = jumpPower / 100f;
        var look = getLookAngle();
        float forwards = 1.2f;
        float upwards = 0.6f;
        float horizontal = 0.6f;
        float vertical = 0.8f;
        var area = getBoundingBox().move(look.x * forwards, upwards, look.z * forwards).inflate(horizontal, vertical, horizontal);
        var targets = level().getEntitiesOfClass(LivingEntity.class, area,
                target -> {
                    if (target == this) {
                        return false;
                    }
                    if (!EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(target)) {
                        return false;
                    }
                    var owner = getControllingPassenger();
                    if (owner != null) {
                        if (target == owner || target.isAlliedTo(owner)) {
                            return false;
                        }
                    }
                    return target.isAlive() && hasLineOfSight(target);
                });
        float hurtVolume = 1.5f;
        float attackStrength = delta;
        int cooldown = CHARGE_ATTACK_COOLDOWN;
        var sound = NMLSounds.MOOSE_SWINGS;
        if (attackStrength >= 0.95f && !targets.isEmpty()) {
            attackStrength = CHARGE_ATTACK_PERFECT_DAMAGE;
            cooldown = CHARGE_ATTACK_PERFECT_COOLDOWN;
            sound = NMLSounds.MOOSE_SWINGS_PERFECT;
        }
        else if (targets.isEmpty()) {
            sound = NMLSounds.MOOSE_SWINGS_BLUNDER;
        }
        for (LivingEntity target : targets) {
            if (doHurtTarget(target, attackStrength)) {
                playSound(NMLSounds.MOOSE_HITS_TARGET.get(), hurtVolume, 1.2f + random.nextFloat() * 0.4f);
                hurtVolume *= 0.65f;
            }
        }
        playSound(sound.get(), 0.5f, 0.8f + delta*0.3f);
        setChargeAttackCooldown(cooldown);
        level().broadcastEntityEvent(this, Moose.CHARGED_ATTACK_END_EVENT);
    }

    @Override
    public void handleStopJump() {

    }

    /**
     * Partially Matches behavior from {@link net.minecraft.world.entity.animal.horse.AbstractHorse}
     * All non-jump-mechanic related code is brought over without changes.
     */
    @Override
    protected void tickRidden(@NotNull Player player, @NotNull Vec3 travelVector) {
        super.tickRidden(player, travelVector);
        Vec2 vec2 = getRiddenRotation(player);
        setRot(vec2.y, vec2.x);
        yRotO = yBodyRot = yHeadRot = getYRot();
    }

    /**
     * Matches behavior from {@link net.minecraft.world.entity.animal.horse.AbstractHorse}
     */
    protected Vec2 getRiddenRotation(LivingEntity entity) {
        return new Vec2(entity.getXRot() * 0.5F, entity.getYRot());
    }

    /**
     * Partially matches behavior from {@link net.minecraft.world.entity.animal.horse.AbstractHorse}.
     * The Moose cannot jump and thus the appropriate code tied to jumping behavior is removed.
     */
    @Override
    protected @NotNull Vec3 getRiddenInput(Player player, @NotNull Vec3 travelVector) {
        float x = player.xxa * 0.5F;
        float z = player.zza;
        if (z <= 0.0F) {
            z *= 0.25F;
        }

        return new Vec3(x, 0, z);
    }

    /**
     * Mostly Matches behavior from {@link net.minecraft.world.entity.animal.horse.AbstractHorse}
     */
    @Override
    protected float getRiddenSpeed(@NotNull Player player) {
        float baseSpeed = (float) getAttributeValue(Attributes.MOVEMENT_SPEED);
        if (level().isClientSide) {
            if (NMLMooseChargeAttackHandler.shouldMoveSlower(this)) {
                return baseSpeed * CHARGING_ATTACK_SPEED_MULTIPLIER;
            }
        }
        return baseSpeed * RIDDEN_SPEED_MULTIPLIER;
    }

    /**
     * Copied From & Matches aptly named method in {@link net.minecraft.world.entity.animal.horse.AbstractHorse}
     */
    @Nullable
    private Vec3 getDismountLocationInDirection(Vec3 direction, LivingEntity passenger) {
        double d0 = getX() + direction.x;
        double d1 = getBoundingBox().minY;
        double d2 = getZ() + direction.z;
        var mutable = new BlockPos.MutableBlockPos();

        for (Pose pose : passenger.getDismountPoses()) {
            mutable.set(d0, d1, d2);
            double d3 = getBoundingBox().maxY + 0.75;

            while (true) {
                double d4 = level().getBlockFloorHeight(mutable);
                if ((double) mutable.getY() + d4 > d3) {
                    break;
                }

                if (DismountHelper.isBlockFloorValid(d4)) {
                    AABB aabb = passenger.getLocalBoundsForPose(pose);
                    Vec3 vec3 = new Vec3(d0, (double) mutable.getY() + d4, d2);
                    if (DismountHelper.canDismountTo(level(), passenger, aabb.move(vec3))) {
                        passenger.setPose(pose);
                        return vec3;
                    }
                }

                mutable.move(Direction.UP);
                if ((double) mutable.getY() < d3) {
                    break;
                }
            }
        }

        return null;
    }

    /**
     * Copied From & Matches {@link net.minecraft.world.entity.animal.horse.AbstractHorse}
     */
    public @NotNull Vec3 getDismountLocationForPassenger(LivingEntity livingEntity) {
        float mooseWidth = getBbWidth();
        float passengerWidth = livingEntity.getBbWidth();
        Vec3 collisionVector = getCollisionHorizontalEscapeVector(mooseWidth, passengerWidth, getYRot() + (livingEntity.getMainArm() == HumanoidArm.RIGHT ? 90.0F : -90.0F));
        Vec3 dismountLocation = getDismountLocationInDirection(collisionVector, livingEntity);
        if (dismountLocation != null) {
            return dismountLocation;
        } else {
            Vec3 vec32 = getCollisionHorizontalEscapeVector(mooseWidth, passengerWidth, getYRot() + (livingEntity.getMainArm() == HumanoidArm.LEFT ? 90.0F : -90.0F));
            Vec3 vec33 = getDismountLocationInDirection(vec32, livingEntity);
            return vec33 != null ? vec33 : position();
        }
    }

    @Override
    protected void positionRider(@NotNull Entity passenger, Entity.@NotNull MoveFunction callback) {
        super.positionRider(passenger, callback);
        if (passenger instanceof LivingEntity living) {
            living.yBodyRot = this.yBodyRot;
        }
    }

    public void lookAtAndFaceTarget(Entity target) {
        var moveControl = getMoveControl();
        if (target == null || !target.isAlive()) {
            moveControl.target = null;
            return;
        }
        if (navigation.getPath() != null && !navigation.isDone()) {
            moveControl.replaceBodyDirection(MooseMoveControl.BodyDirection.FACE_TARGET);
        } else {
            faceTarget(target);
        }
        getLookControl().setLookAt(target, 60.0F, 60.0F);
        moveControl.target = target;
    }

    public void faceTarget(Entity target) {
        double xTargetDiff = target.getX() - getX();
        double zTargetDiff = target.getZ() - getZ();
        float toTarget = (float) (Mth.atan2(zTargetDiff, xTargetDiff) * 180.0F / (float) Math.PI) - 90.0F;

        float rate = 8f + movementData.getMotion().length() * 32f;
        setYRot(getMoveControl().rotlerp(getYRot(), toTarget, rate));
    }

    protected void addWarningFeedback() {
        warningAnimationState.start(tickCount);
        float forwardsYaw = yBodyRot - 180F;
        float x = Mth.sin(-forwardsYaw * (float) (Math.PI / 180.0) - (float) Math.PI);
        float z = Mth.cos(-forwardsYaw * (float) (Math.PI / 180.0) - (float) Math.PI);
        float forwardsOffset = getBbWidth() * 0.9f;
        var headPosition = position().add(x * forwardsOffset, getEyeHeight(), z * forwardsOffset);

        float sideX = Mth.sin(-forwardsYaw * (float) (Math.PI / 180.0) - 1.57f);
        float sideZ = Mth.cos(-forwardsYaw * (float) (Math.PI / 180.0) - 4.71f);

        for (int i = 0; i < 20; i++) {
            double sideOffset = 0.4f;
            double xPos = headPosition.x + sideOffset * sideX;
            double yPos = headPosition.y - i * 0.01f;
            double zPos = headPosition.z + sideOffset * sideZ;
            double xVelocity = (0.8f + random.nextFloat() * 0.2f) * x * 0.2f;
            double yVelocity = -0.05F;
            double zVelocity = (0.8f + random.nextFloat() * 0.2f) * z * 0.2f;
            level().addParticle(ParticleTypes.SMOKE, xPos, yPos, zPos, xVelocity, yVelocity, zVelocity);
        }
    }

    protected void addStompFeedback() {
        float forwardsYaw = yBodyRot - 180F;
        float x = Mth.sin(-forwardsYaw * (float) (Math.PI / 180.0) - (float) Math.PI);
        float z = Mth.cos(-forwardsYaw * (float) (Math.PI / 180.0) - (float) Math.PI);
        float offset = getBbWidth() / 2f;
        var stompPosition = position().add(x * offset, 0, z * offset);
        var pos = BlockPos.containing(Math.round(stompPosition.x), Math.round(stompPosition.y) - 1, Math.round(stompPosition.z));
        var state = level().getBlockState(pos);
        var particle = new BlockParticleOption(ParticleTypes.DUST_PILLAR, state);
        level().levelEvent(2001, pos, Block.getId(state));

        for (int i = 0; i < 40; i++) {
            double radialOffset = 0.75f;
            double xPos = stompPosition.x + radialOffset * Math.cos(i) + random.nextGaussian() / 2.0;
            double yPos = stompPosition.y + 0.1f;
            double zPos = stompPosition.z + radialOffset * Math.sin(i) + random.nextGaussian() / 2.0;
            double xVelocity = random.nextGaussian() * 0.05F;
            double yVelocity = random.nextGaussian() * 0.075F;
            double zVelocity = random.nextGaussian() * 0.05F;
            level().addParticle(particle, xPos, yPos, zPos, xVelocity, yVelocity, zVelocity);
        }
    }

    protected void addStompSequenceStartFeedback() {
        stompAnimationState.start(tickCount);
    }

    protected void addAttackSequenceStartFeedback() {
        attackAnimationState.start(tickCount);
    }

    protected void addChargedAttackStartFeedback() {
        chargedAttackStartAnimationState.start(tickCount);
        chargeLoopAnimationDelay = 16;
    }

    protected void addChargedAttackEndFeedback() {
        chargedAttackStartAnimationState.stop();
        chargedAttackHoldAnimationState.stop();
        chargedAttackEndAnimationState.start(tickCount);
    }

    /**
     * Copied From & Matches {@link net.minecraft.world.entity.animal.horse.AbstractHorse}
     */
    protected void spawnTamingParticles(boolean tamed) {
        var particle = tamed ? ParticleTypes.HEART : ParticleTypes.SMOKE;
        double motion = 0.02;
        for (int i = 0; i < 7; i++) {
            double x = random.nextGaussian() * motion;
            double y = random.nextGaussian() * motion;
            double z = random.nextGaussian() * motion;
            level().addParticle(particle, getRandomX(1.0F), getRandomY() + (double) 0.5F, getRandomZ(1.0F), x, y, z);
        }
    }

    protected void addRejectedFoodFeedback() {
        spawnTamingParticles(false);
    }

    protected void addRejectedSaddleFeedback() {
        shakeAnimationState.start(tickCount);
    }

    /**
     * Copied From & Matches {@link net.minecraft.world.entity.animal.horse.AbstractHorse}
     */
    @Override
    public @Nullable LivingEntity getControllingPassenger() {
        if (isSaddled() && isPacified()) {
            if (getFirstPassenger() instanceof Player player) {
                return player;
            }
        }

        return super.getControllingPassenger();
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return NMLSounds.MOOSE_AMBIENT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return NMLSounds.MOOSE_HURT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return NMLSounds.MOOSE_DEATH.get();
    }

    @Override
    protected void playStepSound(@NotNull BlockPos pos, @NotNull BlockState state) {
        playSound(NMLSounds.MOOSE_STEP.get(), 0.15F, 1.0F);
    }

    //Makes the moose produce step sounds less frequently when ridden
    @Override
    protected float nextStep() {
        return this.moveDist + 1.5f;
    }

    @Override
    protected float getSoundVolume() {
        return 0.6F;
    }

    public int getPacificationStage() {
        return pacificationStage;
    }

    public void setPacificationStage(int stage) {
        pacificationStage = stage;
        if (stage >= MINIMUM_TAME_ATTEMPTS) {
            setIsPacified(true);
        }
    }

    public int getChargeAttackCooldown() {
        return chargeAttackCooldown;
    }

    public void setChargeAttackCooldown(int cooldown) {
        if ((chargeAttackCooldown == 0) != (cooldown == 0)) {
            entityData.set(DATA_CAN_CHARGE_ATTACK, cooldown == 0);
        }
        chargeAttackCooldown = cooldown;
    }

    @Override
    public boolean isSaddled() {
        return entityData.get(DATA_IS_SADDLED);
    }

    public boolean isPacified() {
        return entityData.get(DATA_IS_PACIFIED);
    }

    public void setIsSaddled(boolean isSaddled) {
        entityData.set(DATA_IS_SADDLED, isSaddled);
    }

    public void setIsPacified(boolean isPacified) {
        entityData.set(DATA_IS_PACIFIED, isPacified);
    }

    public static boolean checkMooseSpawnRules(
            EntityType<? extends Moose> moose, LevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random
    ) {
        boolean flag = MobSpawnType.ignoresLightRequirements(spawnType) || isBrightEnoughToSpawn(level, pos);
        return level.getBlockState(pos.below()).is(BlockTags.ANIMALS_SPAWNABLE_ON) && flag;
    }

    protected static boolean isBrightEnoughToSpawn(BlockAndTintGetter level, BlockPos pos) {
        return level.getRawBrightness(pos, 0) > 8;
    }
}