package com.farcr.nomansland.common.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class AncestralCarvingBlock extends DirectionalBlock {
    public static final EnumProperty<CarvingFormation> FORMATION = EnumProperty.create("formation", CarvingFormation.class);
    public static final IntegerProperty ROTATION = IntegerProperty.create("rotation", 0, 3);

    public AncestralCarvingBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.UP)
                .setValue(FORMATION, CarvingFormation.SINGLE)
                .setValue(ROTATION, 0));
    }

    @Override
    protected MapCodec<? extends DirectionalBlock> codec() {
        return simpleCodec(AncestralCarvingBlock::new);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, FORMATION, ROTATION);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        if (rotation == Rotation.NONE) return state;
        Direction facing = state.getValue(FACING);
        int rotValue = state.getValue(ROTATION);
        if (facing.getAxis() == Direction.Axis.Y) {
            int delta = switch (rotation) {
                case CLOCKWISE_90 -> 1;
                case CLOCKWISE_180 -> 2;
                case COUNTERCLOCKWISE_90 -> 3;
                default -> 0;
            };
            rotValue = (rotValue + delta) & 3;
        }
        return state.setValue(FACING, rotation.rotate(facing)).setValue(ROTATION, rotValue);
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos placedPos = context.getClickedPos();

        Direction facing;
        int rotation;

        BlockState clicked = level.getBlockState(context.getClickedPos().relative(context.getClickedFace().getOpposite()));
        if (clicked.getBlock() instanceof AncestralCarvingBlock) {
            facing = clicked.getValue(FACING);
            rotation = clicked.getValue(ROTATION);
        } else {
            float pitch = context.getPlayer() != null ? context.getPlayer().getXRot() : 0;
            if (pitch > 60) {
                facing = Direction.UP;
                rotation = getRotationForPlayer(context, facing);
            } else if (pitch < -60) {
                facing = Direction.DOWN;
                rotation = getRotationForPlayer(context, facing);
            } else {
                facing = context.getHorizontalDirection().getOpposite();
                rotation = 0;
            }
        }

        return this.defaultBlockState()
                .setValue(FACING, facing)
                .setValue(FORMATION, CarvingFormation.SINGLE)
                .setValue(ROTATION, rotation);
    }

    public int getRotationForPlayer(BlockPlaceContext context, Direction facing) {
        if (context.getPlayer() == null) return 0;
        if (facing == Direction.DOWN) {
            return switch (context.getHorizontalDirection()) {
                case NORTH -> 0;
                case EAST -> 1;
                case SOUTH -> 2;
                case WEST -> 3;
                default -> 0;
            };
        }
        return switch (context.getHorizontalDirection()) {
            case SOUTH -> 0;
            case WEST -> 1;
            case NORTH -> 2;
            case EAST -> 3;
            default -> 0;
        };
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!level.isClientSide && !oldState.is(this)) {
            Direction facing = state.getValue(FACING);
            int rotation = state.getValue(ROTATION);
            Direction right = getPlaneRight(facing, rotation);
            Direction down = getPlaneDown(facing, rotation);

            if (!tryFormSize(level, pos, facing, rotation, right, down, 3))
                tryFormSize(level, pos, facing, rotation, right, down, 2);
        }
    }

    private boolean tryFormSize(Level level, BlockPos origin, Direction facing, int rotation, Direction right, Direction down, int size) {
        for (int col = 0; col < size; col++) {
            for (int row = 0; row < size; row++) {
                BlockPos p = origin.relative(right, col).relative(down, row);
                BlockState s = level.getBlockState(p);
                if (!(s.getBlock() instanceof AncestralCarvingBlock)) return false;
                if (s.getValue(FACING) != facing) return false;
                if (s.getValue(ROTATION) != rotation) return false;
            }
        }
        for (int col = 0; col < size; col++) {
            for (int row = 0; row < size; row++) {
                BlockPos p = origin.relative(right, col).relative(down, row);
                CarvingFormation formation = CarvingFormation.getForPosition(size, col, row);
                level.setBlock(p, this.defaultBlockState()
                        .setValue(FACING, facing)
                        .setValue(FORMATION, formation)
                        .setValue(ROTATION, rotation), 2);
            }
        }
        return true;
    }

    public static int[] rotateFormationCoords(int col, int row, int size, int rotation) {
        return switch (rotation) {
            case 0 -> new int[]{size - 1 - col, size - 1 - row};
            case 1 -> new int[]{size - 1 - row, col};
            case 2 -> new int[]{col, row};
            case 3 -> new int[]{row, size - 1 - col};
            default -> new int[]{col, row};
        };
    }

    private static Direction rotateCW(Direction dir) {
        return switch (dir) {
            case EAST -> Direction.SOUTH;
            case SOUTH -> Direction.WEST;
            case WEST -> Direction.NORTH;
            case NORTH -> Direction.EAST;
            default -> dir;
        };
    }

    public static Direction getPlaneRight(Direction facing, int rotation) {
        Direction right = switch (facing) {
            case UP -> Direction.WEST;
            case DOWN -> Direction.EAST;
            case NORTH -> Direction.WEST;
            case SOUTH -> Direction.EAST;
            case EAST -> Direction.NORTH;
            case WEST -> Direction.SOUTH;
        };
        for (int i = 0; i < rotation; i++) right = rotateCW(right);
        return right;
    }

    public static Direction getPlaneDown(Direction facing, int rotation) {
        Direction down = switch (facing) {
            case UP -> Direction.NORTH;
            case DOWN -> Direction.NORTH;
            case NORTH, SOUTH, EAST, WEST -> Direction.DOWN;
        };
        for (int i = 0; i < rotation; i++) down = rotateCW(down);
        return down;
    }
}
