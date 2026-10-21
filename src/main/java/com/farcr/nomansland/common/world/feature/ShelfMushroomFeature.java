package com.farcr.nomansland.common.world.feature;

import com.farcr.nomansland.common.block.ShelfMushroomBlock;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class ShelfMushroomFeature extends Feature<NoneFeatureConfiguration> {
    public ShelfMushroomFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel worldgenlevel = context.level();
        BlockPos blockpos = context.origin();
        context.config();
        if (worldgenlevel.isEmptyBlock(blockpos)) {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockState state = NMLBlocks.SHELF_MUSHROOM.get().defaultBlockState().setValue(ShelfMushroomBlock.FACING, direction.getOpposite());
                if (NMLBlocks.SHELF_MUSHROOM.get().canSurvivePublic(state, worldgenlevel, blockpos)) {
                    worldgenlevel.setBlock(blockpos, NMLBlocks.SHELF_MUSHROOM.get().defaultBlockState().setValue(ShelfMushroomBlock.FACING, direction.getOpposite()), 2);
                    return true;
                }
            }
        }
        return false;
    }
}
