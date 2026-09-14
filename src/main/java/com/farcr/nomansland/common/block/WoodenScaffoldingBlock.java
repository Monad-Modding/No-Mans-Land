package com.farcr.nomansland.common.block;

import com.mojang.serialization.MapCodec;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.ScaffoldingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

public class WoodenScaffoldingBlock extends ScaffoldingBlock {
    public WoodenScaffoldingBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(DISTANCE, 6).setValue(WATERLOGGED, false).setValue(BOTTOM, false));
    }
    public static final MapCodec<ScaffoldingBlock> CODEC = simpleCodec(WoodenScaffoldingBlock::new)
            .xmap(block -> (ScaffoldingBlock) block, block -> (WoodenScaffoldingBlock) block);

    @Override
    public MapCodec<ScaffoldingBlock> codec() {
        return CODEC;
    }

    public static int getDistance(BlockGetter level, BlockPos pos) {
        BlockPos.MutableBlockPos blockpos$mutableblockpos = pos.mutable().move(Direction.DOWN);
        BlockState blockstate = level.getBlockState(blockpos$mutableblockpos);
        int i = 6;
        if (blockstate.is(NMLBlocks.WOODEN_SCAFFOLDING.block())) {
            i = blockstate.getValue(DISTANCE);
        } else if (blockstate.isFaceSturdy(level, blockpos$mutableblockpos, Direction.UP)) {
            return 0;
        }

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockState blockstate1 = level.getBlockState(blockpos$mutableblockpos.setWithOffset(pos, direction));
            if (blockstate1.is(NMLBlocks.WOODEN_SCAFFOLDING.block())) {
                i = Math.min(i, blockstate1.getValue(DISTANCE) + 1);
                if (i == 1) {
                    break;
                }
            }
        }

        return i;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos $$1 = context.getClickedPos();
        Level $$2 = context.getLevel();
        int $$3 = getDistance($$2, $$1);
        return this.defaultBlockState().setValue(WATERLOGGED, $$2.getFluidState($$1).getType() == Fluids.WATER).setValue(DISTANCE, $$3).setValue(BOTTOM, this.isBottom($$2, $$1, $$3));
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int i = getDistance(level, pos);
        BlockState blockstate = state.setValue(DISTANCE, i).setValue(BOTTOM, isBottom(level, pos, i));
        if (blockstate.getValue(DISTANCE).equals(6)) {
            if (state.getValue(DISTANCE).equals(6)) {
                FallingBlockEntity.fall(level, pos, blockstate);
            } else {
                level.destroyBlock(pos, true);
            }
        } else if (state != blockstate) {
            level.setBlock(pos, blockstate, 3);
        }

    }


    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return getDistance(level, pos) < 6;
    }

    private boolean isBottom(BlockGetter level, BlockPos pos, int distance) {
        return distance > 0 && !level.getBlockState(pos.below()).is(this);
    }

    @Override
    public boolean isScaffolding(BlockState state, LevelReader level, BlockPos pos, LivingEntity entity) {
        return true;
    }
}
