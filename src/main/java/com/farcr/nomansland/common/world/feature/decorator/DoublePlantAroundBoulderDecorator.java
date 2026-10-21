package com.farcr.nomansland.common.world.feature.decorator;

import com.farcr.nomansland.common.registry.worldgen.NMLBoulderDecoratorTypes;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DoublePlantAroundBoulderDecorator extends BoulderDecorator {
    public static final MapCodec<DoublePlantAroundBoulderDecorator> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    IntProvider.codec(0, 16).fieldOf("count").forGetter(decorator -> decorator.count),
                    BlockStateProvider.CODEC.fieldOf("block_provider").forGetter(decorator -> decorator.blockProvider)
            ).apply(instance, DoublePlantAroundBoulderDecorator::new)
    );

    protected final IntProvider count;
    protected final BlockStateProvider blockProvider;

    public DoublePlantAroundBoulderDecorator(IntProvider count, BlockStateProvider blockProvider) {
        this.count = count;
        this.blockProvider = blockProvider;
    }

    @Override
    protected BoulderDecoratorType<?> type() {
        return NMLBoulderDecoratorTypes.DOUBLE_PLANT_AROUND.get();
    }

    @Override
    public void place(Context context) {
        RandomSource random = context.random();
        Set<BlockPos> boulder = new HashSet<>(context.stone());
        Set<BlockPos> candidates = new HashSet<>();

        for (BlockPos stone : context.stone()) {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                for (int distance = 1; distance <= 2; distance++) {
                    BlockPos side = stone.relative(direction, distance);
                    if (boulder.contains(side)) continue;
                    if (!context.isAir(side) || !context.isAir(side.above())) continue;
                    if (context.isAir(side.below()) || boulder.contains(side.below())) continue;
                    candidates.add(side);
                }
            }
        }

        if (candidates.isEmpty()) return;

        List<BlockPos> shuffled = Util.shuffledCopy(new ArrayList<>(candidates).toArray(new BlockPos[0]), random);
        Set<BlockPos> occupied = new HashSet<>();
        int remaining = count.sample(random);

        for (BlockPos pos : shuffled) {
            if (remaining <= 0) break;
            if (occupied.contains(pos)) continue;

            BlockState state = blockProvider.getState(random, pos);
            if (!(state.getBlock() instanceof DoublePlantBlock) || !state.canSurvive(context.levelReader(), pos)) continue;

            context.setBlock(pos, state.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER));
            context.setBlock(pos.above(), state.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER));
            occupied.add(pos);
            occupied.add(pos.above());
            remaining--;
        }
    }
}
