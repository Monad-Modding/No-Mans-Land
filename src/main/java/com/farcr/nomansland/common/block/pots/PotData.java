package com.farcr.nomansland.common.block.pots;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumSet;
import java.util.Set;

public record PotData(BlockState state, ResourceLocation variant, Set<PotModifier> modifiers) {

    public PotData(BlockState state, ResourceLocation variant) {
        this(state, variant, EnumSet.noneOf(PotModifier.class));
    }

    public static PotData read(CompoundTag tag, HolderLookup.Provider registries) {
        BlockState state = NbtUtils.readBlockState(registries.lookupOrThrow(Registries.BLOCK), tag.getCompound("state"));
        ResourceLocation variant = ResourceLocation.parse(tag.getString("variant"));
        return new PotData(state, variant, PotContents.readModifiers(tag));
    }

    public CompoundTag write() {
        CompoundTag tag = new CompoundTag();
        tag.put("state", NbtUtils.writeBlockState(state));
        tag.putString("variant", variant.toString());
        PotContents.writeModifiers(tag, modifiers);
        return tag;
    }
}
