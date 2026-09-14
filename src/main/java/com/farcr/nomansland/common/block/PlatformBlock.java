package com.farcr.nomansland.common.block;

import com.mojang.serialization.MapCodec;
import com.farcr.nomansland.common.registry.NMLSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

public class PlatformBlock extends Block implements SimpleWaterloggedBlock {

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final EnumProperty<Direction.Axis> HORIZONTAL_AXIS = BlockStateProperties.HORIZONTAL_AXIS;
    public static final BooleanProperty UNSTABLE = BlockStateProperties.UNSTABLE;
    protected static final VoxelShape TOP_SHAPE = Block.box(0.0D, 12.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape BOTTOM_SHAPE = Block.box(0.0D, 4.0D, 0.0D, 16.0D, 8.0D, 16.0D);

    public PlatformBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(WATERLOGGED, false).setValue(UP, false).setValue(UNSTABLE, false));
    }

    @Override
    public MapCodec<PlatformBlock> codec() {
        return simpleCodec(PlatformBlock::new);
    }

    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (state.getValue(UP)) {
            return TOP_SHAPE;
        }
        return BOTTOM_SHAPE;
    }

    @Override
    public BlockState getToolModifiedState(BlockState state, UseOnContext context, ItemAbility itemAbility, boolean simulate) {
        if (itemAbility.equals(ItemAbilities.SHEARS_TRIM) && !state.getValue(UNSTABLE)) {
            context.getLevel().playSound(null, context.getClickedPos(), NMLSounds.WOODEN_PLATFORM_CRACKS.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            return state.setValue(UNSTABLE, true);
        }
        return null;
    }

    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    protected BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }

    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluidstate = context.getLevel().getFluidState(context.getClickedPos());
        Direction.Axis axis = context.getHorizontalDirection().getAxis();
        BlockState against = context.getLevel().getBlockState(context.getClickedPos().relative(context.getClickedFace().getOpposite()));
        if (against.is(this)) {
            axis = against.getValue(HORIZONTAL_AXIS);
        }
        return defaultBlockState()
                .setValue(UP, Mth.frac(context.getClickLocation().y)>0.5)
                .setValue(HORIZONTAL_AXIS, axis)
                .setValue(WATERLOGGED, fluidstate.getType() == Fluids.WATER);
    }

    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (level instanceof ServerLevel serverLevel && entity.onGround() && state.getValue(UNSTABLE)) {
            collapse(serverLevel, pos, level.getRandom());
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        collapse(level, pos, random);
    }

    public static void collapse(ServerLevel level, BlockPos pos, RandomSource random) {
        level.playSound(null, pos, NMLSounds.WOODEN_PLATFORM_BREAKS.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        level.destroyBlock(pos, false);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos neighbor = pos.relative(direction);
            BlockState neighborState = level.getBlockState(neighbor);
            if (isPlatform(neighborState) && neighborState.getValue(UNSTABLE) && random.nextFloat() < 0.45F) {
                level.scheduleTick(neighbor, neighborState.getBlock(), 1);
            }
        }
    }

    public static boolean isPlatform(BlockState state) {
        return state.getBlock() instanceof PlatformBlock || state.getBlock() instanceof PlatformStairsBlock;
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return switch (rotation) {
            case CLOCKWISE_90, COUNTERCLOCKWISE_90 -> state.setValue(HORIZONTAL_AXIS,
                    state.getValue(HORIZONTAL_AXIS) == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X);
            default -> state;
        };
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(UP, WATERLOGGED, HORIZONTAL_AXIS, UNSTABLE);
    }
}

