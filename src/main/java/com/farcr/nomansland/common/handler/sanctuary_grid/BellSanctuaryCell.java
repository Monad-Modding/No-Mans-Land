package com.farcr.nomansland.common.handler.sanctuary_grid;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Map;

public class BellSanctuaryCell implements Iterable<BellSanctuaryCell.SanctuaryPair> {

    private final CellPos pos;
    private final Map<ChunkPos, SanctuaryPair> trackedPairs = new Object2ObjectOpenHashMap<>();

    public BellSanctuaryCell(final int x, final int z) {
        this.pos = new CellPos(x, z);
    }

    public boolean containsPosition(final ChunkPos toCheck) {
        return this.trackedPairs.containsKey(toCheck);
    }

    @Nullable
    public SanctuaryPair getPair(final ChunkPos pos) {
        return this.trackedPairs.get(pos);
    }

    @Override
    public @NotNull Iterator<SanctuaryPair> iterator() {
        return this.trackedPairs.values().iterator();
    }

    public void addPair(final SanctuaryPair newPair) {
        this.trackedPairs.put(newPair.first, newPair);
        this.trackedPairs.put(newPair.second, newPair);
    }

    public boolean clean() {
        this.trackedPairs.clear();
        return true;
    }

    public record SanctuaryPair(ChunkPos first, ChunkPos second) {

        public ChunkPos getOther(final ChunkPos toCheck) {
            if (this.first.equals(toCheck)) {
                return this.second;
            } else if (this.second.equals(toCheck)) {
                return this.first;
            }

            throw new IllegalArgumentException("Passed ChunkPos must be contained withing this pair!");
        }

    }

    public record CellPos(int x, int z) {

    }
}
