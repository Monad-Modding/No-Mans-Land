package com.farcr.nomansland.common.friend.dialogue;

import com.farcr.nomansland.common.networking.dialogue.ClientboundDialogueTrackerPacket;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class DialogueTracker extends SavedData {
    public static final String NAME = "dialogue_tracker";

    public static DialogueTracker getOrDefault(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(new SavedData.Factory<>(
            DialogueTracker::new,
            (tag, provider) -> new DialogueTracker().load(tag)
        ), NAME);
    }

    public static class PlayerDialogueData {
        public boolean heardAnyDialogue = false;
        public final Map<ResourceLocation, Set<ResourceLocation>> heardByRegistry = new HashMap<>();
    }

    private final Map<UUID, PlayerDialogueData> playerData = new HashMap<>();

    public PlayerDialogueData getData(UUID playerUUID) {
        return playerData.computeIfAbsent(playerUUID, (uuid) -> new PlayerDialogueData());
    }

    public boolean hasHeardFrom(UUID playerUUID, ResourceLocation registryLocation) {
        PlayerDialogueData data = playerData.get(playerUUID);
        return data != null && !data.heardByRegistry.getOrDefault(registryLocation, Set.of()).isEmpty();
    }

    public void markDialogueHeard(ServerPlayer player, ResourceLocation registryLocation, ResourceLocation dialogueLocation) {
        PlayerDialogueData data = getData(player.getUUID());
        boolean changed = !data.heardAnyDialogue;
        data.heardAnyDialogue = true;
        if (dialogueLocation != null)
            changed |= data.heardByRegistry.computeIfAbsent(registryLocation, (key) -> new HashSet<>()).add(dialogueLocation);
        if (changed) {
            setDirty();
            sync(player);
        }
    }

    public void sync(ServerPlayer player) {
        PlayerDialogueData data = getData(player.getUUID());
        Map<ResourceLocation, List<ResourceLocation>> heard = new HashMap<>();
        data.heardByRegistry.forEach((registry, dialogues) -> heard.put(registry, new ArrayList<>(dialogues)));
        PacketDistributor.sendToPlayer(player, new ClientboundDialogueTrackerPacket(data.heardAnyDialogue, heard));
    }

    public DialogueTracker load(CompoundTag tag) {
        playerData.clear();
        for (Tag entryTag : tag.getList("Players", Tag.TAG_COMPOUND)) {
            if (!(entryTag instanceof CompoundTag playerTag) || !playerTag.contains("Player"))
                continue;
            PlayerDialogueData data = new PlayerDialogueData();
            data.heardAnyDialogue = playerTag.getBoolean("HeardAnyDialogue");
            CompoundTag heardTag = playerTag.getCompound("DialoguesHeard");
            for (String registryKey : heardTag.getAllKeys()) {
                ResourceLocation registry = ResourceLocation.tryParse(registryKey);
                if (registry == null) continue;
                Set<ResourceLocation> dialogues = new HashSet<>();
                for (Tag dialogueTag : heardTag.getList(registryKey, Tag.TAG_STRING)) {
                    ResourceLocation dialogue = ResourceLocation.tryParse(dialogueTag.getAsString());
                    if (dialogue != null) dialogues.add(dialogue);
                }
                data.heardByRegistry.put(registry, dialogues);
            }
            playerData.put(NbtUtils.loadUUID(playerTag.get("Player")), data);
        }
        return this;
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag playersTag = new ListTag();
        playerData.forEach((uuid, data) -> {
            CompoundTag playerTag = new CompoundTag();
            playerTag.put("Player", NbtUtils.createUUID(uuid));
            playerTag.putBoolean("HeardAnyDialogue", data.heardAnyDialogue);
            CompoundTag heardTag = new CompoundTag();
            data.heardByRegistry.forEach((registry, dialogues) -> {
                ListTag dialogueList = new ListTag();
                dialogues.forEach((dialogue) -> dialogueList.add(StringTag.valueOf(dialogue.toString())));
                heardTag.put(registry.toString(), dialogueList);
            });
            playerTag.put("DialoguesHeard", heardTag);
            playersTag.add(playerTag);
        });
        tag.put("Players", playersTag);
        return tag;
    }
}
