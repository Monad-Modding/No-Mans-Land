package com.farcr.nomansland.common.registry.worldgen;

import com.farcr.nomansland.NoMansLand;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

import java.util.ArrayList;
import java.util.List;

public class NMLBiomes {

    public static List<ResourceKey<Biome>> BIOMES = new ArrayList<>();

    //Surface
    public static final ResourceKey<Biome> AUTUMNAL_FOREST = createKey("autumnal_forest");

    public static final ResourceKey<Biome> MAPLE_FOREST = createKey("maple_forest");
    public static final ResourceKey<Biome> MAPLE_GROVE = createKey("maple_grove");

    public static final ResourceKey<Biome> FROZEN_WOODS = createKey("frozen_woods");

    public static final ResourceKey<Biome> OLD_GROWTH_FOREST = createKey("old_growth_forest");
    public static final ResourceKey<Biome> OLD_GROWTH_FOREST_CLEARING = createKey("old_growth_forest_clearing");
    public static final ResourceKey<Biome> OLD_GROWTH_FOREST_EDGE = createKey("old_growth_forest_edge");

    public static final ResourceKey<Biome> BOREAL_FOREST = createKey("boreal_forest");
    public static final ResourceKey<Biome> DARK_TAIGA = createKey("dark_taiga");

    public static final ResourceKey<Biome> DARK_SWAMP = createKey("dark_swamp");
    public static final ResourceKey<Biome> BAYOU = createKey("bayou");
    public static final ResourceKey<Biome> BOG = createKey("bog");

    public static final ResourceKey<Biome> PRAIRIE = createKey("prairie");

    public static final ResourceKey<Biome> LAVENDER_FIELD = createKey("lavender_field");

    public static final ResourceKey<Biome> LUSH_RIVER = createKey("lush_river");
    public static final ResourceKey<Biome> BLACKWATER_RIVER = createKey("blackwater_river");
    public static final ResourceKey<Biome> DESERT_RIVER = createKey("desert_river");

    public static final ResourceKey<Biome> MUD_BEACH = createKey("mud_beach");
    public static final ResourceKey<Biome> FROZEN_SHORE = createKey("frozen_shore");
    public static final ResourceKey<Biome> TROPICAL_BEACH = createKey("tropical_beach");

    // Dream
    public static final ResourceKey<Biome> DREAM = createKey("dream");

    //Underground
    public static final ResourceKey<Biome> CAVES = createKey("caves");
    public static final ResourceKey<Biome> CAVE_DEPTHS = createKey("cave_depths");
    public static final ResourceKey<Biome> ALCHEMIST_RUINS = createKey("alchemist_ruins");

    public static Holder<Biome> CAVES_HOLDER;
    public static Holder<Biome> CAVE_DEPTHS_HOLDER;
    public static Holder<Biome> ALCHEMIST_RUINS_HOLDER;

    public static ResourceKey<Biome> createKey(String name) {
        ResourceKey<Biome> biome = ResourceKey.create(Registries.BIOME, NoMansLand.location(name));
        BIOMES.add(biome);
        return biome;
    }

    public static String langKey(ResourceKey<Biome> biome) {
        return "biome." + biome.location().toString().replaceAll(":", ".");
    }

    public static String langName(ResourceKey<Biome> biome) {
        String processed = biome.location().getPath().replace("_", " ");

        String[] words = processed.split(" ");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
        }

        return result.toString().trim();
    }
}
