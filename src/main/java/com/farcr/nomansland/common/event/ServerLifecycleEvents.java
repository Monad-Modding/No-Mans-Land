package com.farcr.nomansland.common.event;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.renderer.dreams.ClientDreamRenderer;
import com.farcr.nomansland.common.commands.DreamCommand;
import com.farcr.nomansland.common.commands.MeetingPointCommand;
import com.farcr.nomansland.common.commands.SunDogCommand;
import com.farcr.nomansland.common.dreams.DreamManager;
import com.farcr.nomansland.common.dreams.dreamlevel.DreamingPlayer;
import com.farcr.nomansland.common.entity.buddy.BuddyChunkAnchor;
import com.farcr.nomansland.common.friend.FriendMoon;
import com.farcr.nomansland.common.friend.condition.DialogueConditionCompiler;
import com.farcr.nomansland.common.friend.dialogue.DialogueTracker;
import com.farcr.nomansland.common.handler.InvertedBellServerHandler;
import com.farcr.nomansland.common.networking.*;
import com.farcr.nomansland.common.networking.alchemist_tools.*;
import com.farcr.nomansland.common.networking.dialogue.ClientboundDialogueRegistrySyncPacket;
import com.farcr.nomansland.common.networking.dream.ClientboundDimensionSyncPacket;
import com.farcr.nomansland.common.registry.worldgen.NMLBiomes;
import com.farcr.nomansland.common.world.densityfunction.LazilyCachedDensityFunctionSeedifier;
import com.farcr.nomansland.common.world.saved_data.RegeneratingPotsData;
import com.farcr.nomansland.common.worldevent.SunDog;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = NoMansLand.MODID)
public class ServerLifecycleEvents {
    @SubscribeEvent
    public static void onServerStart(ServerAboutToStartEvent event) {
        NMLBiomes.CAVES_HOLDER = event.getServer().registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(NMLBiomes.CAVES);
        NMLBiomes.CAVE_DEPTHS_HOLDER = event.getServer().registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(NMLBiomes.CAVE_DEPTHS);
        NMLBiomes.ALCHEMIST_RUINS_HOLDER = event.getServer().registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(NMLBiomes.ALCHEMIST_RUINS);
    }

    @SubscribeEvent
    public static void onLevelSave(LevelEvent.Save event) {
        if (event.getLevel() instanceof ServerLevel serverLevel)
            BuddyChunkAnchor.getOrDefault(serverLevel).drainQueuedAnchors();
    }

    @SubscribeEvent
    public static void onServerStop(ServerStoppingEvent event) {
        LazilyCachedDensityFunctionSeedifier.clearCache();
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Pre event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            if (event.getLevel().equals(serverLevel.getServer().overworld()))
                FriendMoon.getOrDefault(serverLevel).tick();
            BuddyChunkAnchor.getOrDefault(serverLevel).drainQueuedAnchors();
            RegeneratingPotsData.getOrDefault(serverLevel).tick();
            SunDog.getOrDefault(serverLevel).tick();
            InvertedBellServerHandler.get(serverLevel).tick(serverLevel);
            DreamManager.getOrDefault(serverLevel.getServer())
                .updateFlaggedPlayers();
        } else {
            SunDog.Client.INSTANCE.tick();
            ClientDreamRenderer.getInstance().tick();
        }
    }

    @SubscribeEvent
    public static void registerListeners(RegisterCommandsEvent event) {
        DreamCommand.register(event.getDispatcher(), event.getBuildContext());
        SunDogCommand.register(event.getDispatcher());
        MeetingPointCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        MeetingPointCommand.applyPersistedOverride(event.getServer());
    }

    @SubscribeEvent
    public static void onReload(AddReloadListenerEvent event) {
        event.addListener(new DialogueConditionCompiler(event.getRegistryAccess()));
    }

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        event.getRelevantPlayers().forEach((player) -> {
            PacketDistributor.sendToPlayer(player, new ClientboundDialogueRegistrySyncPacket());
            DialogueTracker.getOrDefault(player.server).sync(player);
        });
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new ClientboundDimensionSyncPacket(serverPlayer.server.levelKeys()));
            SunDog.getOrDefault(serverPlayer.serverLevel()).informPlayerOfSunDogState(serverPlayer);
            if (FriendMoon.isFriendMoonDimension(serverPlayer.serverLevel()))
                FriendMoon.getOrDefault(serverPlayer.getServer().overworld()).playerSendShadowPacket(serverPlayer);
            DreamManager.getOrDefault(serverPlayer.getServer()).notifyClient(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            DreamManager manager = DreamManager.getOrDefault(event.getEntity().getServer());
            DreamingPlayer dreamingPlayer = manager.getDreamingPlayer(serverPlayer);
            if (dreamingPlayer != null) dreamingPlayer.discardTether(false);
        }
    }
}
