package com.farcr.nomansland.common.entity.barrel;

import com.farcr.nomansland.common.block.PhysicsBarrelBlock;
import com.farcr.nomansland.common.block.PhysicsBarrelCarriers;
import com.farcr.nomansland.common.registry.NMLSounds;
import com.farcr.nomansland.common.registry.entities.NMLEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class PhysicsBarrel extends Entity {
    private static final EntityDataAccessor<BlockState> DATA_STATE = SynchedEntityData.defineId(PhysicsBarrel.class, EntityDataSerializers.BLOCK_STATE);
    private static final EntityDataAccessor<Integer> DATA_FUSE = SynchedEntityData.defineId(PhysicsBarrel.class, EntityDataSerializers.INT);
    private static final double GRAVITY = 0.04;
    private static final double AIR_DRAG = 0.98;
    private static final double GROUND_FRICTION = 0.7;
    private static final double PUNCH_SPEED = 0.6;
    private static final double PUNCH_LIFT = 0.25;
    private static final double STEER_GAIN = 0.4;
    private static final double STEER_MAX_SPEED = 0.6;
    private static final double SETTLE_SPEED = 0.01;
    private static final int SETTLE_TICKS = 20;

    private int restingTicks;

    public PhysicsBarrel(EntityType<? extends PhysicsBarrel> type, Level level) {
        super(type, level);
        this.blocksBuilding = true;
    }

    public static PhysicsBarrel fromBlock(Level level, BlockPos pos, BlockState state, int fuse) {
        PhysicsBarrel barrel = new PhysicsBarrel(NMLEntities.PHYSICS_BARREL.get(), level);
        barrel.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        barrel.xo = barrel.getX();
        barrel.yo = barrel.getY();
        barrel.zo = barrel.getZ();
        barrel.setBlockState(state);
        barrel.setFuse(fuse);
        return barrel;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_STATE, Blocks.AIR.defaultBlockState());
        builder.define(DATA_FUSE, 0);
    }

    public BlockState getBlockState() {
        return this.entityData.get(DATA_STATE);
    }

    public void setBlockState(BlockState state) {
        this.entityData.set(DATA_STATE, state);
    }

    public int getFuse() {
        return this.entityData.get(DATA_FUSE);
    }

    public void setFuse(int fuse) {
        this.entityData.set(DATA_FUSE, fuse);
    }

    public boolean isLit() {
        BlockState state = this.getBlockState();
        return state.hasProperty(PhysicsBarrelBlock.LIT) && state.getValue(PhysicsBarrelBlock.LIT);
    }

    public void ignite(int fuse) {
        if (this.isLit() && this.getFuse() <= fuse) return;
        if (!this.isLit()) {
            this.setBlockState(this.getBlockState().setValue(PhysicsBarrelBlock.LIT, true));
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), NMLSounds.BOMB_FUSED.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        this.setFuse(fuse);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.isNoGravity()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, -GRAVITY, 0.0));
        }
        this.move(MoverType.SELF, this.getDeltaMovement());
        this.setDeltaMovement(this.getDeltaMovement().scale(AIR_DRAG));
        if (this.onGround()) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(GROUND_FRICTION, -0.5, GROUND_FRICTION));
        }
        if (this.level().isClientSide()) {
            if (this.isLit()) {
                this.level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + 1.05, this.getZ(), 0.0, 0.05, 0.0);
            }
            return;
        }
        if (!(this.getBlockState().getBlock() instanceof PhysicsBarrelBlock)) {
            this.discard();
            return;
        }
        if (this.isLit()) {
            int fuse = this.getFuse() - 1;
            this.setFuse(fuse);
            if (fuse <= 0 || this.isInLava() || this.level().getBlockState(this.blockPosition()).getBlock() instanceof BaseFireBlock) {
                this.detonate();
            }
            return;
        }
        this.settle();
    }

    private void settle() {
        if (PhysicsBarrelCarriers.isCarried(this) || !this.onGround() || this.getDeltaMovement().horizontalDistanceSqr() > SETTLE_SPEED * SETTLE_SPEED) {
            this.restingTicks = 0;
            return;
        }
        if (++this.restingTicks < SETTLE_TICKS) return;
        BlockPos pos = this.blockPosition();
        BlockState state = this.getBlockState();
        if (this.level().getBlockState(pos).canBeReplaced() && state.canSurvive(this.level(), pos)) {
            this.level().setBlock(pos, state, Block.UPDATE_ALL);
        } else {
            this.spawnAtLocation(new ItemStack(state.getBlock()));
        }
        this.discard();
    }

    public void detonate() {
        if (!(this.level() instanceof ServerLevel server) || !(this.getBlockState().getBlock() instanceof PhysicsBarrelBlock block)) return;
        Vec3 centre = this.position().add(0.0, 0.5, 0.0);
        this.discard();
        block.getDetonation().blow(server, centre);
        PhysicsBarrelBlock.chain(server, centre);
    }

    public void steer(Player holder) {
        Vec3 goal = holder.getEyePosition().add(holder.getLookAngle().scale(PhysicsBarrelCarriers.HOLD_DISTANCE));
        Vec3 desired = goal.subtract(this.position().add(0.0, 0.5, 0.0)).scale(STEER_GAIN);
        if (desired.length() > STEER_MAX_SPEED) {
            desired = desired.normalize().scale(STEER_MAX_SPEED);
        }
        this.setDeltaMovement(desired.add(0.0, GRAVITY, 0.0));
        this.hurtMarked = true;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.level().isClientSide() || this.isRemoved() || this.isInvulnerableTo(source)) return false;
        if (source.is(DamageTypeTags.IS_EXPLOSION)) {
            this.ignite(PhysicsBarrelBlock.CHAIN_FUSE);
            return true;
        }
        if (!(source.getEntity() instanceof Player player) || source.getDirectEntity() != player) return false;
        if (player.isShiftKeyDown() && !this.isLit()) {
            if (!player.getAbilities().instabuild) {
                this.spawnAtLocation(new ItemStack(this.getBlockState().getBlock()));
            }
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.WOOD_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
            this.discard();
            return true;
        }
        Vec3 look = player.getLookAngle();
        this.setDeltaMovement(this.getDeltaMovement().add(look.x * PUNCH_SPEED, PUNCH_LIFT, look.z * PUNCH_SPEED));
        this.hurtMarked = true;
        return true;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!this.isLit() && PhysicsBarrelBlock.isFirestarter(stack)) {
            this.level().playSound(player, this.getX(), this.getY(), this.getZ(), SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, this.random.nextFloat() * 0.4F + 0.8F);
            if (!this.level().isClientSide()) {
                this.ignite(PhysicsBarrelBlock.MIN_FUSE + this.random.nextInt(PhysicsBarrelBlock.FUSE_SPREAD));
                if (!player.getAbilities().instabuild) {
                    if (stack.isDamageableItem()) {
                        stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
                    } else {
                        stack.shrink(1);
                    }
                }
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }
        if (hand != InteractionHand.MAIN_HAND || !player.getMainHandItem().isEmpty()) return InteractionResult.PASS;
        if (!this.level().isClientSide()) {
            PhysicsBarrelCarriers.grab(player, this);
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide());
    }

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    protected double getDefaultGravity() {
        return GRAVITY;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.setBlockState(NbtUtils.readBlockState(this.level().holderLookup(Registries.BLOCK), tag.getCompound("state")));
        this.setFuse(tag.getInt("fuse"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.put("state", NbtUtils.writeBlockState(this.getBlockState()));
        tag.putInt("fuse", this.getFuse());
    }
}
