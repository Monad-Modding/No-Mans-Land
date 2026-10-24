package com.farcr.nomansland.common.block.tap;

import com.farcr.nomansland.common.registry.NMLTags;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderSet;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.ArrayDeque;
import java.util.Optional;

public final class TapTrees {

    public static final int MAX_TRUNK = 32;
    public static final int MIN_LEAVES = 12;
    public static final int MAX_LEAVES = 256;
    private static final int LEAF_DECAY_DISTANCE = 7;
    private static final int MAX_TRUNK_BLOCKS = MAX_TRUNK * 4;
    private static final int MAX_WOOD_BLOCKS = MAX_TRUNK_BLOCKS * 2;

    private TapTrees() {
    }

    public static boolean isTree(Level level, BlockPos log, BlockState logState, TapInteraction.TreeRequirement requirement) {
        if (!isTrunk(logState, logState.getBlock(), requirement.trunk())) return false;
        Block species = logState.getBlock();
        BlockPos.MutableBlockPos cursor = log.mutable();
        int below = 0;
        while (below < MAX_TRUNK) {
            cursor.move(Direction.DOWN);
            if (!level.isLoaded(cursor) || !isTrunk(level.getBlockState(cursor), species, requirement.trunk())) break;
            below++;
        }
        if (!level.isLoaded(cursor) || !isGround(level.getBlockState(cursor), requirement.ground())) return false;
        cursor.set(log);
        int above = 0;
        while (below + above < MAX_TRUNK) {
            cursor.move(Direction.UP);
            if (!level.isLoaded(cursor) || !isTrunk(level.getBlockState(cursor), species, requirement.trunk())) break;
            above++;
        }
        if (below + above + 1 < requirement.minHeight()) return false;
        return hasLeaves(level, log, species, requirement);
    }

    private static boolean isTrunk(BlockState state, Block species, Optional<HolderSet<Block>> trunk) {
        boolean kind = trunk.isPresent() ? state.is(trunk.get()) : state.is(species);
        return kind && (!state.hasProperty(BlockStateProperties.AXIS) || state.getValue(BlockStateProperties.AXIS) == Direction.Axis.Y);
    }

    private static boolean isGround(BlockState state, Optional<HolderSet<Block>> ground) {
        return ground.isPresent() ? state.is(ground.get()) : state.is(NMLTags.TREE_GROUND);
    }

    public static int countTaps(Level level, BlockPos log, BlockState logState, TapInteraction.TreeRequirement requirement) {
        Block species = logState.getBlock();
        LongOpenHashSet visited = new LongOpenHashSet();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        visited.add(log.asLong());
        queue.add(log);
        int taps = 0;
        while (!queue.isEmpty()) {
            BlockPos trunk = queue.poll();
            for (Direction direction : Direction.values()) {
                BlockPos next = trunk.relative(direction);
                if (!level.isLoaded(next)) continue;
                BlockState state = level.getBlockState(next);
                if (direction.getAxis().isHorizontal() && state.getBlock() instanceof TapBlock && !state.getValue(TapBlock.CLOSED) && TapBlock.getSourcePos(next, state).equals(trunk)) {
                    taps++;
                } else if (visited.size() < MAX_TRUNK_BLOCKS && isTrunk(state, species, requirement.trunk()) && visited.add(next.asLong())) {
                    queue.add(next);
                }
            }
        }
        return Math.max(1, taps);
    }

    private static boolean hasLeaves(Level level, BlockPos log, Block species, TapInteraction.TreeRequirement requirement) {
        int needed = requirement.minLeaves();
        if (needed <= 0) return true;
        LongOpenHashSet visited = new LongOpenHashSet();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        visited.add(log.asLong());
        queue.add(log);
        int wood = 1;
        int leaves = 0;
        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            for (Direction direction : Direction.values()) {
                BlockPos next = current.relative(direction);
                if (!level.isLoaded(next) || visited.contains(next.asLong())) continue;
                BlockState state = level.getBlockState(next);
                if (isLeaf(state, requirement.leaves())) {
                    visited.add(next.asLong());
                    if (++leaves >= needed) return true;
                    queue.add(next);
                } else if (wood < MAX_WOOD_BLOCKS && isWood(state, species, requirement.trunk())) {
                    visited.add(next.asLong());
                    wood++;
                    queue.add(next);
                }
            }
        }
        return false;
    }

    private static boolean isWood(BlockState state, Block species, Optional<HolderSet<Block>> trunk) {
        return trunk.isPresent() ? state.is(trunk.get()) : state.is(species);
    }

    private static boolean isLeaf(BlockState state, Optional<HolderSet<Block>> leaves) {
        boolean kind = leaves.isPresent() ? state.is(leaves.get()) : state.is(BlockTags.LEAVES);
        return kind && isNatural(state);
    }

    private static boolean isNatural(BlockState state) {
        if (state.hasProperty(LeavesBlock.PERSISTENT) && state.getValue(LeavesBlock.PERSISTENT)) return false;
        return !state.hasProperty(LeavesBlock.DISTANCE) || state.getValue(LeavesBlock.DISTANCE) < LEAF_DECAY_DISTANCE;
    }
}
