package com.farcr.nomansland.common.world.generation;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.world.densityfunction.LazilyCachedDensityFunctionSeedifier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.DensityFunction;

public final class GeothermalSampler {
    public static final ResourceKey<DensityFunction> GEOTHERMAL =
            ResourceKey.create(Registries.DENSITY_FUNCTION, NoMansLand.location("geothermal"));

    private GeothermalSampler() {
    }

    public static double sample(WorldGenLevel level, BlockPos pos) {
        DensityFunction function = level.registryAccess()
                .registryOrThrow(Registries.DENSITY_FUNCTION)
                .getHolderOrThrow(GEOTHERMAL)
                .value();
        DensityFunction seeded = function.mapAll(LazilyCachedDensityFunctionSeedifier.getOrCreate(level));
        return seeded.compute(new DensityFunction.SinglePointContext(pos.getX(), pos.getY(), pos.getZ()));
    }
}
