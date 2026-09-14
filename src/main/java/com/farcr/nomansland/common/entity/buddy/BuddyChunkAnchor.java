package com.farcr.nomansland.common.entity.buddy;

import com.farcr.nomansland.common.extension.LevelChunkExtension;
import com.farcr.nomansland.common.registry.entities.NMLEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BuddyChunkAnchor extends SavedData {
    private static final String NAME = "buddy_anchor";

    // Stores a list of retained values between buddy "respawning"
    public final Map<BlockPos, BuddyData> buddyAnchors = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<ResourceKey<Level>, ConcurrentLinkedQueue<BlockPos>> PENDING_ANCHORS = new ConcurrentHashMap<>();

    private final Set<BlockPos> pendingAnchors = ConcurrentHashMap.newKeySet();

    public static void queuePendingAnchor(ResourceKey<Level> dimension, BlockPos pos) {
        PENDING_ANCHORS.computeIfAbsent(dimension, k -> new ConcurrentLinkedQueue<>()).offer(pos);
    }

    public static BuddyChunkAnchor getOrDefault(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(new SavedData.Factory<>(
            () -> new BuddyChunkAnchor(level),
            (tag, provider) -> BuddyChunkAnchor.create(tag, provider, level)
        ), BuddyChunkAnchor.NAME);
    }

    public static BuddyChunkAnchor create(CompoundTag tag, HolderLookup.Provider provider, ServerLevel serverLevel) {
        BuddyChunkAnchor anchor = new BuddyChunkAnchor(serverLevel);
        return anchor.load(tag, provider);
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag listTag = new ListTag();
        for (Map.Entry<BlockPos, BuddyData> entry : buddyAnchors.entrySet()) {
            BlockPos pos = entry.getKey();
            BuddyData buddyData = entry.getValue();

            CompoundTag entryTag = new CompoundTag();
            entryTag.put("Pos", NbtUtils.writeBlockPos(pos));
            entryTag.put("BuddyData", BuddyData.CODEC.encodeStart(NbtOps.INSTANCE, buddyData).getOrThrow());

            listTag.add(entryTag);
        }
        tag.put("BuddySpawnAnchors", listTag);

        ListTag pendingTag = new ListTag();
        for (BlockPos pos : pendingAnchors) {
            CompoundTag posTag = new CompoundTag();
            posTag.put("Pos", NbtUtils.writeBlockPos(pos));
            pendingTag.add(posTag);
        }
        tag.put("PendingBuddyAnchors", pendingTag);
        return tag;
    }

    public BuddyChunkAnchor load(CompoundTag tag, HolderLookup.Provider provider) {
        buddyAnchors.clear();
        for (Tag entryTag : tag.getList("BuddySpawnAnchors", 10)) {
            if (entryTag instanceof CompoundTag dataTag) {
                BlockPos anchorPosition = NbtUtils.readBlockPos(dataTag, "Pos").orElseThrow();
                BuddyData buddyData = BuddyData.CODEC.parse(NbtOps.INSTANCE, dataTag.get("BuddyData")).getOrThrow();

                buddyAnchors.put(anchorPosition, buddyData);
            }
        }
        pendingAnchors.clear();
        for (Tag pendingTag : tag.getList("PendingBuddyAnchors", 10)) {
            if (pendingTag instanceof CompoundTag posTag)
                NbtUtils.readBlockPos(posTag, "Pos").ifPresent(pendingAnchors::add);
        }
        return this;
    }

    private final ServerLevel level;
    public BuddyChunkAnchor(ServerLevel level) {
        this.level = level;
    }

    public void tickChunk(LevelChunk chunk) {
        LevelChunkExtension extensionChunk = (LevelChunkExtension) chunk;
        if (extensionChunk.nml$shouldIgnoreBuddyAnchor()) return;

        ChunkPos chunkPos = chunk.getPos();
        boolean chunkHasAnchor = false;

        Iterator<BlockPos> pending = pendingAnchors.iterator();
        while (pending.hasNext()) {
            BlockPos pos = pending.next();
            if (chunkContains(chunkPos, pos)) {
                chunkHasAnchor = true;
                if (attemptSpawn(pos)) {
                    pending.remove();
                    setDirty();
                }
            }
        }

        for (Map.Entry<BlockPos, BuddyData> entry : buddyAnchors.entrySet()) {
            BlockPos anchor = entry.getKey();
            if (chunkContains(chunkPos, anchor)) {
                chunkHasAnchor = true;
                if (tryRespawning(entry.getValue())) {
                    attemptSpawn(anchor);
                }
            }
        }

        if (!chunkHasAnchor) {
            extensionChunk.nml$setIgnoreBuddyAnchor();
        }
    }

    public void drainQueuedAnchors() {
        ConcurrentLinkedQueue<BlockPos> queued = PENDING_ANCHORS.get(level.dimension());
        if (queued == null) return;
        BlockPos pos;
        while ((pos = queued.poll()) != null) {
            if (!buddyAnchors.containsKey(pos) && pendingAnchors.add(pos)) {
                setDirty();
                LevelChunk anchorChunk = level.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
                if (anchorChunk != null) ((LevelChunkExtension) anchorChunk).nml$clearIgnoreBuddyAnchor();
            }
        }
    }

    private static boolean chunkContains(ChunkPos chunkPos, BlockPos blockPos) {
        return (blockPos.getX() >> 4) == chunkPos.x && (blockPos.getZ() >> 4) == chunkPos.z;
    }

    private static final long MOON_CYCLE_DAYS = 8L;
    private static final long NEW_MOON_DAY = 4L;

    public int getCurrentMoonCycle(long dayTime) {
        return (int)((dayTime / 24000L + NEW_MOON_DAY) / MOON_CYCLE_DAYS);
    }

    public boolean tryRespawning(BuddyData existingBuddyData) {
        return existingBuddyData.getShouldRespawn() && (getCurrentMoonCycle(level.getDayTime()) > existingBuddyData.getMoonCycle());
    }

    private static final int SPAWN_RADIUS = 2;
    private static final int SPAWN_ATTEMPTS = 16;

    private @Nullable BlockPos findSpawnPosition(BlockPos anchorPosition, EntityType<? extends Buddy> type) {
        for (int attempt = 0; attempt < SPAWN_ATTEMPTS; attempt++) {
            int offsetX = level.getRandom().nextInt(-SPAWN_RADIUS, SPAWN_RADIUS + 1);
            int offsetZ = level.getRandom().nextInt(-SPAWN_RADIUS, SPAWN_RADIUS + 1);
            if ((offsetX * offsetX) + (offsetZ * offsetZ) > SPAWN_RADIUS * SPAWN_RADIUS) continue;

            BlockPos surface = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                new BlockPos(anchorPosition.getX() + offsetX, 0, anchorPosition.getZ() + offsetZ));
            if (Buddy.checkBuddySpawnRules(type, level, MobSpawnType.EVENT, surface, level.getRandom())) return surface;
        }

        return null;
    }

    private boolean attemptSpawn(BlockPos spawnBlock) {
        BuddyData existingBuddyData = buddyAnchors.get(spawnBlock);
        boolean shouldSpawn = (existingBuddyData == null || tryRespawning(existingBuddyData));
        if (!shouldSpawn) return false;

        // Try spawning the buddy !!!
        Buddy buddy = NMLEntities.BUDDY.get().create(level);
        BlockPos heightmapSpawnPosition = buddy == null ? null : this.findSpawnPosition(spawnBlock, (EntityType<? extends Buddy>) buddy.getType());

        if (buddy != null && heightmapSpawnPosition != null) {
            buddy.setPos(heightmapSpawnPosition.above().getBottomCenter());

            // Prepare Buddy & Anchor
            buddy.prepareAnchor(spawnBlock);

            // Load Buddy NBT Data
            if (existingBuddyData != null && existingBuddyData.getNBTData().isPresent()) {
                // Save original buddy data as fallback
                CompoundTag fallbackTag = new CompoundTag();
                buddy.saveWithoutId(fallbackTag);

                // Replace data with previously saved buddy data
                CompoundTag replacementData = existingBuddyData.getNBTData().get();
                for (String key : replacementData.getAllKeys())
                    fallbackTag.put(key, Objects.requireNonNull(replacementData.get(key)));

                buddy.load(fallbackTag);
            } else EventHooks.finalizeMobSpawn(buddy, level, level.getCurrentDifficultyAt(spawnBlock), MobSpawnType.EVENT, null);

            // Replace last anchor
            updateAnchors(spawnBlock, createData());
            level.addFreshEntity(buddy);
            return true;
        }

        return false;
    }

    public void updateAnchors(BlockPos anchorPosition, BuddyData newData) {
        buddyAnchors.put(anchorPosition, newData);
        setDirty();
    }

    private BuddyData createData() {
        int lastMoonCycle = getCurrentMoonCycle(level.dayTime());
        return new BuddyData(lastMoonCycle);
    }

    public void queryRespawn(BlockPos anchorPosition, Buddy buddy) {
        BuddyData buddyData = createData();
        buddyData.queryRespawn();

        CompoundTag buddySaveData = new CompoundTag();
        buddy.saveWithoutId(buddySaveData);

        CompoundTag storedSaveData = new CompoundTag();
        for (String copiedKey : Buddy.COPY_ON_RESPAWN) {
            if (buddySaveData.contains(copiedKey) && buddySaveData.get(copiedKey) != null)
                storedSaveData.put(copiedKey, Objects.requireNonNull(buddySaveData.get(copiedKey)));
        }
        buddyData.setNBTData(storedSaveData);

        // signal update to position
        updateAnchors(anchorPosition, buddyData);
    }
}
