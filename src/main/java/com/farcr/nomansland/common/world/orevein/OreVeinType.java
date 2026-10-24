package com.farcr.nomansland.common.world.orevein;

import com.farcr.nomansland.common.registry.NMLTags;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.blockpredicates.StateTestingPredicate;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;

import java.util.Map;
import java.util.Optional;

/*
    put all ore vein jsons in resources/data/nomansland/nomansland/worldgen/ore_vein/
    EXAMPLE JSON:

    {
       -- PLACEMENT --
        * determines the biomes that the vein's center is allowed to be in.
        * a vein may spread well outside its biome! so, be careful...
        * an optional biome tag or a list of biomes. if unspecified, the vein may spawn in any biome.
      "biomes": "#minecraft:is_badlands",
        * if biomes exists, determines whether the biome will be sampled at the surface height,
        * rather than the vein's center.
        * an optional boolean. defaults to false.
      "sample_biome_at_surface": true,
        * for spacing and separation, see https://minecraft.wiki/w/Bastion_Remnant#/media/File:Java_Nether_Structure_Generation1.png
        * the maximum distance between ore veins (the size of a grid cell on that graph)
        * an integer. must be a positive multiple of 16
      "spacing": 128,
        * the minimum distance between ore veins (the red area on that graph)
        * an integer. must be positive and strictly less than spacing.
      "separation": 32,
        * the probability that a vein will be discarded completely.
        * a number between 0.0 and 1.0
      "probability": 1.0,
        * the generation order of the vein
        * higher values = generates later
        * an integer. default: 0
      "generation_order": 0,

        -- SIZE CONTROLS --
        * the radius of the ore vein. an IntProvider
      "radius": {
        "type": "uniform",
        "min_inclusive": 8,
        "max_inclusive": 48
      },
        * for min and max height, if max height is less than min height, then the ore vein will not generate.
        * determines the lower bound of the ore vein.
        * a HeightProvider.
      "min_height": {
        "type": "constant",
        "value": {
          "above_bottom": 80
        }
      },
        * determines the upper bound of the ore vein.
        * a HeightProvider.
      "max_height": {
        "type": "constant",
        "value": {
          "above_bottom": 160
        }
      },

        -- SHAPE CONTROLS --
        * "radius" of an individual ore vein
        * or, if inverted is true, the radius of the "bubbles" formed between the veins.
        * if inverted is true, then the value should probably be larger...
        * reasonable range = ~3 to ~64, but can theoretically be any positive number
        * a FloatProvider. defaults to 5.12, which is approximately vanilla's.
      "vein_radius": {
        "type": "constant",
        "value": 5.12
      },
        * whether the ore veins should place in, kind of like, a vein-y manor, or a bubbly manor...
        * idk exactly how to describe this one. just try it and see
        * a boolean. defaults to false
      "inverted": false,

        -- BLOCK CONTROLS --
         * the condition in which the ore vein will place a block.
         * an optional BlockPredicate. if not specified, the vein will only place blocks
         * where the current block is #nomansland:ore_vein_replaceable
       "target_condition": {
         "type": "matching_block_tag",
         "tag": "nomansland:ore_vein_replaceable"
       }
        * the "filler" vein blockstate provider
      "filler": {
          * a block state provider, telling the vein what block to generate
        "state_provider": {
          "type": "minecraft:simple_state_provider",
          "state": { "Name": "minecraft:andesite" }
        },
          * the probability that this block will be randomly discarded
          * an optional number from 0.0 to 1.0. defaults to 1.0
          * note: if the filler block is discarded, the core block will attempt generation.
        "probability": 1.0,
          * how strictly this block sticks to the noise's shape. think of it as dithering.
          * an optional positive number. defaults to 0.0
        "incoherence": 0.5
      },
        * the "core" vein blockstate provider
        * follows the same conventions as the filler.
        * optional. if not included, no core will generate.
      "core": {
        "state_provider": {
          "type": "nomansland:extended_weighted_state_provider",
          "entries": [
            {
              "data": {
                "type": "nomansland:vertical_gradient_state_provider",
                "above_state": {
                  "type": "minecraft:simple_state_provider",
                  "state": { "Name": "minecraft:gold_ore" }
                },
                "below_state": {
                  "type": "minecraft:simple_state_provider",
                  "state": { "Name": "minecraft:deepslate_gold_ore" }
                },
                "gradient_top_height": 8,
                "gradient_bottom_height": 0
              },
              "weight": 18
            },
            {
              "data": {
                "type": "minecraft:simple_state_provider",
                "state": { "Name": "minecraft:raw_gold_block" }
              },
              "weight": 2
            }
          ]
        },
        "probability": 0.2,
        "incoherence": 0.5
      }
    }
 */
public record OreVeinType(boolean sampleBiomeAtSurface,
                          Optional<HolderSet<Biome>> biomes,
                          int spacing, int separation, float probability,
                          int generationOrder,
                          IntProvider radius,
                          HeightProvider minHeight, HeightProvider maxHeight,
                          FloatProvider veinRadius, boolean invert,
                          BlockPredicate targetCondition,
                          OreBlockState filler,
                          OreBlockState core) {
    public static final Codec<OreVeinType> CODEC = RecordCodecBuilder.create(
            codec -> codec.group(
                    // placement controls
                    Codec.BOOL.optionalFieldOf("sample_biome_at_surface", false).forGetter(OreVeinType::sampleBiomeAtSurface),
                    RegistryCodecs.homogeneousList(Registries.BIOME).optionalFieldOf("biomes").forGetter(OreVeinType::biomes),
                    Codec.INT.fieldOf("spacing").validate(OreVeinType::validateSpacing).forGetter(OreVeinType::spacing),
                    Codec.INT.fieldOf("separation").validate(OreVeinType::validateSeparation).forGetter(OreVeinType::separation),
                    Codec.floatRange(0.0F, 1.0F).fieldOf("probability").forGetter(OreVeinType::probability),
                    Codec.INT.optionalFieldOf("generation_order", 0).forGetter(OreVeinType::generationOrder),
                    // size controls
                    IntProvider.CODEC.fieldOf("radius").forGetter(OreVeinType::radius),
                    HeightProvider.CODEC.fieldOf("min_height").forGetter(OreVeinType::minHeight),
                    HeightProvider.CODEC.fieldOf("max_height").forGetter(OreVeinType::maxHeight),
                    // shape controls
                    FloatProvider.codec(0, 100000000.0F).optionalFieldOf("vein_radius", ConstantFloat.of(5.12F)).forGetter(OreVeinType::veinRadius),
                    Codec.BOOL.optionalFieldOf("invert", false).forGetter(OreVeinType::invert),
                    // block controls
                    BlockPredicate.CODEC.optionalFieldOf("target_condition", BlockPredicate.matchesTag(NMLTags.ORE_VEIN_REPLACEABLE)).forGetter(OreVeinType::targetCondition),
                    OreBlockState.CODEC.fieldOf("filler").forGetter(OreVeinType::filler),
                    OreBlockState.CODEC.optionalFieldOf("core", OreBlockState.EMPTY).forGetter(OreVeinType::core)
            ).apply(codec, OreVeinType::new)
    );

    public boolean targets(WorldGenLevel level, BlockPos pos, BlockState state) {
        if (this.targetCondition instanceof StateTestingPredicate predicate && predicate.offset.equals(Vec3i.ZERO)) return predicate.test(state);
        return this.targetCondition.test(level, pos);
    }

    private static DataResult<Integer> validateSpacing(Integer spacing) {
        return (spacing % 16 != 0) || (spacing <= 0) ? DataResult.error(() -> "Spacing must be positive and multiple of 16! Currently " + spacing) : DataResult.success(spacing);
    }

    private static DataResult<Integer> validateSeparation(Integer separation) {
        return separation <= 0 ? DataResult.error(() -> "Separation must be positive!") : DataResult.success(separation);
    }

    public record OreBlockState(BlockStateProvider stateProvider, float incoherence, float probability, Map<Block, Map<Block, Block>> replacements) {
        public static final OreBlockState EMPTY = new OreBlockState(BlockStateProvider.simple(Blocks.AIR), 0, 0, Map.of());
        public static final Codec<OreBlockState> CODEC = RecordCodecBuilder.create(
                codec -> codec.group(
                        BlockStateProvider.CODEC.fieldOf("state_provider").forGetter(OreBlockState::stateProvider),
                        Codec.floatRange(0.0F, 100000000.0F).optionalFieldOf("incoherence", 0.0F).forGetter(OreBlockState::incoherence),
                        Codec.floatRange(0.0F, 1.0F).optionalFieldOf("probability", 1.0F).forGetter(OreBlockState::probability),
                        Codec.unboundedMap(
                                BuiltInRegistries.BLOCK.byNameCodec(),
                                Codec.unboundedMap(BuiltInRegistries.BLOCK.byNameCodec(), BuiltInRegistries.BLOCK.byNameCodec())
                        ).optionalFieldOf("replacements", Map.of()).forGetter(OreBlockState::replacements)
                ).apply(codec, OreBlockState::new)
        );

        public BlockState resolve(RandomSource random, BlockPos pos, BlockState currentState) {
            BlockState resolved = stateProvider.getState(random, pos);
            Map<Block, Block> mapping = replacements.get(resolved.getBlock());
            if (mapping == null) return resolved;

            Block targetBlock = mapping.get(currentState.getBlock());
            if (targetBlock == null) return resolved;
            return targetBlock.withPropertiesOf(currentState);
        }

        private static <T extends Comparable<T>> BlockState copyProperty(BlockState target, BlockState source, Property<T> property) {
            if (!target.hasProperty(property)) return target;
            return target.setValue(property, source.getValue(property));
        }
    }
}
