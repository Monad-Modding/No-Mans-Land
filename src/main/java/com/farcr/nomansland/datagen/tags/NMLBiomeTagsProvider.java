package com.farcr.nomansland.datagen.tags;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.registry.NMLTags;
import com.farcr.nomansland.common.registry.worldgen.NMLBiomes;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class NMLBiomeTagsProvider extends BiomeTagsProvider {
    public NMLBiomeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, provider, NoMansLand.MODID, existingFileHelper);
    }



    @SuppressWarnings("unchecked")
    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(BiomeTags.IS_OVERWORLD).add(
                NMLBiomes.AUTUMNAL_FOREST,
                NMLBiomes.BAYOU,
                NMLBiomes.BOG,
                NMLBiomes.DARK_SWAMP,
                NMLBiomes.DARK_TAIGA,
                NMLBiomes.BOREAL_FOREST,
                NMLBiomes.MAPLE_FOREST,
                NMLBiomes.MAPLE_GROVE,
                NMLBiomes.FROZEN_WOODS,
                NMLBiomes.PRAIRIE,
                NMLBiomes.LAVENDER_FIELD,
                NMLBiomes.LUSH_RIVER,
                NMLBiomes.BLACKWATER_RIVER,
                NMLBiomes.DESERT_RIVER,
                NMLBiomes.MUD_BEACH,
                NMLBiomes.FROZEN_SHORE,
                NMLBiomes.TROPICAL_BEACH,

                NMLBiomes.ALCHEMIST_RUINS
        );

        tag(NMLTags.IS_CRAGLAND).add(
                Biomes.SPARSE_JUNGLE,
                Biomes.WINDSWEPT_GRAVELLY_HILLS
        );
        tag(NMLTags.IS_OCEANIC_CRAGLAND).add(
                Biomes.COLD_OCEAN,
                Biomes.DEEP_COLD_OCEAN,
                Biomes.OCEAN,
                Biomes.DEEP_OCEAN,
                Biomes.STONY_SHORE
        );
        tag(Tags.Biomes.IS_CAVE).add(
                NMLBiomes.ALCHEMIST_RUINS
        );

        tag(BiomeTags.IS_OVERWORLD).addTags(NMLTags.OLD_GROWTH_FOREST, NMLTags.CAVES);
        addToTags(NMLTags.CAVES, Tags.Biomes.IS_CAVE);
        addToTags(NMLTags.CAVES, BiomeTags.HAS_MINESHAFT, BiomeTags.HAS_TRIAL_CHAMBERS, BiomeTags.HAS_RUINED_PORTAL_STANDARD);

        addToTags(NMLBiomes.AUTUMNAL_FOREST, BiomeTags.IS_FOREST, Tags.Biomes.IS_DECIDUOUS_TREE, Tags.Biomes.IS_TEMPERATE_OVERWORLD);
        addToTags(NMLBiomes.BAYOU, BiomeTags.HAS_SWAMP_HUT, BiomeTags.IS_JUNGLE, Tags.Biomes.IS_SWAMP, Tags.Biomes.IS_DENSE_VEGETATION_OVERWORLD, Tags.Biomes.IS_HOT_OVERWORLD, Tags.Biomes.IS_JUNGLE_TREE, Tags.Biomes.IS_WET_OVERWORLD);
        addToTags(NMLBiomes.BOG, BiomeTags.HAS_SWAMP_HUT, Tags.Biomes.IS_DECIDUOUS_TREE, Tags.Biomes.IS_SWAMP, Tags.Biomes.IS_CONIFEROUS_TREE, Tags.Biomes.IS_COLD_OVERWORLD, Tags.Biomes.IS_WET_OVERWORLD);
        addToTags(NMLBiomes.DARK_SWAMP, BiomeTags.HAS_SWAMP_HUT, BiomeTags.IS_FOREST, Tags.Biomes.IS_SWAMP, Tags.Biomes.IS_DENSE_VEGETATION_OVERWORLD, Tags.Biomes.IS_SPOOKY, Tags.Biomes.IS_TEMPERATE_OVERWORLD, Tags.Biomes.IS_DECIDUOUS_TREE, Tags.Biomes.IS_WET_OVERWORLD, BiomeTags.HAS_WOODLAND_MANSION);
        addToTags(NMLBiomes.DARK_TAIGA, BiomeTags.IS_TAIGA, Tags.Biomes.IS_CONIFEROUS_TREE, Tags.Biomes.IS_COLD_OVERWORLD, Tags.Biomes.IS_SPOOKY, BiomeTags.HAS_VILLAGE_TAIGA);
        addToTags(NMLBiomes.BOREAL_FOREST, BiomeTags.IS_FOREST, Tags.Biomes.IS_CONIFEROUS_TREE, Tags.Biomes.IS_DECIDUOUS_TREE, Tags.Biomes.IS_TEMPERATE_OVERWORLD, BiomeTags.HAS_VILLAGE_TAIGA);
        addToTags(NMLBiomes.MAPLE_FOREST, BiomeTags.IS_FOREST, Tags.Biomes.IS_DECIDUOUS_TREE, Tags.Biomes.IS_TEMPERATE_OVERWORLD);
        addToTags(NMLBiomes.MAPLE_GROVE, BiomeTags.IS_FOREST, Tags.Biomes.IS_DECIDUOUS_TREE, Tags.Biomes.IS_COLD_OVERWORLD, Tags.Biomes.IS_SNOWY, Tags.Biomes.IS_PLATEAU);
        addToTags(NMLTags.OLD_GROWTH_FOREST, BiomeTags.IS_FOREST, Tags.Biomes.IS_DECIDUOUS_TREE, Tags.Biomes.IS_DENSE_VEGETATION_OVERWORLD, Tags.Biomes.IS_TEMPERATE_OVERWORLD, Tags.Biomes.IS_OLD_GROWTH, Tags.Biomes.IS_RARE);
        addToTags(NMLBiomes.FROZEN_WOODS, Tags.Biomes.IS_RARE, Tags.Biomes.IS_CONIFEROUS_TREE, Tags.Biomes.IS_COLD_OVERWORLD, Tags.Biomes.IS_SNOWY, BiomeTags.IS_TAIGA, Tags.Biomes.IS_DEAD, BiomeTags.HAS_IGLOO);
        addToTags(NMLBiomes.PRAIRIE, Tags.Biomes.IS_PLAINS, Tags.Biomes.IS_HOT_OVERWORLD, Tags.Biomes.IS_DRY_OVERWORLD, Tags.Biomes.IS_SPARSE_VEGETATION_OVERWORLD, BiomeTags.HAS_VILLAGE_PLAINS);
        addToTags(NMLBiomes.LAVENDER_FIELD, Tags.Biomes.IS_PLAINS, Tags.Biomes.IS_DECIDUOUS_TREE, Tags.Biomes.IS_FLORAL, BiomeTags.IS_HILL, BiomeTags.HAS_VILLAGE_PLAINS);

        addToTags(NMLBiomes.LUSH_RIVER, BiomeTags.IS_RIVER, BiomeTags.IS_JUNGLE, Tags.Biomes.IS_HOT_OVERWORLD, Tags.Biomes.IS_DENSE_VEGETATION_OVERWORLD, Tags.Biomes.IS_JUNGLE_TREE);
        addToTags(NMLBiomes.BLACKWATER_RIVER, BiomeTags.IS_RIVER, Tags.Biomes.IS_TEMPERATE_OVERWORLD, Tags.Biomes.IS_SWAMP, Tags.Biomes.IS_DENSE_VEGETATION_OVERWORLD);
        addToTags(NMLBiomes.DESERT_RIVER, BiomeTags.IS_RIVER, Tags.Biomes.IS_DESERT, Tags.Biomes.IS_HOT_OVERWORLD, Tags.Biomes.IS_SANDY);

        addToTags(NMLBiomes.TROPICAL_BEACH, BiomeTags.IS_BEACH, Tags.Biomes.IS_HOT_OVERWORLD, Tags.Biomes.IS_LUSH, Tags.Biomes.IS_JUNGLE_TREE);
        addToTags(NMLBiomes.FROZEN_SHORE, Tags.Biomes.IS_STONY_SHORES, Tags.Biomes.IS_COLD_OVERWORLD, Tags.Biomes.IS_SNOWY, Tags.Biomes.IS_ICY, Tags.Biomes.IS_AQUATIC_ICY);
        addToTags(NMLBiomes.MUD_BEACH, Tags.Biomes.IS_AQUATIC, Tags.Biomes.IS_RARE, Tags.Biomes.IS_WET_OVERWORLD);


        //Misc Tag Tweaks
        addToTags(Biomes.FROZEN_RIVER, Tags.Biomes.IS_SNOWY);

        List<ResourceKey<Biome>> surfaceBiomes = List.of(
                NMLBiomes.AUTUMNAL_FOREST, NMLBiomes.BAYOU, NMLBiomes.BOG, NMLBiomes.DARK_SWAMP,
                NMLBiomes.DARK_TAIGA, NMLBiomes.BOREAL_FOREST, NMLBiomes.MAPLE_FOREST, NMLBiomes.MAPLE_GROVE,
                NMLBiomes.FROZEN_WOODS, NMLBiomes.PRAIRIE, NMLBiomes.LAVENDER_FIELD,
                NMLBiomes.LUSH_RIVER, NMLBiomes.BLACKWATER_RIVER, NMLBiomes.DESERT_RIVER,
                NMLBiomes.MUD_BEACH, NMLBiomes.FROZEN_SHORE, NMLBiomes.TROPICAL_BEACH
        );
        surfaceBiomes.forEach(biome -> addToTags(biome, BiomeTags.HAS_TRIAL_CHAMBERS));
        addToTags(NMLTags.OLD_GROWTH_FOREST, BiomeTags.HAS_TRIAL_CHAMBERS);

        List.of(NMLBiomes.BOG, NMLBiomes.PRAIRIE, NMLBiomes.FROZEN_SHORE, NMLBiomes.MUD_BEACH)
                .forEach(biome -> addToTags(biome, BiomeTags.HAS_MINESHAFT));
        List.of(NMLBiomes.PRAIRIE, NMLBiomes.FROZEN_SHORE)
                .forEach(biome -> addToTags(biome, BiomeTags.HAS_RUINED_PORTAL_STANDARD));
        List.of(NMLBiomes.BOG, NMLBiomes.MUD_BEACH)
                .forEach(biome -> addToTags(biome, BiomeTags.HAS_RUINED_PORTAL_SWAMP));
    }

    @SafeVarargs
    protected final void addToTags(ResourceKey<Biome> biome, TagKey<Biome>... biomeTags) {
        List.of(biomeTags).forEach(biomeTag -> tag(biomeTag).add(biome));
    }

    @SafeVarargs
    protected final void addToTags(TagKey<Biome> biome, TagKey<Biome>... biomeTags) {
        List.of(biomeTags).forEach(biomeTag -> tag(biomeTag).addTag(biome));
    }
}
