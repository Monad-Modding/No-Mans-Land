package com.farcr.nomansland.common.networking.dialogue;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.ClientDialogueTracker;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record ClientboundDialogueTrackerPacket(
    boolean heardAnyDialogue,
    Map<ResourceLocation, List<ResourceLocation>> heardByRegistry
) implements CustomPacketPayload {
    public static final StreamCodec<ByteBuf, ClientboundDialogueTrackerPacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL,
        ClientboundDialogueTrackerPacket::heardAnyDialogue,
        ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list())),
        ClientboundDialogueTrackerPacket::heardByRegistry,
        ClientboundDialogueTrackerPacket::new
    );

    public static final Type<ClientboundDialogueTrackerPacket> TYPE = new Type<>(NoMansLand.location("client/dialogue/tracker"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handleData(final IPayloadContext context) {
        if (context.flow().isClientbound()) {
            context.enqueueWork(() -> ClientDialogueTracker.acceptSync(heardAnyDialogue, heardByRegistry));
        }
    }
}
