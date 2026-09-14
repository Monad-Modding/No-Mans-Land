package com.farcr.nomansland.common.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BellAttachType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

public class AncientBronzeBellBlock extends Block {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<BellAttachType> ATTACHMENT = BlockStateProperties.BELL_ATTACHMENT;

    private static final VoxelShape BODY = Shapes.or(
            shape(4, 4, 4, 12, 14, 12),
            shape(3, 2, 3, 13, 4, 13),
            shape(7, 2, 7, 9, 10, 9),
            shape(7, 14, 7, 9, 16, 9));

    private static final VoxelShape BAR = Shapes.or(
            shape(0, 12, 6, 2, 16, 10),
            shape(14, 12, 6, 16, 16, 10),
            shape(2, 12, 7, 14, 16, 9));

    private static final VoxelShape POSTS = Shapes.or(
            shape(0, 0, 6, 2, 12, 10),
            shape(14, 0, 6, 16, 12, 10));

    private static final Map<Direction, VoxelShape> FLOOR_SHAPES =
            byFacing(Shapes.or(BODY, BAR, POSTS));
    private static final Map<Direction, VoxelShape> WALL_SHAPES =
            byFacing(Shapes.or(BODY, rotate(BAR, Direction.EAST)));

    public AncientBronzeBellBlock(final Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(ATTACHMENT, BellAttachType.FLOOR));
    }

    public static final MapCodec<AncientBronzeBellBlock> CODEC = simpleCodec(AncientBronzeBellBlock::new);

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ATTACHMENT);
    }

    @Override
    protected VoxelShape getShape(final BlockState state, final BlockGetter level, final BlockPos pos,
                                  final CollisionContext context) {
        final Direction facing = state.getValue(FACING);
        return state.getValue(ATTACHMENT) == BellAttachType.DOUBLE_WALL
                ? WALL_SHAPES.get(facing)
                : FLOOR_SHAPES.get(facing);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context) {
        final Direction clicked = context.getClickedFace();
        final BlockPos pos = context.getClickedPos();
        final LevelReader level = context.getLevel();
        if (clicked.getAxis() == Direction.Axis.Y) {
            final BlockState floor = defaultBlockState()
                    .setValue(ATTACHMENT, BellAttachType.FLOOR)
                    .setValue(FACING, context.getHorizontalDirection());
            return floor.canSurvive(level, pos) ? floor : null;
        }
        final BlockState wall = defaultBlockState()
                .setValue(ATTACHMENT, BellAttachType.DOUBLE_WALL)
                .setValue(FACING, clicked.getOpposite());
        return wall.canSurvive(level, pos) ? wall : null;
    }

    @Override
    protected boolean canSurvive(final BlockState state, final LevelReader level, final BlockPos pos) {
        if (state.getValue(ATTACHMENT) == BellAttachType.DOUBLE_WALL) {
            final Direction along = state.getValue(FACING);
            return sturdy(level, pos, along) && sturdy(level, pos, along.getOpposite());
        }
        final BlockPos below = pos.below();
        return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
    }

    private static boolean sturdy(final LevelReader level, final BlockPos pos, final Direction side) {
        final BlockPos support = pos.relative(side);
        return level.getBlockState(support).isFaceSturdy(level, support, side.getOpposite());
    }

    @Override
    protected BlockState updateShape(final BlockState state, final Direction direction, final BlockState neighbour,
                                     final net.minecraft.world.level.LevelAccessor level, final BlockPos pos,
                                     final BlockPos neighbourPos) {
        return canSurvive(state, level, pos) ? state
                : net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
    }

    private static VoxelShape shape(final double x1, final double y1, final double z1,
                                  final double x2, final double y2, final double z2) {
        return Shapes.box(x1 / 16.0, y1 / 16.0, z1 / 16.0, x2 / 16.0, y2 / 16.0, z2 / 16.0);
    }

    private static Map<Direction, VoxelShape> byFacing(final VoxelShape north) {
        final Map<Direction, VoxelShape> shapes = new EnumMap<>(Direction.class);
        for (final Direction facing : Direction.Plane.HORIZONTAL) {
            shapes.put(facing, rotate(north, facing));
        }
        return shapes;
    }

    private static VoxelShape rotate(final VoxelShape shape, final Direction facing) {
        VoxelShape turned = shape;
        for (int step = 0; step < switch (facing) {
            case EAST -> 1;
            case SOUTH -> 2;
            case WEST -> 3;
            default -> 0;
        }; step++) {
            VoxelShape next = Shapes.empty();
            for (final var part : turned.toAabbs()) {
                next = Shapes.or(next, Shapes.box(
                        1.0 - part.maxZ, part.minY, part.minX,
                        1.0 - part.minZ, part.maxY, part.maxX));
            }
            turned = next;
        }
        return turned;
    }
}
