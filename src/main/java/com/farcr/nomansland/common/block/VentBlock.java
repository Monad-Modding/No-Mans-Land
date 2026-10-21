package com.farcr.nomansland.common.block;

import com.farcr.nomansland.common.blockentity.VentBlockEntity;
import com.farcr.nomansland.common.registry.NMLBlockEntities;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class VentBlock extends DirectionalBlock implements EntityBlock, SimpleWaterloggedBlock {
    public static final int RANGE = 5;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final Direction[] DIRECTIONS = Direction.values();
    private static final int MAX_AFFECTED_CELLS = 256;

    private static final Map<Direction, VoxelShape> SHAPES = new EnumMap<>(Direction.class);
    static {
        VoxelShape up = Shapes.or(Block.box(2, 0, 2, 14, 8, 14), Block.box(4, 8, 4, 12, 16, 12));
        for (Direction dir : Direction.values()) SHAPES.put(dir, rotateShape(up, dir));
    }

    protected VentBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP).setValue(WATERLOGGED, false));
    }

    @Nullable
    public ParticleOptions getVentParticle() {
        return null;
    }

    public abstract void affectEntity(Level level, BlockPos ventPos, Entity entity, int distance);

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    private static VoxelShape rotateShape(VoxelShape up, Direction dir) {
        VoxelShape[] result = {Shapes.empty()};
        up.forAllBoxes((x0, y0, z0, x1, y1, z1) -> {
            double[] a = transform(dir, x0, y0, z0);
            double[] b = transform(dir, x1, y1, z1);
            result[0] = Shapes.or(result[0], Shapes.box(
                    Math.min(a[0], b[0]), Math.min(a[1], b[1]), Math.min(a[2], b[2]),
                    Math.max(a[0], b[0]), Math.max(a[1], b[1]), Math.max(a[2], b[2])));
        });
        return result[0];
    }

    private static double[] transform(Direction dir, double x, double y, double z) {
        return switch (dir) {
            case UP -> new double[]{x, y, z};
            case DOWN -> new double[]{x, 1 - y, 1 - z};
            case NORTH -> new double[]{x, z, 1 - y};
            case SOUTH -> new double[]{x, 1 - z, y};
            case EAST -> new double[]{y, 1 - x, z};
            case WEST -> new double[]{1 - y, x, z};
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean water = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
        return this.defaultBlockState()
                .setValue(FACING, context.getNearestLookingDirection().getOpposite())
                .setValue(WATERLOGGED, water);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
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

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new VentBlockEntity(pos, state);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != NMLBlockEntities.VENT.get()) return null;
        BlockEntityTicker<VentBlockEntity> ticker = level.isClientSide() ? VentBlockEntity::clientTick : VentBlockEntity::serverTick;
        return (BlockEntityTicker<T>) ticker;
    }

    public static boolean isWater(LevelAccessor level, BlockPos pos) {
        return level.getFluidState(pos).is(FluidTags.WATER);
    }

    public static Map<BlockPos, Integer> computeAffectedWater(LevelAccessor level, BlockPos ventPos, Direction facing) {
        Map<BlockPos, Integer> affected = new HashMap<>();
        BlockPos front = ventPos.relative(facing);
        if (!isWater(level, front)) return affected;

        Deque<BlockPos> queue = new ArrayDeque<>();
        affected.put(front, 1);
        queue.add(front);
        while (!queue.isEmpty() && affected.size() < MAX_AFFECTED_CELLS) {
            BlockPos current = queue.poll();
            int distance = affected.get(current);
            if (distance >= RANGE) continue;
            for (Direction direction : DIRECTIONS) {
                BlockPos neighbor = current.relative(direction).immutable();
                if (affected.containsKey(neighbor) || !isWater(level, neighbor)) continue;
                affected.put(neighbor, distance + 1);
                queue.add(neighbor);
            }
        }
        return affected;
    }

    public static void computeAffectedWater(LevelAccessor level, BlockPos ventPos, Direction facing, Long2IntOpenHashMap out) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        cursor.set(ventPos).move(facing);
        if (!isWater(level, cursor)) return;

        LongArrayFIFOQueue queue = new LongArrayFIFOQueue();
        long start = cursor.asLong();
        out.put(start, 1);
        queue.enqueue(start);
        while (!queue.isEmpty() && out.size() < MAX_AFFECTED_CELLS) {
            long current = queue.dequeueLong();
            int distance = out.get(current);
            if (distance >= RANGE) continue;
            int cx = BlockPos.getX(current);
            int cy = BlockPos.getY(current);
            int cz = BlockPos.getZ(current);
            for (Direction direction : DIRECTIONS) {
                cursor.set(cx + direction.getStepX(), cy + direction.getStepY(), cz + direction.getStepZ());
                long key = cursor.asLong();
                if (out.containsKey(key) || !isWater(level, cursor)) continue;
                out.put(key, distance + 1);
                queue.enqueue(key);
            }
        }
    }

    public static boolean isAffectedBy(LevelAccessor level, BlockPos pos, Class<? extends VentBlock> type) {
        if (!isWater(level, pos)) return false;

        Set<BlockPos> visited = new HashSet<>();
        Deque<BlockPos> queue = new ArrayDeque<>();
        Map<BlockPos, Integer> distances = new HashMap<>();
        BlockPos start = pos.immutable();
        visited.add(start);
        distances.put(start, 1);
        queue.add(start);
        while (!queue.isEmpty() && visited.size() < MAX_AFFECTED_CELLS) {
            BlockPos current = queue.poll();
            int distance = distances.get(current);
            for (Direction direction : DIRECTIONS) {
                BlockPos neighbor = current.relative(direction).immutable();
                BlockState state = level.getBlockState(neighbor);
                if (type.isInstance(state.getBlock()) && neighbor.relative(state.getValue(FACING)).equals(current)) return true;
                if (distance >= RANGE) continue;
                if (visited.contains(neighbor) || !isWater(level, neighbor)) continue;
                visited.add(neighbor);
                distances.put(neighbor, distance + 1);
                queue.add(neighbor);
            }
        }
        return false;
    }
}
