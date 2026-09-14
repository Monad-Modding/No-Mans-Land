package com.farcr.nomansland.common.world.orevein;

import com.farcr.nomansland.common.registry.NMLRegistries;
import com.farcr.nomansland.common.world.InterpolatedNoiseField;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class OreVeinSystem {
    private static final ResourceLocation ORE_VEIN_RANDOM = NMLRegistries.ORE_VEIN_KEY.location();
    private static final int CACHE_SIZE = 512;

    final Set<Holder<OreVeinType>> oreVeinTypes;
    final Object2ObjectArrayMap<Holder<OreVeinType>, Long2ObjectLinkedOpenHashMap<Optional<OreVeinInstance>>> instanceCacheByType;
    final ThreadLocal<ObjectOpenHashSet<OreVeinInstance>> instances;
    final ChunkGenerator chunkGenerator;

    public OreVeinSystem(WorldGenLevel level, ChunkGenerator chunkGenerator) {
        Registry<OreVeinType> registry = level.registryAccess().registryOrThrow(NMLRegistries.ORE_VEIN_KEY);
        Set<Holder<Biome>> possibleBiomes = chunkGenerator.getBiomeSource().possibleBiomes();
        this.oreVeinTypes = registry.asLookup()
                .listElements()
                .filter(holder -> holder.value().biomes()
                        .map(allowed -> allowed.stream().anyMatch(possibleBiomes::contains))
                        .orElse(true)
                ).collect(Collectors.toUnmodifiableSet());
        this.instanceCacheByType = new Object2ObjectArrayMap<>(this.oreVeinTypes.size());
        for (Holder<OreVeinType> oreVeinType : this.oreVeinTypes) {
            this.instanceCacheByType.put(oreVeinType, new Long2ObjectLinkedOpenHashMap<>(CACHE_SIZE + 1));
        }
        this.instances = ThreadLocal.withInitial(() -> new ObjectOpenHashSet<>(this.oreVeinTypes.size()));
        this.chunkGenerator = chunkGenerator;
    }

    public void buildVeins(WorldGenRegion level, ChunkAccess chunk, WorldGenerationContext context, RandomState random, BlockState defaultBlock) {
        ObjectOpenHashSet<OreVeinInstance> oreVeinsInChunk = this.collectVeinsInChunk(level, chunk, context, random);
        if (oreVeinsInChunk.isEmpty()) return;
        // sort by generation order.
        // if generation orders are equal, fallback to sort by manhattan distance to world origin
        List<OreVeinInstance> sortedOreVeinsInChunk = oreVeinsInChunk
                .stream()
                .sorted(Comparator
                        .comparingInt((OreVeinInstance instance) -> instance.type().generationOrder())
                        .thenComparingInt(instance -> Math.abs(instance.x) + Math.abs(instance.z)))
                .toList();
        this.fill(sortedOreVeinsInChunk, level, chunk, random);
    }

    private Optional<OreVeinInstance> getOrCreateOreVein(Holder<OreVeinType> typeHolder, OreVeinType type, int cellX, int cellZ, WorldGenRegion level, WorldGenerationContext context, RandomState random) {
        long key = ChunkPos.asLong(cellX, cellZ);
        Optional<OreVeinInstance> instance = null;
        Long2ObjectLinkedOpenHashMap<Optional<OreVeinInstance>> instanceCache = instanceCacheByType.get(typeHolder);

        synchronized (instanceCache) {
            instance = instanceCache.getOrDefault(key, null);
        }

        if (instance == null) {
            instance = createOreVein(typeHolder, type, cellX, cellZ, level, context, random);
            synchronized (instanceCache) {
                if (!instanceCache.containsKey(key)) {
                    instanceCache.putAndMoveToFirst(key, instance);
                    if (instanceCache.size() > CACHE_SIZE) instanceCache.removeLast();
                }
            }
        }

        return instance;
    }

    private Optional<OreVeinInstance> createOreVein(Holder<OreVeinType> typeHolder, OreVeinType type, int cellX, int cellZ, WorldGenRegion level, WorldGenerationContext context, RandomState random) {
        RandomSource veinRandom = random.getOrCreateRandomFactory(typeHolder.getKey().location()).at(cellX, 0, cellZ);

        if (veinRandom.nextFloat() > type.probability()) return Optional.empty();

        int minY = type.minHeight().sample(veinRandom, context),
            maxY = type.maxHeight().sample(veinRandom, context);
        if (minY > maxY) return Optional.empty();

        int centerY = (minY + maxY) / 2;

        int minX = cellX * type.spacing(),
            minZ = cellZ * type.spacing();
        int maxX = minX + (type.spacing() - type.separation()),
            maxZ = minZ + (type.spacing() - type.separation());
        int centerX = veinRandom.nextInt(minX, maxX),
            centerZ = veinRandom.nextInt(minZ, maxZ);

        if (type.biomes().isPresent()) {
            HolderSet<Biome> allowedBiomes = type.biomes().get();
            int sampleY = centerY;
            if (type.sampleBiomeAtSurface()) {
                sampleY = chunkGenerator.getBaseHeight(centerX, centerZ, Heightmap.Types.OCEAN_FLOOR, level, random);
            }
            if (!allowedBiomes.contains(level.getUncachedNoiseBiome(
                    QuartPos.fromBlock(centerX),
                    QuartPos.fromBlock(sampleY),
                    QuartPos.fromBlock(centerZ)
            ))) return Optional.empty();
        }

        int radius = type.radius().sample(veinRandom);
        if (radius <= 0) return Optional.empty();

        float veinRadius = type.veinRadius().sample(veinRandom);
        if (veinRadius <= 0) return Optional.empty();

        return Optional.of(new OreVeinInstance(type, centerX, centerZ, minY, maxY, radius, radius * radius, veinRadius));
    }

    private ObjectOpenHashSet<OreVeinInstance> collectVeinsInChunk(WorldGenRegion level, ChunkAccess chunk, WorldGenerationContext context, RandomState random) {
        ObjectOpenHashSet<OreVeinInstance> oreVeinsInChunk = this.instances.get();
        oreVeinsInChunk.clear();
        int chunkMinX = chunk.getPos().getMinBlockX(),
            chunkMinZ = chunk.getPos().getMinBlockZ();
        for (Holder<OreVeinType> holder : oreVeinTypes) {
            OreVeinType type = holder.value();
            int centerCellX = Math.floorDiv(chunkMinX, type.spacing()),
                centerCellZ = Math.floorDiv(chunkMinZ, type.spacing());

            // collect veins from all neighboring cells
            for (int cellXOffset = -1; cellXOffset <= 1; cellXOffset++) {
                for (int cellZOffset = -1; cellZOffset <= 1; cellZOffset++) {
                    int cellX = centerCellX + cellXOffset, cellZ = centerCellZ + cellZOffset;

                    Optional<OreVeinInstance> maybeInstance = getOrCreateOreVein(holder, type, cellX, cellZ, level, context, random);

                    if (maybeInstance.isEmpty()) continue;

                    OreVeinInstance instance = maybeInstance.get();
                    if (Mth.length(chunkMinX - instance.x, chunkMinZ - instance.z) > instance.radius + 24) continue;

                    oreVeinsInChunk.add(instance);
                }
            }
        }

        return oreVeinsInChunk;
    }

    public static void fill(List<OreVeinInstance> oreVeinsInChunk, WorldGenLevel level, ChunkAccess chunk, RandomState randomState) {
        ChunkPos chunkpos = chunk.getPos();
        int chunkMinX = chunkpos.getMinBlockX(),
            chunkMinZ = chunkpos.getMinBlockZ();
        int chunkHeight = chunk.getHeight(),
            chunkMinY = chunk.getMinBuildHeight();

        // find the maximum possible y for any ore vein
        int maxY = Integer.MIN_VALUE, minY = Integer.MAX_VALUE;
        for (OreVeinInstance oreVeinInstance : oreVeinsInChunk) {
            minY = Math.min(minY, oreVeinInstance.minY);
            maxY = Math.max(maxY, oreVeinInstance.maxY);
        }
        int maxHeight = Integer.MIN_VALUE;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                maxHeight = Math.max(chunk.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z), maxHeight);
            }
        }
        maxY = Math.min(maxY, maxHeight);
        minY = Math.max(minY, chunkMinY + 1);
        if (minY > maxY) return;

        NormalNoise oreVeinA = randomState.getOrCreateNoise(Noises.ORE_VEIN_A),
                oreVeinB = randomState.getOrCreateNoise(Noises.ORE_VEIN_B),
                oreGap = randomState.getOrCreateNoise(Noises.ORE_GAP);
        InterpolatedNoiseField oreVeinAField = new InterpolatedNoiseField(chunkHeight, 2, 2),
                oreVeinBField = new InterpolatedNoiseField(chunkHeight, 2, 2),
                oreGapField = new InterpolatedNoiseField(chunkHeight, 2, 2);
        int fieldStartY = minY - chunkMinY, fieldEndY = maxY - chunkMinY;
        oreVeinAField.fill(fieldStartY, fieldEndY, chunkMinX, chunkMinY, chunkMinZ, (x, y, z) -> oreVeinA.getValue(x * 4, y * 4, z * 4));
        oreVeinBField.fill(fieldStartY, fieldEndY, chunkMinX, chunkMinY, chunkMinZ, (x, y, z) -> oreVeinB.getValue(x * 4, y * 4, z * 4));
        oreGapField.fill(fieldStartY, fieldEndY, chunkMinX, chunkMinY, chunkMinZ, oreGap::getValue);

        RandomSource fillRandom = randomState.getOrCreateRandomFactory(ORE_VEIN_RANDOM).at(chunkMinX, 0, chunkMinZ);

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = 0; x < 16; x++) {
            int worldX = x + chunkMinX;
            pos.setX(worldX);
            for (int z = 0; z < 16; z++) {
                int worldZ = z + chunkMinZ;
                pos.setZ(worldZ);
                for (OreVeinInstance oreVeinInstance : oreVeinsInChunk) {
                    fillColumnForVein(
                            oreVeinInstance, x, z, worldX, worldZ, chunkMinY, pos,
                            oreVeinAField, oreVeinBField, oreGapField,
                            level, chunk, fillRandom
                    );
                }
            }
        }
    }

    private static void fillColumnForVein(OreVeinInstance veinInstance,
                                   int localX, int localZ, int worldX, int worldZ, int chunkMinY, BlockPos.MutableBlockPos pos,
                                   InterpolatedNoiseField oreVeinAField, InterpolatedNoiseField oreVeinBField, InterpolatedNoiseField oreGapField,
                                   WorldGenLevel level, ChunkAccess chunk, RandomSource fillRandom) {
        int minY = veinInstance.minY,
            maxY = veinInstance.maxY;
        double xzDistSq = Mth.lengthSquared(worldX - veinInstance.x, worldZ - veinInstance.z);
        if (xzDistSq > veinInstance.radiusSquared) return;
        double xzDist = Math.sqrt(xzDistSq);

        int minColumnY = Math.max(minY, chunkMinY + 1),
            maxColumnY = Math.min(chunk.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, localX, localZ), maxY);

        LevelChunkSection chunkSection = null;
        int lastSectionIndex = Integer.MIN_VALUE;

        OreVeinType veinType = veinInstance.type();
        for (int y = maxColumnY; y >= minColumnY; y--) {
            int localY = y - chunkMinY;
            int sectionY = y & 15;
            int sectionIndex = chunk.getSectionIndex(y);
            if (sectionIndex != lastSectionIndex) {
                chunkSection = chunk.getSection(sectionIndex);
                lastSectionIndex = sectionIndex;
            }
            pos.setY(y);

            BlockState currentState = chunkSection.getBlockState(localX, sectionY, localZ);
            if (currentState.isAir()) continue;

            double veinANoise = oreVeinAField.retrieve(localX, localY, localZ),
                   veinBNoise = oreVeinBField.retrieve(localX, localY, localZ),
                   veinGapNoise = oreGapField.retrieve(localX, localY, localZ);
            double veinRidgeNoise = Math.max(Math.abs(veinANoise), Math.abs(veinBNoise));

            if (veinType.targetCondition().test(level, pos)) {
                int yDist = Math.min(maxY - y, y - minY);
                int yDiff = maxY - minY;

                if (fillRandom.nextFloat() > veinType.filler().probability()) continue;

                if (veinType.invert()) veinRidgeNoise = 1 - veinRidgeNoise;
                float veinRadius = veinInstance.veinRadius();
                veinRadius = (float) Mth.clampedMap(yDist, 0, yDiff * 0.25F, 0, veinRadius);
                veinRadius = (float) Mth.clampedMap(xzDist, veinInstance.radius * 0.75F, veinInstance.radius, veinRadius, 0);

                veinRidgeNoise = veinRidgeNoise * 64 - veinRadius;
                if (veinRidgeNoise >= fillRandom.triangle(0.0, veinType.filler().incoherence())) continue;

                if (veinGapNoise > -0.3F &&
                        fillRandom.nextFloat() <= veinType.core().probability() &&
                        -veinRidgeNoise >= fillRandom.triangle(veinRadius * 0.5, veinType.core().incoherence() * 0.5F)) {
                    chunkSection.setBlockState(localX, sectionY, localZ, veinType.core().resolve(fillRandom, pos, currentState), false);
                } else {
                    chunkSection.setBlockState(localX, sectionY, localZ, veinType.filler().resolve(fillRandom, pos, currentState), false);
                }
            }
        }
    }

    public record OreVeinInstance(OreVeinType type, int x, int z, int minY, int maxY, int radius, int radiusSquared, float veinRadius) {}
}
