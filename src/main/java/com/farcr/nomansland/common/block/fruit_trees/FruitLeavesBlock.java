package com.farcr.nomansland.common.block.fruit_trees;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

import static com.farcr.nomansland.common.block.fruit_trees.FruitBlock.AGE;

public class FruitLeavesBlock extends LeavesBlock {

    private final FruitType fruitType;
    public Holder<Block> fruit;
    public Holder<Block> leaves;
    public int growthSpeed;

    public FruitLeavesBlock(Properties properties, FruitType fruitType) {
        super(properties);
        this.fruitType = fruitType;
        fruit = fruitType.getFruitBlock();
        leaves = fruitType.getLeaves();
        growthSpeed = fruitType.getGrowthSpeed();
    }

    public static final MapCodec<FruitLeavesBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            propertiesCodec(),
            FruitType.CODEC.fieldOf("fruit_type").forGetter(f -> f.fruitType)
    ).apply(instance, FruitLeavesBlock::new));

    @Override
    public MapCodec<FruitLeavesBlock> codec() {
        return CODEC;
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.randomTick(state, level, pos, random);
        int fruitLeaves = 0;
        int regularLeaves = 0;
        if (!state.getValue(PERSISTENT)) {
            for (Direction direction : Direction.values()) {
                BlockPos relativePos = pos.relative(direction);
                if (level.getBlockState(relativePos).is(this)) fruitLeaves++;
                if (level.getBlockState(relativePos).is(leaves)) regularLeaves++;
                for (Direction direction1 : Direction.values()) {
                    if (direction1 == direction.getOpposite()) continue;
                    BlockPos relativePos1 = relativePos.relative(direction1);
                    if (level.getBlockState(relativePos1).is(this)) fruitLeaves++;
                    if (level.getBlockState(relativePos1).is(leaves)) regularLeaves++;
                }
            }

            if (regularLeaves < 4) rot(level, pos, state);
            if (fruitLeaves > 4) rot(level, pos, state);
        }
        // place or grow the fruit block underneath
        BlockPos fruitPos = pos.below();
        BlockState fruitState = level.getBlockState(fruitPos);
        if (!level.isAreaLoaded(fruitPos, 1)) return;
        if (level.getRawBrightness(fruitPos, 0) >= 9 && random.nextInt(growthSpeed, 10) == growthSpeed) {
            if (fruitState.getBlock() instanceof FruitBlock fruitBlock) {
                int fruitAge = fruitState.getValue(AGE);
                if (fruitAge < fruitBlock.getMaxAge()) {
                    level.setBlockAndUpdate(fruitPos, fruitState.setValue(AGE, fruitAge+1));
                }
            } else if (fruitState.isAir()) level.setBlockAndUpdate(fruitPos, fruit.value().defaultBlockState().setValue(AGE, 0));
        }
    }

    public void rot(Level level, BlockPos pos, BlockState state) {
        int distance = state.getValue(DISTANCE);
        boolean waterlogged = state.getValue(WATERLOGGED);
        if (level.getBlockState(pos.below()).getBlock() instanceof FruitBlock) level.setBlockAndUpdate(pos.below(), Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(pos, leaves.value().defaultBlockState().setValue(DISTANCE, distance).setValue(WATERLOGGED, waterlogged));
    }
}
