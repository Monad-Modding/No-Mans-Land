package com.farcr.nomansland.common.block;

import com.farcr.nomansland.common.registry.NMLParticleTypes;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

public class ToxicGasBlock extends Block {
    public static final double HITBOX_HALF = 0.875;
    private static final int PARTICLE_DELAY = 5;
    private static final int MAX_FLOW_CLUMP = 1024;
    private static final int AIR_NEIGHBORS_TO_DISSIPATE = 3;
    private static final float DISSIPATE_CHANCE = 0.0025F;
    private static final int GILL_FEED_RANGE = 5;

    public ToxicGasBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public boolean propagatesSkylightDown(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) {
        return true;
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public void onPlace(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState oldState, boolean isMoving) {
        if (!oldState.is(this)) level.scheduleTick(pos, this, PARTICLE_DELAY);
    }

    @Override
    public void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide() && !newState.is(this) && !newState.isAir() && newState.getFluidState().isEmpty() && level instanceof ServerLevel serverLevel) {
            place(serverLevel, pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public void tick(@NotNull BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        if (canFlowInto(level.getBlockState(pos.below()))) {
            level.removeBlock(pos, false);
            place(level, pos.below());
            return;
        }

        BlockPos slip = findSlip(level, pos, random);
        if (slip != null) {
            level.removeBlock(pos, false);
            place(level, slip);
            return;
        }

        if (countAirNeighbors(level, pos) >= AIR_NEIGHBORS_TO_DISSIPATE && random.nextFloat() < DISSIPATE_CHANCE) {
            level.removeBlock(pos, false);
            return;
        }

        level.sendParticles(NMLParticleTypes.TOXIC_GAS.get(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1, 0.45, 0.45, 0.45, 0.0);
        level.scheduleTick(pos, this, PARTICLE_DELAY + random.nextInt(PARTICLE_DELAY));
    }

    private static BlockPos findSlip(ServerLevel level, BlockPos pos, RandomSource random) {
        int start = random.nextInt(4);
        for (int i = 0; i < 4; i++) {
            Direction direction = Direction.from2DDataValue(start + i);
            BlockPos side = pos.relative(direction);
            BlockPos sideDown = side.below();
            if (canFlowInto(level.getBlockState(side))
                    && canFlowInto(level.getBlockState(sideDown))
                    && canFlowInto(level.getBlockState(sideDown.below()))) {
                return sideDown;
            }
        }
        return null;
    }

    private static int countAirNeighbors(ServerLevel level, BlockPos pos) {
        int count = 0;
        for (Direction direction : Direction.values()) {
            if (level.getBlockState(pos.relative(direction)).isAir()) count++;
        }
        return count;
    }

    public static boolean isToxicGas(BlockState state) {
        return state.getBlock() instanceof ToxicGasBlock;
    }

    public static boolean canFlowInto(BlockState state) {
        if (isToxicGas(state)) return false;
        if (state.isAir()) return true;
        return state.canBeReplaced() && state.getFluidState().isEmpty();
    }

    private static void setGas(LevelAccessor level, BlockPos pos) {
        if (!level.getBlockState(pos).isAir() && level instanceof Level realLevel) {
            realLevel.destroyBlock(pos, false);
        }
        Block block = NMLBlocks.TOXIC_GAS.get();
        level.setBlock(pos, block.defaultBlockState(), 3);
        level.scheduleTick(pos, block, PARTICLE_DELAY);
    }

    public static void place(LevelAccessor level, BlockPos pos) {
        if (canFlowInto(level.getBlockState(pos))) {
            setGas(level, pos.immutable());
            return;
        }
        BlockPos free = findFreeSpot(level, pos);
        if (free != null) setGas(level, free);
    }

    private static BlockPos findFreeSpot(LevelAccessor level, BlockPos origin) {
        Set<BlockPos> clump = new HashSet<>();
        Deque<BlockPos> queue = new ArrayDeque<>();
        if (isToxicGas(level.getBlockState(origin))) {
            BlockPos start = origin.immutable();
            clump.add(start);
            queue.add(start);
        } else {
            for (Direction direction : Direction.values()) {
                BlockPos neighbor = origin.relative(direction).immutable();
                if (isToxicGas(level.getBlockState(neighbor)) && clump.add(neighbor)) queue.add(neighbor);
            }
        }
        if (clump.isEmpty()) return null;

        while (!queue.isEmpty() && clump.size() < MAX_FLOW_CLUMP) {
            BlockPos current = queue.poll();
            for (Direction direction : Direction.values()) {
                BlockPos neighbor = current.relative(direction).immutable();
                if (isToxicGas(level.getBlockState(neighbor)) && clump.add(neighbor)) queue.add(neighbor);
            }
        }

        for (BlockPos p : clump) {
            BlockPos below = p.below();
            if (canFlowInto(level.getBlockState(below))) return below;
        }
        for (BlockPos p : clump) {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos side = p.relative(direction);
                if (canFlowInto(level.getBlockState(side))) return side;
            }
        }
        for (BlockPos p : clump) {
            BlockPos above = p.above();
            if (canFlowInto(level.getBlockState(above))) return above;
        }
        return null;
    }

    public static void scatterAround(ServerLevel level, BlockPos center, int count) {
        BlockPos gill = MinersGillBlock.findFeedable(level, center, GILL_FEED_RANGE);
        for (int i = 0; i < count; i++) {
            if (gill != null && MinersGillBlock.feed(level, gill)) continue;
            gill = null;
            place(level, center);
        }
    }

    public static void emitInFront(ServerLevel level, BlockPos containerPos, Direction facing) {
        BlockPos gill = MinersGillBlock.findFeedable(level, containerPos, GILL_FEED_RANGE);
        if (gill != null && MinersGillBlock.feed(level, gill)) return;
        place(level, containerPos.relative(facing));
    }

    public static void clearArea(ServerLevel level, Vec3 center, double radius) {
        int r = Mth.ceil(radius);
        double radiusSq = radius * radius;
        BlockPos origin = BlockPos.containing(center);
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-r, -r, -r), origin.offset(r, r, r))) {
            if (isToxicGas(level.getBlockState(pos)) && pos.distToCenterSqr(center.x, center.y, center.z) <= radiusSq) {
                level.removeBlock(pos, false);
            }
        }
    }

    private static AABB gasBox(BlockPos pos) {
        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 0.5;
        double cz = pos.getZ() + 0.5;
        return new AABB(cx - HITBOX_HALF, cy - HITBOX_HALF, cz - HITBOX_HALF, cx + HITBOX_HALF, cy + HITBOX_HALF, cz + HITBOX_HALF);
    }

    public static boolean isBodyInGas(LivingEntity entity) {
        Level level = entity.level();
        AABB box = entity.getBoundingBox();
        for (BlockPos pos : BlockPos.betweenClosed(
                BlockPos.containing(box.minX - 1, box.minY - 1, box.minZ - 1),
                BlockPos.containing(box.maxX + 1, box.maxY + 1, box.maxZ + 1))) {
            if (isToxicGas(level.getBlockState(pos)) && gasBox(pos).intersects(box)) return true;
        }
        return false;
    }

    public static boolean isHeadInGas(LivingEntity entity) {
        Level level = entity.level();
        Vec3 eye = entity.getEyePosition();
        BlockPos origin = BlockPos.containing(eye);
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos pos = origin.offset(x, y, z);
                    if (isToxicGas(level.getBlockState(pos)) && gasBox(pos).contains(eye)) return true;
                }
            }
        }
        return false;
    }
}
