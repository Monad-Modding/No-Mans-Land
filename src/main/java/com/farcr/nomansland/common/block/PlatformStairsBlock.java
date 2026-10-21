package com.farcr.nomansland.common.block;

import com.mojang.serialization.MapCodec;
import com.farcr.nomansland.common.registry.NMLSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

public class PlatformStairsBlock extends Block implements SimpleWaterloggedBlock {

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty UNSTABLE = BlockStateProperties.UNSTABLE;
    protected static final VoxelShape SHAPE_NORTH = Shapes.or(box(0.0D, 12.0D, 0.0D, 16.0D, 16.0D, 8.0D),box(0.0D, 4.0D, 8.0D, 16.0D, 8.0D, 16.0D));
    protected static final VoxelShape SHAPE_SOUTH = Shapes.or(box(0.0D, 4.0D, 0.0D, 16.0D, 8.0D, 8.0D),box(0.0D, 12.0D, 8.0D, 16.0D, 16.0D, 16.0D));
    protected static final VoxelShape SHAPE_EAST = Shapes.or(box(0.0D, 4.0D, 0.0D, 8.0D, 8.0D, 16.0D),box(8.0D, 12.0D, 0.0D, 16.0D, 16.0D, 16.0D));
    protected static final VoxelShape SHAPE_WEST = Shapes.or(box(0.0D, 12.0D, 0.0D, 8.0D, 16.0D, 16.0D),box(8.0D, 4.0D, 0.0D, 16.0D, 8.0D, 16.0D));

    public PlatformStairsBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(WATERLOGGED, false).setValue(UNSTABLE, false));
    }

    @Override
    public MapCodec<PlatformStairsBlock> codec() {
        return simpleCodec(PlatformStairsBlock::new);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
        switch (state.getValue(FACING)) {
            case NORTH:
            default:
                return SHAPE_NORTH;
            case SOUTH:
                return SHAPE_SOUTH;
            case EAST:
                return SHAPE_EAST;
            case WEST:
                return SHAPE_WEST;
        }
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
        return defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection())
                .setValue(WATERLOGGED, fluidstate.getType() == Fluids.WATER);
    }

    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (entity.onGround() && state.getValue(UNSTABLE)) {
            level.scheduleTick(pos, this, 1);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        PlatformBlock.collapse(level, pos, random);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED, FACING, UNSTABLE);
    }
}

