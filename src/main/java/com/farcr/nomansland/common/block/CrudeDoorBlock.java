package com.farcr.nomansland.common.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class CrudeDoorBlock extends DoorBlock {

    private static final VoxelShape LOWER_PANEL_NORTH = Block.box(0, 3, 0, 16, 29, 3.0);
    private static final VoxelShape LOWER_PANEL_SOUTH = Block.box(0, 3, 13, 16, 29, 16.0);
    private static final VoxelShape LOWER_PANEL_WEST = Block.box(0, 3, 0, 3, 29, 16.0);
    private static final VoxelShape LOWER_PANEL_EAST = Block.box(13, 3, 0, 16, 29, 16.0);

    private static final VoxelShape UPPER_PANEL_NORTH = Block.box(0, -13, 0, 16, 13, 3.0);
    private static final VoxelShape UPPER_PANEL_SOUTH = Block.box(0, -13, 13, 16, 13, 16.0);
    private static final VoxelShape UPPER_PANEL_WEST = Block.box(0, -13, 0, 3, 13, 16.0);
    private static final VoxelShape UPPER_PANEL_EAST = Block.box(13, -13, 0, 16, 13, 16.0);

    public CrudeDoorBlock(BlockSetType blockSetType, Properties properties) {
        super(blockSetType, properties);
    }

    public static final MapCodec<CrudeDoorBlock> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(BlockSetType.CODEC.fieldOf("block_set_type").forGetter(CrudeDoorBlock::type), propertiesCodec())
                    .apply(instance, CrudeDoorBlock::new)
    );

    @Override
    public MapCodec<? extends DoorBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction panelDir;
        if (!state.getValue(OPEN)) {
            panelDir = state.getValue(FACING).getOpposite();
        } else if (state.getValue(HINGE) == DoorHingeSide.LEFT) {
            panelDir = state.getValue(FACING).getCounterClockWise();
        } else {
            panelDir = state.getValue(FACING).getClockWise();
        }
        boolean isLower = state.getValue(HALF) == DoubleBlockHalf.LOWER;
        return switch (panelDir) {
            case NORTH -> isLower ? LOWER_PANEL_NORTH : UPPER_PANEL_NORTH;
            case SOUTH -> isLower ? LOWER_PANEL_SOUTH : UPPER_PANEL_SOUTH;
            case EAST -> isLower ? LOWER_PANEL_EAST : UPPER_PANEL_EAST;
            default -> isLower ? LOWER_PANEL_WEST : UPPER_PANEL_WEST;
        };
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    private Direction getHingeDirection(BlockState state) {
        Direction facing = state.getValue(FACING);
        return state.getValue(HINGE) == DoorHingeSide.LEFT
                ? facing.getCounterClockWise()
                : facing.getClockWise();
    }

    private boolean hasHingeSupport(LevelReader level, BlockPos pos, Direction hingeDir) {
        BlockPos supportPos = pos.relative(hingeDir);
        BlockState supportState = level.getBlockState(supportPos);
        return supportState.isFaceSturdy(level, supportPos, hingeDir.getOpposite());
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction hingeDir = getHingeDirection(state);
         return hasHingeSupport(level, pos, hingeDir);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null) return null;
        Direction hingeDir = getHingeDirection(state);
        BlockPos pos = context.getClickedPos();
        if (!hasHingeSupport(context.getLevel(), pos, hingeDir) || !hasHingeSupport(context.getLevel(), pos.above(), hingeDir)) {
            return null;
        }
        return state;
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        DoubleBlockHalf half = state.getValue(HALF);
        if (direction.getAxis() == Direction.Axis.Y && (half == DoubleBlockHalf.LOWER) == (direction == Direction.UP)) {
            return neighborState.getBlock() instanceof DoorBlock && neighborState.getValue(HALF) != half
                    ? neighborState.setValue(HALF, half)
                    : Blocks.AIR.defaultBlockState();
        }
        if (!state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }
}
