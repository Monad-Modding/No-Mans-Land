package com.farcr.nomansland.client;

import com.farcr.nomansland.common.friend.dialogue.DialogueUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ClientDialogueTracker {
    private ClientDialogueTracker() {}

    private static boolean heardAnyDialogue = false;
    private static final Map<ResourceLocation, Set<ResourceLocation>> heardByRegistry = new HashMap<>();

    public static void acceptSync(boolean heardAny, Map<ResourceLocation, List<ResourceLocation>> heard) {
        heardAnyDialogue = heardAny;
        heardByRegistry.clear();
        heard.forEach((registry, dialogues) -> heardByRegistry.put(registry, new HashSet<>(dialogues)));
    }

    public static boolean hasHeardAnyDialogue() {
        return heardAnyDialogue;
    }

    public static boolean hasHeardDialogue(ResourceLocation registryLocation, ResourceLocation dialogueLocation) {
        Set<ResourceLocation> dialogues = heardByRegistry.get(registryLocation);
        return dialogues != null && dialogues.contains(dialogueLocation);
    }

    public static List<ResourceLocation> getOfferingDialoguesForItem(Item item) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null)
            return List.of();
        return DialogueUtil.getOfferingDialogueLocations(minecraft.level.registryAccess(), item);
    }

    public static List<ResourceLocation> getOfferingDialoguesForEntity(EntityType<?> entityType) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null)
            return List.of();
        return DialogueUtil.getOfferingDialogueLocations(minecraft.level.registryAccess(), entityType);
    }
}
