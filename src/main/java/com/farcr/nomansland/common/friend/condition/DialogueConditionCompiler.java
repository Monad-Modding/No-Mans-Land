package com.farcr.nomansland.common.friend.condition;

import com.farcr.nomansland.common.friend.dialogue.DialoguePool;
import com.farcr.nomansland.common.friend.dialogue.DialogueRegistry;
import com.farcr.nomansland.common.registry.NMLRegistries;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public class DialogueConditionCompiler implements PreparableReloadListener {
    private final RegistryAccess registryAccess;
    public DialogueConditionCompiler(RegistryAccess registryAccess) {
        this.registryAccess = registryAccess;
    }

    public static final List<ResourceKey<Registry<DialoguePool>>> REGISTRIES = List.of(
        NMLRegistries.GREETING_DIALOGUE_KEY,
        NMLRegistries.PASSIVE_DIALOGUE_KEY,
        NMLRegistries.OFFERING_DIALOGUE_KEY,
        NMLRegistries.NEGATIVE_DIALOGUE_KEY,
        NMLRegistries.CONTEXTUAL_DIALOGUE_KEY,
        NMLRegistries.LEAVING_DIALOGUE_KEY,
        NMLRegistries.SPECIAL_DIALOGUE_KEY
    );

    // blank dummy object to access getMap() methods
    public static void forEachCondition(Consumer<DialogueRegistry.DialogueCondition> conditionConsumer) {
        NMLRegistries.DIALOGUE_CONDITIONAL_TYPE.forEach((condition) -> {
            try {
                DialogueRegistry.DialogueCondition dialogueCondition = condition.codec().parse(JsonOps.INSTANCE, new JsonObject()).result().get();
                conditionConsumer.accept(dialogueCondition);
            } catch (Exception e) {
                throw new RuntimeException("Dialogue Condition not set up properly! Please make all fields Optional to allow for creation of an Instance");
            }
        });
    }

    public void clearConditionMaps() {
        forEachCondition((dialogueCondition) -> {
            if (dialogueCondition instanceof DialogueRegistry.CompiledCondition<?> compiledCondition) {
                compiledCondition.getMap().clear();
                compiledCondition.getTagMap().clear();
            }
            if (dialogueCondition instanceof DialogueRegistry.ListCondition listCondition)
                listCondition.getList().clear();
        });
    }

    public void compileConditionMaps(Void data) {
        for (ResourceKey<Registry<DialoguePool>> registryKey : REGISTRIES) {
            Registry<DialoguePool> dialoguePools =
                registryAccess.registryOrThrow(registryKey);
            dialoguePools.forEach((dialoguePool) -> {
                if (dialoguePool.condition().isPresent() && dialoguePool.condition().get().validate(registryKey)) {
                    DialogueRegistry.DialogueCondition condition = dialoguePool.condition().get();
                    if (condition instanceof DialogueRegistry.CompiledCondition<?> compiledCondition)
                        compiledCondition.consume(dialoguePool);
                    if (condition instanceof DialogueRegistry.ListCondition listCondition)
                        listCondition.append(dialoguePool);
                }
            });
        }
    }

    @Override
    public CompletableFuture<Void> reload(
        PreparationBarrier preparationBarrier, ResourceManager resourceManager,
        ProfilerFiller profilerFiller, ProfilerFiller profilerFiller1, Executor executor, Executor executor1
    ) {
        return CompletableFuture.runAsync(this::clearConditionMaps, executor)
            .thenCompose(preparationBarrier::wait)
            .thenAcceptAsync(this::compileConditionMaps, executor1);
    }
}
