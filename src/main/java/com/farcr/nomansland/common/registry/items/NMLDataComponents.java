package com.farcr.nomansland.common.registry.items;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.block.pots.SeededPotionTable;
import com.mojang.serialization.Codec;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class NMLDataComponents {
    public static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, NoMansLand.MODID);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Long>> TIME_WHEN_DISABLED = DATA_COMPONENTS.registerComponentType(
            "time_when_disabled", builder -> builder
                    .persistent(Codec.LONG).networkSynchronized(ByteBufCodecs.VAR_LONG));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> PUNCH_COOLDOWN = DATA_COMPONENTS.registerComponentType(
            "punch_cooldown", builder -> builder
                    .persistent(Codec.INT));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> PUNCH_COUNT = DATA_COMPONENTS.registerComponentType(
            "punch_count", builder -> builder
                    .persistent(Codec.INT));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ResourceLocation>> POT_VARIANT = DATA_COMPONENTS.registerComponentType(
            "pot_variant", builder -> builder
                    .persistent(ResourceLocation.CODEC)
    );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<GlobalPos>> INVERTED_BELL_TARGET = DATA_COMPONENTS.registerComponentType(
            "inverted_bell_target", builder -> builder
                    .persistent(GlobalPos.CODEC)
    );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<SeededPotionTable>> POT_POTION_TABLE = DATA_COMPONENTS.registerComponentType(
            "pot_potion_table", builder -> builder
                    .persistent(SeededPotionTable.CODEC)
    );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<java.util.List<String>>> POT_MODIFIERS = DATA_COMPONENTS.registerComponentType(
            "pot_modifiers", builder -> builder
                    .persistent(Codec.STRING.listOf())
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()))
    );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> OATH_SWORD_USE_TIME = DATA_COMPONENTS.registerComponentType(
        "oath_sword_use_time", builder -> builder.networkSynchronized(ByteBufCodecs.INT)
    );
}
