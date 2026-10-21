package com.farcr.nomansland.common.friend.dialogue;

import com.farcr.nomansland.common.registry.NMLRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Function;

public class DialogueRegistry {
    public interface DialogueCondition {
        default boolean validate(ResourceKey<Registry<DialoguePool>> registrykey) { return true; }
        MapCodec<? extends DialogueCondition> codec();
        Codec<DialogueCondition> CODEC = NMLRegistries.DIALOGUE_CONDITIONAL_TYPE
            .byNameCodec().dispatch(DialogueCondition::codec, Function.identity());
    }

    /*
    * Simple List Conditionals, for "hardcoded" conditions
     */
    public interface ListCondition extends DialogueCondition {
        public ArrayList<DialoguePool> getList();
        default void append(DialoguePool dialoguePool) {
            getList().add(dialoguePool);
        }
    }

    /*
    * Compiled Condition, for simple holder conditions
    * that need to be compiled to hashmaps at runtime.
     */
    public interface CompiledCondition<T> extends DialogueCondition {
        static <T> CompiledCondition<T> getInstance() { return null; }
        HolderSet<T> getValue();
        HashMap<T, ArrayList<DialoguePool>> getMap();
        HashMap<TagKey<T>, ArrayList<DialoguePool>> getTagMap();

        static <K> void applyMap(DialoguePool dialoguePool, HashMap<K, ArrayList<DialoguePool>> map, K key) {
            ArrayList<DialoguePool> poolList = map.getOrDefault(key, new ArrayList<>());
            poolList.add(dialoguePool);
            map.put(key, poolList);
        }
        default void consume(DialoguePool dialoguePool) {
            if (getValue() instanceof HolderSet.Named<T> namedTag) {
                applyMap(dialoguePool, getTagMap(), namedTag.key());
                return;
            }
            getValue().forEach((holder) ->
                applyMap(dialoguePool, getMap(), holder.value()));
        };
    }
}