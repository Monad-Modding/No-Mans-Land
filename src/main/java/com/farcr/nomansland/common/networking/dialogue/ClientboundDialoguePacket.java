package com.farcr.nomansland.common.networking.dialogue;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.renderer.DialogueRenderer;
import com.farcr.nomansland.common.friend.dialogue.DialogueContainer;
import com.farcr.nomansland.common.friend.dialogue.DialoguePool;
import com.farcr.nomansland.common.friend.dialogue.DialogueState;
import com.farcr.nomansland.common.friend.dialogue.DialogueUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Registry;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record ClientboundDialoguePacket(
        ResourceLocation resourceLocation,
        ResourceLocation registryLocation,
        Optional<UUID> playerUUID,
        Integer timing
) implements CustomPacketPayload {
    public static final StreamCodec<ByteBuf, ClientboundDialoguePacket> STREAM_CODEC = StreamCodec.composite(
        ResourceLocation.STREAM_CODEC,
        ClientboundDialoguePacket::resourceLocation,
        ResourceLocation.STREAM_CODEC,
        ClientboundDialoguePacket::registryLocation,
        ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC),
        ClientboundDialoguePacket::playerUUID,
        ByteBufCodecs.INT,
        ClientboundDialoguePacket::timing,
        ClientboundDialoguePacket::new
    );

    private static final int MIN_TICKS = 100;
    private static final int MAX_TICKS = 160;
    public static ClientboundDialoguePacket timedDialoguePacket(
        ResourceLocation resourceLocation, ResourceLocation registryLocation, Optional<UUID> playerUUID, RandomSource randomSource
    ) {
        return new ClientboundDialoguePacket(
            resourceLocation, registryLocation, playerUUID,
            randomSource.nextInt(MIN_TICKS, MAX_TICKS)
        );
    }

    // moved the dialogue length here because the packet needs to know how long the dialogue is now
    public int getDialogueLengthTicks(Level level) {
        ResourceKey<Registry<DialoguePool>> tempKey = ResourceKey.createRegistryKey(registryLocation);
        Registry<DialoguePool> dialogueRegistry = DialogueUtil.getDialogueRegistry(level, tempKey);
        DialogueContainer dialogueContainer = new DialogueContainer(Objects.requireNonNull(dialogueRegistry.get(resourceLocation)).text());
        return (int) (dialogueContainer.getTextLength() / (DialogueState.DIALOGUE_SPEED)) + timing;
    }

    public static final CustomPacketPayload.Type<ClientboundDialoguePacket> TYPE = new CustomPacketPayload.Type<>(NoMansLand.location("client/dialogue/update"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void applyPacket(Level level, Player player) {
        ResourceKey<Registry<DialoguePool>> tempKey = ResourceKey.createRegistryKey(registryLocation);
        Registry<DialoguePool> dialogueRegistry = DialogueUtil.getDialogueRegistry(level, tempKey);
        DialoguePool dialoguePool = dialogueRegistry.get(resourceLocation);

        // Set Dialogue
        assert dialoguePool != null;
        DialogueRenderer.setCurrentState(new DialogueState(
            resourceLocation, tempKey.location().getPath().replace("/", "."), dialoguePool));
        if (playerUUID.isPresent()) {
            Player targetPlayer = level.getPlayerByUUID(playerUUID.get());
            if (targetPlayer != null) {
                DialogueRenderer.getCurrentState().translateDialogue.setPlayerName(
                    targetPlayer.getName().getString()
                );
            }
        }
        DialogueRenderer.getCurrentState().setTicks(getDialogueLengthTicks(level));
    }

    public void handleData(final IPayloadContext context) {
        if (context.flow().isClientbound()) {
            context.enqueueWork(() -> {
                Player player = context.player();
                Level level = player.level();
                applyPacket(level, player);
            });
        }
    }
}