package com.farcr.nomansland.common.dreams.dreamlevel;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.dreams.DreamType;
import com.farcr.nomansland.common.extension.MinecraftServerExtension;
import com.farcr.nomansland.common.extension.PlayerExtension;
import com.farcr.nomansland.common.networking.dream.ClientboundDimensionSyncPacket;
import com.farcr.nomansland.common.registry.NMLDreamTypes;
import com.farcr.nomansland.common.registry.worldgen.NMLBiomes;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess;
import net.minecraft.world.level.storage.WorldData;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.Executor;

public class DreamLevelHandler implements AutoCloseable {
    private static DreamLevelHandler INSTANCE;
    public static DreamLevelHandler getInstance() {
        if (INSTANCE == null)
            INSTANCE = new DreamLevelHandler();
        return INSTANCE;
    }

    public static void destroy() {
        INSTANCE.close();
        INSTANCE = null;
    }

    @Override
    public void close() {}

    public static <T> ResourceKey<T> resourceKey(ResourceKey<? extends Registry<T>> resourceKey, ResourceLocation dreamLocation, Player player) {
        return ResourceKey.create(resourceKey,
            ResourceLocation.fromNamespaceAndPath(
                dreamLocation.getNamespace(),
                dreamLocation.getPath() + "_" + player.getStringUUID()
            )
        );
    }
    public static <T> ResourceKey<T> resourceKeyNoPlayer(ResourceKey<? extends Registry<T>> resourceKey, ResourceLocation dreamLocation) {
        return ResourceKey.create(resourceKey,
            ResourceLocation.fromNamespaceAndPath(
                dreamLocation.getNamespace(),
                dreamLocation.getPath()
            )
        );
    }

    public static Optional<DreamType> keyToDream(ResourceKey<Level> resourceKey) {
        Registry<DreamType> dreamRegistry = NMLDreamTypes.DREAM_TYPES_REGISTRY.getRegistry().get();
        return dreamRegistry.stream().filter(
            (dreamType) -> resourceKey.location()
                .getPath().contains(dreamRegistry.getKey(dreamType).getPath())
        ).findFirst();
    }

    private static void logFallback(ServerPlayer serverPlayer) {
        NoMansLand.LOGGER.info(
            "Teleporting " + serverPlayer.getGameProfile().getName() +
            " from a Dream to last valid point as a last resort! Did the server crash previously?"
        );
    }

    public static BlockPos fallbackPosition(ServerPlayer serverPlayer) {
        MinecraftServer server = serverPlayer.getServer();
        PlayerExtension playerExtension = (PlayerExtension) serverPlayer;
        BlockPos respawnPosition = playerExtension.nml$getLastSleepPosition();
        if (respawnPosition == null) respawnPosition = serverPlayer.getRespawnPosition();
        if (respawnPosition == null) respawnPosition = server.overworld().getSharedSpawnPos();
        return respawnPosition;
    }

    public static ServerLevel fallbackDimension(ServerPlayer serverPlayer) {
        MinecraftServer server = serverPlayer.getServer();
        PlayerExtension playerExtension = (PlayerExtension) serverPlayer;

        ServerLevel respawnDimension = server.getLevel(serverPlayer.getRespawnDimension());
        if (playerExtension.nml$getLastSleepDimension() != null) {
            ServerLevel storedRespawnDimension = server.getLevel(playerExtension.nml$getLastSleepDimension());
            if (storedRespawnDimension != null) respawnDimension = storedRespawnDimension;
        }
        if (respawnDimension == null || keyToDream(respawnDimension.dimension()).isPresent())
            respawnDimension = server.overworld();
        return respawnDimension;
    }

    public static ServerLevel playerLoadFallback(ServerPlayer serverPlayer) {
        logFallback(serverPlayer);
        serverPlayer.setPos(fallbackPosition(serverPlayer).getCenter());
        return fallbackDimension(serverPlayer);
    }

    public static void playerTeleportFallback(ServerPlayer serverPlayer) {
        logFallback(serverPlayer);
        BlockPos respawnPosition = fallbackPosition(serverPlayer);
        serverPlayer.changeDimension(new DimensionTransition(
            fallbackDimension(serverPlayer),
            respawnPosition.getCenter(),
            respawnPosition.getCenter(),
            0, 0,
            DimensionTransition.DO_NOTHING
        ));
    }

    public final List<UUID> dirtyClients = new ArrayList<>();
    public boolean playerIsUpdated(ServerPlayer player) {
        return !dirtyClients.contains(player.getUUID());
    }

    public void playerHasReceivedPacket(Player player) {
        dirtyClients.remove(player.getUUID());
    }

    public static DreamServerLevel getDreamLevel(MinecraftServer server, DreamType dreamType, ServerPlayer serverPlayer) {
        ResourceLocation dreamLocation = NMLDreamTypes.DREAM_TYPES_REGISTRY.getRegistry().get().getKey(dreamType);
        ResourceKey<Level> dreamKey = resourceKey(Registries.DIMENSION, dreamLocation, serverPlayer);

        Map<ResourceKey<Level>, ServerLevel> levelList = ((MinecraftServerExtension) server).nml$getLevelList();
        Executor executor = ((MinecraftServerExtension) server).nml$getExecutor();
        LevelStorageAccess storageAccess = ((MinecraftServerExtension) server).nml$getLevelStorageAccess();

        if (!levelList.containsKey(dreamKey)) {
            ServerLevel overworld = server.overworld();
            BiomeSource biomeSource = new FixedBiomeSource(
                server.registryAccess().registryOrThrow(Registries.BIOME)
                    .getHolderOrThrow(NMLBiomes.DREAM)
            );

            WorldData worldData = server.getWorldData();
            ChunkProgressListener chunkprogresslistener = ((MinecraftServerExtension) server).nml$getProgressListener();
            Holder<DimensionType> dreamHolder = registerDimensionType(server.registryAccess(), dreamType, serverPlayer);

            DreamServerLevel newLevel = new DreamServerLevel(
                server, executor, storageAccess,
                new DerivedLevelData(worldData, worldData.overworldData()),
                dreamKey, new LevelStem(
                    dreamHolder, new DreamChunkGenerator(dreamType, biomeSource)
                ),
                chunkprogresslistener, worldData.isDebugWorld(),
                overworld.getSeed(), List.of(), false,
                overworld.getRandomSequences(), dreamType
            );

            if (dreamType.worldBorder > 0) newLevel.getWorldBorder().setSize(dreamType.worldBorder);

            levelList.put(dreamKey, newLevel);
            getInstance().getDHLevel(newLevel);

            // not entirely trustworthy
            // but https://github.com/Commoble/infiniverse/blob/main/src/main/java/net/commoble/infiniverse/internal/InfiniverseMod.java
            server.markWorldsDirty();

            // REMEMBER to tell players what the new dimension set is
            PacketDistributor.sendToAllPlayers(
                new ClientboundDimensionSyncPacket(server.levelKeys()));
            getInstance().dirtyClients.addAll(server.getPlayerList().getPlayers().stream().map(Player::getUUID).toList());
        }
        return (DreamServerLevel) levelList.get(dreamKey);
    }

    protected void getDHLevel(ServerLevel level) {
        if (!ModList.get().isLoaded("distanthorizons")) return;
        DHLevelWrapper.getDHLevel(level);
    }

    /*
    * Has to be separate, in case dreams need any of the dimensiontype
    * parameters as their own / to be modifiable !!!
    *
    * also has to be separate to register on both client and server !!!
    */
    public static Holder<DimensionType> registerDimensionType(
        RegistryAccess registryAccess, DreamType dreamType,
        Player player, @Nullable ResourceLocation optionalKey
    ) {
        Registry<DimensionType> dimensionRegistry =
            registryAccess.registryOrThrow(Registries.DIMENSION_TYPE);

        DimensionType dimensionType = new DimensionType(
            OptionalLong.of(18000),
            false, false, false, true,
            1d, false, false, 0,
            128, 128, BlockTags.INFINIBURN_OVERWORLD,
            BuiltinDimensionTypes.OVERWORLD_EFFECTS, 0.0F,
            new DimensionType.MonsterSettings(false, false, ConstantInt.of(0), 0)
        );

        ResourceLocation dreamLocation = NMLDreamTypes.DREAM_TYPES_REGISTRY.getRegistry().get().getKey(dreamType);
        ResourceKey<DimensionType> dimensionKey = resourceKey(Registries.DIMENSION_TYPE, dreamLocation, player);
        if (optionalKey != null) dimensionKey = resourceKeyNoPlayer(Registries.DIMENSION_TYPE, optionalKey);
        if (!dimensionRegistry.containsKey(dimensionKey) && dimensionRegistry instanceof MappedRegistry<DimensionType> writableRegistry) {
            writableRegistry.unfreeze();
            writableRegistry.register(dimensionKey, dimensionType,
                new RegistrationInfo(Optional.empty(), Lifecycle.stable())
            );
        }
        return dimensionRegistry.getHolderOrThrow(dimensionKey);
    }
    public static Holder<DimensionType> registerDimensionType(RegistryAccess registryAccess, DreamType dreamType, Player player) {
        return registerDimensionType(registryAccess, dreamType, player, null);
    }
}
