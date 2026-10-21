package com.farcr.nomansland.common.world.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public record StringOreConfiguration(BlockStateProvider state, IntProvider length, float thickenChance) implements FeatureConfiguration {
    public static final Codec<StringOreConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockStateProvider.CODEC.fieldOf("state").forGetter(StringOreConfiguration::state),
            IntProvider.CODEC.fieldOf("length").forGetter(StringOreConfiguration::length),
            Codec.floatRange(0.0F, 1.0F).optionalFieldOf("thicken_chance", 0.3F).forGetter(StringOreConfiguration::thickenChance)
    ).apply(instance, StringOreConfiguration::new));
}
