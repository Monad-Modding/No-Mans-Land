package com.farcr.nomansland.common.world.surfacerule;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.SurfaceRules;

import java.util.List;

public record BiomeTagConditionSource(List<TagKey<Biome>> biomeTags) implements SurfaceRules.ConditionSource {
    public static final KeyDispatchDataCodec<BiomeTagConditionSource> CODEC = KeyDispatchDataCodec.of(
            RecordCodecBuilder.mapCodec(
                    record -> record.group(
                            TagKey.codec(Registries.BIOME).listOf().fieldOf("is_biome_tags").forGetter(BiomeTagConditionSource::biomeTags)
                    ).apply(record, BiomeTagConditionSource::new)
            )
    );

    public BiomeTagConditionSource(TagKey<Biome>... biomeTags) {
        this(List.of(biomeTags));
    }

    @Override
    public KeyDispatchDataCodec<? extends SurfaceRules.ConditionSource> codec() {
        return CODEC;
    }

    public SurfaceRules.Condition apply(final SurfaceRules.Context surfaceContext) {
        class BiomeCondition extends SurfaceRules.LazyYCondition {
            BiomeCondition() {
                super(surfaceContext);
            }

            @Override
            protected boolean compute() {
                Holder<Biome> biome = this.context.biome.get();
                for (TagKey<Biome> biomeTag : BiomeTagConditionSource.this.biomeTags()) {
                    if (biome.is(biomeTag)) return true;
                }
                return false;
            }
        }

        return new BiomeCondition();
    }

    @Override
    public String toString() {
        return "BiomeConditionSource[biomesTags=" + this.biomeTags + "]";
    }
}
