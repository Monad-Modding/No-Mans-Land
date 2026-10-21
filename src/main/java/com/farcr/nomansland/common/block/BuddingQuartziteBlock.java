package com.farcr.nomansland.common.block;

import com.mojang.serialization.MapCodec;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BuddingAmethystBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

public class BuddingQuartziteBlock extends BuddingAmethystBlock {
    public static final int GROWTH_CHANCE = 5;
    private static final Direction[] DIRECTIONS = Direction.values();

    public BuddingQuartziteBlock(Properties properties) {
        super(properties);
    }
    public static final MapCodec<BuddingAmethystBlock> CODEC = simpleCodec(BuddingQuartziteBlock::new)
            .xmap(block -> (BuddingAmethystBlock) block, block -> (BuddingQuartziteBlock) block);

    @Override
    public MapCodec<BuddingAmethystBlock> codec() {
        return CODEC;
    }


    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(GROWTH_CHANCE) == 0) {
            Direction direction = DIRECTIONS[random.nextInt(DIRECTIONS.length)];
            BlockPos blockpos = pos.relative(direction);
            BlockState blockstate = level.getBlockState(blockpos);
            Block block = null;
            if (canClusterGrowAtState(blockstate)) {
                block = NMLBlocks.SMALL_QUARTZITE_BUD.get();
            } else if (blockstate.is(NMLBlocks.SMALL_QUARTZITE_BUD.get()) && blockstate.getValue(AmethystClusterBlock.FACING) == direction) {
                block = NMLBlocks.MEDIUM_QUARTZITE_BUD.get();
            } else if (blockstate.is(NMLBlocks.MEDIUM_QUARTZITE_BUD.get()) && blockstate.getValue(AmethystClusterBlock.FACING) == direction) {
                block = NMLBlocks.LARGE_QUARTZITE_BUD.get();
            } else if (blockstate.is(NMLBlocks.LARGE_QUARTZITE_BUD.get()) && blockstate.getValue(AmethystClusterBlock.FACING) == direction) {
                block = NMLBlocks.QUARTZITE_CLUSTER.get();
            }

            if (block != null) {
                BlockState blockstate1 = block.defaultBlockState().setValue(AmethystClusterBlock.FACING, direction).setValue(AmethystClusterBlock.WATERLOGGED, Boolean.valueOf(blockstate.getFluidState().getType() == Fluids.WATER));
                level.setBlockAndUpdate(blockpos, blockstate1);
            }

        }
    }
}
