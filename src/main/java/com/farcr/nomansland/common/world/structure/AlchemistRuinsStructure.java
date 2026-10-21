package com.farcr.nomansland.common.world.structure;

import com.farcr.nomansland.common.registry.worldgen.NMLBiomes;
import com.farcr.nomansland.common.registry.worldgen.NMLStructureTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pools.DimensionPadding;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;

import java.util.Map;
import java.util.Optional;

public class AlchemistRuinsStructure extends Structure {
    public static final MapCodec<AlchemistRuinsStructure> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    settingsCodec(instance),
                    StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(s -> s.startPool),
                    Codec.intRange(0, 20).fieldOf("size").forGetter(s -> s.maxDepth),
                    HeightProvider.CODEC.fieldOf("start_height").forGetter(s -> s.startHeight),
                    Codec.intRange(1, 128).fieldOf("max_distance_from_center").forGetter(s -> s.maxDistanceFromCenter),
                    Codec.BOOL.optionalFieldOf("use_expansion_hack", false).forGetter(s -> s.useExpansionHack),
                    Heightmap.Types.CODEC.optionalFieldOf("project_start_to_heightmap").forGetter(s -> s.projectStartToHeightmap),
                    DimensionPadding.CODEC.optionalFieldOf("dimension_padding", DimensionPadding.ZERO).forGetter(s -> s.dimensionPadding),
                    LiquidSettings.CODEC.optionalFieldOf("liquid_settings", LiquidSettings.APPLY_WATERLOGGING).forGetter(s -> s.liquidSettings),
                    Codec.unboundedMap(
                            BuiltInRegistries.BLOCK.byNameCodec(),
                            ResourceKey.codec(Registries.CONFIGURED_FEATURE)
                    ).optionalFieldOf("feature_placeholders", Map.of()).forGetter(s -> s.featurePlaceholders)
            ).apply(instance, AlchemistRuinsStructure::new)
    );

    private final Holder<StructureTemplatePool> startPool;
    private final int maxDepth;
    private final HeightProvider startHeight;
    private final int maxDistanceFromCenter;
    private final boolean useExpansionHack;
    private final Optional<Heightmap.Types> projectStartToHeightmap;
    private final DimensionPadding dimensionPadding;
    private final LiquidSettings liquidSettings;
    private final Map<Block, ResourceKey<ConfiguredFeature<?, ?>>> featurePlaceholders;

    public AlchemistRuinsStructure(
            StructureSettings settings,
            Holder<StructureTemplatePool> startPool,
            int maxDepth,
            HeightProvider startHeight,
            int maxDistanceFromCenter,
            boolean useExpansionHack,
            Optional<Heightmap.Types> projectStartToHeightmap,
            DimensionPadding dimensionPadding,
            LiquidSettings liquidSettings,
            Map<Block, ResourceKey<ConfiguredFeature<?, ?>>> featurePlaceholders
    ) {
        super(settings);
        this.startPool = startPool;
        this.maxDepth = maxDepth;
        this.startHeight = startHeight;
        this.maxDistanceFromCenter = maxDistanceFromCenter;
        this.useExpansionHack = useExpansionHack;
        this.projectStartToHeightmap = projectStartToHeightmap;
        this.dimensionPadding = dimensionPadding;
        this.liquidSettings = liquidSettings;
        this.featurePlaceholders = featurePlaceholders;
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();
        int x = chunkPos.getMiddleBlockX();
        int z = chunkPos.getMiddleBlockZ();
        int y = startHeight.sample(context.random(), new WorldGenerationContext(context.chunkGenerator(), context.heightAccessor()));
        if (projectStartToHeightmap.isPresent()) {
            y += context.chunkGenerator().getFirstOccupiedHeight(x, z, projectStartToHeightmap.get(), context.heightAccessor(), context.randomState());
        }
        int surfaceY = context.chunkGenerator().getFirstOccupiedHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());
        y = Math.min(y, surfaceY - 5);
        BlockPos blockPos = new BlockPos(x, y, z);
        return JigsawPlacement.addPieces(
                context, startPool, Optional.empty(), maxDepth, blockPos,
                useExpansionHack, Optional.empty(), maxDistanceFromCenter,
                PoolAliasLookup.EMPTY, dimensionPadding, liquidSettings
        );
    }

    @Override
    public void afterPlace(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random, BoundingBox box, ChunkPos chunkPos, PiecesContainer pieces) {
        super.afterPlace(level, structureManager, generator, random, box, chunkPos, pieces);

        Holder<Biome> alchemistBiome = level.registryAccess()
            .registryOrThrow(Registries.BIOME)
            .getHolderOrThrow(NMLBiomes.ALCHEMIST_RUINS);
        ChunkAccess chunk = level.getChunk(chunkPos.x, chunkPos.z);

        StructureFeatureHelper.replacePlaceholders(featurePlaceholders, level, generator, random, chunkPos, pieces);
        StructureBiomeHelper.applyBiome(alchemistBiome, level, chunk, generator, chunkPos, pieces);
    }

    @Override
    public StructureType<?> type() {
        return NMLStructureTypes.ALCHEMIST_RUINS.get();
    }
}