package com.farcr.nomansland.datagen;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.datagen.worldgen.NMLBiomeModifiers;
import com.farcr.nomansland.datagen.worldgen.NMLBiomeSlices;
import com.teamabnormals.blueprint.core.registry.BlueprintDataPackRegistries;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class NMLDatapackEntriesProvider extends DatapackBuiltinEntriesProvider {

    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(BlueprintDataPackRegistries.MODDED_BIOME_SLICES, NMLBiomeSlices::bootstrap)
            .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, NMLBiomeModifiers::new);

    public NMLDatapackEntriesProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, NMLBiomeSlices::bootstrapConditions, Set.of(NoMansLand.MODID));
    }
}
