package com.farcr.nomansland.common.friend.dialogue;

import com.farcr.nomansland.common.friend.condition.MoonlightOfferingConditions;
import com.farcr.nomansland.common.registry.NMLRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public class DialogueUtil {
    public static final int FRIEND_MOON_TEXT_COLOR = 9276752;
    public static final int NOBODY_CAME_TEXT_COLOR = 16711696;
    public static Registry<DialoguePool> getDialogueRegistry(Level level, ResourceKey<Registry<DialoguePool>> resourceKey) {
        try {
            return level.registryAccess().registryOrThrow(resourceKey);
        } catch (IllegalStateException e) {
            throw new RuntimeException("Error obtaining dialogue registry: " + e);
        }
    }


    public static DialoguePool getWeightedEntry(List<DialoguePool> list, RandomSource randomSource) {
        if (list.isEmpty())
            return null;
        WeightedRandomList<DialoguePool> weightedRandomList = WeightedRandomList.create(list);
        return getWeightedEntryInternal(weightedRandomList, randomSource);
    }

    /* It's probably fine to make this recursive? lol??? */
    private static DialoguePool getWeightedEntryInternal(WeightedRandomList<DialoguePool> list, RandomSource randomSource) {
        Optional<DialoguePool> optionalDialogue = list.getRandom(randomSource);
        return optionalDialogue.orElseGet(() -> getWeightedEntryInternal(list, randomSource));
    }

    public static List<ResourceLocation> getOfferingDialogueLocations(RegistryAccess registryAccess, Item item) {
        Holder<Item> holder = BuiltInRegistries.ITEM.wrapAsHolder(item);
        return collectOfferingDialogueLocations(registryAccess, (condition) ->
            condition instanceof MoonlightOfferingConditions.ItemOfferingConditional itemConditional
                && itemConditional.getValue().contains(holder));
    }

    public static List<ResourceLocation> getOfferingDialogueLocations(RegistryAccess registryAccess, EntityType<?> entityType) {
        Holder<EntityType<?>> holder = BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(entityType);
        return collectOfferingDialogueLocations(registryAccess, (condition) ->
            condition instanceof MoonlightOfferingConditions.EntityOfferingConditional entityConditional
                && entityConditional.getValue().contains(holder));
    }

    private static List<ResourceLocation> collectOfferingDialogueLocations(
        RegistryAccess registryAccess, Predicate<DialogueRegistry.DialogueCondition> conditionPredicate
    ) {
        Optional<Registry<DialoguePool>> optionalRegistry = registryAccess.registry(NMLRegistries.OFFERING_DIALOGUE_KEY);
        if (optionalRegistry.isEmpty())
            return List.of();
        List<ResourceLocation> locations = new ArrayList<>();
        for (Map.Entry<ResourceKey<DialoguePool>, DialoguePool> entry : optionalRegistry.get().entrySet()) {
            Optional<DialogueRegistry.DialogueCondition> condition = entry.getValue().condition();
            if (condition.isPresent() && conditionPredicate.test(condition.get()))
                locations.add(entry.getKey().location());
        }
        return locations;
    }

    public static <T> void appendTags(
            T value, RegistryAccess registryAccess,
            ResourceKey<Registry<T>> registry,
            HashMap<T, ArrayList<DialoguePool>> map,
            HashMap<TagKey<T>, ArrayList<DialoguePool>> tagMap,
            ArrayList<DialoguePool> emptyPool
    ) {
        if (map.containsKey(value))
            emptyPool.addAll(map.get(value));

        Optional<Registry<T>> optionalRegistry = registryAccess.registry(registry);
        if (optionalRegistry.isPresent()) {
            Registry<T> obtainedRegistry = optionalRegistry.get();
            Optional<Holder.Reference<T>> holder =
                obtainedRegistry.getHolder(obtainedRegistry.getKey(value));
            if (holder.isPresent()) {
                for (TagKey<T> tag : holder.get().tags().toList()) {
                    if (tagMap.containsKey(tag))
                        emptyPool.addAll(tagMap.get(tag));
                }
            }
        }
    }
}
