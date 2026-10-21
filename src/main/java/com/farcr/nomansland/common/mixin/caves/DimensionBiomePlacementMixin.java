package com.farcr.nomansland.common.mixin.caves;

import com.farcr.nomansland.NMLConfig;
import com.farcr.nomansland.common.registry.worldgen.NMLBiomes;
import com.llamalad7.mixinextras.sugar.Local;
import com.terraformersmc.biolith.api.biome.BiolithFittestNodes;
import com.terraformersmc.biolith.impl.biome.DimensionBiomePlacement;
import com.terraformersmc.biolith.impl.biome.OverworldBiomePlacement;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.neoforged.neoforge.common.Tags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DimensionBiomePlacement.class)
public class DimensionBiomePlacementMixin {
    @Inject(method = "getReplacement", at = @At("RETURN"), cancellable = true)
    private void addCaveReplacement(int x, int y, int z, Climate.TargetPoint noisePoint, BiolithFittestNodes<Holder<Biome>> fittestNodes, CallbackInfoReturnable<Holder<Biome>> cir, @Local(name = "biomeEntry") Holder<Biome> biomeEntry) {
        if ((DimensionBiomePlacement)(Object)this instanceof OverworldBiomePlacement) {
            if (NMLConfig.CAVES_BIOMES.get()) {
                if (!biomeEntry.is(Tags.Biomes.IS_UNDERGROUND) && !nml$likelyUnderground(fittestNodes, noisePoint.depth())) {
                    if (y < 0) {
                        cir.setReturnValue(NMLBiomes.CAVE_DEPTHS_HOLDER);
                    } else if (noisePoint.depth() > 0.1F * 10000) {
                        cir.setReturnValue(NMLBiomes.CAVES_HOLDER);
                    }
                }
            }
        }
    }

    @Unique
    private static boolean nml$likelyUnderground(BiolithFittestNodes<Holder<Biome>> fittestNodes, long depth) {
        if (fittestNodes == null || fittestNodes.ultimate() == null) return false;
        Climate.Parameter[] parameterSpace = ((ClimateRTreeNodeAccessor) (Object) fittestNodes.ultimate()).nml$getParameterSpace();
        if (parameterSpace == null || parameterSpace.length <= 4) return false;
        Climate.Parameter depthParameter = parameterSpace[4];
        return depth >= depthParameter.min() && depth <= depthParameter.max();
    }
}
