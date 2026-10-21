package com.farcr.nomansland.common.block.tap;

import com.farcr.nomansland.common.registry.NMLTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderSet;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.Optional;

public final class TapTrees {

    public static final int MAX_TRUNK = 32;
    private static final int LEAF_REACH = 3;
    private static final int LEAF_BELOW = 2;
    private static final int LEAF_ABOVE = 3;
    private static final int LEAF_DECAY_DISTANCE = 7;

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
        return hasLeaves(level, log.above(above), requirement.leaves());
    }

    private static boolean isTrunk(BlockState state, Block species, Optional<HolderSet<Block>> trunk) {
        boolean kind = trunk.isPresent() ? state.is(trunk.get()) : state.is(species);
        return kind && (!state.hasProperty(BlockStateProperties.AXIS) || state.getValue(BlockStateProperties.AXIS) == Direction.Axis.Y);
    }

    private static boolean isGround(BlockState state, Optional<HolderSet<Block>> ground) {
        return ground.isPresent() ? state.is(ground.get()) : state.is(NMLTags.TREE_GROUND);
    }

    private static boolean hasLeaves(Level level, BlockPos top, Optional<HolderSet<Block>> leaves) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dy = -LEAF_BELOW; dy <= LEAF_ABOVE; dy++) {
            for (int dx = -LEAF_REACH; dx <= LEAF_REACH; dx++) {
                for (int dz = -LEAF_REACH; dz <= LEAF_REACH; dz++) {
                    cursor.setWithOffset(top, dx, dy, dz);
                    if (!level.isLoaded(cursor)) continue;
                    BlockState state = level.getBlockState(cursor);
                    boolean kind = leaves.isPresent() ? state.is(leaves.get()) : state.is(BlockTags.LEAVES);
                    if (kind && isNatural(state)) return true;
                }
            }
        }
        return false;
    }

    private static boolean isNatural(BlockState state) {
        if (state.hasProperty(LeavesBlock.PERSISTENT) && state.getValue(LeavesBlock.PERSISTENT)) return false;
        return !state.hasProperty(LeavesBlock.DISTANCE) || state.getValue(LeavesBlock.DISTANCE) < LEAF_DECAY_DISTANCE;
    }
}
