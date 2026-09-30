package com.farcr.nomansland.common.block;

import com.farcr.nomansland.common.registry.NMLSounds;
import com.mojang.serialization.MapCodec;
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
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import org.jetbrains.annotations.Nullable;

public class PlatformStepBlock extends Block implements SimpleWaterloggedBlock {
    public static final MapCodec<PlatformStepBlock> CODEC = simpleCodec(PlatformStepBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty UNSTABLE = BlockStateProperties.UNSTABLE;

    public PlatformStepBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(UP, false).setValue(WATERLOGGED, false).setValue(UNSTABLE, false));
    }

    @Override
    protected MapCodec<PlatformStepBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        double minY = state.getValue(UP) ? 12.0 : 4.0;
        double maxY = minY + 4.0;
        return switch (state.getValue(FACING)) {
            case SOUTH -> box(0.0, minY, 8.0, 16.0, maxY, 16.0);
            case EAST -> box(8.0, minY, 0.0, 16.0, maxY, 16.0);
            case WEST -> box(0.0, minY, 0.0, 8.0, maxY, 16.0);
            default -> box(0.0, minY, 0.0, 16.0, maxY, 8.0);
        };
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Vec3 click = context.getClickLocation().subtract(pos.getX(), pos.getY(), pos.getZ());
        Direction facing = context.getHorizontalDirection();
        boolean farHalf = switch (facing) {
            case SOUTH -> click.z >= 0.5;
            case EAST -> click.x >= 0.5;
            case WEST -> click.x < 0.5;
            default -> click.z < 0.5;
        };
        if (!farHalf) {
            facing = facing.getOpposite();
        }
        return this.defaultBlockState().setValue(FACING, facing).setValue(UP, click.y > 0.5).setValue(WATERLOGGED, context.getLevel().getFluidState(pos).getType() == Fluids.WATER);
    }

    @Override
    public BlockState getToolModifiedState(BlockState state, UseOnContext context, ItemAbility itemAbility, boolean simulate) {
        if (itemAbility.equals(ItemAbilities.SHEARS_TRIM) && !state.getValue(UNSTABLE)) {
            context.getLevel().playSound(null, context.getClickedPos(), NMLSounds.WOODEN_PLATFORM_CRACKS.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            return state.setValue(UNSTABLE, true);
        }
        return null;
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (level instanceof ServerLevel serverLevel && entity.onGround() && state.getValue(UNSTABLE)) {
            PlatformBlock.collapse(serverLevel, pos, level.getRandom());
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        PlatformBlock.collapse(level, pos, random);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, UP, WATERLOGGED, UNSTABLE);
    }
}
