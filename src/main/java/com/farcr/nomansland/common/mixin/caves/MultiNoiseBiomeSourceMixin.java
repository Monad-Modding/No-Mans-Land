package com.farcr.nomansland.common.mixin.caves;

import com.farcr.nomansland.common.registry.worldgen.NMLBiomes;
import com.terraformersmc.biolith.impl.biome.BiomeCoordinator;
import com.terraformersmc.biolith.impl.biome.InterfaceBiomeSource;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

@Mixin(MultiNoiseBiomeSource.class)
public abstract class MultiNoiseBiomeSourceMixin extends BiomeSource {
    @Override
    public @NotNull Set<Holder<Biome>> possibleBiomes() {
        Set<Holder<Biome>> biomes = new HashSet<>(super.possibleBiomes());
        // The cave biomes are only ever placed in the overworld (see DimensionBiomePlacementMixin),
        // so they must not be reported as possible biomes of other dimensions' sources. Mods like
        // BetterNether merge the vanilla nether source's possible biomes into their own biome
        // picker, which would make the cave biomes actually generate in the nether.
        if (BuiltinDimensionTypes.OVERWORLD.location()
                .equals(((InterfaceBiomeSource) (Object) this).biolith$getDimensionType().location())) {
            BiomeCoordinator.getBiomeLookup().ifPresent(lookup ->
                    Stream.of(NMLBiomes.CAVES, NMLBiomes.CAVE_DEPTHS)
                            .forEach(key -> lookup.get(key).ifPresent(biomes::add))
            );
        }
        return biomes;
    }
}
