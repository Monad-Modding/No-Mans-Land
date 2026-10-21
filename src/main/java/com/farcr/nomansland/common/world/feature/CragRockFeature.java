package com.farcr.nomansland.common.world.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

/* "crag_rock" feature type.

 places a large rock, with some material on top (ex. grass and dirt)

 EXAMPLE SYNTAX:

 {
  "type": "nomansland:crag_rock",
  "config": {
    // required - no default
    //   takes in any integer number provider. valid range: 1 - 8
    //   determines the approximate radius of the rock.
    "radius": {
      "type": "uniform",
      "min_inclusive": 4,
      "max_inclusive": 7
    },
    // required - no default
    //   takes in any integer number provider. valid range: 1 - infinity
    //   determines the approximate height of the rock.
    "height": {
      "type": "uniform",
      "min_inclusive": 6,
      "max_inclusive": 18
    },
    // defaults to 1.0
    //   takes in any floating point number provider.
    //   determines how circular the shape of the rock is.
    "radius_strength": 1.0,
    // defaults to 5.0
    //   takes in any floating point number provider.
    //   determines how much the shape of the rock is affected by random noise.
    "noise_strength": 5.0,

    // required - no default
    //    takes in any variety of block provider.
    //    the base block type, making up the rock.
    "base_block_provider": {
      "type": "minecraft:simple_state_provider",
      "state": {
        "Name": "minecraft:stone"
      }
    },
    // required - no default
    //    takes in any variety of block provider.
    //    the block type making up the under layer of the surface.
    "soil_block_provider": {
      "type": "minecraft:simple_state_provider",
      "state": {
        "Name": "minecraft:dirt"
      }
    },
    // required - no default
    //    takes in any variety of block provider.
    //    the block type making up the top layer of the surface.
    "surface_block_provider": {
      "type": "minecraft:simple_state_provider",
      "state": {
        "Name": "minecraft:grass_block"
      }
    },
    // defaults to false!
    //    takes in "true" or "false"
    //    determines whether the top layer should generate underwater
    "generate_surface_under_fluids": false
  }
 } */
public class CragRockFeature extends Feature<CragRockFeatureConfiguration> {
    private static final NormalNoise NOISE = NormalNoise.create(RandomSource.create(0), 0, 1);

    public CragRockFeature(Codec<CragRockFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<CragRockFeatureConfiguration> context) {
        BlockPos origin = context.origin();
        WorldGenLevel level = context.level();
        RandomSource random = context.random();

        CragRockFeatureConfiguration config = context.config();
        int radius = config.radius().sample(random);
        int height = config.height().sample(random);
        double radiusStrength = config.radiusStrength().sample(random);
        double noiseStrength = config.noiseStrength().sample(random);

        int range = Math.min(Mth.ceil(radius + 3), 8);
        int depth = Mth.ceil(radius);

        int noiseSampleOffset = random.nextIntBetweenInclusive(-1000, 1000);

        boolean placedBlock = false;
        BlockPos.MutableBlockPos pos = origin.mutable();
        for (int x = -range; x <= range; x++) {
            for (int z = -range; z <= range; z++) {
                // height stuffs
                int baseHeight = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, origin.getX() + x, origin.getZ() + z) - depth - 1;
                double topHeight = origin.getY() + height + NOISE.getValue(pos.getX() * 0.05, 1000 + noiseSampleOffset, pos.getZ() * 0.05) * 2;
                double maxYDistance = topHeight - (baseHeight + depth);

                // skip if we're inside terrain already
                if (baseHeight > topHeight) continue;

                // surface stuffs
                int surfaceDepth = 0;
                double soilDepth = 2 + NOISE.getValue(pos.getX() * 0.05, 2000 + noiseSampleOffset, pos.getZ() * 0.05) * 1.5;
                boolean placingSurface = false;

                for (int y = Mth.ceil(topHeight); y >= baseHeight; y--) {
                    pos.set(x + origin.getX(), y, z + origin.getZ());

                    double noiseFac = NOISE.getValue(pos.getX() * 0.15, pos.getY() * 0.02 + noiseSampleOffset, pos.getZ() * 0.15);

                    double radiusFac;
                    double xzDist = origin.distToCenterSqr(pos.getX() + 0.5, origin.getY() + 0.5, pos.getZ() + 0.5);
                    double yDist = y - (baseHeight + depth);

                    if (yDist > 1) {
                        // radius if we're above ground
                        double radiusMultiplier = Mth.clampedMap(yDist, 0, maxYDistance - 1, 1, 0.7);
                        radiusMultiplier *= Mth.clampedMap(yDist, maxYDistance - 2, maxYDistance, 1, 0.8);
                        double dist = Math.sqrt(origin.distToCenterSqr(pos.getX() + 0.5, origin.getY() + 0.5, pos.getZ() + 0.5));
                        radiusFac = radius * radiusMultiplier - dist;
                    } else {
                        // radius if we're underground
                        double dist = Math.sqrt(xzDist + yDist);
                        radiusFac = (radius + 1) - dist;
                    }

                    if (radiusFac * radiusStrength + noiseFac * noiseStrength > 0) {
                        // conditions for whether the surface should be placed
                        if ((yDist > maxYDistance * 0.75 || yDist < 2) && surfaceDepth == 0 && yDist > 0) placingSurface = true;
                        BlockState existing = level.getBlockState(pos);
                        if (existing.isAir() || existing.liquid() || existing.canBeReplaced() || existing.is(BlockTags.OVERWORLD_CARVER_REPLACEABLES)) {
                            level.setBlock(pos, getBlockState(config, pos, level, random, surfaceDepth, soilDepth, placingSurface), 2);
                            placedBlock = true;
                        }
                        surfaceDepth++;
                    } else {
                        placingSurface = false;
                        surfaceDepth = 0;
                    }
                }
            }
        }

        return placedBlock;
    }

    // gets the current surface block based off depth and other things
    private BlockState getBlockState(CragRockFeatureConfiguration config, BlockPos pos, WorldGenLevel level, RandomSource random, int surfaceDepth, double soilDepth, boolean placingSurface) {
        if (placingSurface && level.getBlockState(pos).canBeReplaced()) {
            if (surfaceDepth == 0 && (config.generateSurfaceUnderwater() || level.getBlockState(pos.above()).getFluidState().isEmpty())) {
                return config.surfaceBlockProvider().getState(random, pos);
            } else if (surfaceDepth < soilDepth) {
                return config.soilBlockProvider().getState(random, pos);
            }
        }
        return config.baseBlockProvider().getState(random, pos);
    }
}
