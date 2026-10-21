package com.farcr.nomansland.datagen;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.registry.NMLConditions;
import com.farcr.nomansland.common.registry.worldgen.NMLBiomes;
import com.google.gson.JsonObject;
import com.teamabnormals.blueprint.common.remolder.data.RemolderProvider;
import com.teamabnormals.blueprint.core.api.conditions.ConfigValueCondition;
import com.teamabnormals.blueprint.core.util.modification.selection.ConditionedResourceSelector;
import com.teamabnormals.blueprint.core.util.modification.selection.selectors.NamesResourceSelector;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

import java.util.concurrent.CompletableFuture;

import static com.farcr.nomansland.common.world.generation.NMLBiomeProviders.*;
import static com.teamabnormals.blueprint.common.remolder.RemolderTypes.*;
import static com.teamabnormals.blueprint.common.remolder.data.DynamicReference.*;

public class NMLDataRemolderProvider extends RemolderProvider {

    public NMLDataRemolderProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> provider) {
        super(NoMansLand.MODID, PackOutput.Target.DATA_PACK, packOutput, provider);
    }

    @Override
    protected void registerEntries(HolderLookup.Provider provider) {
        ConditionedResourceSelector overworldReplacementsSelector = new ConditionedResourceSelector(
            new NamesResourceSelector("blueprint:blueprint/modded_biome_slices/overworld"),
            new ConfigValueCondition(NMLConditions.CONFIG.get(), "biomes")
        );
        this.entry("modded_biome_slices/initial_overworld_replacements")
            .path(overworldReplacementsSelector)
            .remolder(
                replace(target("$provider"), value(new JsonObject(), e -> e)),
                // NML biomes aren't data generated so datagen can't use them directly
                // Thus they must be more manually defined here
                replace(target("$provider[\"transitional_prairie\"]"), value(NMLBiomes.PRAIRIE.location(), ResourceLocation.CODEC)),
                replace(target("$provider[\"transitional_dark_swamp\"]"), value(NMLBiomes.DARK_SWAMP.location(), ResourceLocation.CODEC)),
                replace(target("$provider[\"transitional_bayou\"]"), value(NMLBiomes.BAYOU.location(), ResourceLocation.CODEC)),
                replace(target("$provider[\"transitional_bog\"]"), value(NMLBiomes.BOG.location(), ResourceLocation.CODEC)),
                replace(target("$provider[\"frozen_shore\"]"), value(NMLBiomes.FROZEN_SHORE.location(), ResourceLocation.CODEC)),
                replace(target("$provider[\"tropical_beach\"]"), value(NMLBiomes.TROPICAL_BEACH.location(), ResourceLocation.CODEC)),
                replace(target("$provider[\"mud_beach\"]"), value(NMLBiomes.MUD_BEACH.location(), ResourceLocation.CODEC)),
                replace(target("$provider[\"blackwater_river\"]"), value(NMLBiomes.BLACKWATER_RIVER.location(), ResourceLocation.CODEC)),
                replace(target("$provider[\"lush_river\"]"), value(NMLBiomes.LUSH_RIVER.location(), ResourceLocation.CODEC)),
                replace(target("$provider[\"desert_river\"]"), value(NMLBiomes.DESERT_RIVER.location(), ResourceLocation.CODEC)),
                replace(target("$provider[\"type\"]"), value(INITIAL_OVERWORLD_REPLACEMENTS, ResourceLocation.CODEC)),
                replace(target("$provider[\"base\"]"), target("provider")),
                replace(target("provider"), target("$provider"))
            );
    }
}
