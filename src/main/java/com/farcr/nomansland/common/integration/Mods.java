package com.farcr.nomansland.common.integration;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.fml.ModList;

public enum Mods {
    FARMERSDELIGHT,
    BLOCKBOX,
    CREATE,
    BOATLOAD,
    BLUEPRINT,
    NIRVANA,
    EVERYCOMP,
    SNOWREALMAGIC,
    VANITY,
    ETCHED,
    FIELDGUIDE,
    MOONLIGHT;

    private final String id;

    Mods() {
        id = name().toLowerCase();
    }

    public boolean isLoaded() {
        return ModList.get().isLoaded(id);
    }

    public ResourceLocation location(String path) {
        return ResourceLocation.fromNamespaceAndPath(id, path);
    }

    public Block getBlock(String id) {
        return BuiltInRegistries.BLOCK.get(location(id));
    }

    public Item getItem(String id) {
        return BuiltInRegistries.ITEM.get(location(id));
    }

    public Fluid getFluid(String id) {
        return BuiltInRegistries.FLUID.get(location(id));
    }
}
