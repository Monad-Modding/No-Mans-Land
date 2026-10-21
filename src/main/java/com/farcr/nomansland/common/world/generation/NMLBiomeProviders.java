package com.farcr.nomansland.common.world.generation;

import com.farcr.nomansland.NMLConfig;
import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.registry.worldgen.NMLBiomes;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.teamabnormals.blueprint.common.world.modification.ModdedBiomeSlicesManager;
import com.teamabnormals.blueprint.core.registry.BlueprintBiomes;
import com.teamabnormals.blueprint.core.util.BiomeUtil;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.neoforged.neoforge.common.Tags;

import java.util.Set;

public class NMLBiomeProviders {
    public static final ResourceLocation INITIAL_OVERWORLD_REPLACEMENTS = NoMansLand.location("initial_overworld_replacements");
    public static final ResourceLocation GLOBAL_OVERWORLD_OVERLAYS = NoMansLand.location("global_overworld_overlays");
    // Based on vanilla's climate separations
    // Continentalness
    public static final long COAST_START = -1900L;
    public static final long COAST_END = -1100L;
    public static final long MID_INLAND_START = 300L;
    // Weirdness
    public static final long VALLEYS_END = 500L;
    public static final long LOW_END = 2666L;
    public static final long MAIN_MID_END = 4000L;
    public static final long PEAKS_START = 5666L;
    public static final long PEAKS_END = 7666L;
    public static final long SMALL_MID_START = 9333L;
    // Erosion
    public static final long MODERATE_ACCRETION_START = -3750L;
    public static final long ACCRETION_START = -2225L;
    public static final long SLIGHTLY_ERODED_START = 500L;
    public static final long MODERATELY_ERODED_START = 4500L;
    public static final long DEEPLY_ERODED_START = 5500L;
    // Temperature
    public static final long COLD_START = -4500L;
    public static final long TEMPERATE_START = -1500L;
    public static final long TEMPERATE_END = 2000L;
    public static final long WARM_END = 5500L;
    // Humidity
    public static final long SEMI_ARID_START = -3500L;
    public static final long BALMY_START = -1000L;
    public static final long BALMY_END = 1000L;
    public static final long WET_START = 3000L;

    public static void register() {
        BiomeUtil.registerBiomeProvider(INITIAL_OVERWORLD_REPLACEMENTS, InitialOverworldReplacements.CODEC);
        BiomeUtil.registerBiomeProvider(GLOBAL_OVERWORLD_OVERLAYS, GlobalOverworldOverlays.CODEC);
        ModdedBiomeSlicesManager.PROVIDER_LOADING_EVENT.registerListener((server, key, levels, provider) -> {
            if (!NMLConfig.BIOMES.biomes.get() || !NMLConfig.BIOMES.cavesBiomes.get() || levels.size() != 1 || !levels.contains(LevelStem.OVERWORLD))
                return provider;
            var biomes = server.registryAccess().registryOrThrow(Registries.BIOME);
            return new GlobalOverworldOverlays(provider, biomes.getHolderOrThrow(NMLBiomes.CAVES), biomes.getHolderOrThrow(NMLBiomes.CAVE_DEPTHS));
        });
    }

    /* For replacing overworld biomes such that the replacements run before slices are shaped
       Therefore these are like "absolute" replacements:
       they get placed always where possible instead of sometimes having random slice shapes
       Blueprint doesn't have a biome provider type for this built-in because
       bypassing the slice distribution is discouraged and possibly incompatible with mods that do the same.
       To understand why, consider two mods that want to replace the same area:
       How should they play along? Which mod should get to place there?
       The practical answer would be sometimes mod A places and sometimes mod B does,
       but then we end up where we started: the slices system.
       */
    public record InitialOverworldReplacements(
        BiomeUtil.ModdedBiomeProvider base,
        Holder<Biome> transitionalPrairie,
        Holder<Biome> transitionalDarkSwamp,
        Holder<Biome> transitionalBayou,
        Holder<Biome> transitionalBog,
        Holder<Biome> frozenShore,
        Holder<Biome> tropicalBeach,
        Holder<Biome> mudBeach,
        Holder<Biome> blackwaterRiver,
        Holder<Biome> lushRiver,
        Holder<Biome> desertRiver
    ) implements BiomeUtil.ModdedBiomeProvider {
        public static final MapCodec<InitialOverworldReplacements> CODEC = RecordCodecBuilder.mapCodec(instance -> {
            return instance.group(
                BiomeUtil.ModdedBiomeProvider.CODEC.fieldOf("base").forGetter(provider -> provider.base),
                Biome.CODEC.fieldOf("transitional_prairie").forGetter(provider -> provider.transitionalPrairie),
                Biome.CODEC.fieldOf("transitional_dark_swamp").forGetter(provider -> provider.transitionalDarkSwamp),
                Biome.CODEC.fieldOf("transitional_bayou").forGetter(provider -> provider.transitionalBayou),
                Biome.CODEC.fieldOf("transitional_bog").forGetter(provider -> provider.transitionalBog),
                Biome.CODEC.fieldOf("frozen_shore").forGetter(provider -> provider.frozenShore),
                Biome.CODEC.fieldOf("tropical_beach").forGetter(provider -> provider.tropicalBeach),
                Biome.CODEC.fieldOf("mud_beach").forGetter(provider -> provider.mudBeach),
                Biome.CODEC.fieldOf("blackwater_river").forGetter(provider -> provider.blackwaterRiver),
                Biome.CODEC.fieldOf("lush_river").forGetter(provider -> provider.lushRiver),
                Biome.CODEC.fieldOf("desert_river").forGetter(provider -> provider.desertRiver)
            ).apply(instance, InitialOverworldReplacements::new);
        });

        @Override
        public void finalize(MinecraftServer server, long seed) {
            this.base.finalize(server, seed);
        }

        private static Holder<Biome> intoSwampTransition(Holder<Biome> transition, Holder<Biome> base, long weirdness, long continentalness, long erosion) {
            if (continentalness >= COAST_END &&
                (
                    (erosion >= 5100L && erosion <= DEEPLY_ERODED_START && weirdness <= MAIN_MID_END)
                        || (erosion >= 5400L && weirdness >= MAIN_MID_END && weirdness <= 4600L)
                )
            ) return transition;
            return base;
        }

        @Override
        public Holder<Biome> getNoiseBiome(int x, int y, int z, BiomeUtil.ScopedDensityFunctionContext context, BiomeSource original, Registry<Biome> registry) {
            return this.getNoiseBiome(this.base.getNoiseBiome(x, y, z, context, original, registry), context.getTargetPoint());
        }

        // TODO: Remove next MC version when Blueprint breaking changes are allowed
        @Deprecated
        @Override
        public Holder<Biome> getNoiseBiome(int x, int y, int z, Climate.Sampler sampler, BiomeSource original, Registry<Biome> registry) {
            return this.getNoiseBiome(this.base.getNoiseBiome(x, y, z, sampler, original, registry), sampler.sample(x, y, z));
        }

        private Holder<Biome> getNoiseBiome(Holder<Biome> baseBiome, Climate.TargetPoint point) {
            Holder<Biome> biome = this.getNoiseBiome(point, baseBiome);
            if (biome.is(BlueprintBiomes.ORIGINAL_SOURCE_MARKER)) return baseBiome;
            return biome;
        }

        private Holder<Biome> getNoiseBiome(Climate.TargetPoint point, Holder<Biome> baseBiome) {
            long temperature = point.temperature();
            long humidity = point.humidity();
            long continentalness = point.continentalness();
            long erosion = point.erosion();
            long weirdness = point.weirdness();
            long absWeirdness = Math.abs(weirdness);
            if (
                NMLConfig.BIOMES.blackwaterRiver.get()
                    && absWeirdness <= VALLEYS_END
                    && !baseBiome.is(Tags.Biomes.IS_COLD)
                    && (
                    (erosion <= 6300L
                        && baseBiome.is(Tags.Biomes.IS_SWAMP)
                        && !baseBiome.is(Tags.Biomes.IS_RIVER)
                    ) || (baseBiome.is(Tags.Biomes.IS_RIVER) && erosion >= 5350L)
                )
            ) return this.blackwaterRiver;
            if (baseBiome.is(Biomes.RIVER)) {
                if (
                    NMLConfig.BIOMES.lushRiver.get()
                        && temperature >= TEMPERATE_END && temperature <= WARM_END
                        && humidity >= BALMY_END
                ) return this.lushRiver;

                if (NMLConfig.BIOMES.desertRiver.get() && temperature >= WARM_END && erosion >= MODERATE_ACCRETION_START) {
                    if (erosion <= ACCRETION_START) {
                        if (continentalness >= COAST_END && continentalness <= MID_INLAND_START)
                            return this.desertRiver;
                    } else if (erosion <= SLIGHTLY_ERODED_START) {
                        if (continentalness <= MID_INLAND_START)
                            return this.desertRiver;
                    } else if (erosion <= MODERATELY_ERODED_START) {
                        return this.desertRiver;
                    } else if (erosion <= DEEPLY_ERODED_START) {
                        if (
                            continentalness >= MID_INLAND_START
                                || (continentalness >= COAST_START && !(weirdness > 0 && humidity <= WET_START))
                        ) return this.desertRiver;
                    } else if (continentalness <= COAST_END)
                        return this.desertRiver;
                }
                return baseBiome;
            } else if (baseBiome.is(Biomes.SAVANNA)) {
                if (NMLConfig.BIOMES.prairie.get()) {
                    if ((humidity >= SEMI_ARID_START && temperature >= TEMPERATE_END && temperature <= 3200L)
                        || (weirdness >= 0L && humidity >= -1800L && humidity <= BALMY_START)
                    ) return this.transitionalPrairie;
                }
                return baseBiome;
            } else if (baseBiome.is(Biomes.PLAINS)) {
                if (NMLConfig.BIOMES.prairie.get()) {
                    if (
                        (humidity <= BALMY_START && temperature >= 800L && temperature <= TEMPERATE_END)
                            || (weirdness >= 0L && humidity <= -670L && humidity >= BALMY_START && temperature >= TEMPERATE_END)
                    ) return this.transitionalPrairie;
                }
                if (NMLConfig.BIOMES.bog.get())
                    return intoSwampTransition(this.transitionalBog, baseBiome, absWeirdness, continentalness, erosion);
                return baseBiome;
            } else if (baseBiome.is(Biomes.SWAMP)) {
                if (NMLConfig.BIOMES.bog.get()) {
                    if (
                        // In "Plains" (Swamp-able) climate
                        humidity <= BALMY_START && (
                            (temperature >= COLD_START && temperature <= TEMPERATE_START) ||
                                (
                                    humidity >= SEMI_ARID_START && temperature >= TEMPERATE_START
                                        && temperature <= TEMPERATE_END
                                )
                        ) &&
                            continentalness >= COAST_END && absWeirdness >= VALLEYS_END
                            // In "Plains" transitionary terrain
                            && (
                            (
                                (erosion >= DEEPLY_ERODED_START && erosion <= 5900L)
                                    && !(absWeirdness > LOW_END && continentalness >= MID_INLAND_START)
                            ) || (
                                (absWeirdness >= LOW_END && absWeirdness <= MAIN_MID_END)
                                    || (absWeirdness >= SMALL_MID_START && absWeirdness <= 9650L)
                            )
                        )
                    ) return this.transitionalBog;
                }
                return baseBiome;
            }
            if (NMLConfig.BIOMES.darkSwamp.get() && baseBiome.is(Biomes.DARK_FOREST))
                return intoSwampTransition(this.transitionalDarkSwamp, baseBiome, absWeirdness, continentalness, erosion);
            if (NMLConfig.BIOMES.bayou.get() && baseBiome.is(Biomes.JUNGLE))
                return intoSwampTransition(this.transitionalBayou, baseBiome, absWeirdness, continentalness, erosion);
            if (
                NMLConfig.BIOMES.frozenShore.get()
                    && baseBiome.is(Biomes.STONY_SHORE)
                    && temperature <= COLD_START && humidity <= WET_START
            ) return this.frozenShore;
            if (
                NMLConfig.BIOMES.tropicalBeach.get()
                    && baseBiome.is(Biomes.BEACH) && humidity >= BALMY_END
                    && temperature >= TEMPERATE_END && temperature <= WARM_END
            ) return this.tropicalBeach;
            if (
                NMLConfig.BIOMES.mudBeach.get()
                    && baseBiome.is(Biomes.BEACH)
                    && temperature >= -9000L && temperature <= 20000L
                    && weirdness >= -20000L && weirdness <= -18600L
                    && erosion >= 11000L && erosion <= 20000L
            ) return this.mudBeach;
            return baseBiome;
        }

        @Override
        public Set<Holder<Biome>> getPossibleBiomes(Set<Holder<Biome>> originalPossibleBiomes, Registry<Biome> registry) {
            ImmutableSet.Builder<Holder<Biome>> builder = ImmutableSet.builder();
            this.addPossibleBiomes(builder);
            builder.addAll(this.base.getPossibleBiomes(originalPossibleBiomes, registry));
            return builder.build();
        }

        @Override
        public Set<Holder<Biome>> getAdditionalPossibleBiomes(Registry<Biome> registry) {
            ImmutableSet.Builder<Holder<Biome>> builder = ImmutableSet.builder();
            this.addPossibleBiomes(builder);
            builder.addAll(this.base.getAdditionalPossibleBiomes(registry));
            return builder.build();
        }

        private void addPossibleBiomes(ImmutableSet.Builder<Holder<Biome>> builder) {
            if (NMLConfig.BIOMES.prairie.get()) builder.add(this.transitionalPrairie);
            if (NMLConfig.BIOMES.darkSwamp.get()) builder.add(this.transitionalDarkSwamp);
            if (NMLConfig.BIOMES.bayou.get()) builder.add(this.transitionalBayou);
            if (NMLConfig.BIOMES.bog.get()) builder.add(this.transitionalBog);
            if (NMLConfig.BIOMES.frozenShore.get()) builder.add(this.frozenShore);
            if (NMLConfig.BIOMES.tropicalBeach.get()) builder.add(this.tropicalBeach);
            if (NMLConfig.BIOMES.mudBeach.get()) builder.add(this.mudBeach);
            if (NMLConfig.BIOMES.blackwaterRiver.get()) builder.add(this.blackwaterRiver);
            if (NMLConfig.BIOMES.lushRiver.get()) builder.add(this.lushRiver);
            if (NMLConfig.BIOMES.desertRiver.get()) builder.add(this.desertRiver);
        }

        @Override
        public MapCodec<? extends BiomeUtil.ModdedBiomeProvider> codec() {
            return CODEC;
        }
    }

    /* For modifying the behavior of all overworld slices.
       Useful for placing "global" biomes in other modded territories in a more compatible way.
     */
    public record GlobalOverworldOverlays(
        BiomeUtil.ModdedBiomeProvider base,
        Holder<Biome> caves,
        Holder<Biome> caveDepths
    ) implements BiomeUtil.ModdedBiomeProvider {
        public static final MapCodec<GlobalOverworldOverlays> CODEC = RecordCodecBuilder.mapCodec(instance -> {
            return instance.group(
                BiomeUtil.ModdedBiomeProvider.CODEC.fieldOf("base").forGetter(provider -> provider.base),
                Biome.CODEC.fieldOf("caves").forGetter(provider -> provider.caves),
                Biome.CODEC.fieldOf("cave_depths").forGetter(provider -> provider.caveDepths)
            ).apply(instance, GlobalOverworldOverlays::new);
        });

        @Override
        public void finalize(MinecraftServer server, long seed) {
            this.base.finalize(server, seed);
        }

        @Override
        public Holder<Biome> getNoiseBiome(int x, int y, int z, BiomeUtil.ScopedDensityFunctionContext context, BiomeSource original, Registry<Biome> registry) {
            return this.getNoiseBiome(this.base.getNoiseBiome(x, y, z, context, original, registry), y, context.getClimateSampler().depth().compute(context));
        }

        // TODO: Remove next MC version when Blueprint breaking changes are allowed
        @Override
        @Deprecated
        public Holder<Biome> getNoiseBiome(int x, int y, int z, Climate.Sampler sampler, BiomeSource biomeSource, Registry<Biome> registry) {
            return this.getNoiseBiome(this.base.getNoiseBiome(x, y, z, sampler, biomeSource, registry), y, sampler.depth().compute(new DensityFunction.SinglePointContext(QuartPos.toBlock(x), QuartPos.toBlock(y), QuartPos.toBlock(z))));
        }

        public Holder<Biome> getNoiseBiome(Holder<Biome> baseBiome, int y, double depth) {
            if (baseBiome.is(BlueprintBiomes.ORIGINAL_SOURCE_MARKER)) return baseBiome;
            if (!baseBiome.is(Tags.Biomes.IS_UNDERGROUND)) {
                if (y < 0) return this.caveDepths;
                if (depth > 0.1) return this.caves;
            }
            return baseBiome;
        }

        @Override
        public Set<Holder<Biome>> getPossibleBiomes(Set<Holder<Biome>> originalPossibleBiomes, Registry<Biome> registry) {
            ImmutableSet.Builder<Holder<Biome>> builder = ImmutableSet.builder();
            builder.add(this.caves);
            builder.add(this.caveDepths);
            builder.addAll(this.base.getPossibleBiomes(originalPossibleBiomes, registry));
            return builder.build();
        }

        @Override
        public Set<Holder<Biome>> getAdditionalPossibleBiomes(Registry<Biome> registry) {
            ImmutableSet.Builder<Holder<Biome>> builder = ImmutableSet.builder();
            builder.add(this.caves);
            builder.add(this.caveDepths);
            builder.addAll(this.base.getAdditionalPossibleBiomes(registry));
            return builder.build();
        }

        @Override
        public MapCodec<? extends BiomeUtil.ModdedBiomeProvider> codec() {
            return CODEC;
        }
    }
}
