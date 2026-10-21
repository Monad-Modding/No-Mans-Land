package com.farcr.nomansland.common.world.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public record UndergroundPoolConfiguration(
        IntProvider cavernDiameter,
        IntProvider cavernHeight,
        IntProvider lakeDepth,
        IntProvider waterHeight,
        int maxGasLayers,
        IntProvider veinCount,
        Holder<PlacedFeature> veinFeature
) implements FeatureConfiguration {
    public static final Codec<UndergroundPoolConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            IntProvider.codec(4, 64).fieldOf("cavern_diameter").forGetter(UndergroundPoolConfiguration::cavernDiameter),
            IntProvider.codec(3, 96).fieldOf("cavern_height").forGetter(UndergroundPoolConfiguration::cavernHeight),
            IntProvider.codec(1, 32).fieldOf("lake_depth").forGetter(UndergroundPoolConfiguration::lakeDepth),
            IntProvider.codec(0, 32).fieldOf("water_height").forGetter(UndergroundPoolConfiguration::waterHeight),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("max_gas_layers").orElse(3).forGetter(UndergroundPoolConfiguration::maxGasLayers),
            IntProvider.codec(0, 64).fieldOf("vein_count").forGetter(UndergroundPoolConfiguration::veinCount),
            PlacedFeature.CODEC.fieldOf("vein_feature").forGetter(UndergroundPoolConfiguration::veinFeature)
    ).apply(instance, UndergroundPoolConfiguration::new));
}
