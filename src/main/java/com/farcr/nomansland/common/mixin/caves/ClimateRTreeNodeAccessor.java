package com.farcr.nomansland.common.mixin.caves;

import net.minecraft.world.level.biome.Climate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.world.level.biome.Climate$RTree$Node")
public interface ClimateRTreeNodeAccessor {
    @Accessor("parameterSpace")
    Climate.Parameter[] nml$getParameterSpace();
}
