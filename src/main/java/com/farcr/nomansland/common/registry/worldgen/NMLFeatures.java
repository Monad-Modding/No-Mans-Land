package com.farcr.nomansland.common.registry.worldgen;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.world.feature.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.VegetationPatchConfiguration;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class NMLFeatures {

    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, NoMansLand.MODID);

    public static final Supplier<Feature<SpreadPatchConfiguration>> SPREAD_PATCH = FEATURES.register("spread_patch",
            () -> new SpreadPatchFeature(SpreadPatchConfiguration.CODEC));

    public static final Supplier<Feature<StringOreConfiguration>> STRING_ORE = FEATURES.register("string_ore",
            () -> new StringOreFeature(StringOreConfiguration.CODEC));

    public static final Supplier<Feature<MultiSpreadPatchConfiguration>> MULTISPREAD_PATCH = FEATURES.register("multispread_patch",
            () -> new MultiSpreadPatchFeature(MultiSpreadPatchConfiguration.CODEC));

    public static final Supplier<Feature<NoneFeatureConfiguration>> SHELF_MUSHROOM = FEATURES.register("shelf_mushroom",
            () -> new ShelfMushroomFeature(NoneFeatureConfiguration.CODEC));

    public static final Supplier<Feature<PondFeatureConfiguration>> POND = FEATURES.register("pond",
            () -> new PondFeature(PondFeatureConfiguration.CODEC));

    public static final Supplier<Feature<BoulderFeatureConfiguration>> BOULDER = FEATURES.register("boulder",
            () -> new BoulderFeature(BoulderFeatureConfiguration.CODEC));

    public static final Supplier<Feature<FallenTreeFeatureConfiguration>> FALLEN_TREE = FEATURES.register("fallen_tree",
            () -> new FallenTreeFeature(FallenTreeFeatureConfiguration.CODEC));

    public static final Supplier<Feature<FoliageCircleFeatureConfiguration>> FOLIAGE_CIRCLE = FEATURES.register("foliage_circle",
            () -> new FoliageCircleFeature(FoliageCircleFeatureConfiguration.CODEC));

    public static final Supplier<Feature<FoliageCircleFeatureConfiguration>> BUDDY_FAIRY_RING = FEATURES.register("buddy_fairy_ring",
            () -> new BuddyFairyRingFeature(FoliageCircleFeatureConfiguration.CODEC));

    public static final Supplier<Feature<DitheredPatchFeatureConfiguration>> DITHERED_PATCH = FEATURES.register("dithered_patch",
            () -> new DitheredPatchFeature(DitheredPatchFeatureConfiguration.CODEC));

    public static final Supplier<Feature<CragRockFeatureConfiguration>> CRAG_ROCK = FEATURES.register("crag_rock",
            () -> new CragRockFeature(CragRockFeatureConfiguration.CODEC));

    public static final Supplier<Feature<TortoiseBurrowFeature.Configuration>> TORTOISE_BURROW = FEATURES.register("tortoise_burrow",
            () -> new TortoiseBurrowFeature(TortoiseBurrowFeature.Configuration.CODEC));

    public static final Supplier<Feature<NoneFeatureConfiguration>> ANCIENT_TREE = FEATURES.register("ancient_tree",
            () -> new AncientTreeFeature(NoneFeatureConfiguration.CODEC));

    public static final Supplier<Feature<PotPatchConfiguration>> POT_PATCH = FEATURES.register("pot_patch",
            () -> new PotPatchFeature(PotPatchConfiguration.CODEC));

    public static final Supplier<Feature<AncestralEffigyFeature.Configuration>> ANCESTRAL_EFFIGY = FEATURES.register("ancestral_effigy",
            AncestralEffigyFeature::new);

    public static final Supplier<Feature<NoneFeatureConfiguration>> ANCESTRAL_CARVING = FEATURES.register("ancestral_carving",
            () -> new AncestralCarvingFeature(NoneFeatureConfiguration.CODEC));

    public static final Supplier<Feature<VegetationPatchConfiguration>> SUSPICIOUS_VEGETATION_PATCH = FEATURES.register("suspicious_vegetation_patch",
            () -> new SuspiciousVegetationPatchFeature(SuspiciousVegetationPatchConfiguration.CODEC));

    public static final ResourceKey<ConfiguredFeature<?, ?>> HUGE_FIELD_MUSHROOM = register("huge_field_mushroom");

    public static final ResourceKey<ConfiguredFeature<?, ?>> OAK_APPLE_01 = register("oak_apple_01");
    public static final ResourceKey<ConfiguredFeature<?, ?>> OAK_APPLE_05 = register("oak_apple_05");
    public static final ResourceKey<ConfiguredFeature<?, ?>> FANCY_OAK_APPLE_01 = register("fancy_oak_apple_01");
    public static final ResourceKey<ConfiguredFeature<?, ?>> FANCY_OAK_APPLE_05 = register("fancy_oak_apple_05");
    public static final ResourceKey<ConfiguredFeature<?, ?>> AUTUMNAL_OAK = register("autumnal_oak");
    public static final ResourceKey<ConfiguredFeature<?, ?>> LARGE_AUTUMNAL_OAK = register("large_autumnal_oak");
    public static final ResourceKey<ConfiguredFeature<?, ?>> AUTUMNAL_OAK_PEAR_05 = register("autumnal_oak_pear_05");
    public static final ResourceKey<ConfiguredFeature<?, ?>> LARGE_AUTUMNAL_OAK_PEAR_05 = register("large_autumnal_oak_pear_05");
    public static final ResourceKey<ConfiguredFeature<?, ?>> FROSTED_SPRUCE = register("frosted_spruce");
    public static final ResourceKey<ConfiguredFeature<?, ?>> FROSTED_SPRUCE_ALT = register("frosted_spruce_alt");
    public static final ResourceKey<ConfiguredFeature<?, ?>> FROSTED_PINE = register("frosted_pine");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MEGA_FROSTED_SPRUCE = register("mega_frosted_spruce");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MEGA_FROSTED_SPRUCE_ALT = register("mega_frosted_spruce_alt");
    public static final ResourceKey<ConfiguredFeature<?, ?>> PINE = register("pine");
    public static final ResourceKey<ConfiguredFeature<?, ?>> LARGE_PINE = register("large_pine");

    private static ResourceKey<ConfiguredFeature<?, ?>> register(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, NoMansLand.location(name));
    }

}
