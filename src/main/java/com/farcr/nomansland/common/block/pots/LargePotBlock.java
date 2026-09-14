package com.farcr.nomansland.common.block.pots;

import com.mojang.serialization.MapCodec;
import com.farcr.nomansland.common.blockentity.PotBlockEntity;
import com.farcr.nomansland.common.entity.FallingPotEntity;
import com.farcr.nomansland.common.registry.NMLRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

public class LargePotBlock extends PotBlock {

    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    private static final VoxelShape LARGE_FALLBACK = Shapes.or(
            Shapes.box(0, 2.0 / 16, 0, 1, 21.0 / 16, 1),
            Shapes.box(3.0 / 16, 22.0 / 16, 3.0 / 16, 13.0 / 16, 25.0 / 16, 13.0 / 16)
    );

    public LargePotBlock(Properties properties) {
        super(PotSize.LARGE, properties);
        registerDefaultState(defaultBlockState().setValue(HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    public MapCodec<LargePotBlock> codec() {
        return simpleCodec(LargePotBlock::new);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HALF);
    }

    public boolean isUpper(BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.UPPER;
    }

    private BlockPos getLowerPos(BlockState state, BlockPos pos) {
        return isUpper(state) ? pos.below() : pos;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        if (pos.getY() >= level.getMaxBuildHeight() - 1 || !level.getBlockState(pos.above()).canBeReplaced(context)) {
            return null;
        }
        return super.getStateForPlacement(context);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (isUpper(state) && direction == Direction.DOWN && (!neighborState.is(this) || isUpper(neighborState))) {
            return Blocks.AIR.defaultBlockState();
        }
        if (!isUpper(state) && direction == Direction.UP && (!neighborState.is(this) || !isUpper(neighborState))) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (isUpper(state)) return;
        if (!movedByPiston) {
            BlockPos abovePos = pos.above();
            boolean upperWaterlogged = level.getFluidState(abovePos).getType() == Fluids.WATER;
            level.setBlock(abovePos, state.setValue(HALF, DoubleBlockHalf.UPPER).setValue(BlockStateProperties.WATERLOGGED, upperWaterlogged), 3);
        }
        super.onPlace(state, level, pos, oldState, movedByPiston);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && isUpper(state)) {
            BlockPos lowerPos = pos.below();
            BlockState lowerState = level.getBlockState(lowerPos);
            if (lowerState.is(this) && !isUpper(lowerState)) {
                super.playerWillDestroy(level, lowerPos, lowerState, player);
                if (!player.isCreative()) {
                    BlockEntity pot = level.getBlockEntity(lowerPos);
                    dropResources(lowerState, level, lowerPos, pot, player, player.getMainHandItem());
                }
                level.removeBlock(lowerPos, false);
            }
            return state;
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (isUpper(state)) return;
        if (!state.is(newState.getBlock())) {
            BlockState above = level.getBlockState(pos.above());
            if (above.is(this) && isUpper(above)) {
                level.removeBlock(pos.above(), false);
            }
            super.onRemove(state, level, pos, newState, movedByPiston);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (isUpper(state)) return;
        if (state.getValue(BlockStateProperties.POWERED)) {
            level.setBlock(pos, state.setValue(BlockStateProperties.POWERED, false), 2);
            level.updateNeighborsAt(pos, this);
        }
        if (PotBlock.isFree(level.getBlockState(pos.below())) && pos.getY() >= level.getMinBuildHeight()) {
            if (level.getBlockEntity(pos) instanceof PotBlockEntity pot && pot.isLiving()) {
                BlockState above = level.getBlockState(pos.above());
                if (above.is(this) && isUpper(above)) {
                    level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
                }
                pot.wakeUpSilent();
                return;
            }
            CompoundTag beData = null;
            ResourceLocation variantId = null;
            if (level.getBlockEntity(pos) instanceof PotBlockEntity pot) {
                beData = pot.saveCustomOnly(level.registryAccess());
                if (pot.variant != null) {
                    variantId = level.registryAccess().registryOrThrow(NMLRegistries.POT_VARIANT_KEY).getKey(pot.variant);
                }
                pot.skipBreakEffects = true;
            }
            BlockState above = level.getBlockState(pos.above());
            if (above.is(this) && isUpper(above)) {
                level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
            }
            FallingPotEntity falling = FallingPotEntity.fall(level, pos, state.setValue(HALF, DoubleBlockHalf.LOWER));
            if (beData != null) {
                falling.blockData = beData;
            }
            if (variantId != null) {
                falling.setVariantId(variantId);
            }
        } else {
            level.scheduleTick(pos, this, this.getDelayAfterPlace());
        }
    }

    @Override
    public void onLand(Level level, BlockPos pos, BlockState state, BlockState replaceableState, FallingBlockEntity fallingBlock) {
        super.onLand(level, pos, state, replaceableState, fallingBlock);
        if (level.getBlockState(pos).is(this)) {
            BlockPos abovePos = pos.above();
            boolean upperWaterlogged = level.getFluidState(abovePos).getType() == Fluids.WATER;
            level.setBlock(abovePos, state.setValue(HALF, DoubleBlockHalf.UPPER).setValue(BlockStateProperties.WATERLOGGED, upperWaterlogged), 3);
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (isUpper(state)) return null;
        return super.newBlockEntity(pos, state);
    }

    private static final Map<VoxelShape, VoxelShape> UPPER_SHAPE_CACHE = new ConcurrentHashMap<>();

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        PotBlockEntity pot = level.getBlockEntity(getLowerPos(state, pos)) instanceof PotBlockEntity p ? p : null;
        VoxelShape shape = PotBlock.variantShapeOf(pot, LARGE_FALLBACK);
        if (!isUpper(state)) return shape;
        return UPPER_SHAPE_CACHE.computeIfAbsent(shape, s -> offsetShape(s, -1));
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return PotBlock.collisionShapeOf(getShape(state, level, pos, context));
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return super.useItemOn(stack, state, level, getLowerPos(state, pos), player, hand, hit);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return super.useWithoutItem(state, level, getLowerPos(state, pos), player, hit);
    }

    @Override
    public void attack(BlockState state, Level level, BlockPos pos, Player player) {
        super.attack(state, level, getLowerPos(state, pos), player);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, getLowerPos(state, pos), entity);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        super.stepOn(level, getLowerPos(state, pos), state, entity);
    }

    @Override
    protected void onProjectileHit(Level level, BlockState state, BlockHitResult hit, Projectile projectile) {
        super.onProjectileHit(level, state, new BlockHitResult(hit.getLocation(), hit.getDirection(), getLowerPos(state, hit.getBlockPos()), hit.isInside()), projectile);
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return super.getCloneItemStack(level, getLowerPos(state, pos), state);
    }

    @Override
    protected void onExplosionHit(BlockState state, Level level, BlockPos pos, Explosion explosion, BiConsumer<ItemStack, BlockPos> dropConsumer) {
        if (isUpper(state)) {
            BlockPos lowerPos = pos.below();
            BlockState lowerState = level.getBlockState(lowerPos);
            if (lowerState.is(this) && !isUpper(lowerState)) {
                super.onExplosionHit(lowerState, level, lowerPos, explosion, dropConsumer);
                return;
            }
        }
        super.onExplosionHit(state, level, pos, explosion, dropConsumer);
    }

    @Override
    public void onCaughtFire(BlockState state, Level level, BlockPos pos, @Nullable Direction direction, @Nullable LivingEntity igniter) {
        super.onCaughtFire(state, level, getLowerPos(state, pos), direction, igniter);
    }

    @Override
    public int getExpDrop(BlockState state, LevelAccessor level, BlockPos pos, BlockEntity blockEntity, Entity breaker, ItemStack tool) {
        BlockPos lowerPos = getLowerPos(state, pos);
        BlockEntity pot = isUpper(state) ? level.getBlockEntity(lowerPos) : blockEntity;
        return super.getExpDrop(state, level, lowerPos, pot, breaker, tool);
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return super.getAnalogOutputSignal(state, level, getLowerPos(state, pos));
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction side) {
        if (isUpper(state)) {
            BlockPos lowerPos = pos.below();
            BlockState lowerState = level.getBlockState(lowerPos);
            if (lowerState.is(this) && !isUpper(lowerState)) {
                return super.getSignal(lowerState, level, lowerPos, side);
            }
            return 0;
        }
        return super.getSignal(state, level, pos, side);
    }

    @Override
    protected float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        return super.getDestroyProgress(state, player, level, getLowerPos(state, pos));
    }

    @Override
    public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return super.isFlammable(state, level, getLowerPos(state, pos), direction);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (isUpper(state)) return;
        super.animateTick(state, level, pos, random);
    }

    private static VoxelShape offsetShape(VoxelShape shape, double yOffset) {
        final VoxelShape[] holder = { Shapes.empty() };
        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
            holder[0] = Shapes.or(holder[0], Shapes.box(minX, minY + yOffset, minZ, maxX, maxY + yOffset, maxZ));
        });
        return holder[0].isEmpty() ? Shapes.block() : holder[0];
    }
}
