package com.farcr.nomansland.datagen.worldgen;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.registry.NMLTags;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import com.farcr.nomansland.common.registry.worldgen.NMLBiomes;
import com.farcr.nomansland.common.world.surfacerule.AndConditionSource;
import com.farcr.nomansland.common.world.surfacerule.BelowOrEqualToYConditionSource;
import com.farcr.nomansland.common.world.surfacerule.BiomeTagConditionSource;
import com.farcr.nomansland.common.world.surfacerule.OrConditionSource;
import com.teamabnormals.blueprint.common.world.modification.chunk.ChunkGeneratorModifierProvider;
import com.teamabnormals.blueprint.common.world.modification.chunk.modifiers.SurfaceRuleModifier;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.placement.CaveSurface;
import net.neoforged.neoforge.common.Tags;

import java.util.concurrent.CompletableFuture;

import static net.minecraft.world.level.levelgen.SurfaceRules.*;

public class NMLChunkGeneratorModifierProvider extends ChunkGeneratorModifierProvider {
    private static final RuleSource COARSE_DIRT = state(Blocks.COARSE_DIRT.defaultBlockState());
    private static final RuleSource PODZOL = state(Blocks.PODZOL.defaultBlockState());
    private static final RuleSource MUD = state(Blocks.MUD.defaultBlockState());
    private static final RuleSource SILT = state(NMLBlocks.SILT.get().defaultBlockState());
    private static final RuleSource WATER = state(Blocks.WATER.defaultBlockState());
    private static final RuleSource GRAVEL = state(Blocks.GRAVEL.defaultBlockState());
    private static final RuleSource SAND = state(Blocks.SAND.defaultBlockState());
    private static final RuleSource SNOW_BLOCK = state(Blocks.SNOW_BLOCK.defaultBlockState());
    private static final RuleSource PACKED_ICE = state(Blocks.PACKED_ICE.defaultBlockState());
    private static final RuleSource ICE = state(Blocks.ICE.defaultBlockState());
    private static final RuleSource STONE = state(Blocks.STONE.defaultBlockState());
    private static final RuleSource SANDSTONE_UNDER_SAND = sequence(
        ifTrue(ON_FLOOR, SAND),
        ifTrue(UNDER_FLOOR, state(Blocks.SANDSTONE.defaultBlockState()))
    );
    private static final RuleSource DEEP_GRAVEL = sequence(
        ifTrue(ON_FLOOR, GRAVEL),
        ifTrue(UNDER_FLOOR, state(Blocks.GRAVEL.defaultBlockState()))
    );
    private static final RuleSource CLASSIC_SOIL = sequence(
        ifTrue(ON_FLOOR, state(Blocks.GRASS_BLOCK.defaultBlockState())),
        ifTrue(UNDER_FLOOR, state(Blocks.DIRT.defaultBlockState()))
    );
    private static final ConditionSource BEACH = new AndConditionSource(
        new BelowOrEqualToYConditionSource(VerticalAnchor.absolute(69), true, 1.5F, 0.5F),
        yBlockCheck(VerticalAnchor.absolute(53), 2)
    );
    private static final ConditionSource SHORE = new AndConditionSource(
        new BelowOrEqualToYConditionSource(VerticalAnchor.absolute(65), true, 1, 0.75F),
        yBlockCheck(VerticalAnchor.absolute(53), 2)
    );

    public NMLChunkGeneratorModifierProvider(PackOutput output, CompletableFuture<Provider> provider) {
        super(NoMansLand.MODID, output, provider);
    }

    @Override
    protected void registerEntries(Provider provider) {
        RuleSource gravel_shores = ifTrue(
            new BiomeTagConditionSource(NMLTags.HAS_GRAVEL_SHORE),
            ifTrue(SHORE, sequence(
                ifTrue(surfaceNoiseAbove(0.33), SILT),
                GRAVEL
            ))
        );
        RuleSource mud_shores = ifTrue(
            new BiomeTagConditionSource(Tags.Biomes.IS_SWAMP),
            ifTrue(SHORE, MUD)
        );
        //Biomes
        RuleSource jungle = ifTrue(
            isBiome(Biomes.JUNGLE),
            ifTrue(surfaceNoiseAbove(1.25), COARSE_DIRT)
        );

        RuleSource darkForest = ifTrue(
            isBiome(Biomes.DARK_FOREST),
            ifTrue(surfaceNoiseAbove(1.25), ifTrue(ON_FLOOR, PODZOL))
        );

        RuleSource autumnalForest = ifTrue(
            isBiome(NMLBiomes.AUTUMNAL_FOREST),
            sequence(
                ifTrue(surfaceNoiseAbove(1.75), ifTrue(ON_FLOOR, COARSE_DIRT)),
                ifTrue(surfaceNoiseAbove(-0.95), ifTrue(ON_FLOOR, PODZOL)))
        );

        RuleSource mapleForest = ifTrue(
            isBiome(NMLBiomes.MAPLE_FOREST),
            ifTrue(surfaceNoiseAbove(1.25), ifTrue(ON_FLOOR, PODZOL))
        );

        RuleSource oldGrowthForest = ifTrue(
            isBiome(NMLBiomes.OLD_GROWTH_FOREST),
            ifTrue(surfaceNoiseAbove(1.25), COARSE_DIRT)
        );

        RuleSource frozenWoods = ifTrue(
            isBiome(NMLBiomes.FROZEN_WOODS),
            sequence(
                ifTrue(surfaceNoiseAbove(2.25), SNOW_BLOCK),
                ifTrue(surfaceNoiseAbove(1.5), MUD),
                ifTrue(surfaceNoiseAbove(1.0), SILT)
            )
        );

        RuleSource bog = ifTrue(
            isBiome(NMLBiomes.BOG),
            ifTrue(yBlockCheck(VerticalAnchor.absolute(62), 0),
                ifTrue(not(yBlockCheck(VerticalAnchor.absolute(63), 0)),
                    ifTrue(noiseCondition(Noises.SWAMP, 0.0), WATER)))
        );

        RuleSource bayou = ifTrue(
            isBiome(NMLBiomes.BAYOU),
            sequence(
                ifTrue(surfaceNoiseAbove(2.0), ifTrue(ON_FLOOR, MUD)),
                ifTrue(surfaceNoiseAbove(1.75), ifTrue(ON_FLOOR, PODZOL)),
                ifTrue(yBlockCheck(VerticalAnchor.absolute(62), 0),
                    ifTrue(not(yBlockCheck(VerticalAnchor.absolute(63), 0)),
                        ifTrue(noiseCondition(Noises.SWAMP, 0.0), WATER))))
        );

        RuleSource darkSwamp = ifTrue(
            isBiome(NMLBiomes.DARK_SWAMP),
            sequence(
                ifTrue(surfaceNoiseAbove(1.25), ifTrue(ON_FLOOR, PODZOL)),
                ifTrue(yBlockCheck(VerticalAnchor.absolute(62), 0),
                    ifTrue(not(yBlockCheck(VerticalAnchor.absolute(63), 0)),
                        ifTrue(noiseCondition(Noises.SWAMP, 0.0), WATER))))
        );

        RuleSource stonyShore = ifTrue(
            isBiome(Biomes.STONY_SHORE),
            sequence(
                ifTrue(surfaceNoiseAbove(1.9), SILT),
                ifTrue(surfaceNoiseAbove(1.7), sequence(
                    ifTrue(noiseCondition(Noises.SWAMP, 0.0), SILT),
                    GRAVEL
                )),
                ifTrue(surfaceNoiseAbove(-0.45), GRAVEL),
                ifTrue(surfaceNoiseAbove(-0.95), ifTrue(noiseCondition(Noises.SWAMP, 0.0), GRAVEL)))
        );

        RuleSource mushroom_fields = ifTrue(isBiome(Biomes.MUSHROOM_FIELDS), ifTrue(BEACH, SILT));

        //River Biomes
        RuleSource lush_river = ifTrue(
            isBiome(NMLBiomes.LUSH_RIVER),
            sequence(
                ifTrue(surfaceNoiseAbove(1.25), COARSE_DIRT),
                ifTrue(yBlockCheck(VerticalAnchor.absolute(62), 0),
                    ifTrue(not(yBlockCheck(VerticalAnchor.absolute(63), 0)),
                        ifTrue(noiseCondition(Noises.SWAMP, 0.0), WATER))))
        );

        RuleSource blackwater_river = ifTrue(
            isBiome(NMLBiomes.BLACKWATER_RIVER),
            sequence(
                ifTrue(surfaceNoiseAbove(-0.75), MUD),
                ifTrue(yBlockCheck(VerticalAnchor.absolute(62), 0),
                    ifTrue(not(yBlockCheck(VerticalAnchor.absolute(63), 0)),
                        ifTrue(noiseCondition(Noises.SWAMP, 0.0), WATER))))
        );

        RuleSource desert_river = ifTrue(
            isBiome(NMLBiomes.DESERT_RIVER),
            ifTrue(not(BEACH), SANDSTONE_UNDER_SAND)
        );
        //Beach Biomes
        RuleSource mud_beach = ifTrue(
            isBiome(NMLBiomes.MUD_BEACH),
            ifTrue(
                BEACH,
                ifTrue(
                    UNDER_FLOOR,
                    state(Blocks.MUD.defaultBlockState())
                )
            )
        );
        RuleSource frozen_shore = ifTrue(
            isBiome(NMLBiomes.FROZEN_SHORE),
            sequence(
                ifTrue(surfaceNoiseAbove(1.9), SILT),
                ifTrue(surfaceNoiseAbove(1.7), sequence(
                    ifTrue(noiseCondition(Noises.SWAMP, 0.0), SILT),
                    GRAVEL
                )),
                ifTrue(surfaceNoiseAbove(-0.45), GRAVEL),
                ifTrue(surfaceNoiseAbove(-0.95), ifTrue(noiseCondition(Noises.SWAMP, 0.0), GRAVEL)))
        );
        RuleSource tropical_beach = ifTrue(
            isBiome(NMLBiomes.TROPICAL_BEACH),
            ifTrue(BEACH, SANDSTONE_UNDER_SAND)
        );

        RuleSource caves = ifTrue(
            isBiome(NMLBiomes.CAVES),
            ifTrue(stoneDepthCheck(0, true, CaveSurface.FLOOR),
                state(Blocks.STONE.defaultBlockState())
            )
        );

        RuleSource caves_surface = ifTrue(isBiome(NMLBiomes.CAVES), DEEP_GRAVEL);

        RuleSource cave_depths = ifTrue(
            isBiome(NMLBiomes.CAVE_DEPTHS),
            ifTrue(stoneDepthCheck(0, true, CaveSurface.FLOOR),
                state(Blocks.DEEPSLATE.defaultBlockState())
            )
        );

        var ruleSource = ifTrue(
            abovePreliminarySurface(),
            sequence(
                caves_surface,
                // deeper layer biome modifiers - sand, beaches...
                sequence(
                    gravel_shores,
                    mud_shores,
                    mushroom_fields,
                    desert_river,
                    mud_beach,
                    tropical_beach),
                // biome modifiers that handle their own ON_FLOOR/UNDER_FLOOR substrate
                ifTrue(waterBlockCheck(0, 0),
                    sequence(
                        darkForest, autumnalForest,
                        mapleForest, bayou, darkSwamp
                    )
                ),
                // top layer biome modifiers - grasses, etc.
                ifTrue(ON_FLOOR,
                    sequence(
                        // above water
                        ifTrue(waterBlockCheck(0, 0),
                            sequence(
                                jungle,
                                oldGrowthForest,
                                frozenWoods,
                                bog
                            )
                        ),
                        // below water, or if nothing generated in the previous step
                        sequence(
                            stonyShore,
                            frozen_shore,
                            lush_river,
                            blackwater_river
                        )
                    )
                )
            )
        );

        this.entry("overworld_surface_rules").selects("minecraft:overworld")
            .addModifier(new SurfaceRuleModifier(ruleSource, false));
    }

    private static ConditionSource surfaceNoiseAbove(double value) {
        return noiseCondition(Noises.SURFACE, value / 8.25, Double.MAX_VALUE);
    }

    private static ConditionSource or(ConditionSource a, ConditionSource b) {
        return new OrConditionSource(a, b);
    }

    private static ConditionSource and(ConditionSource a, ConditionSource b) {
        return new AndConditionSource(a, b);
    }
}
