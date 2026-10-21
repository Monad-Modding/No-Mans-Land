package com.farcr.nomansland.common.block;

import com.farcr.nomansland.NMLConfig;
import com.farcr.nomansland.common.blockentity.WardingEffigyBlockEntity;
import com.farcr.nomansland.common.world.saved_data.WardedSpacesData;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class WardingEffigyBlock extends BaseEntityBlock {
    public static final IntegerProperty EFFIGIES = IntegerProperty.create("effigies", 1, 4);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    protected static final VoxelShape Z_ONE_AABB = Block.box(5.5, 0, 5.5, 10.5, 10, 10.5);
    protected static final VoxelShape Z_TWO_AABB = Block.box(1.5, 0, 5.5, 14.5, 10, 10.5);
    protected static final VoxelShape Z_FULL_AABB = Block.box(1.5, 0, 1.5, 14.5, 10, 14.5);
    protected static final VoxelShape X_ONE_AABB = Block.box(5.5, 0, 5.5, 10.5, 10, 10.5);
    protected static final VoxelShape X_TWO_AABB = Block.box(5.5, 0, 1.5, 10.5, 10, 14.5);
    protected static final VoxelShape X_FULL_AABB = Block.box(1.5, 0, 1.5, 14.5, 10, 14.5);

    public WardingEffigyBlock(final Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(EFFIGIES, 1));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(WardingEffigyBlock::new);
    }

    @Override
    public @NotNull RenderShape getRenderShape(final BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    public BlockState getStateForPlacement(final BlockPlaceContext context) {
        final BlockEntity blockEntity = context.getLevel().getBlockEntity(context.getClickedPos());
        final BlockState state;
        if (blockEntity != null) {
            state = blockEntity.getBlockState();
        } else {
            state = context.getLevel().getBlockState(context.getClickedPos());
        }

        if (state.is(this)) {
            return state.setValue(EFFIGIES, Math.min(4, state.getValue(EFFIGIES) + 1));
        }

        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    public static int getRange(final BlockState state) {
        final int i = state.getValue(EFFIGIES);
        return NMLConfig.WARDING_EFFIGY_BASE_RANGE.getAsInt()+NMLConfig.WARDING_EFFIGY_RANGE_PER_EFFIGY.getAsInt()*(i-1)-2*(i-1)*(i-2);
    }

    protected boolean mayPlaceOn(final BlockState state, final BlockGetter level, final BlockPos pos) {
        return !state.getCollisionShape(level, pos).getFaceShape(Direction.UP).isEmpty() || state.isFaceSturdy(level, pos, Direction.UP);
    }

    protected boolean canSurvive(final BlockState state, final LevelReader level, final BlockPos pos) {
        final BlockPos blockpos = pos.below();
        return this.mayPlaceOn(level.getBlockState(blockpos), level, blockpos);
    }

    protected VoxelShape getShape(final BlockState state, final BlockGetter level, final BlockPos pos, final CollisionContext context) {
        return switch (state.getValue(EFFIGIES)) {
            case 2 -> switch (state.getValue(FACING)) {
                case EAST, WEST -> X_TWO_AABB;
                default -> Z_TWO_AABB;
            };
            case 3, 4 -> switch (state.getValue(FACING)) {
                case EAST, WEST -> X_FULL_AABB;
                default -> Z_FULL_AABB;
            };
            default -> switch (state.getValue(FACING)) {
                case EAST, WEST -> X_ONE_AABB;
                default -> Z_ONE_AABB;
            };
        };
    }

    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, EFFIGIES);
    }

    @Override
    protected BlockState rotate(final BlockState state, final Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(final BlockState state, final Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    protected boolean isPathfindable(final BlockState state, final PathComputationType pathComputationType) {
        return false;
    }

    protected boolean canBeReplaced(final BlockState state, final BlockPlaceContext useContext) {
        return !useContext.isSecondaryUseActive() && useContext.getItemInHand().is(this.asItem()) && state.getValue(EFFIGIES) < 4 || super.canBeReplaced(state, useContext);
    }

    @Override
    protected void onRemove(final BlockState state, final Level level, final BlockPos pos, final BlockState newState, final boolean movedByPiston) {
        if (level instanceof final ServerLevel serverLevel) {
            final WardedSpacesData wardedSpacesData = serverLevel.getDataStorage().computeIfAbsent(new SavedData.Factory<>(
                    WardedSpacesData::new, WardedSpacesData::load), WardedSpacesData.NAME);

            wardedSpacesData.removeEffigy(pos);
        }

        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected void onPlace(final BlockState state, final Level level, final BlockPos pos, final BlockState oldState, final boolean movedByPiston) {
        if (level instanceof final ServerLevel serverLevel) {
            final WardedSpacesData wardedSpacesData = WardedSpacesData.get(serverLevel);

            wardedSpacesData.addEffigy(pos, getRange(state));
        }

        super.onPlace(state, level, pos, oldState, movedByPiston);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state) {
        return new WardingEffigyBlockEntity(pos, state);
    }
}
