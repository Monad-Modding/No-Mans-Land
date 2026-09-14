package com.farcr.nomansland.common.event;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.block.pots.PotVariant;
import com.farcr.nomansland.common.block.pots.PotionTable;
import com.farcr.nomansland.common.block.tap.TapInteraction;
import com.farcr.nomansland.common.entity.buddy.BuddyFood;
import com.farcr.nomansland.common.friend.dialogue.DialoguePool;
import com.farcr.nomansland.common.networking.*;
import com.farcr.nomansland.common.networking.alchemist_tools.*;
import com.farcr.nomansland.common.registry.NMLRegistries;
import com.farcr.nomansland.common.world.orevein.OreVeinType;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;

@EventBusSubscriber(modid = NoMansLand.MODID)
public class RegistrySetupEvents {
    @SubscribeEvent
    public static void registerRegistries(final NewRegistryEvent event) {
        event.register(NMLRegistries.POND_DECORATOR_TYPE);
        event.register(NMLRegistries.BOULDER_DECORATOR_TYPE);
        event.register(NMLRegistries.FALLEN_TREE_DECORATOR_TYPE);
        event.register(NMLRegistries.FOG_MODIFIERS);
        event.register(NMLRegistries.CONTEXTUAL_MUSIC);
        event.register(NMLRegistries.DREAM_TYPE);
        event.register(NMLRegistries.EXTINGUISHABLE_BLOCKS);
        event.register(NMLRegistries.DIALOGUE_CONDITIONAL_TYPE);
        event.register(NMLRegistries.CARVING_TYPE);
    }

    @SubscribeEvent
    public static void registerDatapackRegistries(final DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(NMLRegistries.TAP_INTERACTION_KEY, TapInteraction.CODEC, TapInteraction.CODEC);
        event.dataPackRegistry(NMLRegistries.POT_VARIANT_KEY, PotVariant.CODEC, PotVariant.CODEC);
        event.dataPackRegistry(NMLRegistries.ORE_VEIN_KEY, OreVeinType.CODEC, null);

        /* Moonlight Dialogue Registry */
        event.dataPackRegistry(NMLRegistries.GREETING_DIALOGUE_KEY, DialoguePool.CODEC, DialoguePool.CODEC);
        event.dataPackRegistry(NMLRegistries.PASSIVE_DIALOGUE_KEY, DialoguePool.CODEC, DialoguePool.CODEC);
        event.dataPackRegistry(NMLRegistries.NEGATIVE_DIALOGUE_KEY, DialoguePool.CODEC, DialoguePool.CODEC);
        event.dataPackRegistry(NMLRegistries.OFFERING_DIALOGUE_KEY, DialoguePool.CODEC, DialoguePool.CODEC);
        event.dataPackRegistry(NMLRegistries.CONTEXTUAL_DIALOGUE_KEY, DialoguePool.CODEC, DialoguePool.CODEC);
        event.dataPackRegistry(NMLRegistries.LEAVING_DIALOGUE_KEY, DialoguePool.CODEC, DialoguePool.CODEC);
        event.dataPackRegistry(NMLRegistries.SPECIAL_DIALOGUE_KEY, DialoguePool.CODEC, DialoguePool.CODEC);

        event.dataPackRegistry(NMLRegistries.BUDDY_FOOD_KEY, BuddyFood.CODEC, BuddyFood.CODEC);
        event.dataPackRegistry(NMLRegistries.POTION_TABLE_KEY, PotionTable.CODEC, null);
    }
}
