package com.farcr.nomansland.common.registry;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.ambience.fogmodifiers.FogModifier;
import com.farcr.nomansland.client.music.condition.MusicCondition;
import com.farcr.nomansland.common.block.pots.PotVariant;
import com.farcr.nomansland.common.block.pots.PotionTable;
import com.farcr.nomansland.common.block.tap.TapInteraction;
import com.farcr.nomansland.common.block.torches.ExtinguishableBlockPairing;
import com.farcr.nomansland.common.carving.CarvingType;
import com.farcr.nomansland.common.dreams.DreamType;
import com.farcr.nomansland.common.dreams.DreamType;
import com.farcr.nomansland.common.entity.buddy.BuddyFood;
import com.farcr.nomansland.common.friend.dialogue.DialoguePool;
import com.farcr.nomansland.common.friend.dialogue.DialogueRegistry;
import com.farcr.nomansland.common.world.feature.decorator.BoulderDecoratorType;
import com.farcr.nomansland.common.world.feature.decorator.FallenTreeDecoratorType;
import com.farcr.nomansland.common.world.feature.decorator.PondDecoratorType;
import com.farcr.nomansland.common.world.orevein.OreVeinType;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.RegistryBuilder;

public class NMLRegistries {
    public static final ResourceKey<Registry<PondDecoratorType<?>>> POND_DECORATOR_TYPE_KEY = ResourceKey.createRegistryKey(NoMansLand.location("worldgen/ponddecorator"));
    public static final Registry<PondDecoratorType<?>> POND_DECORATOR_TYPE = new RegistryBuilder<>(POND_DECORATOR_TYPE_KEY).create();

    public static final ResourceKey<Registry<BoulderDecoratorType<?>>> BOULDER_DECORATOR_TYPE_KEY = ResourceKey.createRegistryKey(NoMansLand.location("worldgen/boulderdecorator"));
    public static final Registry<BoulderDecoratorType<?>> BOULDER_DECORATOR_TYPE = new RegistryBuilder<>(BOULDER_DECORATOR_TYPE_KEY).create();

    public static final ResourceKey<Registry<FallenTreeDecoratorType<?>>> FALLEN_TREE_DECORATOR_TYPE_KEY = ResourceKey.createRegistryKey(NoMansLand.location("worldgen/fallentreedecorator"));
    public static final Registry<FallenTreeDecoratorType<?>> FALLEN_TREE_DECORATOR_TYPE = new RegistryBuilder<>(FALLEN_TREE_DECORATOR_TYPE_KEY).create();

    public static final ResourceKey<Registry<FogModifier>> FOG_MODIFIER_TYPE_KEY = ResourceKey.createRegistryKey(NoMansLand.location("client/fogmodifier"));
    public static final Registry<FogModifier> FOG_MODIFIERS = new RegistryBuilder<>(FOG_MODIFIER_TYPE_KEY).create();

    public static final ResourceKey<Registry<MusicCondition>> CONTEXTUAL_MUSIC_TYPE_KEY = ResourceKey.createRegistryKey(NoMansLand.location("client/contextual_music"));
    public static final Registry<MusicCondition> CONTEXTUAL_MUSIC = new RegistryBuilder<>(CONTEXTUAL_MUSIC_TYPE_KEY).create();

    public static final ResourceKey<Registry<DreamType>> DREAM_TYPE_KEY =
        ResourceKey.createRegistryKey(NoMansLand.location("dream_types"));
    public static final Registry<DreamType> DREAM_TYPE = new RegistryBuilder<>(DREAM_TYPE_KEY).create();

    public static final ResourceKey<Registry<TapInteraction>> TAP_INTERACTION_KEY = ResourceKey.createRegistryKey(NoMansLand.location("tapping"));
    public static final ResourceKey<Registry<PotVariant>> POT_VARIANT_KEY = ResourceKey.createRegistryKey(NoMansLand.location("pots"));
    public static final ResourceKey<Registry<PotionTable>> POTION_TABLE_KEY = ResourceKey.createRegistryKey(NoMansLand.location("potion_tables"));

    public static final ResourceKey<Registry<OreVeinType>> ORE_VEIN_KEY = ResourceKey.createRegistryKey(NoMansLand.location("worldgen/ore_vein"));

    /* Moonlight Dialogue Pools */
    public static final ResourceKey<Registry<MapCodec<? extends DialogueRegistry.DialogueCondition>>> DIALOGUE_CONDITIONAL_TYPE_KEY
        = ResourceKey.createRegistryKey(NoMansLand.location("dialogue_pools/conditional"));
    public static final Registry<MapCodec<? extends DialogueRegistry.DialogueCondition>> DIALOGUE_CONDITIONAL_TYPE =
        new RegistryBuilder<>(DIALOGUE_CONDITIONAL_TYPE_KEY).defaultKey(NoMansLand.location("none")).create();

    public static final ResourceKey<Registry<DialoguePool>> GREETING_DIALOGUE_KEY = ResourceKey.createRegistryKey(NoMansLand.location("dialogue_pools/greeting"));
    public static final ResourceKey<Registry<DialoguePool>> PASSIVE_DIALOGUE_KEY = ResourceKey.createRegistryKey(NoMansLand.location("dialogue_pools/passive"));
    public static final ResourceKey<Registry<DialoguePool>> NEGATIVE_DIALOGUE_KEY = ResourceKey.createRegistryKey(NoMansLand.location("dialogue_pools/negative"));
    public static final ResourceKey<Registry<DialoguePool>> OFFERING_DIALOGUE_KEY = ResourceKey.createRegistryKey(NoMansLand.location("dialogue_pools/offering"));
    public static final ResourceKey<Registry<DialoguePool>> CONTEXTUAL_DIALOGUE_KEY = ResourceKey.createRegistryKey(NoMansLand.location("dialogue_pools/contextual"));
    public static final ResourceKey<Registry<DialoguePool>> LEAVING_DIALOGUE_KEY = ResourceKey.createRegistryKey(NoMansLand.location("dialogue_pools/leaving"));
    public static final ResourceKey<Registry<DialoguePool>> SPECIAL_DIALOGUE_KEY = ResourceKey.createRegistryKey(NoMansLand.location("dialogue_pools/special"));

    public static final ResourceKey<Registry<BuddyFood>> BUDDY_FOOD_KEY = ResourceKey.createRegistryKey(NoMansLand.location("buddy_food"));

    public static final ResourceKey<Registry<ExtinguishableBlockPairing>> EXTINGUISHABLE_BLOCKS_KEY = ResourceKey.createRegistryKey(NoMansLand.location("extinguishable_blocks"));
    public static final Registry<ExtinguishableBlockPairing> EXTINGUISHABLE_BLOCKS = new RegistryBuilder<>(EXTINGUISHABLE_BLOCKS_KEY).create();

    public static final ResourceKey<Registry<CarvingType>> CARVING_TYPE_KEY = ResourceKey.createRegistryKey(NoMansLand.location("carving_type"));
    public static final Registry<CarvingType> CARVING_TYPE = new RegistryBuilder<>(CARVING_TYPE_KEY).sync(true).create();
}
