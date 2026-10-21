package com.farcr.nomansland.common.block;

import com.mojang.serialization.MapCodec;
import com.farcr.nomansland.common.registry.NMLTags;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseCoralWallFanBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;

public class ShelfMushroomBlock extends BaseCoralWallFanBlock implements BonemealableBlock {

    private static final Map<Direction, VoxelShape> SHAPES = Maps.newEnumMap(ImmutableMap.of(
            Direction.NORTH, Block.box(4, 6, 9, 12, 9, 16.0),
            Direction.SOUTH, Block.box(4, 6, 0, 12, 9, 7),
            Direction.WEST, Block.box(9, 6, 4, 16, 9, 12),
            Direction.EAST, Block.box(0, 6, 4, 7, 9, 12)));

    public ShelfMushroomBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(WATERLOGGED, Boolean.valueOf(false)));
    }

    @Override
    public MapCodec<ShelfMushroomBlock> codec() {
        return simpleCodec(ShelfMushroomBlock::new);
    }

    public boolean canSurvivePublic(BlockState state, LevelReader level, BlockPos pos) {
        return canSurvive(state, level, pos);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction direction = state.getValue(FACING);
        BlockPos blockpos = pos.relative(direction.getOpposite());
        BlockState blockstate = level.getBlockState(blockpos);
        if (blockstate.hasProperty(SlabBlock.TYPE) && blockstate.getValue(SlabBlock.TYPE) != SlabType.DOUBLE)
            return false;
        if (!canSupportCenter(level, blockpos, direction))
            return false;
        return blockstate.is(BlockTags.LOGS) || blockstate.is(NMLTags.MUSHROOM_BLOCKS);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel serverLevel, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
        Direction facing = blockState.getValue(BaseCoralWallFanBlock.FACING);
        BlockPos sidePos = serverLevel.random.nextBoolean() && serverLevel.isEmptyBlock(blockPos.relative(facing.getClockWise())) ?
                blockPos.relative(facing.getClockWise()) : serverLevel.isEmptyBlock(blockPos.relative(facing.getCounterClockWise())) ?
                blockPos.relative(facing.getCounterClockWise()) : serverLevel.isEmptyBlock(blockPos.relative(facing.getClockWise())) ?
                blockPos.relative(facing.getClockWise()) : null;
        BlockState newState = NMLBlocks.SHELF_MUSHROOM_BLOCK.get().defaultBlockState()
                .setValue(SlabBlock.TYPE, serverLevel.random.nextBoolean() ? SlabType.BOTTOM : SlabType.TOP);

        serverLevel.setBlockAndUpdate(blockPos, newState);
        if (sidePos != null) {
            serverLevel.setBlockAndUpdate(sidePos, newState);
            if (serverLevel.isEmptyBlock(sidePos.relative(facing.getOpposite()))) {
                serverLevel.setBlockAndUpdate(sidePos.relative(facing.getOpposite()), newState);
            }
        }
    }
}
