package com.farcr.nomansland.common.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.util.TriState;

public class CutSugarCaneBlock extends SugarCaneBlock {
    protected static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 13.0D, 14.0D);

    public CutSugarCaneBlock(Properties properties) {
        super(properties);
    }
    public static final MapCodec<SugarCaneBlock> CODEC = simpleCodec(CutSugarCaneBlock::new)
            .xmap(block -> (SugarCaneBlock) block, block -> (CutSugarCaneBlock) block);

    @Override
    public MapCodec<SugarCaneBlock> codec() {
        return CODEC;
    }


    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState soil = level.getBlockState(pos.below());
        if (soil.canSustainPlant(level, pos.below(), Direction.UP, this.defaultBlockState()) == TriState.TRUE) return true;
        BlockState blockstate = level.getBlockState(pos.below());
        if (blockstate.is(Blocks.SUGAR_CANE)) {
            return true;
        } else {
            if (blockstate.is(BlockTags.DIRT) || blockstate.is(BlockTags.SAND)) {
                BlockPos blockpos = pos.below();

                for (Direction direction : Direction.Plane.HORIZONTAL) {
                    BlockState hydratingState = level.getBlockState(blockpos.relative(direction));
                    FluidState fluidstate = level.getFluidState(blockpos.relative(direction));
                    if (state.canBeHydrated(level, pos, fluidstate, blockpos.relative(direction)) || hydratingState.is(Blocks.FROSTED_ICE)) {
                        return true;
                    }
                }
            }

            return false;
        }
    }
}
