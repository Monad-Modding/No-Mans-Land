package com.farcr.nomansland.common.registry;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.friend.condition.MoonlightContextualConditions.EffectContextualCondition;
import com.farcr.nomansland.common.friend.condition.MoonlightContextualConditions.EquipmentContextualConditional;
import com.farcr.nomansland.common.friend.condition.MoonlightGreetingConditions.AdditionToCommuneConditional;
import com.farcr.nomansland.common.friend.condition.MoonlightGreetingConditions.DreamGreetingConditional;
import com.farcr.nomansland.common.friend.condition.MoonlightGreetingConditions.FirstTimeGreetingConditional;
import com.farcr.nomansland.common.friend.condition.MoonlightLeavingConditions.OnDeathConditional;
import com.farcr.nomansland.common.friend.condition.MoonlightOfferingConditions.EntityOfferingConditional;
import com.farcr.nomansland.common.friend.condition.MoonlightOfferingConditions.ItemOfferingConditional;
import com.farcr.nomansland.common.friend.dialogue.DialogueRegistry;
import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class NMLDialogueConditions {
    public static final DeferredRegister<MapCodec<? extends DialogueRegistry.DialogueCondition>> DIALOGUE_CONDITION_REGISTRY =
        DeferredRegister.create(NMLRegistries.DIALOGUE_CONDITIONAL_TYPE, NoMansLand.MODID);

    public static final DeferredHolder<MapCodec<? extends DialogueRegistry.DialogueCondition>, MapCodec<ItemOfferingConditional>> OFFERING_ITEM_CONDITIONAL =
        DIALOGUE_CONDITION_REGISTRY.register("item_conditional", () -> ItemOfferingConditional.CODEC);
    public static final DeferredHolder<MapCodec<? extends DialogueRegistry.DialogueCondition>, MapCodec<EntityOfferingConditional>> OFFERING_ENTITY_CONDITIONAL =
        DIALOGUE_CONDITION_REGISTRY.register("entity_conditional", () -> EntityOfferingConditional.CODEC);

    public static final DeferredHolder<MapCodec<? extends DialogueRegistry.DialogueCondition>, MapCodec<EffectContextualCondition>> CONTEXTUAL_EFFECT_CONDITIONAL =
        DIALOGUE_CONDITION_REGISTRY.register("effect_conditional", () -> EffectContextualCondition.CODEC);
    public static final DeferredHolder<MapCodec<? extends DialogueRegistry.DialogueCondition>, MapCodec<EquipmentContextualConditional>> CONTEXTUAL_EQUIPMENT_CONDITIONAL =
        DIALOGUE_CONDITION_REGISTRY.register("equipment_conditional", () -> EquipmentContextualConditional.CODEC);

    public static final DeferredHolder<MapCodec<? extends DialogueRegistry.DialogueCondition>, MapCodec<FirstTimeGreetingConditional>> FIRST_TIME_GREETING_CONDITIONAL =
        DIALOGUE_CONDITION_REGISTRY.register("first_time", () -> FirstTimeGreetingConditional.CODEC);
    public static final DeferredHolder<MapCodec<? extends DialogueRegistry.DialogueCondition>, MapCodec<AdditionToCommuneConditional>> ADDITION_TO_COMMUNE_CONDITIONAL =
        DIALOGUE_CONDITION_REGISTRY.register("addition_to_commune", () -> AdditionToCommuneConditional.CODEC);
    public static final DeferredHolder<MapCodec<? extends DialogueRegistry.DialogueCondition>, MapCodec<DreamGreetingConditional>> FROM_DREAM =
        DIALOGUE_CONDITION_REGISTRY.register("from_dream", () -> DreamGreetingConditional.CODEC);
    public static final DeferredHolder<MapCodec<? extends DialogueRegistry.DialogueCondition>, MapCodec<OnDeathConditional>> ON_DEATH_LEAVING_CONDITIONAL =
        DIALOGUE_CONDITION_REGISTRY.register("on_death", () -> OnDeathConditional.CODEC);
}
