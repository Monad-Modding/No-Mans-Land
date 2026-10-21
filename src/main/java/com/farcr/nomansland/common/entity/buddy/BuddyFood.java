package com.farcr.nomansland.common.entity.buddy;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.world.item.Item;

public record BuddyFood(
    HolderSet<Item> item,
    int happinessTicks
) {
    public static final Codec<BuddyFood> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            HolderSetCodec.create(Registries.ITEM, BuiltInRegistries.ITEM.holderByNameCodec(), false)
                .fieldOf("items").forGetter(BuddyFood::item),
            Codec.INT.fieldOf("happiness_duration")
                .forGetter(BuddyFood::happinessTicks)
        ).apply(instance, BuddyFood::new)
    );
}
