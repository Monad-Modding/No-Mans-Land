package com.farcr.nomansland.common.friend.dialogue;

import com.farcr.nomansland.common.networking.dialogue.ClientboundDialoguePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/*
* Confusingly named, just exists because I need to be able to store resource and registry locations in one object
* as well as operate on it
*/
public class DialogueLocation {
    private final ResourceLocation dialogueLocation;
    private final ResourceLocation registryLocation;
    private final RandomSource randomSource;

    public DialogueLocation(
        ResourceLocation dialogueLocation,
        ResourceLocation registryLocation,
        RandomSource randomSource
    ) {
       this.dialogueLocation = dialogueLocation;
       this.registryLocation = registryLocation;
       this.randomSource = randomSource;
    }

    private UUID targetPlayer;
    // Provides a player to be the focus of the dialogue, as opposed to any player.
    public DialogueLocation setTargetPlayer(ServerPlayer serverPlayer) {
        if (serverPlayer != null) targetPlayer = serverPlayer.getUUID();
        return this;
    }

    private ClientboundDialoguePacket createPacket() {
        return ClientboundDialoguePacket.timedDialoguePacket(
            dialogueLocation, registryLocation,
            Optional.ofNullable(targetPlayer),
            randomSource
        );
    }

    // Dispatch returns the length of the dialogue for the server to operate on, but it isn't forced to use it
    public int dispatch(Level level, ServerPlayer player) {
        if (dialogueLocation == null) return 0;
        ClientboundDialoguePacket clientPacket = createPacket();
        PacketDistributor.sendToPlayer(player, clientPacket);
        markHeard(level, player);
        return clientPacket.getDialogueLengthTicks(level);
    }

    public int dispatch(Level level, List<ServerPlayer> playerList) {
        if (dialogueLocation == null)
            return 0;
        ClientboundDialoguePacket clientPacket = createPacket();
        for (ServerPlayer player : playerList) {
            PacketDistributor.sendToPlayer(player, clientPacket);
            markHeard(level, player);
        }
        return clientPacket.getDialogueLengthTicks(level);
    }

    private void markHeard(Level level, ServerPlayer player) {
        if (level instanceof ServerLevel serverLevel)
            DialogueTracker.getOrDefault(serverLevel.getServer()).markDialogueHeard(player, registryLocation, dialogueLocation);
    }
}
