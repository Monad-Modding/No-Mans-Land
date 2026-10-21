package com.farcr.nomansland.common.world.feature;

import com.farcr.nomansland.common.entity.buddy.BuddyChunkAnchor;
import com.farcr.nomansland.common.registry.NMLTags;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.HashCommon;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class BuddyFairyRingFeature extends Feature<FoliageCircleFeatureConfiguration> {
    public BuddyFairyRingFeature(Codec<FoliageCircleFeatureConfiguration> codec) {
        super(codec);
    }

    private static final int CHUNK_SIZE = 16;
    private static final int LAND_SAMPLE_STEP = 4;
    private static final int WINDOW_SIZE = 40;
    private static final int WINDOW_OFFSET = (WINDOW_SIZE - CHUNK_SIZE) / 2;
    private static final int UNREACHED = Integer.MAX_VALUE / 4;
    private static final int BIOME_SAMPLE_HEIGHT = 8;
    private static final int SEPARATION_RADIUS = 12;
    private static final int GRID_RADIUS = SEPARATION_RADIUS + 1;
    private static final int GRID_SIZE = (GRID_RADIUS * 2) + 1;

    @Override
    public boolean place(FeaturePlaceContext<FoliageCircleFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        FoliageCircleFeatureConfiguration config = context.config();
        ServerLevel serverLevel = level.getLevel();
        int radius = config.radius().sample(random);

        BlockPos centre;
        if (level instanceof WorldGenRegion) {
            ChunkPos originChunk = new ChunkPos(context.origin());
            if (!isIslandAnchorChunk(serverLevel, originChunk)) return false;
            centre = findInlandCentre(level, originChunk, radius);
        } else {
            centre = drySurfaceAt(level, context.origin().getX(), context.origin().getZ());
        }
        if (centre == null) return false;

        int count = 0;
        int x = 0;
        int y = radius;
        int d = 3 - 2 * radius;
        count += drawCircle(centre.getX(), centre.getZ(), x, y, level, random, config);
        while (y >= x) {
            if (d > 0) {
                y--;
                d = d + 4 * (x - y) + 10;
            } else {
                d = d + 4 * x + 6;
            }
            x++;
            count += drawCircle(centre.getX(), centre.getZ(), x, y, level, random, config);
        }

        if (count <= 0) return false;

        BuddyChunkAnchor.queuePendingAnchor(serverLevel.dimension(), centre);
        return true;
    }

    private static boolean isIslandAnchorChunk(ServerLevel level, ChunkPos originChunk) {
        ChunkGenerator generator = level.getChunkSource().getGenerator();
        RandomState randomState = level.getChunkSource().randomState();
        Registry<Biome> biomes = level.registryAccess().registryOrThrow(Registries.BIOME);

        boolean[] island = new boolean[GRID_SIZE * GRID_SIZE];
        for (int gridZ = 0; gridZ < GRID_SIZE; gridZ++) {
            for (int gridX = 0; gridX < GRID_SIZE; gridX++) {
                island[(gridZ * GRID_SIZE) + gridX] = isIslandChunk(generator, randomState, biomes,
                    new ChunkPos(originChunk.x - GRID_RADIUS + gridX, originChunk.z - GRID_RADIUS + gridZ));
            }
        }
        if (!island[(GRID_RADIUS * GRID_SIZE) + GRID_RADIUS]) return false;

        long seed = level.getSeed();
        long originScore = chunkScore(island, GRID_RADIUS, GRID_RADIUS, seed, originChunk.x, originChunk.z);
        for (int gridZ = 1; gridZ < GRID_SIZE - 1; gridZ++) {
            for (int gridX = 1; gridX < GRID_SIZE - 1; gridX++) {
                if (gridX == GRID_RADIUS && gridZ == GRID_RADIUS) continue;
                if (!island[(gridZ * GRID_SIZE) + gridX]) continue;

                int chunkX = originChunk.x - GRID_RADIUS + gridX;
                int chunkZ = originChunk.z - GRID_RADIUS + gridZ;
                long score = chunkScore(island, gridX, gridZ, seed, chunkX, chunkZ);
                if (score > originScore) return false;
                if (score == originScore
                && (chunkZ < originChunk.z || (chunkZ == originChunk.z && chunkX < originChunk.x))) return false;
            }
        }

        return true;
    }

    private static long chunkScore(boolean[] island, int gridX, int gridZ, long seed, int chunkX, int chunkZ) {
        int interior = 0;
        for (int offsetZ = -1; offsetZ <= 1; offsetZ++) {
            for (int offsetX = -1; offsetX <= 1; offsetX++) {
                if (island[((gridZ + offsetZ) * GRID_SIZE) + gridX + offsetX]) interior++;
            }
        }

        long hash = HashCommon.mix(seed ^ ((long) chunkX * 341873128712L) ^ ((long) chunkZ * 132897987541L));
        return ((long) interior << 32) | (hash & 0xFFFFFFFFL);
    }

    private static boolean isIslandChunk(ChunkGenerator generator, RandomState randomState, Registry<Biome> biomes, ChunkPos chunkPos) {
        Holder<Biome> biome = generator.getBiomeSource().getNoiseBiome(
            QuartPos.fromBlock(chunkPos.getMiddleBlockX()),
            QuartPos.fromBlock(generator.getSeaLevel() + BIOME_SAMPLE_HEIGHT),
            QuartPos.fromBlock(chunkPos.getMiddleBlockZ()),
            randomState.sampler());
        return isIslandBiome(biomes, biome);
    }

    private static boolean isIslandBiome(Registry<Biome> biomes, Holder<Biome> biome) {
        if (biome.is(NMLTags.SPAWNS_BUDDY)) return true;
        Optional<ResourceKey<Biome>> key = biome.unwrapKey();
        return key.isPresent() && biomes.getHolderOrThrow(key.get()).is(NMLTags.SPAWNS_BUDDY);
    }

    private static boolean isIslandLand(WorldGenLevel level, int x, int z) {
        BlockPos surface = drySurfaceAt(level, x, z);
        return surface != null && level.getBiome(surface).is(NMLTags.SPAWNS_BUDDY);
    }

    private static @Nullable BlockPos findInlandCentre(WorldGenLevel level, ChunkPos chunkPos, int radius) {
        int originX = chunkPos.getMinBlockX() - WINDOW_OFFSET;
        int originZ = chunkPos.getMinBlockZ() - WINDOW_OFFSET;

        int[] distance = new int[WINDOW_SIZE * WINDOW_SIZE];
        for (int cellZ = 0; cellZ < WINDOW_SIZE; cellZ++) {
            for (int cellX = 0; cellX < WINDOW_SIZE; cellX++) {
                distance[(cellZ * WINDOW_SIZE) + cellX] =
                    isIslandLand(level, originX + cellX, originZ + cellZ) ? UNREACHED : 0;
            }
        }

        for (int cellZ = 0; cellZ < WINDOW_SIZE; cellZ++) {
            for (int cellX = 0; cellX < WINDOW_SIZE; cellX++) {
                int index = (cellZ * WINDOW_SIZE) + cellX;
                if (distance[index] == 0) continue;
                int best = Math.min(distance[index], neighbourDistance(distance, cellX - 1, cellZ) + 1);
                best = Math.min(best, neighbourDistance(distance, cellX, cellZ - 1) + 1);
                best = Math.min(best, neighbourDistance(distance, cellX - 1, cellZ - 1) + 1);
                best = Math.min(best, neighbourDistance(distance, cellX + 1, cellZ - 1) + 1);
                distance[index] = best;
            }
        }

        int bestIndex = -1;
        int bestDistance = 0;
        for (int cellZ = WINDOW_SIZE - 1; cellZ >= 0; cellZ--) {
            for (int cellX = WINDOW_SIZE - 1; cellX >= 0; cellX--) {
                int index = (cellZ * WINDOW_SIZE) + cellX;
                if (distance[index] == 0) continue;
                int best = Math.min(distance[index], neighbourDistance(distance, cellX + 1, cellZ) + 1);
                best = Math.min(best, neighbourDistance(distance, cellX, cellZ + 1) + 1);
                best = Math.min(best, neighbourDistance(distance, cellX + 1, cellZ + 1) + 1);
                best = Math.min(best, neighbourDistance(distance, cellX - 1, cellZ + 1) + 1);
                distance[index] = best;
                if (best > bestDistance) {
                    bestDistance = best;
                    bestIndex = index;
                }
            }
        }

        if (bestIndex == -1) return null;
        int bestX = originX + (bestIndex % WINDOW_SIZE);
        int bestZ = originZ + (bestIndex / WINDOW_SIZE);
        return drySurfaceAt(level, bestX, bestZ);
    }

    private static int neighbourDistance(int[] distance, int cellX, int cellZ) {
        if (cellX < 0 || cellZ < 0 || cellX >= WINDOW_SIZE || cellZ >= WINDOW_SIZE) return 0;
        return distance[(cellZ * WINDOW_SIZE) + cellX];
    }

    private static @Nullable BlockPos drySurfaceAt(WorldGenLevel level, int x, int z) {
        int y = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, 0, z)).getY();
        BlockPos pos = new BlockPos(x, y, z);
        if (!level.getFluidState(pos).isEmpty()) return null;
        if (!level.getFluidState(pos.below()).isEmpty()) return null;
        return pos;
    }

    private int drawCircle(int xc, int zc, int x, int z, WorldGenLevel level, RandomSource random, FoliageCircleFeatureConfiguration config) {
        int count = 0;
        count += placeBlock(xc + x, zc + z, level, random, config);
        count += placeBlock(xc - x, zc + z, level, random, config);
        count += placeBlock(xc + x, zc - z, level, random, config);
        count += placeBlock(xc - x, zc - z, level, random, config);
        count += placeBlock(xc + z, zc + x, level, random, config);
        count += placeBlock(xc - z, zc + x, level, random, config);
        count += placeBlock(xc + z, zc - x, level, random, config);
        count += placeBlock(xc - z, zc - x, level, random, config);
        return count;
    }

    private int placeBlock(int x, int z, WorldGenLevel level, RandomSource random, FoliageCircleFeatureConfiguration config) {
        BlockPos pos = drySurfaceAt(level, x, z);
        if (pos == null) return 0;
        BlockState state = config.state().getState(random, pos);
        if (state.canSurvive(level, pos)) {
            level.setBlock(pos, state, 3);
            return 1;
        }
        return 0;
    }
}
