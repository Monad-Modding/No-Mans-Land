package com.farcr.nomansland.common.world.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.phys.Vec3;

public class StringOreFeature extends Feature<StringOreConfiguration> {
    public StringOreFeature(Codec<StringOreConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<StringOreConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        StringOreConfiguration config = context.config();
        int length = config.length().sample(random);

        double yaw = random.nextDouble() * Math.PI * 2.0;
        double pitch = (random.nextDouble() - 0.5) * 0.6;
        Vec3 direction = new Vec3(
                Math.cos(yaw) * Math.cos(pitch),
                Math.sin(pitch),
                Math.sin(yaw) * Math.cos(pitch));

        Vec3 cursor = Vec3.atCenterOf(context.origin());
        boolean placedAny = false;
        for (int i = 0; i < length; i++) {
            BlockPos pos = BlockPos.containing(cursor);
            placedAny |= tryPlace(level, pos, config, random);
            if (random.nextFloat() < config.thickenChance()) {
                placedAny |= tryPlace(level, pos.relative(Direction.getRandom(random)), config, random);
            }
            direction = direction.add(
                    (random.nextDouble() - 0.5) * 0.5,
                    (random.nextDouble() - 0.5) * 0.3,
                    (random.nextDouble() - 0.5) * 0.5).normalize();
            cursor = cursor.add(direction);
        }
        return placedAny;
    }

    private static boolean tryPlace(WorldGenLevel level, BlockPos pos, StringOreConfiguration config, RandomSource random) {
        BlockState current = level.getBlockState(pos);
        if (!current.is(BlockTags.STONE_ORE_REPLACEABLES) && !current.is(BlockTags.DEEPSLATE_ORE_REPLACEABLES)) return false;
        level.setBlock(pos, config.state().getState(random, pos), 2);
        return true;
    }
}
