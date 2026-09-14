package com.farcr.nomansland.common.event;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.networking.*;
import com.farcr.nomansland.common.networking.alchemist_tools.*;
import com.farcr.nomansland.common.networking.buddy.ClientboundBuddyCrouchPacket;
import com.farcr.nomansland.common.networking.dialogue.ClientboundDialoguePacket;
import com.farcr.nomansland.common.networking.dialogue.ClientboundDialogueRegistrySyncPacket;
import com.farcr.nomansland.common.networking.dialogue.ClientboundDialogueResetPacket;
import com.farcr.nomansland.common.networking.dialogue.ClientboundDialogueTrackerPacket;
import com.farcr.nomansland.common.networking.dream.ClientboundDimensionSyncPacket;
import com.farcr.nomansland.common.networking.dream.ClientboundDreamPacket;
import com.farcr.nomansland.common.networking.dream.ServerboundDreamAcknowledgePacket;
import com.farcr.nomansland.common.networking.friend.ClientboundMeetingPointPacket;
import com.farcr.nomansland.common.networking.friend.ClientboundMoonlightBasinTrackPacket;
import com.farcr.nomansland.common.networking.friend.FriendMoonUpdatePacket;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = NoMansLand.MODID)
public class NetworkSetupEvents {
    @SubscribeEvent
    public static void registerPackets(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");


        //Ominous Moose Behavior that sadly demands our own networking
        registrar.playToServer(ServerboundMooseBeginJumpSequencePacket.TYPE, ServerboundMooseBeginJumpSequencePacket.STREAM_CODEC, ServerboundMooseBeginJumpSequencePacket::handleData);

        // Dialogue Packet from Server
        registrar.playToClient(ClientboundDialoguePacket.TYPE, ClientboundDialoguePacket.STREAM_CODEC, ClientboundDialoguePacket::handleData);
        registrar.playToClient(ClientboundDialogueResetPacket.TYPE, ClientboundDialogueResetPacket.STREAM_CODEC, ClientboundDialogueResetPacket::handleData);
        registrar.playToClient(ClientboundDialogueRegistrySyncPacket.TYPE, ClientboundDialogueRegistrySyncPacket.STREAM_CODEC, ClientboundDialogueRegistrySyncPacket::handleData);
        registrar.playToClient(ClientboundDialogueTrackerPacket.TYPE, ClientboundDialogueTrackerPacket.STREAM_CODEC, ClientboundDialogueTrackerPacket::handleData);

        // Friend Moon related packets
        registrar.playToServer(FriendMoonUpdatePacket.ToServer.TYPE, FriendMoonUpdatePacket.ToServer.STREAM_CODEC, FriendMoonUpdatePacket.ToServer::handleData);
        registrar.playToClient(FriendMoonUpdatePacket.ToClient.TYPE, FriendMoonUpdatePacket.ToClient.STREAM_CODEC, FriendMoonUpdatePacket.ToClient::handleData);

        registrar.playToClient(ClientboundMoonlightBasinTrackPacket.TYPE, ClientboundMoonlightBasinTrackPacket.STREAM_CODEC, ClientboundMoonlightBasinTrackPacket::handleData);
        registrar.playToClient(ClientboundMeetingPointPacket.TYPE, ClientboundMeetingPointPacket.STREAM_CODEC, ClientboundMeetingPointPacket::handleData);
        registrar.playToClient(ClientboundCandleLightPacket.TYPE, ClientboundCandleLightPacket.STREAM_CODEC, ClientboundCandleLightPacket::handleData);

        /* Dream Packets */
        registrar.playToClient(ClientboundDreamPacket.TYPE, ClientboundDreamPacket.STREAM_CODEC, ClientboundDreamPacket::handleData);
        registrar.playToClient(ClientboundDimensionSyncPacket.TYPE, ClientboundDimensionSyncPacket.STREAM_CODEC, ClientboundDimensionSyncPacket::handleData);
        registrar.playToServer(ServerboundDreamAcknowledgePacket.TYPE, ServerboundDreamAcknowledgePacket.STREAM_CODEC, ServerboundDreamAcknowledgePacket::handleData);

        registrar.playToClient(ClientboundBuddyCrouchPacket.TYPE, ClientboundBuddyCrouchPacket.STREAM_CODEC, ClientboundBuddyCrouchPacket::handleData);

        registrar.playToClient(ClientboundZoomEffectPacket.TYPE, ClientboundZoomEffectPacket.STREAM_CODEC, ClientboundZoomEffectPacket::handleData);

        // sun dog update packet
        registrar.playToClient(ClientboundSunDogStatePacket.TYPE, ClientboundSunDogStatePacket.STREAM_CODEC, ClientboundSunDogStatePacket::handleData);

        registrar.playToClient(ClientboundInvertedBellPacket.TYPE, ClientboundInvertedBellPacket.STREAM_CODEC, ClientboundInvertedBellPacket::handleData);
        registrar.playToClient(ClientboundDistantChunkPacket.TYPE, ClientboundDistantChunkPacket.STREAM_CODEC, ClientboundDistantChunkPacket::handleData);

        registrar.playToClient(ClientboundBandageSoundPacket.TYPE, ClientboundBandageSoundPacket.STREAM_CODEC, ClientboundBandageSoundPacket::handleData);

        // ancestral oath sword packets
        registrar.playToClient(ClientboundOathSwordAnimate.TYPE, ClientboundOathSwordAnimate.STREAM_CODEC, ClientboundOathSwordAnimate::handleData);
        registrar.playToClient(ClientboundOathSwordParry.TYPE, ClientboundOathSwordParry.STREAM_CODEC, ClientboundOathSwordParry::handleData);
        registrar.playToServer(ServerboundOathSwordAnimate.TYPE, ServerboundOathSwordAnimate.STREAM_CODEC, ServerboundOathSwordAnimate::handleData);

        // ritual pickaxe packets
        registrar.playToServer(ServerboundRitualPickRequestPacket.TYPE, ServerboundRitualPickRequestPacket.STREAM_CODEC, ServerboundRitualPickRequestPacket::handleData);
        registrar.playToClient(ClientboundRitualPickResponsePacket.TYPE, ClientboundRitualPickResponsePacket.STREAM_CODEC, ClientboundRitualPickResponsePacket::handleData);
        registrar.playToServer(ServerBoundChiselPacket.TYPE, ServerBoundChiselPacket.STREAM_CODEC, ServerBoundChiselPacket::handleData);
    }
}
