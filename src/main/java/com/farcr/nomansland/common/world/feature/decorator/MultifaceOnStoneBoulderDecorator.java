package com.farcr.nomansland.common.world.feature.decorator;

import com.farcr.nomansland.common.registry.worldgen.NMLBoulderDecoratorTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

public class MultifaceOnStoneBoulderDecorator extends BoulderDecorator {
    public static final MapCodec<MultifaceOnStoneBoulderDecorator> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    Codec.floatRange(0.0F, 1.0F).fieldOf("probability").forGetter(decorator -> decorator.probability),
                    BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block").forGetter(decorator -> decorator.block)
            ).apply(instance, MultifaceOnStoneBoulderDecorator::new)
    );

    protected final float probability;
    protected final Block block;

    public MultifaceOnStoneBoulderDecorator(float probability, Block block) {
        this.probability = probability;
        this.block = block;
    }

    @Override
    protected BoulderDecoratorType<?> type() {
        return NMLBoulderDecoratorTypes.MULTIFACE_ON_STONE.get();
    }

    @Override
    public void place(Context context) {
        if (!(block instanceof MultifaceBlock multifaceBlock)) return;

        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        Map<BlockPos, BlockState> growth = new HashMap<>();

        for (BlockPos stone : context.stone()) {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos target = stone.relative(direction);
                if (!context.isAir(target)) continue;
                if (random.nextFloat() >= probability) continue;

                BlockState current = growth.getOrDefault(target, level.getBlockState(target));
                BlockState placed = multifaceBlock.getStateForPlacement(current, level, target, direction.getOpposite());
                if (placed != null) growth.put(target, placed);
            }
        }

        for (Map.Entry<BlockPos, BlockState> entry : growth.entrySet())
            context.setBlock(entry.getKey(), entry.getValue());
    }
}
