package com.farcr.nomansland.datagen.worldgen;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.registry.NMLConditions;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.teamabnormals.blueprint.common.world.modification.ModdedBiomeSlice;
import com.teamabnormals.blueprint.core.api.conditions.ConfigValueCondition;
import com.teamabnormals.blueprint.core.registry.BlueprintDataPackRegistries;
import com.teamabnormals.blueprint.core.util.BiomeUtil.MultiNoiseModdedBiomeProvider;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.dimension.LevelStem;
import net.neoforged.neoforge.common.conditions.AndCondition;
import net.neoforged.neoforge.common.conditions.ICondition;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import static net.minecraft.world.level.biome.Biomes.*;
import static com.farcr.nomansland.common.registry.worldgen.NMLBiomes.*;
import static com.farcr.nomansland.common.world.generation.NMLBiomeProviders.*;

public class NMLBiomeSlices {
    public static final ResourceKey<ModdedBiomeSlice> MAPLE_SLICE = createKey("maple");
    public static final ResourceKey<ModdedBiomeSlice> FROZEN_WOODS_SLICE = createKey("frozen_woods");
    public static final ResourceKey<ModdedBiomeSlice> AUTUMNAL_FOREST_SLICE = createKey("autumnal_forest");
    public static final ResourceKey<ModdedBiomeSlice> AUTUMNAL_FOREST_WITH_BOREAL_FOREST_SLICE = createKey("autumnal_forest_with_boreal_forest");
    public static final ResourceKey<ModdedBiomeSlice> BOREAL_FOREST_SLICE = createKey("boreal_forest");
    public static final ResourceKey<ModdedBiomeSlice> PRAIRIE_SLICE = createKey("prairie");
    public static final ResourceKey<ModdedBiomeSlice> LAVENDER_SLICE = createKey("lavender");
    public static final ResourceKey<ModdedBiomeSlice> DARK_TAIGA_SLICE = createKey("dark_taiga");
    public static final ResourceKey<ModdedBiomeSlice> DARK_SWAMP_SLICE = createKey("dark_swamp");
    public static final ResourceKey<ModdedBiomeSlice> BAYOU_SLICE = createKey("bayou");
    public static final ResourceKey<ModdedBiomeSlice> BOG_SLICE = createKey("bog");
    public static final ResourceKey<ModdedBiomeSlice> OLD_GROWTH_FOREST_SLICE = createKey("old_growth_forest");

    public static final ResourceKey<Biome> FOREST_SNOWY_EDGE = createBiomeKey("forest_snowy_edge");
    public static final ResourceKey<Biome> DRY_WARM_TAIGA = createBiomeKey("dry_warm_taiga");

    public static void bootstrap(BootstrapContext<ModdedBiomeSlice> context) {
        var overworldDefaultParameterList = context.lookup(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST).getOrThrow(MultiNoiseBiomeSourceParameterLists.OVERWORLD);

        // TODO (Suggestion): Use OverlayModdedBiomeProvider in the future
        // NML Biomes are not defined in bootstrap (yet) due to them not being data-generated
        // so they're not easily usable for overlays.
        // Some more complex slices would be best not as overlays, such as Old Growth Forest
        List<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> entries = new ArrayList<>();
        new OverworldBiomeBuilder().addBiomes(entry -> {
            var biome = entry.getSecond();
            var point = entry.getFirst();
            var temperature = point.temperature();
            var humidity = point.humidity();
            if (biome == FOREST) {
                if (temperature.min() == COLD_START && temperature.max() == TEMPERATE_START) {
                    addRange(entries, point, ClimateAxis.TEMPERATURE, FOREST_SNOWY_EDGE, COLD_START, -4200L);
                    addRange(entries, point, ClimateAxis.TEMPERATURE, FOREST, -4200L, TEMPERATE_START);
                    return;
                }
            } else if (biome == TAIGA) {
                if (temperature.min() >= COLD_START && humidity.min() == BALMY_END && humidity.max() == WET_START) {
                    addRange(entries, point, ClimateAxis.HUMIDITY, DRY_WARM_TAIGA, BALMY_END, 1400L);
                    addRange(entries, point, ClimateAxis.HUMIDITY, TAIGA, 1400L, WET_START);
                    return;
                }
            }
            entries.add(entry);
        });

        context.register(MAPLE_SLICE, new ModdedBiomeSlice(48, MultiNoiseModdedBiomeProvider.builder().biomes(entries::forEach)
            .area(FOREST, MAPLE_FOREST)
            .area(GROVE, MAPLE_GROVE)
            .area(FOREST_SNOWY_EDGE, MAPLE_GROVE)
            .build(), LevelStem.OVERWORLD));

        context.register(FROZEN_WOODS_SLICE, new ModdedBiomeSlice(25, MultiNoiseModdedBiomeProvider.builder().biomes(overworldDefaultParameterList)
            .area(SNOWY_PLAINS, FROZEN_WOODS)
            .area(SNOWY_TAIGA, FROZEN_WOODS)
            .build(), LevelStem.OVERWORLD));

        context.register(AUTUMNAL_FOREST_SLICE, new ModdedBiomeSlice(42, MultiNoiseModdedBiomeProvider.builder().biomes(overworldDefaultParameterList)
            .area(FOREST, AUTUMNAL_FOREST)
            .build(), LevelStem.OVERWORLD));

        context.register(AUTUMNAL_FOREST_WITH_BOREAL_FOREST_SLICE, new ModdedBiomeSlice(42, MultiNoiseModdedBiomeProvider.builder().biomes(entries::forEach)
            .area(FOREST, AUTUMNAL_FOREST)
            .area(DRY_WARM_TAIGA, BOREAL_FOREST)
            .build(), LevelStem.OVERWORLD));

        context.register(BOREAL_FOREST_SLICE, new ModdedBiomeSlice(42, MultiNoiseModdedBiomeProvider.builder().biomes(overworldDefaultParameterList)
            .area(FOREST, BOREAL_FOREST)
            .area(TAIGA, BOREAL_FOREST)
            .build(), LevelStem.OVERWORLD));

        context.register(PRAIRIE_SLICE, new ModdedBiomeSlice(43, MultiNoiseModdedBiomeProvider.builder().biomes(overworldDefaultParameterList)
            .area(SAVANNA, PRAIRIE)
            .build(), LevelStem.OVERWORLD));

        context.register(LAVENDER_SLICE, new ModdedBiomeSlice(43, MultiNoiseModdedBiomeProvider.builder().biomes(overworldDefaultParameterList)
            .area(MEADOW, LAVENDER_FIELD)
            .build(), LevelStem.OVERWORLD));

        context.register(DARK_TAIGA_SLICE, new ModdedBiomeSlice(43, MultiNoiseModdedBiomeProvider.builder().biomes(overworldDefaultParameterList)
            .area(TAIGA, DARK_TAIGA)
            .build(), LevelStem.OVERWORLD));

        context.register(DARK_SWAMP_SLICE, new ModdedBiomeSlice(42, MultiNoiseModdedBiomeProvider.builder().biomes(overworldDefaultParameterList)
            .area(DARK_FOREST, DARK_SWAMP)
            .area(SWAMP, DARK_SWAMP)
            .build(), LevelStem.OVERWORLD));

        context.register(BAYOU_SLICE, new ModdedBiomeSlice(33, MultiNoiseModdedBiomeProvider.builder().biomes(overworldDefaultParameterList)
            .area(MANGROVE_SWAMP, BAYOU)
            .area(JUNGLE, BAYOU)
            .build(), LevelStem.OVERWORLD));

        context.register(BOG_SLICE, new ModdedBiomeSlice(21, MultiNoiseModdedBiomeProvider.builder().biomes(overworldDefaultParameterList)
            .area(SWAMP, BOG)
            .area(MANGROVE_SWAMP, BOG)
            .build(), LevelStem.OVERWORLD));

        // Old Growth Forest:
        // Subdivides entries for precise edge and "clearing" biomes
        long valleySideEnd = 1750L;
        long peaksNearEnd = 7166L;
        Predicate<ResourceKey<Biome>> isTarget = biome -> {
            return biome == DARK_FOREST || biome == OLD_GROWTH_BIRCH_FOREST || biome == FOREST;
        };
        entries.clear();
        new OverworldBiomeBuilder().addBiomes(entry -> {
            var point = entry.getFirst();
            var biome = entry.getSecond();
            var weirdness = point.weirdness();
            boolean lowerW = weirdness.min() < 0;
            if (matchesRange(weirdness, VALLEYS_END, LOW_END)) {
                addWeirdnessRange(entries, point, biome, valleySideEnd, LOW_END, lowerW);
                if (isTarget.test(biome)
                    && (point.erosion().max() > MODERATE_ACCRETION_START
                    || point.continentalness().min() < MID_INLAND_START)
                ) biome = OLD_GROWTH_FOREST_EDGE;
                addWeirdnessRange(entries, point, biome, VALLEYS_END, valleySideEnd, lowerW);
            } else if (matchesRange(weirdness, PEAKS_START, PEAKS_END)) {
                addWeirdnessRange(entries, point, biome, PEAKS_START, peaksNearEnd, lowerW);
                addWeirdnessRange(entries, point,
                    isTarget.test(biome) ? OLD_GROWTH_FOREST_CLEARING : biome,
                    peaksNearEnd, PEAKS_END, lowerW
                );
            } else if (isTarget.test(biome) && weirdness.min() == -10000L && weirdness.max() == -SMALL_MID_START) {
                // Targets often-circular depressions after peaks in half of weirdness.
                entries.add(Pair.of(point, OLD_GROWTH_FOREST_CLEARING));
            } else entries.add(entry);
        });
        replaceEdges(new ArrayList<>(entries), entries, isTarget, OLD_GROWTH_FOREST_EDGE, 0.04F);
        context.register(OLD_GROWTH_FOREST_SLICE, new ModdedBiomeSlice(35, MultiNoiseModdedBiomeProvider.builder().biomes(entries::forEach)
            .area(FOREST, OLD_GROWTH_FOREST)
            .area(DARK_FOREST, OLD_GROWTH_FOREST)
            .area(OLD_GROWTH_BIRCH_FOREST, OLD_GROWTH_FOREST)
            .area(OLD_GROWTH_FOREST_EDGE, OLD_GROWTH_FOREST_EDGE)
            .area(OLD_GROWTH_FOREST_CLEARING, OLD_GROWTH_FOREST_CLEARING)
            .build(), LevelStem.OVERWORLD));
    }

    public static void bootstrapConditions(BiConsumer<ResourceKey<?>, ICondition> conditions) {
        conditions.accept(MAPLE_SLICE, biomeCondition("maple_biomes"));
        conditions.accept(FROZEN_WOODS_SLICE, biomeCondition("frozen_woods"));
        conditions.accept(OLD_GROWTH_FOREST_SLICE, biomeCondition("old_growth_forest"));
        // If Boreal Forests are disabled, disable transition behavior with them
        conditions.accept(
            AUTUMNAL_FOREST_SLICE,
            biomeConditions(Pair.of("autumnal_forest", false), Pair.of("boreal_forest", true))
        );
        conditions.accept(
            AUTUMNAL_FOREST_WITH_BOREAL_FOREST_SLICE,
            biomeConditions(Pair.of("autumnal_forest", false), Pair.of("boreal_forest", false))
        );
        conditions.accept(BOREAL_FOREST_SLICE, biomeCondition("boreal_forest"));
        conditions.accept(PRAIRIE_SLICE, biomeCondition("prairie"));
        conditions.accept(LAVENDER_SLICE, biomeCondition("lavender_field"));
        conditions.accept(DARK_TAIGA_SLICE, biomeCondition("dark_taiga"));
        conditions.accept(DARK_SWAMP_SLICE, biomeCondition("dark_swamp"));
        conditions.accept(BAYOU_SLICE, biomeCondition("bayou"));
        conditions.accept(BOG_SLICE, biomeCondition("bog"));
    }

    @SafeVarargs
    private static ICondition biomeConditions(Pair<String, Boolean>... conditions) {
        ImmutableList.Builder<ICondition> builder = ImmutableList.builder();
        builder.add(new ConfigValueCondition(NMLConditions.CONFIG.get(), "biomes"));
        for (var condition : conditions) {
            builder.add(new ConfigValueCondition(NMLConditions.CONFIG.get(), condition.getFirst(), condition.getSecond()));
        }
        return new AndCondition(builder.build());
    }

    private static ICondition biomeCondition(String condition) {
        return biomeConditions(Pair.of(condition, false));
    }

    public static ResourceKey<ModdedBiomeSlice> createKey(String name) {
        return ResourceKey.create(BlueprintDataPackRegistries.MODDED_BIOME_SLICES, NoMansLand.location(name));
    }

    public static ResourceKey<Biome> createBiomeKey(String name) {
        return ResourceKey.create(Registries.BIOME, NoMansLand.location(name));
    }

    private static boolean matchesRange(Climate.Parameter parameter, long min, long max) {
        return parameter.min() == min && parameter.max() == max || parameter.min() == -max && parameter.max() == -min;
    }

    private static void addRange(List<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> entries, Climate.ParameterPoint point, ClimateAxis axis, ResourceKey<Biome> biome, long min, long max) {
        entries.add(Pair.of(withParameter(point, axis, new Climate.Parameter(min, max)), biome));
    }

    private static void addWeirdnessRange(List<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> entries, Climate.ParameterPoint point, ResourceKey<Biome> biome, long min, long max, boolean lower) {
        Climate.Parameter range = lower ? new Climate.Parameter(-max, -min) : new Climate.Parameter(min, max);
        entries.add(Pair.of(withParameter(point, ClimateAxis.WEIRDNESS, range), biome));
    }

    /**
     * Adds in edge biomes transitioning into adjacent entries of target biomes.
     * <p>For best results, ensure originalEntries is the list before any post-processing.</p>
     */
    private static void replaceEdges(List<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> originalEntries, List<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> newEntries, Predicate<ResourceKey<Biome>> isTarget, ResourceKey<Biome> edge, float size) {
        int originalCount = originalEntries.size();
        HashSet<Climate.ParameterPoint> edgePoints = new HashSet<>();
        long quantizedSize = Climate.quantizeCoord(size);
        for (var entry : originalEntries) {
            if (edge.equals(entry.getSecond()))
                edgePoints.add(entry.getFirst());
        }
        for (int targetIndex = 0; targetIndex < originalCount; targetIndex++) {
            var targetEntry = originalEntries.get(targetIndex);
            if (!isTarget.test(targetEntry.getSecond())) continue;
            Climate.ParameterPoint originalTargetPoint = targetEntry.getFirst();
            boolean[][] bordersBiome = new boolean[ClimateAxis.values().length][2];
            boolean[][] bordersEdge = new boolean[ClimateAxis.values().length][2];
            for (int candidateIndex = 0; candidateIndex < originalCount; candidateIndex++) {
                if (candidateIndex == targetIndex) continue;
                var candidateEntry = originalEntries.get(candidateIndex);
                Climate.ParameterPoint candidatePoint = candidateEntry.getFirst();
                for (ClimateAxis axis : ClimateAxis.values()) {
                    if (!sameExceptAxis(originalTargetPoint, candidatePoint, axis))
                        continue;
                    BorderSide side = adjacentSide(
                        getParameter(originalTargetPoint, axis),
                        getParameter(candidatePoint, axis)
                    );
                    if (side == null) continue;
                    ResourceKey<Biome> candidateBiome = candidateEntry.getSecond();
                    if (edge.equals(candidateBiome)) {
                        bordersEdge[axis.ordinal()][side.ordinal()] = true;
                    } else if (!isTarget.test(candidateBiome)) {
                        bordersBiome[axis.ordinal()][side.ordinal()] = true;
                    }
                }
            }
            List<Climate.ParameterPoint> additions = new ArrayList<>(10);
            Climate.ParameterPoint shrunkenTargetPoint = originalTargetPoint;
            for (ClimateAxis axis : ClimateAxis.values()) {
                boolean addLower = bordersBiome[axis.ordinal()][BorderSide.LOWER.ordinal()]
                    && !bordersEdge[axis.ordinal()][BorderSide.LOWER.ordinal()];
                boolean addUpper = bordersBiome[axis.ordinal()][BorderSide.UPPER.ordinal()]
                    && !bordersEdge[axis.ordinal()][BorderSide.UPPER.ordinal()];
                int sideCount = (addLower ? 1 : 0) + (addUpper ? 1 : 0);
                if (sideCount == 0) continue;
                Climate.Parameter range = getParameter(shrunkenTargetPoint, axis);
                long requiredWidth = quantizedSize * sideCount;
                long availableWidth = range.max() - range.min();
                if (availableWidth < requiredWidth) continue;
                long newMin = range.min();
                long newMax = range.max();
                if (addLower) {
                    long split = range.min() + quantizedSize;
                    additions.add(withParameter(originalTargetPoint, axis, new Climate.Parameter(range.min(), split)));
                    newMin = split;
                }
                if (addUpper) {
                    long split = range.max() - quantizedSize;
                    additions.add(withParameter(originalTargetPoint, axis, new Climate.Parameter(split, range.max())));
                    newMax = split;
                }
                shrunkenTargetPoint = withParameter(shrunkenTargetPoint, axis, new Climate.Parameter(newMin, newMax));
            }
            if (!shrunkenTargetPoint.equals(originalTargetPoint))
                newEntries.set(targetIndex, Pair.of(shrunkenTargetPoint, targetEntry.getSecond()));
            for (Climate.ParameterPoint edgePoint : additions) {
                if (edgePoints.add(edgePoint)) {
                    newEntries.add(Pair.of(edgePoint, edge));
                }
            }
        }
    }

    private static boolean sameExceptAxis(Climate.ParameterPoint first, Climate.ParameterPoint second, ClimateAxis excludedAxis) {
        if (first.offset() != second.offset() || !first.depth().equals(second.depth()))
            return false;
        for (ClimateAxis axis : ClimateAxis.values()) {
            if (axis == excludedAxis) continue;
            if (!getParameter(first, axis).equals(getParameter(second, axis)))
                return false;
        }
        return true;
    }

    private static BorderSide adjacentSide(Climate.Parameter target, Climate.Parameter candidate) {
        if (candidate.max() == target.min() && candidate.min() < target.min())
            return BorderSide.LOWER;
        if (target.max() == candidate.min() && candidate.max() > target.max())
            return BorderSide.UPPER;
        return null;
    }

    private static Climate.Parameter getParameter(Climate.ParameterPoint point, ClimateAxis axis) {
        return switch (axis) {
            case TEMPERATURE -> point.temperature();
            case HUMIDITY -> point.humidity();
            case CONTINENTALNESS -> point.continentalness();
            case EROSION -> point.erosion();
            case WEIRDNESS -> point.weirdness();
        };
    }

    private static Climate.ParameterPoint withParameter(Climate.ParameterPoint point, ClimateAxis axis, Climate.Parameter parameter) {
        return new Climate.ParameterPoint(
            axis == ClimateAxis.TEMPERATURE ? parameter : point.temperature(),
            axis == ClimateAxis.HUMIDITY ? parameter : point.humidity(),
            axis == ClimateAxis.CONTINENTALNESS ? parameter : point.continentalness(),
            axis == ClimateAxis.EROSION ? parameter : point.erosion(),
            point.depth(),
            axis == ClimateAxis.WEIRDNESS ? parameter : point.weirdness(),
            point.offset()
        );
    }

    private enum ClimateAxis {
        TEMPERATURE, HUMIDITY, CONTINENTALNESS, EROSION, WEIRDNESS
    }

    private enum BorderSide {
        LOWER, UPPER
    }
}
