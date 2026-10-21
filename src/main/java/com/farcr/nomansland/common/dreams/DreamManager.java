package com.farcr.nomansland.common.dreams;

import com.farcr.nomansland.client.renderer.dreams.ClientDreamRenderer;
import com.farcr.nomansland.common.dreams.dreamlevel.DreamLevelHandler;
import com.farcr.nomansland.common.dreams.dreamlevel.DreamServerLevel;
import com.farcr.nomansland.common.dreams.dreamlevel.DreamingPlayer;
import com.farcr.nomansland.common.networking.dream.ClientboundDreamPacket;
import com.farcr.nomansland.common.registry.NMLDreamTypes;
import com.farcr.nomansland.common.registry.NMLRegistries;
import com.farcr.nomansland.common.registry.entities.NMLEntities;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiFunction;

/*
* Wanted to write more generalized code for this, maybe some of the specifics can be adapted later
* but for now this only needs to be used for one thing, the Friend Moon Dream
 */
public class DreamManager extends SavedData {
    public static final String NAME = "dream_manager";
    private final MinecraftServer server;
    public DreamManager(MinecraftServer server) {
        this.server = server;
    }

    public static DreamManager getOrDefault(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(new SavedData.Factory<>(() -> new DreamManager(server),
            (tag, provider) -> DreamManager.create(tag, provider, server)
        ), DreamManager.NAME);
    }

    Map<UUID, DreamStorage> storageMap = new HashMap<>();
    public DreamStorage getPlayerStorage(ServerPlayer player) {
        UUID uuid = player.getUUID();
        if (!storageMap.containsKey(uuid)) storageMap.put(uuid, new DreamStorage());
        return storageMap.get(uuid);
    }

    public Optional<DreamStorage> getPlayerStorageOptional(ServerPlayer player) {
        if (!storageMap.containsKey(player.getUUID()))
            return Optional.empty();
        return Optional.of(getPlayerStorage(player));
    }

    public DreamStorage clearPlayerStorage(ServerPlayer player, DreamType dreamType) {
        DreamStorage storage = storageMap.get(player.getUUID());
        if (storageMap.containsKey(player.getUUID())
        && storage.removeInformationAboutDream(dreamType)) {
            setDirty();
            return storage;
        }
        return null;
    }

    // eventually this will be changed to (player, dream)
    public boolean playerHasExperiencedDream(ServerPlayer player, DreamType dreamType) {
        if (!storageMap.containsKey(player.getUUID())) return false;
        return getPlayerStorage(player).getHasExperiencedDream(dreamType);
    }

    // should not be serialized or stored as when the server starts unloading all players should return to their dreaming players
    private final Map<UUID, DreamingPlayer> dreamerMap = new HashMap<>();
    public int getDreamingPlayerCount() {
        int i = 0;
        for (UUID uuid : dreamerMap.keySet()) {
            ServerPlayer player = server.getPlayerList().getPlayer(uuid);
            if (player == null)
                continue;
            if (getDreamingPlayer(player) == null)
                continue;
            i++;
        }
        return i;
    }

    public static boolean IGNORE_UPDATE_CONTEXT = false;
    public void createDreamingPlayer(ServerPlayer serverPlayer) {
        UUID playerUUID = serverPlayer.getUUID();
        if (!dreamerMap.containsKey(playerUUID)
        || (dreamerMap.get(playerUUID) == null)
        || (!dreamerMap.get(playerUUID).isAlive())) {
            ServerLevel level = serverPlayer.serverLevel();
            DreamingPlayer dreamPlayer = NMLEntities.DREAMING_PLAYER.get().create(level);
            dreamPlayer.setTetheredPlayer(serverPlayer);
            dreamPlayer.addChunkTicket(level);
            level.addFreshEntity(dreamPlayer);
            dreamerMap.put(playerUUID, dreamPlayer);
        }
    }

    public DreamingPlayer getDreamingPlayer(ServerPlayer player) {
        DreamingPlayer dreamingPlayer = dreamerMap.get(player.getUUID());
        if (dreamingPlayer != null && dreamingPlayer.isAlive()) return dreamingPlayer;
        return null;
    }

    public boolean transferSleep(ServerPlayer player, DreamType dreamType) {
        DreamServerLevel dreamLevel = DreamLevelHandler.getDreamLevel(player.server, dreamType, player);
        if (DreamLevelHandler.getInstance().playerIsUpdated(player)) {
            dreamLevel.regenerateDreamInstance();

            // summon fake player
            createDreamingPlayer(player);

            // suppress updating the player sleep counter
            IGNORE_UPDATE_CONTEXT = true;

            // move player to other dimension
            player.stopSleeping();
            player.changeDimension(
                new DimensionTransition(
                    dreamLevel, dreamType.spawnPoint,
                    dreamType.spawnPoint, 0, 0,
                    DimensionTransition.DO_NOTHING
                )
            );
            return true;
        }
        return false;
    }

    public void notifyClient(ServerPlayer player) {
        DreamType dreamType = playerGetDream(player);
        if (player.isSleeping()) forceNotify(dreamType, player);
    }

    public void forceNotify(DreamType dreamType, ServerPlayer player) {
        if (dreamType != null) DreamLevelHandler.getDreamLevel(player.server, dreamType, player);
        PacketDistributor.sendToPlayer(player, new ClientboundDreamPacket(
            Optional.ofNullable(NMLRegistries.DREAM_TYPE.getKey(dreamType))
        ));
    }
    private Map<ServerPlayer, DreamType> flaggedPlayerMap = new HashMap<>();
    public void flagForStart(DreamType dreamType, ServerPlayer player) {
        flaggedPlayerMap.put(player, dreamType);
    }

    public void updateFlaggedPlayers() {
        List<ServerPlayer> flagForDeletion = new ArrayList<>();
        for (ServerPlayer player : flaggedPlayerMap.keySet()) {
            boolean transferedSleep = transferSleep(player, flaggedPlayerMap.get(player));
            if (transferedSleep) {
                getDreamingPlayer(player).debug = true;
                flagForDeletion.add(player);
            }
        }
        for (ServerPlayer player : flagForDeletion)
            flaggedPlayerMap.remove(player);
    }

    List<DreamType> instanceList = NMLDreamTypes.DREAM_TYPES_REGISTRY.getRegistry().get().stream().toList();
    public DreamType playerGetDream(ServerPlayer player) {
        for (DreamType dreamType : instanceList) {
            BiFunction<ServerPlayer, ServerLevel, Boolean> function = dreamType.biconsumer;
            if (function != null && !playerHasExperiencedDream(player, dreamType) && function.apply(player, player.serverLevel()))
                return dreamType;
        }
        return null;
    }

    public static boolean innerDreaming(DreamType dreamType, Player player) {
        return player.level().dimension().equals(
            DreamLevelHandler.resourceKey(Registries.DIMENSION,
                NMLDreamTypes.DREAM_TYPES_REGISTRY.getRegistry()
                    .get().getKey(dreamType),
                player
            )
        );
    }

    public static DreamType.DreamTypeInstance getAmbiguousDreamTypeInstance(Player player) {
        if (player instanceof ServerPlayer serverPlayer && DreamManager.getOrDefault(serverPlayer.getServer()).playerIsDreaming(serverPlayer))
            return DreamLevelHandler.getDreamLevel(player.getServer(), DreamManager.getOrDefault(player.getServer())
                    .playerGetDream(serverPlayer), serverPlayer).getDreamTypeInstance();
        if (player.isLocalPlayer() && ClientDreamRenderer.getInstance().dreamShouldRender())
            return ClientDreamRenderer.getInstance().getDreamClientInstance();
        return null;
    }

    public static boolean getPlayerShouldDream(Player player) {
        boolean additionalCondition = player.isSleepingLongEnough()
            && (getAmbiguousDreamTypeInstance(player) == null); // hasnt entered dream yet
        if (player instanceof ServerPlayer serverPlayer)
            return DreamManager.getOrDefault(serverPlayer.getServer()).playerShouldDream(serverPlayer)
                && additionalCondition;
        if (player.isLocalPlayer()) return (ClientDreamRenderer.getInstance().getDream() != null)
            && additionalCondition;
        return false;
    }

    public static DreamType getAmbiguousDreamType(Player player) {
        if (player instanceof ServerPlayer serverPlayer && DreamManager.getOrDefault(serverPlayer.getServer()).playerIsDreaming(serverPlayer))
            return DreamManager.getOrDefault(serverPlayer.getServer()).playerGetDream(serverPlayer);
        if (player.isLocalPlayer() && ClientDreamRenderer.getInstance().dreamShouldRender())
            return ClientDreamRenderer.getInstance().getDream();
        return null;
    }

    public boolean playerIsDreaming(ServerPlayer player) {
        DreamType dreamType = playerGetDream(player);
        if (dreamType == null)
            return false;
        return innerDreaming(dreamType, player);
    }

    public boolean playerShouldDream(ServerPlayer player) {
        return (playerGetDream(player) != null);
    }

    public static DreamManager create(CompoundTag tag, HolderLookup.Provider provider, MinecraftServer server) {
        return new DreamManager(server).load(tag, provider);
    }

    public DreamManager load(CompoundTag compoundTag, HolderLookup.Provider provider) {
        storageMap.clear();
        for (Tag tag : compoundTag.getList("Players", 10)) {
            if (tag instanceof CompoundTag playerDataTag) {
                storageMap.put(
                    playerDataTag.getUUID("UUID"),
                    DreamStorage.CODEC.parse(NbtOps.INSTANCE,
                        playerDataTag.get("DreamStorage")).getOrThrow()
                );
            }
        }
        return this;
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider provider) {
        ListTag listTag = new ListTag();
        storageMap.forEach((playerUUID, dreamInfo) -> {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("UUID", playerUUID);
            tag.put("DreamStorage", DreamStorage.CODEC.encodeStart(NbtOps.INSTANCE, dreamInfo).getOrThrow());
            listTag.add(tag);
        });
        compoundTag.put("Players", listTag);
        return compoundTag;
    }

    public void setDreamExperienced(DreamType dreamType, ServerPlayer player) {
        getPlayerStorage(player).setDreamExperienced(dreamType);
        setDirty();
    }
}
