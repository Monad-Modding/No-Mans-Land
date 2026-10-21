package com.farcr.nomansland.datagen.worldgen;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.registry.NMLParticleTypes;
import com.farcr.nomansland.common.registry.NMLSounds;
import com.farcr.nomansland.common.registry.NMLTags;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.Music;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.farcr.nomansland.common.registry.worldgen.NMLPlacedFeatures.*;
import static net.minecraft.data.worldgen.placement.MiscOverworldPlacements.*;
import static net.minecraft.data.worldgen.placement.VegetationPlacements.*;
import static net.minecraft.sounds.SoundEvents.*;
import static net.neoforged.neoforge.common.world.BiomeModifiers.AddFeaturesBiomeModifier;

public class NMLBiomeModifiers {
    private final BootstrapContext<BiomeModifier> bootstrap;
    private final Map<FeatureWithStep, List<Holder<Biome>>> featureToBiomes = new HashMap<>();

    public NMLBiomeModifiers(BootstrapContext<BiomeModifier> bootstrap) {
        this.bootstrap = bootstrap;
        setup();
    }

    private void setup() {
        GenerationStep.Decoration vegetalDecoration = GenerationStep.Decoration.VEGETAL_DECORATION;
        GenerationStep.Decoration localModifications = GenerationStep.Decoration.LOCAL_MODIFICATIONS;

        modifyBiome(Biomes.BADLANDS)
                .changeColors(15322281,
                        4106959,
                        3048361,
                        11128544
                )
                .addSpawns(
                        new SpawnerData(EntityType.RABBIT, 15, 2, 8),
                        new SpawnerData(EntityType.HUSK, 100, 4, 4)
                )
                .removeSpawns(EntityType.ZOMBIE)
                .build(featureToBiomes);

        modifyBiome(Biomes.BAMBOO_JUNGLE)
                .changeColors(
                        11071699,
                        2403207,
                        1479552,
                        7194319,
                        6798649,
                        6203171
                )
                .changeMusic(
                        new Music(MUSIC_BIOME_JUNGLE, 12000, 24000, false)
                )
                .addFeatures(
                        FeatureWithStep.vegetationFeatures(
                                flowerBed("jungle"),
                                patch("hearty_succulent")
                        )
                )
                .removeFeatures(PATCH_PUMPKIN)
                .changeSpawns(
                        new SpawnerData(EntityType.OCELOT, 8, 1, 3),
                        new SpawnerData(EntityType.PANDA, 80, 1, 4),
                        new SpawnerData(EntityType.PIG, 16, 4, 4),
                        new SpawnerData(EntityType.CHICKEN, 14, 4, 4),
                        new SpawnerData(EntityType.COW, 6, 4, 4)
                )
                .addSpawns(
                        new SpawnerData(EntityType.RABBIT, 15, 2, 8)
                )
                .removeSpawns(EntityType.SHEEP)
                .build(featureToBiomes);


        modifyBiome(Biomes.BEACH)
                .changeColors(
                        12906239,
                        3902136,
                        2388383,
                        7715315,
                        9680231,
                        8960833
                )
                .removeFeatures(
                        PATCH_GRASS_BADLANDS,
                        FLOWER_DEFAULT
                )
                .build(featureToBiomes);

        modifyBiome(Biomes.BIRCH_FOREST)
                .changeColors(
                        12906239,
                        3902136,
                        2388383,
                        7715315,
                        8367967,
                        6594108
                )
                .addFeatures(FeatureWithStep.vegetationFeatures(flowerBed("white"), flowers("birch_forest"), patch("peony_and_lilac")))
                .removeFeatures(FOREST_FLOWERS)
                .changeSpawns(
                        new SpawnerData(EntityType.SHEEP, 4, 1, 4),
                        new SpawnerData(EntityType.CHICKEN, 14, 4, 6),
                        new SpawnerData(EntityType.COW, 2, 4, 4)

                )
                .addSpawns(new SpawnerData(EntityType.RABBIT, 8, 3, 4))
                .build(featureToBiomes);

        modifyBiome(Biomes.CHERRY_GROVE)
                .changeColors(
                        15724287,
                        3895992,
                        2388383,
                        8307162,
                        9620606,
                        9098883
                )
                .addFeatures(FeatureWithStep.vegetationFeatures(
                        trees("cherry_grove"),
                        flowerBed("white_and_violet"),
                        flowers("cherry"),
                        PATCH_SUGAR_CANE,
                        BROWN_MUSHROOM_NORMAL,
                        PATCH_WATERLILY
                ))
                .removeFeatures(TREES_CHERRY)
                .changeSpawns(
                        new SpawnerData(EntityType.PIG, 4, 1, 8),
                        new SpawnerData(EntityType.RABBIT, 8, 2, 6),
                        new SpawnerData(EntityType.SHEEP, 8, 2, 4)
                )
                .addSpawns(new SpawnerData(EntityType.CHICKEN, 4, 2, 6))
                .build(featureToBiomes);

        modifyBiome(Biomes.COLD_OCEAN)
                .changeColors(
                        12639487,
                        3899064,
                        2386591,
                        8431871,
                        9680231,
                        8960833
                )
                .addFeatures(FeatureWithStep.vegetationFeatures(
                        flowers("white"),
                        flowerBed("white")
                ))
                .removeFeatures(FLOWER_DEFAULT, PATCH_PUMPKIN, PATCH_SUGAR_CANE)
                .build(featureToBiomes);

        modifyBiome(Biomes.DARK_FOREST)
                .changeColors(
                        12906239,
                        3902136,
                        2388383,
                        7715315,
                        6387766,
                        6523436
                )
                .addFeatures(FeatureWithStep.vegetationFeatures(
                        flowers("maple"),
                        flowerBed("red"),
                        patch("rose_bush"),
                        feature("shelf_mushroom_dark_forest"),
                        BROWN_MUSHROOM_OLD_GROWTH
                ))
                .removeFeatures(FLOWER_DEFAULT, FOREST_FLOWERS, BROWN_MUSHROOM_NORMAL)
                .changeSpawns(new SpawnerData(EntityType.CHICKEN, 14, 4, 6))
                .addSpawns(
                        new SpawnerData(EntityType.RABBIT, 8, 3, 4),
                        new SpawnerData(EntityType.WOLF, 7, 2, 6)
                )
                .removeSpawns(EntityType.SHEEP, EntityType.COW)
                .build(featureToBiomes);

        modifyBiome(Biomes.DEEP_COLD_OCEAN)
                .changeColors(
                        12639487,
                        3899064,
                        2386591,
                        8431871,
                        9680231,
                        8960833
                )
                .addFeatures(FeatureWithStep.vegetationFeatures(
                        flowers("white"),
                        flowerBed("white")
                ))
                .removeFeatures(FLOWER_DEFAULT, PATCH_PUMPKIN, PATCH_SUGAR_CANE)
                .build(featureToBiomes);

        modifyBiome(Biomes.DEEP_DARK)
                .changeColors(
                        745045,
                        1327441,
                        662565,
                        745045,
                        8622426,
                        7631435
                )
                .changeParticle(NMLParticleTypes.SCULK_AMBIENCE.get(), 0.005F)
                .build(featureToBiomes);

        modifyBiome(Biomes.DEEP_FROZEN_OCEAN)
                .changeColors(
                        15200511,
                        4223408,
                        2383519,
                        8431871,
                        10205860,
                        9680231
                )
                .addFeatures(FeatureWithStep.vegetationFeatures(
                        flowers("white"),
                        flowerBed("white")
                ))
                .removeFeatures(FLOWER_DEFAULT, PATCH_PUMPKIN, PATCH_SUGAR_CANE)
                .build(featureToBiomes);

        modifyBiome(Biomes.DEEP_LUKEWARM_OCEAN)
                .changeColors(
                        12711423,
                        3906239,
                        2979747,
                        6800872,
                        9419352,
                        8960833
                )
                .addFeatures(FeatureWithStep.vegetationFeatures(
                        flowers("white"),
                        flowerBed("white")
                ))
                .removeFeatures(FLOWER_DEFAULT, PATCH_PUMPKIN)
                .build(featureToBiomes);

        modifyBiome(Biomes.DEEP_OCEAN)
                .changeColors(
                        12906239,
                        3902136,
                        2388383,
                        7715315,
                        9680231,
                        8960833
                )
                .addFeatures(FeatureWithStep.vegetationFeatures(
                        flowers("white"),
                        flowerBed("white")
                ))
                .removeFeatures(FLOWER_DEFAULT, PATCH_PUMPKIN)
                .build(featureToBiomes);

        modifyBiome(Biomes.DESERT)
                .changeColors(
                        15527891,
                        4106959,
                        3048361,
                        12113081,
                        14663027,
                        12762457
                )
                .removeFeatures(
                        PATCH_SUGAR_CANE_DESERT
                )
                .changeSpawns(
                        new SpawnerData(EntityType.HUSK, 100, 4, 4),
                        new SpawnerData(EntityType.ZOMBIE_VILLAGER, 5, 1, 1),
                        new SpawnerData(EntityType.RABBIT, 20, 2, 8)
                )
                .addSpawns(new SpawnerData(EntityType.CAMEL, 20, 2, 8))
                .removeSpawns(EntityType.ZOMBIE)
                .build(featureToBiomes);

        modifyBiome(Biomes.DRIPSTONE_CAVES)
                .changeColors(
                        4210492,
                        6913929,
                        3820113,
                        7907327,
                        8622426,
                        7631435
                )
                .changeMusic(new Music(NMLSounds.CAVE_MUSIC, 12000, 24000, false))
                .changeParticle(NMLParticleTypes.CAVE_DUST.get(), 0.01F)
                .addFeatures(new FeatureWithStep(feature("underground/ore_silt"), GenerationStep.Decoration.UNDERGROUND_ORES))
                .build(featureToBiomes);

        modifyBiome(Biomes.ERODED_BADLANDS)
                .changeColors(
                        15322281,
                        4106959,
                        3048361,
                        11128544,
                        10387789,
                        9470285
                )
                .changeSpawns(new SpawnerData(EntityType.ARMADILLO, 8, 1, 2))
                .addSpawns(
                        new SpawnerData(EntityType.RABBIT, 13, 2, 8),
                        new SpawnerData(EntityType.HUSK, 100, 4, 4)
                )
                .removeSpawns(EntityType.ZOMBIE, EntityType.WOLF)
                .build(featureToBiomes);

        modifyBiome(Biomes.FLOWER_FOREST)
                .changeColors(
                        12906239,
                        3902136,
                        2388383,
                        7715315,
                        11393889,
                        10407783
                )
                .addFeatures(FeatureWithStep.vegetationFeatures(
                        flowers("flower_forest"),
                        flowerBed("flower_forest"),
                        feature("patch_peony")
                ))
                .removeFeatures(FLOWER_FOREST_FLOWERS, FLOWER_FLOWER_FOREST, PATCH_PUMPKIN)
                .changeSpawns(
                        new SpawnerData(EntityType.SHEEP, 8, 3, 4),
                        new SpawnerData(EntityType.PIG, 14, 4, 6),
                        new SpawnerData(EntityType.CHICKEN, 12, 4, 8),
                        new SpawnerData(EntityType.COW, 6, 3, 4),
                        new SpawnerData(EntityType.RABBIT, 10, 3, 6)
                )
                .addSpawns(new SpawnerData(EntityType.FOX, 5, 4, 4))
                .build(featureToBiomes);

        modifyBiome(Biomes.FOREST)
                .changeColors(
                        12906239,
                        3902136,
                        2388383,
                        7715315,
                        8497229,
                        8363073
                )
                .addFeatures(FeatureWithStep.vegetationFeatures(
                        trees("forest_noise"),
                        flowers("forest"),
                        flowerBed("white_and_yellow"),
                        patch("peony_and_lilac")
                ))
                .changeSpawns(
                        new SpawnerData(EntityType.SHEEP, 5, 3, 4),
                        new SpawnerData(EntityType.PIG, 14, 4, 6),
                        new SpawnerData(EntityType.CHICKEN, 16, 4, 8),
                        new SpawnerData(EntityType.COW, 4, 3, 4)
                )
                .addSpawns(new SpawnerData(EntityType.RABBIT, 8, 3, 6))
                .removeFeatures(FLOWER_DEFAULT, FOREST_FLOWERS)
                .build(featureToBiomes);

        modifyBiome(Biomes.FROZEN_OCEAN)
                .changeColors(
                        15200511,
                        4220592,
                        2771621,
                        8431871,
                        10205860,
                        5541228
                )
                .addFeatures(FeatureWithStep.vegetationFeatures(
                        flowers("white"),
                        flowerBed("frosted")
                ))
                .removeFeatures(PATCH_PUMPKIN, PATCH_SUGAR_CANE)
                .build(featureToBiomes);

        modifyBiome(Biomes.FROZEN_PEAKS)
                .changeColors(
                        15200511,
                        4220592,
                        2771621,
                        8431871,
                        10205860,
                        5541228
                )
                .addSpawns(new SpawnerData(EntityType.POLAR_BEAR, 1, 1, 2))
                .build(featureToBiomes);

        modifyBiome(Biomes.FROZEN_RIVER)
                .changeColors(
                        15200511,
                        4220592,
                        2771621,
                        10797823,
                        10205860,
                        5541228
                )
                .addFeatures(FeatureWithStep.vegetationFeatures(
                        flowers("white"),
                        flowerBed("frosted")
                ))
                .addSpawns(new SpawnerData(EntityType.POLAR_BEAR, 1, 1, 2))
                .removeFeatures(FLOWER_DEFAULT, PATCH_GRASS_BADLANDS)
                .build(featureToBiomes);

        modifyBiome(Biomes.GROVE)
                .changeColors(
                        15200511,
                        4220592,
                        2771621,
                        7715315,
                        10205860,
                        10205860
                )
                .addFeatures(FeatureWithStep.vegetationFeatures(
                        flowers("white"),
                        flowerBed("frosted")
                ))
                .removeFeatures(PATCH_PUMPKIN, PATCH_SUGAR_CANE)
                .changeSpawns(
                        new SpawnerData(EntityType.WOLF, 3, 1, 1),
                        new SpawnerData(EntityType.RABBIT, 12, 2, 3),
                        new SpawnerData(EntityType.FOX, 7, 2, 4)
                )
                .addSpawns(new SpawnerData(EntityType.POLAR_BEAR, 1, 1, 2))
                .build(featureToBiomes);

        modifyBiome(Biomes.ICE_SPIKES)
                .changeColors(
                        15200511,
                        4220592,
                        2771621,
                        8431871,
                        10205860,
                        5541228
                )
                .addFeatures(FeatureWithStep.vegetationFeatures(
                        flowers("white"),
                        flowerBed("frosted")
                ))
                .changeSpawns(new SpawnerData(EntityType.POLAR_BEAR, 1, 1, 2))
                .removeFeatures(FLOWER_DEFAULT)
                .build(featureToBiomes);

        modifyBiome(Biomes.JAGGED_PEAKS)
                .changeColors(
                        15200511,
                        3899064,
                        2386591,
                        7715315,
                        10205860,
                        5541228
                )
                .build(featureToBiomes);

        modifyBiome(Biomes.JUNGLE)
                .changeColors(
                        11071699,
                        2403207,
                        1479552,
                        7194319,
                        6798649,
                        6203171
                )
                .addFeatures(FeatureWithStep.vegetationFeatures(
                        flowerBed("jungle"),
                        patch("duckweed_sparse"),
                        patch("hearty_succulent"),
                        PATCH_LARGE_FERN
                ))
                .removeFeatures(PATCH_PUMPKIN)
                .changeSpawns(
                        new SpawnerData(EntityType.PIG, 16, 4, 4),
                        new SpawnerData(EntityType.CHICKEN, 14, 4, 4),
                        new SpawnerData(EntityType.CHICKEN, 14, 4, 4),
                        new SpawnerData(EntityType.PANDA, 4, 1, 2),
                        new SpawnerData(EntityType.OCELOT, 8, 1, 3)
                )
                .addSpawns(
                        new SpawnerData(EntityType.FROG, 8, 3, 5)
                )
                .removeSpawns(EntityType.SHEEP)
                .build(featureToBiomes);

        modifyBiome(Biomes.LUKEWARM_OCEAN)
                .changeColors(
                        12711423,
                        3906239,
                        2979747,
                        6800872,
                        9419352,
                        8960833
                )
                .addFeatures(FeatureWithStep.vegetationFeatures(
                        flowers("white"),
                        flowerBed("white")
                ))
                .removeFeatures(FLOWER_DEFAULT, PATCH_PUMPKIN)
                .build(featureToBiomes);

        modifyBiome(Biomes.LUSH_CAVES)
                .changeMusic(new Music(NMLSounds.CAVE_MUSIC, 12000, 24000, false))
                .changeColors(
                        11071699,
                        5536348,
                        2897190,
                        8103167,
                        11851346,
                        11851346
                )
                .addFeatures(FeatureWithStep.vegetationFeatures(feature("underground/patch_duckweed_lush_caves")))
                .addFeatures(new FeatureWithStep(feature("underground/tortoise_burrows/tortoise_burrow_lush"), GenerationStep.Decoration.TOP_LAYER_MODIFICATION))
                .build(featureToBiomes);

        modifyBiome(Biomes.MANGROVE_SWAMP)
                .changeColors(
                        11333800,
                        3832426,
                        5077600,
                        7194319,
                        0,
                        9285927
                )
                .addFeatures(FeatureWithStep.vegetationFeatures(patch("water_mosaic")))
                .build(featureToBiomes);

        modifyBiome(Biomes.MEADOW)
                .changeColors(
                        12639487,
                        3895992,
                        2386591,
                        8431871,
                        8501355,
                        6465351
                )
                .addFeatures(FeatureWithStep.vegetationFeatures(
                        flowerBed("white_and_violet"),
                        patch("field_mushroom_extra"),
                        feature("clover_patch_meadow"),
                        feature("field_mushroom_circle_meadow"),
                        BROWN_MUSHROOM_NORMAL,
                        RED_MUSHROOM_NORMAL
                ))
                .changeSpawns(
                        new SpawnerData(EntityType.DONKEY, 1, 1, 2)
                )
                .build(featureToBiomes);

        modifyBiome(Biomes.MUSHROOM_FIELDS)
                .changeColors(
                        15132415,
                        8616598,
                        5921653,
                        8898024,
                        11772555,
                        10583946
                )
                .addFeatures(
                        FeatureWithStep.vegetationFeatures(
                                patch("mycelium_growths"),
                                patch("duckweed_sparse"),
                                PATCH_DEAD_BUSH,
                                patch("dried_grass_mycelium")
                        )
                )
                .addFeatures(new FeatureWithStep(feature("crag_rock_mycelium"), GenerationStep.Decoration.LOCAL_MODIFICATIONS))
                .addSpawns(
                        new SpawnerData(EntityType.FROG, 4, 3, 5)
                )
                .changeMusic(new Music(NMLSounds.MUSHROOM_BIOME_MUSIC, 12000, 24000, false))
                .removeFeatures(DISK_SAND, DISK_GRAVEL, PATCH_SUGAR_CANE, PATCH_PUMPKIN)
                .build(featureToBiomes);

        modifyBiome(Biomes.OCEAN)
                .changeColors(
                        12906239,
                        3902136,
                        2388383,
                        7715315,
                        9680231,
                        8960833
                )
                .addFeatures(
                        FeatureWithStep.vegetationFeatures(
                                flowers("white"),
                                flowerBed("white")
                        )
                )
                .removeFeatures(FLOWER_DEFAULT, PATCH_PUMPKIN)
                .build(featureToBiomes);

        modifyBiome(Biomes.OLD_GROWTH_BIRCH_FOREST)
                .changeColors(
                        14411213,
                        3902136,
                        2388383,
                        7715315,
                        12300634,
                        6594108
                )
                .addFeatures(
                        FeatureWithStep.vegetationFeatures(
                                trees("old_growth_birch"),
                                flowers("old_growth_birch"),
                                flowerBed("yellow")
                        )
                )
                .removeFeatures(FOREST_FLOWERS, FLOWER_DEFAULT, BIRCH_TALL)
                .changeSpawns(
                        new SpawnerData(EntityType.SHEEP, 4, 1, 4),
                        new SpawnerData(EntityType.CHICKEN, 14, 4, 6),
                        new SpawnerData(EntityType.COW, 2, 4, 4)
                )
                .addSpawns(
                        new SpawnerData(EntityType.RABBIT, 8, 3, 4),
                        new SpawnerData(EntityType.FOX, 2, 2, 6)
                )
                .build(featureToBiomes);

        modifyBiome(Biomes.OLD_GROWTH_PINE_TAIGA)
                .changeColors(
                        12639487,
                        3899064,
                        2386591,
                        8431871,
                        9149532,
                        8292172
                )
                .addFeatures(
                        FeatureWithStep.vegetationFeatures(
                                flowers("taiga"),
                                flowerBed("taiga"),
                                patch("rose_bush")
                        )
                )
                .removeFeatures(FLOWER_DEFAULT, PATCH_DEAD_BUSH, FOREST_ROCK)
                .changeSpawns(
                        new SpawnerData(EntityType.SHEEP, 6, 1, 4),
                        new SpawnerData(EntityType.PIG, 14, 4, 6),
                        new SpawnerData(EntityType.CHICKEN, 10, 4, 8),
                        new SpawnerData(EntityType.WOLF, 9, 2, 4),
                        new SpawnerData(EntityType.RABBIT, 12, 2, 3),
                        new SpawnerData(EntityType.ZOMBIE, 95, 4, 4),
                        new SpawnerData(EntityType.ZOMBIE_VILLAGER, 5, 1, 1)
                )
                .removeSpawns(EntityType.COW)
                .build(featureToBiomes);

        modifyBiome(Biomes.OLD_GROWTH_SPRUCE_TAIGA)
                .changeColors(
                        12639487,
                        3899064,
                        2386591,
                        8431871,
                        9149532,
                        8292172
                )
                .addFeatures(FeatureWithStep.vegetationFeatures(
                        flowers("taiga"),
                        flowerBed("taiga"),
                        patch("rose_bush")
                ))
                .removeFeatures(FLOWER_DEFAULT, FOREST_ROCK)
                .changeSpawns(
                        new SpawnerData(EntityType.SHEEP, 6, 1, 4),
                        new SpawnerData(EntityType.PIG, 14, 4, 6),
                        new SpawnerData(EntityType.CHICKEN, 10, 4, 8),
                        new SpawnerData(EntityType.WOLF, 9, 2, 4),
                        new SpawnerData(EntityType.RABBIT, 12, 2, 3)
                )
                .removeSpawns(EntityType.COW)
                .build(featureToBiomes);

        modifyBiome(Biomes.PLAINS)
                .changeColors(
                        12906239,
                        3902136,
                        2388383,
                        7715315,
                        9419352,
                        8960833
                )
                .addFeatures(FeatureWithStep.vegetationFeatures(
                        flowers("plains"),
                        flowerBed("white_and_yellow")
                ))
                .removeFeatures(FLOWER_PLAINS)
                .changeSpawns(
                        new SpawnerData(EntityType.SHEEP, 16, 4, 8),
                        new SpawnerData(EntityType.PIG, 4, 2, 4),
                        new SpawnerData(EntityType.COW, 14, 4, 6)
                )
                .addSpawns(new SpawnerData(EntityType.RABBIT, 14, 3, 6))
                .removeSpawns(EntityType.CHICKEN)
                .build(featureToBiomes);

        modifyBiome(Biomes.RIVER)
                .changeColors(
                        12906239,
                        3902136,
                        2388383,
                        7715315,
                        9680231,
                        8960833
                )
                .addFeatures(FeatureWithStep.vegetationFeatures(
                        flowers("white"),
                        flowerBed("white"),
                        PATCH_GRASS_NORMAL
                ))
                .removeFeatures(FLOWER_DEFAULT, PATCH_GRASS_BADLANDS)
                .build(featureToBiomes);

        modifyBiome(Biomes.SAVANNA)
                .changeColors(
                        12711423,
                        3906239,
                        2979747,
                        6800872,
                        13415770,
                        10067503
                )
                .addFeatures(FeatureWithStep.vegetationFeatures(
                        flowers("savanna"),
                        flowerBed("savanna"),
                        patch("barrel_cactus_normal"),
                        patch("succulent_normal")
                ))
                .removeFeatures(FLOWER_WARM)
                .changeSpawns(
                        new SpawnerData(EntityType.PIG, 4, 4, 4),
                        new SpawnerData(EntityType.CHICKEN, 4, 4, 4),
                        new SpawnerData(EntityType.COW, 14, 2, 6)
                )
                .addSpawns(new SpawnerData(EntityType.WOLF, 8, 4, 8))
                .build(featureToBiomes);

        modifyBiome(Biomes.SAVANNA_PLATEAU)
                .changeColors(
                        12711423,
                        3906239,
                        2979747,
                        6800872,
                        13415770,
                        10067503
                )
                .addFeatures(FeatureWithStep.vegetationFeatures(
                        flowers("savanna"),
                        flowerBed("savanna"),
                        patch("barrel_cactus_normal"),
                        patch("succulent_normal")
                ))
                .removeFeatures(FLOWER_WARM)
                .changeSpawns(
                        new SpawnerData(EntityType.PIG, 4, 4, 4),
                        new SpawnerData(EntityType.CHICKEN, 4, 4, 4),
                        new SpawnerData(EntityType.COW, 14, 4, 4)
                )
                .removeSpawns(EntityType.ARMADILLO)
                .build(featureToBiomes);

        modifyBiome(Biomes.SNOWY_BEACH)
                .changeColors(
                        15200511,
                        4220592,
                        2771621,
                        10797823,
                        10205860,
                        5541228
                )
                .addFeatures(FeatureWithStep.vegetationFeatures(
                        flowers("white"),
                        flowerBed("frosted"),
                        TREES_WATER
                ))
                .removeFeatures(FLOWER_DEFAULT)
                .build(featureToBiomes);

        modifyBiome(Biomes.SNOWY_PLAINS)
                .changeColors(
                        15200511,
                        4220592,
                        2771621,
                        10797823,
                        10205860,
                        5541228
                )
                .addFeatures(FeatureWithStep.vegetationFeatures(
                        flowers("frosted"),
                        flowerBed("frosted")
                ))
                .removeFeatures(FLOWER_DEFAULT)
                .build(featureToBiomes);

        modifyBiome(Biomes.SNOWY_SLOPES)
                .changeColors(
                        15200511,
                        4220592,
                        2771621,
                        7715315,
                        10205860,
                        10205860
                )
                .build(featureToBiomes);

        modifyBiome(Biomes.SNOWY_TAIGA)
                .changeColors(
                        15200511,
                        4220592,
                        2771621,
                        10797823,
                        10205860,
                        13415770
                )
                .changeMusic(new Music(MUSIC_BIOME_OLD_GROWTH_TAIGA, 12000, 24000, false))
                .addFeatures(FeatureWithStep.vegetationFeatures(
                        trees("snowy_taiga"),
                        flowers("white"),
                        flowerBed("frosted"),
                        patch("grass_snowy"),
                        PATCH_BERRY_COMMON
                ))
                .removeFeatures(TREES_TAIGA, FLOWER_DEFAULT, PATCH_GRASS_TAIGA_2, PATCH_BERRY_RARE)
                .changeSpawns(
                        new SpawnerData(EntityType.SHEEP, 6, 1, 4),
                        new SpawnerData(EntityType.CHICKEN, 10, 4, 8),
                        new SpawnerData(EntityType.RABBIT, 12, 2, 3),
                        new SpawnerData(EntityType.FOX, 10, 2, 4)
                )
                .addSpawns(new SpawnerData(EntityType.POLAR_BEAR, 2, 1, 3))
                .removeSpawns(EntityType.PIG, EntityType.COW)
                .build(featureToBiomes);

        // TODO: change downfall to 0.9 from 0.8
        modifyBiome(Biomes.SPARSE_JUNGLE)
                .changeColors(
                        13107185,
                        3517088,
                        55668872,
                        7192271,
                        8174674,
                        7516981
                )
                .changeMusic(new Music(MUSIC_BIOME_JUNGLE, 12000, 24000, false))
                .addFeatures(FeatureWithStep.vegetationFeatures(
                        flowerBed("jungle"),
                        patch("hearty_succulent")
                ))
                .removeFeatures(PATCH_PUMPKIN)
                .changeSpawns(
                        new SpawnerData(EntityType.PIG, 12, 4, 4),
                        new SpawnerData(EntityType.COW, 10, 4, 4)
                )
                .addSpawns(
                        new SpawnerData(EntityType.PARROT, 10, 1, 2),
                        new SpawnerData(EntityType.OCELOT, 6, 1, 3)
                )
                .removeSpawns(EntityType.SHEEP)
                .build(featureToBiomes);

        modifyBiome(Biomes.STONY_PEAKS)
                .changeColors(
                        12906239,
                        3902136,
                        2388383,
                        7715315,
                        9620606,
                        9098883
                )
                .addFeatures(FeatureWithStep.vegetationFeatures(flowers("birch_forest")))
                .addSpawns(new SpawnerData(EntityType.GOAT, 7, 1, 3))
                .build(featureToBiomes);

        modifyBiome(Biomes.STONY_SHORE)
                .changeColors(
                        12906239,
                        3902136,
                        2388383,
                        7715315,
                        9680231,
                        8960833
                )
                .addFeatures(
                        new FeatureWithStep(feature("pond_stony_shore"), localModifications),
                        new FeatureWithStep(feature("tuff_boulder"), GenerationStep.Decoration.UNDERGROUND_DECORATION) // later stage to avoid ore generation
                )
                .addFeatures(FeatureWithStep.vegetationFeatures(
                        flowers("white"),
                        flowerBed("white")
                ))
                .removeFeatures(FLOWER_DEFAULT)
                .addSpawns(new SpawnerData(EntityType.SHEEP, 5, 1, 3))
                .build(featureToBiomes);

        modifyBiome(Biomes.SUNFLOWER_PLAINS)
                .changeColors(
                        12906239,
                        3902136,
                        2388383,
                        7715315,
                        9943640,
                        10334529
                )
                .addFeatures(FeatureWithStep.vegetationFeatures(
                        trees("sunflower_plains"),
                        flowers("sunflower_plains"),
                        flowerBed("yellow")
                ))
                .removeFeatures(TREES_PLAINS, FLOWER_PLAINS)
                .changeSpawns(
                        new SpawnerData(EntityType.SHEEP, 16, 4, 8),
                        new SpawnerData(EntityType.PIG, 4, 2, 4),
                        new SpawnerData(EntityType.COW, 14, 4, 6)
                )
                .addSpawns(new SpawnerData(EntityType.RABBIT, 8, 3, 6))
                .removeSpawns(EntityType.CHICKEN)
                .build(featureToBiomes);

        modifyBiome(Biomes.SWAMP)
                .changeColors(
                        10996903,
                        5536348,
                        2897190,
                        7715315,
                        6975545
                )
                .changeSpawns(
                        new SpawnerData(EntityType.SHEEP, 6, 4, 4),
                        new SpawnerData(EntityType.PIG, 16, 4, 4),
                        new SpawnerData(EntityType.CHICKEN, 6, 4, 4)
                )
                .addFeatures(FeatureWithStep.vegetationFeatures(
                        PATCH_PUMPKIN
                ))
                .removeFeatures(PATCH_PUMPKIN)
                .build(featureToBiomes);

        // TODO: Change temperature from 0.25 to 0.35
        modifyBiome(Biomes.TAIGA)
                .changeColors(
                        12639487,
                        3899064,
                        2386591,
                        8431871,
                        8035940,
                        8292172
                )
                .changeMusic(new Music(MUSIC_BIOME_OLD_GROWTH_TAIGA, 12000, 24000, false))
                .addFeatures(FeatureWithStep.vegetationFeatures(
                        flowers("taiga"),
                        flowerBed("taiga"),
                        patch("rose_bush")
                ))
                .removeFeatures(FLOWER_DEFAULT)
                .changeSpawns(
                        new SpawnerData(EntityType.SHEEP, 6, 1, 4),
                        new SpawnerData(EntityType.PIG, 14, 4, 6),
                        new SpawnerData(EntityType.CHICKEN, 10, 4, 8),
                        new SpawnerData(EntityType.RABBIT, 12, 2, 3)
                )
                .removeSpawns(EntityType.COW)
                .build(featureToBiomes);

        modifyBiome(Biomes.WARM_OCEAN)
                .changeColors(
                        12711423,
                        4106959,
                        3048361,
                        6800872,
                        8174674,
                        7516981
                )
                .addFeatures(FeatureWithStep.vegetationFeatures(
                        FLOWER_WARM,
                        flowerBed("jungle")
                ))
                .removeFeatures(FLOWER_DEFAULT)
                .build(featureToBiomes);

        modifyBiome(Biomes.WINDSWEPT_FOREST)
                .changeColors(
                        12639487,
                        3902136,
                        2388383,
                        8431871,
                        9088126,
                        8032098
                )
                .changeMusic(new Music(MUSIC_BIOME_MEADOW, 12000, 24000, false))
                .addFeatures(FeatureWithStep.vegetationFeatures(
                        flowers("windswept"),
                        flowerBed("white_and_violet")
                ))
                .removeFeatures(FLOWER_DEFAULT)
                .changeSpawns(
                        new SpawnerData(EntityType.SHEEP, 6, 4, 4),
                        new SpawnerData(EntityType.PIG, 6, 4, 4),
                        new SpawnerData(EntityType.CHICKEN, 6, 4, 4),
                        new SpawnerData(EntityType.COW, 6, 4, 4),
                        new SpawnerData(EntityType.LLAMA, 8, 4, 6)
                )
                .addSpawns(new SpawnerData(EntityType.GOAT, 3, 2, 4))
                .build(featureToBiomes);

        modifyBiome(Biomes.WINDSWEPT_GRAVELLY_HILLS)
                .changeColors(
                        12639487,
                        3902136,
                        2388383,
                        8431871,
                        9546631,
                        8032098
                )
                .changeMusic(new Music(MUSIC_BIOME_MEADOW, 12000, 24000, false))
                .addFeatures(FeatureWithStep.vegetationFeatures(
                        flowers("windswept"),
                        flowerBed("white_and_violet")
                ))
                .removeFeatures(FLOWER_DEFAULT)
                .changeSpawns(
                        new SpawnerData(EntityType.SHEEP, 7, 4, 4),
                        new SpawnerData(EntityType.PIG, 3, 4, 4),
                        new SpawnerData(EntityType.CHICKEN, 4, 4, 4),
                        new SpawnerData(EntityType.COW, 6, 4, 4),
                        new SpawnerData(EntityType.LLAMA, 7, 4, 6)
                )
                .addSpawns(new SpawnerData(EntityType.GOAT, 6, 2, 4))
                .build(featureToBiomes);

        modifyBiome(Biomes.WINDSWEPT_HILLS)
                .changeColors(
                        12639487,
                        3902136,
                        2388383,
                        8431871,
                        9088126,
                        8032098
                )
                .changeMusic(new Music(MUSIC_BIOME_MEADOW, 12000, 24000, false))
                .addFeatures(FeatureWithStep.vegetationFeatures(
                        flowers("windswept"),
                        flowerBed("white_and_violet")
                ))
                .removeFeatures(FLOWER_DEFAULT)
                .changeSpawns(
                        new SpawnerData(EntityType.SHEEP, 8, 4, 4),
                        new SpawnerData(EntityType.PIG, 4, 4, 4),
                        new SpawnerData(EntityType.CHICKEN, 5, 4, 4),
                        new SpawnerData(EntityType.COW, 7, 4, 4),
                        new SpawnerData(EntityType.LLAMA, 8, 4, 6)
                )
                .addSpawns(new SpawnerData(EntityType.GOAT, 4, 2, 4))
                .build(featureToBiomes);

        modifyBiome(Biomes.WINDSWEPT_SAVANNA)
                .changeColors(
                        12711423,
                        3906239,
                        2979747,
                        6800872,
                        12164442,
                        8558386
                )
                .addFeatures(FeatureWithStep.vegetationFeatures(
                        PATCH_TALL_GRASS,
                        flowers("savanna"),
                        flowerBed("savanna"),
                        PATCH_GRASS_SAVANNA,
                        patch("barrel_cactus_normal"),
                        patch("succulent_normal")
                ))
                .removeFeatures(TREES_WINDSWEPT_SAVANNA, FLOWER_DEFAULT, PATCH_GRASS_NORMAL)
                .changeSpawns(
                        new SpawnerData(EntityType.PIG, 8, 4, 4),
                        new SpawnerData(EntityType.CHICKEN, 12, 4, 4),
                        new SpawnerData(EntityType.COW, 5, 4, 4)
                )
                .addSpawns(
                        new SpawnerData(EntityType.GOAT, 6, 4, 4),
                        new SpawnerData(EntityType.LLAMA, 8, 4, 4)
                )
                .removeSpawns(EntityType.SHEEP, EntityType.ARMADILLO)
                .build(featureToBiomes);

        modifyBiome(Biomes.WOODED_BADLANDS)
                .changeColors(
                        15322281,
                        4106959,
                        3048361,
                        11128544,
                        10387789,
                        9470285
                )
                .addSpawns(
                        new SpawnerData(EntityType.RABBIT, 15, 2, 8),
                        new SpawnerData(EntityType.HUSK, 100, 4, 4),
                        new SpawnerData(EntityType.PIG, 5, 2, 8),
                        new SpawnerData(EntityType.CHICKEN, 8, 2, 8)
                )
                .removeSpawns(EntityType.ZOMBIE)
                .build(featureToBiomes);

        /* Tag-based feature additions */
        // Biome category-based broad brushes
        addFeaturesToTag(NMLTags.HAS_CACTUS, vegetalDecoration, patch("barrel_cactus_desert"), patch("succulent_desert"), flowers("all_tulips"));
        addFeaturesToTag(NMLTags.FeatureAddition.HAS_OVERWORLD_FOLIAGE, vegetalDecoration, patch("grass_sprouts_normal"), patch("roots"), patch("cattail"), patch("reeds"), patch("waterlily_common"));
        addFeaturesToTag(NMLTags.FeatureAddition.HAS_SWAMP_FOLIAGE, vegetalDecoration, patch("duckweed"), patch("pickleweed"), patch("reeds_swamp"), patch("cattail_swamp"));
        addFeaturesToTag(NMLTags.IS_SHORELINE, vegetalDecoration, feature("seashells"));

        // Biome category-based terrain modification
        addFeaturesToTag(NMLTags.IS_CRAGLAND, localModifications, feature("crag_rock_craglands"));
        addFeaturesToTag(NMLTags.IS_OCEANIC_CRAGLAND, localModifications, feature("crag_rock_ocean"));

        // Specific foliage patches
        addFeaturesToTag(NMLTags.FeatureAddition.HAS_BEACHGRASS, vegetalDecoration, patch("beachgrass"));
        addFeaturesToTag(NMLTags.FeatureAddition.HAS_CAVE_WEEDS, vegetalDecoration, feature("underground/patch_cave_weeds"));
        addFeaturesToTag(NMLTags.FeatureAddition.HAS_CLOVERS, vegetalDecoration, feature("clover_patch"));
        addFeaturesToTag(NMLTags.FeatureAddition.HAS_DRIED_GRASS, vegetalDecoration, patch("dried_grass"));
        addFeaturesToTag(NMLTags.FeatureAddition.HAS_FERN_PATCH, vegetalDecoration, patch("fern_forest"));
        addFeaturesToTag(NMLTags.FeatureAddition.HAS_FIDDLEHEAD, vegetalDecoration, patch("fiddlehead"));
        addFeaturesToTag(NMLTags.FeatureAddition.HAS_FIELD_MUSHROOM, vegetalDecoration, patch("field_mushroom_normal"));
        addFeaturesToTag(NMLTags.FeatureAddition.HAS_FROSTED_GRASS_FOREST, vegetalDecoration, patch("frosted_grass_forest"));
        addFeaturesToTag(NMLTags.FeatureAddition.HAS_FROSTED_GRASS_PLAINS, vegetalDecoration, patch("frosted_grass_plains"));
        addFeaturesToTag(NMLTags.FeatureAddition.HAS_ICICLES, vegetalDecoration, patch("icicles"));
        addFeaturesToTag(NMLTags.FeatureAddition.HAS_OAT_GRASS, vegetalDecoration, feature("oat_grass_patch"));
        addFeaturesToTag(NMLTags.FeatureAddition.HAS_RAFFLESIA, vegetalDecoration, patch("rafflesia"));
        addFeaturesToTag(NMLTags.FeatureAddition.HAS_GROUND_IVY, vegetalDecoration, feature("ground_ivy_patch"));

        // Fallen trees
        addFeaturesToTag(NMLTags.FeatureAddition.HAS_FALLEN_TREES_DRY, vegetalDecoration, feature("fallen_tree_dry"));
        addFeaturesToTag(NMLTags.FeatureAddition.HAS_FALLEN_TREES_FOREST, vegetalDecoration, feature("fallen_tree_forest"));
        addFeaturesToTag(NMLTags.FeatureAddition.HAS_FALLEN_TREES_JUNGLE, vegetalDecoration, feature("fallen_tree_jungle"));
        addFeaturesToTag(NMLTags.FeatureAddition.HAS_FALLEN_TREES_SNOWY, vegetalDecoration, feature("fallen_tree_snowy"));
        addFeaturesToTag(NMLTags.FeatureAddition.HAS_FALLEN_TREES_SPARSE, vegetalDecoration, feature("fallen_tree_sparse"));

        // Rocks & terrain
        addFeaturesToTag(NMLTags.FeatureAddition.HAS_PEBBLES, localModifications, feature("pebbles"));
        addFeaturesToTag(NMLTags.FeatureAddition.HAS_FOREST_ROCK, localModifications, FOREST_ROCK);
        addFeaturesToTag(NMLTags.FeatureAddition.HAS_STONE_BOULDER, localModifications, feature("stone_boulder"));
        addFeaturesToTag(NMLTags.FeatureAddition.HAS_POND_COMMON, localModifications, feature("pond_common"));
        addFeaturesToTag(NMLTags.FeatureAddition.HAS_POND_COMMON_SNOWY, localModifications, feature("pond_common_snowy"));
        addFeaturesToTag(NMLTags.FeatureAddition.HAS_POND_SWAMP, localModifications, feature("pond_swamp"));
        addFeaturesToTag(NMLTags.FeatureAddition.HAS_QUARTZITE_GEODE, localModifications, feature("underground/quartzite_geode"));
        addFeaturesToTag(NMLTags.FeatureAddition.HAS_NETHER_QUARTZITE_GEODE, localModifications, feature("underground/nether_quartzite_geode"));
        addFeaturesToTag(NMLTags.FeatureAddition.HAS_MUD_PATCH, GenerationStep.Decoration.FLUID_SPRINGS, feature("mud_patch"));
        addFeaturesToTag(NMLTags.HAS_GRAVEL_SHORE, localModifications, feature("gravel_decorator_pebble"), feature("gravel_decorator_boulder"), feature("gravel_decorator_pond"));

        // Extra mushroom stuff
        addFeaturesToTag(NMLTags.FeatureAddition.HAS_FIELD_MUSHROOM_CIRCLE, vegetalDecoration, feature("field_mushroom_circle"));
        addFeaturesToTag(NMLTags.FeatureAddition.HAS_SHELF_MUSHROOM_FOREST, vegetalDecoration, feature("shelf_mushroom_forest"));
        addFeaturesToTag(NMLTags.FeatureAddition.HAS_SHELF_MUSHROOM_OLD_GROWTH, vegetalDecoration, feature("shelf_mushroom_old_growth"));
        addFeaturesToTag(NMLTags.FeatureAddition.HAS_SHELF_MUSHROOM_TAIGA, vegetalDecoration, feature("shelf_mushroom_taiga"));


        addFeaturesToTag(NMLTags.FeatureAddition.REGULAR_TORTOISE_BURROWS, GenerationStep.Decoration.TOP_LAYER_MODIFICATION, feature("underground/tortoise_burrows/tortoise_burrow"));
        /* Tag-based feature removals */

        processFeatures();
    }

    private BiomeModifierBuilder modifyBiome(ResourceKey<Biome> biome) {
        return new BiomeModifierBuilder(bootstrap, biome);
    }

    @SafeVarargs
    private void addFeaturesToTag(TagKey<Biome> biomeTag, GenerationStep.Decoration step, ResourceKey<PlacedFeature>... features) {
        List<Holder<PlacedFeature>> featureHolders = new ArrayList<>();
        List.of(features).forEach(featureKey ->
                featureHolders.add(bootstrap.lookup(Registries.PLACED_FEATURE).getOrThrow(featureKey)));
        String name = biomeTag.location().getPath() + "_tag_features";

        bootstrap.register(
                ResourceKey.create(
                        NeoForgeRegistries.Keys.BIOME_MODIFIERS,
                        NoMansLand.location("add_" + name)
                ),
                new AddFeaturesBiomeModifier(
                        bootstrap.lookup(Registries.BIOME).getOrThrow(biomeTag),
                        HolderSet.direct(featureHolders),
                        step
                )
        );
    }

    private void processFeatures() {
        featureToBiomes.forEach((feature, biomeSet) -> {
            HolderSet<Biome> mergedBiomes = HolderSet.direct(biomeSet);
            addFeatureToSet(feature, mergedBiomes);
        });
    }

    private void addFeatureToSet(FeatureWithStep feature, HolderSet<Biome> biomes) {
        Holder<PlacedFeature> placedFeature = bootstrap.lookup(Registries.PLACED_FEATURE).getOrThrow(feature.key());
        bootstrap.register(
                ResourceKey.create(
                        NeoForgeRegistries.Keys.BIOME_MODIFIERS,
                        NoMansLand.location("add_" + placedFeature.unwrapKey().orElseThrow().location().getPath())
                ),
                new AddFeaturesBiomeModifier(
                        biomes,
                        HolderSet.direct(placedFeature),
                        feature.step()
                )
        );
    }
}
