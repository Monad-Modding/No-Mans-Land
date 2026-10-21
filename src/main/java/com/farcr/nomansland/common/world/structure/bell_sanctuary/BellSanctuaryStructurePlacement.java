package com.farcr.nomansland.common.world.structure.bell_sanctuary;

import com.farcr.nomansland.common.handler.sanctuary_grid.BellSanctuaryCell;
import com.farcr.nomansland.common.handler.sanctuary_grid.BellSanctuaryGridHandler;
import com.farcr.nomansland.common.registry.worldgen.NMLStructurePlacements;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;

import java.util.Optional;

public class BellSanctuaryStructurePlacement extends RandomSpreadStructurePlacement {

    public static final MapCodec<BellSanctuaryStructurePlacement> CODEC = RecordCodecBuilder.mapCodec(
            instance -> placementCodec(instance)
                    .and(
                            instance.group(
                                    Codec.intRange(0, 4096).fieldOf("spacing").forGetter(RandomSpreadStructurePlacement::spacing),
                                    Codec.intRange(0, 4096).fieldOf("separation").forGetter(RandomSpreadStructurePlacement::separation),
                                    RandomSpreadType.CODEC
                                            .optionalFieldOf("spread_type", RandomSpreadType.LINEAR)
                                            .forGetter(RandomSpreadStructurePlacement::spreadType)
                            )
                    )
                    .apply(instance, BellSanctuaryStructurePlacement::new)
    );

    public BellSanctuaryStructurePlacement(final Vec3i locateOffset, final FrequencyReductionMethod frequencyReductionMethod, final float frequency, final int salt, final Optional<ExclusionZone> exclusionZone, final int spacing, final int separation, final RandomSpreadType spreadType) {
        super(locateOffset, frequencyReductionMethod, frequency, salt, exclusionZone, spacing, separation, spreadType);
    }

    /*
     * If super placement chunk succeeds, it wants to place a new beginning pairing OR use an already existing pair
     * Because of this we NEED to call generatePair
     *
     * However, if super placement does not succeed, we still need to check if the position contains a valid sanctuary position
     * getCell -> if cell.contains to check pos then true else false
     */

    @Override
    protected boolean isPlacementChunk(final ChunkGeneratorStructureState chunkGeneratorStructureState, final int chunkX, final int chunkZ) {
        final long levelSeed = chunkGeneratorStructureState.getLevelSeed();
        if (super.isPlacementChunk(chunkGeneratorStructureState, chunkX, chunkZ)) {
            return BellSanctuaryGridHandler.tryGeneratePair(levelSeed, this, new ChunkPos(chunkX, chunkZ));
        }

        final BellSanctuaryCell cell = BellSanctuaryGridHandler.getCell(levelSeed, chunkX * 16, chunkZ * 16);
        if (cell != null) {
            return cell.containsPosition(new ChunkPos(chunkX, chunkZ));
        }

        return false;
    }

    @Override
    public StructurePlacementType<?> type() {
        return NMLStructurePlacements.BELL_SANCTUARY.get();
    }
}
