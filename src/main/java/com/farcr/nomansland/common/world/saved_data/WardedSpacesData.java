package com.farcr.nomansland.common.world.saved_data;

import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.Arrays;

public class WardedSpacesData extends SavedData {
    public static final String NAME = "warded_spaces";

    private static final String MAP_KEY = "warded_spaces";
    protected static final String DATA_VERSION_KEY = "data_version";
    protected static final int DATA_VERSION = 1;

    private static final Vector3d TEMP_POS_A = new Vector3d();
    private static final Vector3d TEMP_POS_B = new Vector3d();

    public Long2IntMap wardedSpaces = new Long2IntOpenHashMap();

    public WardedSpacesData() {
        this.wardedSpaces.defaultReturnValue(Integer.MAX_VALUE);
    }

    public static WardedSpacesData load(final CompoundTag tag, final HolderLookup.Provider lookupProvider) {
        final int dataVersion = tag.contains(DATA_VERSION_KEY) ? tag.getInt(DATA_VERSION_KEY) : 0;
        final WardedSpacesData data = new WardedSpacesData();

        if (dataVersion == 0) {
            // Load legacy WardedSpacesData
            final ArrayList<BlockPos> positions = new ArrayList<>();
            Arrays.stream(tag.getLongArray("positions")).forEachOrdered(pos -> positions.add(BlockPos.of(pos)));

            final ArrayList<Integer> ranges = new ArrayList<>();
            Arrays.stream(tag.getIntArray("ranges")).forEachOrdered(ranges::add);

            assert positions.size() == ranges.size();
            for (int i = 0; i < positions.size(); i++) {
                data.wardedSpaces.put(positions.get(i).asLong(), (int) ranges.get(i));
            }

            return data;
        }

        final CompoundTag mapTag = tag.getCompound(MAP_KEY);

        for (final String key : mapTag.getAllKeys()) {
            final long longKey = Long.parseLong(key);
            data.wardedSpaces.put(longKey, mapTag.getInt(key));
        }

        return data;
    }

    private static final SavedData.Factory<WardedSpacesData> FACTORY =
            new SavedData.Factory<>(WardedSpacesData::new, WardedSpacesData::load);

    public static WardedSpacesData get(final ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, WardedSpacesData.NAME);
    }

    @Override
    public CompoundTag save(final CompoundTag tag, final HolderLookup.Provider registries) {
        tag.putInt(DATA_VERSION_KEY, DATA_VERSION);

        final CompoundTag mapTag = new CompoundTag();

        for (final Long2IntMap.Entry entry : this.wardedSpaces.long2IntEntrySet()) {
            mapTag.putInt(String.valueOf(entry.getLongKey()), entry.getIntValue());
        }

        tag.put(MAP_KEY, mapTag);

        return tag;
    }

    public void addEffigy(final BlockPos pos, final int range) {
        this.removeEffigy(pos);
        this.wardedSpaces.put(pos.asLong(), range);
        this.setDirty();
    }

    public void removeEffigy(final BlockPos pos) {
        if (this.wardedSpaces.remove(pos.asLong()) != Integer.MAX_VALUE) {
            this.setDirty();
        }
    }

    private long snapshotTick = Long.MIN_VALUE;
    private int snapshotSize = -1;

    private double[] snapshotX = new double[0];
    private double[] snapshotY = new double[0];
    private double[] snapshotZ = new double[0];

    private double[] snapshotRange = new double[0];
    private int[] snapshotRadius = new int[0];
    private long[] snapshotPos = new long[0];

    private double boundsMinX;
    private double boundsMinY;
    private double boundsMinZ;
    private double boundsMaxX;
    private double boundsMaxY;
    private double boundsMaxZ;

    private static final int MIN_CELL_SHIFT = 5;
    private static final int LINEAR_SCAN_LIMIT = 64;

    private boolean gridBuilt = false;
    private int maxRadius = 0;
    private int cellShift = MIN_CELL_SHIFT;
    private int cellMask = 0;
    private long[] cellKeys = new long[0];
    private int[] cellHeads = new int[0];
    private int[] cellNext = new int[0];

    private void refreshSnapshot(final Level level) {
        final long tick = level.getGameTime();
        if (this.snapshotTick == tick && this.snapshotSize == this.wardedSpaces.size()) {
            return;
        }

        this.snapshotTick = tick;
        this.snapshotSize = this.wardedSpaces.size();
        if (this.snapshotX.length != this.snapshotSize) {
            this.snapshotX = new double[this.snapshotSize];
            this.snapshotY = new double[this.snapshotSize];
            this.snapshotZ = new double[this.snapshotSize];
            this.snapshotRange = new double[this.snapshotSize];
            this.snapshotRadius = new int[this.snapshotSize];
            this.snapshotPos = new long[this.snapshotSize];
            this.cellNext = new int[this.snapshotSize];
        }

        final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        final Vector3d raw = new Vector3d();
        final Vector3d projected = new Vector3d();
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double minZ = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        double maxZ = -Double.MAX_VALUE;
        int maxRadius = 0;
        int index = 0;
        for (final Long2IntMap.Entry entry : this.wardedSpaces.long2IntEntrySet()) {
            cursor.set(entry.getLongKey());
            JOMLConversion.atBottomCenterOf(cursor, raw);
            SableCompanion.INSTANCE.projectOutOfSubLevel(level, raw, projected);
            this.snapshotX[index] = projected.x;
            this.snapshotY[index] = projected.y;
            this.snapshotZ[index] = projected.z;
            this.snapshotPos[index] = entry.getLongKey();

            final int radius = Math.max(entry.getIntValue(), 0);
            this.snapshotRadius[index] = radius;
            this.snapshotRange[index] = Mth.square((double) radius);
            maxRadius = Math.max(maxRadius, radius);
            minX = Math.min(minX, projected.x - radius);
            minY = Math.min(minY, projected.y - radius);
            minZ = Math.min(minZ, projected.z - radius);
            maxX = Math.max(maxX, projected.x + radius);
            maxY = Math.max(maxY, projected.y + radius);
            maxZ = Math.max(maxZ, projected.z + radius);
            index++;
        }

        this.boundsMinX = minX;
        this.boundsMinY = minY;
        this.boundsMinZ = minZ;
        this.boundsMaxX = maxX;
        this.boundsMaxY = maxY;
        this.boundsMaxZ = maxZ;

        this.maxRadius = maxRadius;
        this.gridBuilt = this.snapshotSize > LINEAR_SCAN_LIMIT;
        if (this.gridBuilt) this.rebuildGrid(maxRadius);
    }

    private void rebuildGrid(final int maxRadius) {
        int shift = MIN_CELL_SHIFT;
        while ((1 << shift) < 2 * maxRadius) {
            shift++;
        }
        this.cellShift = shift;

        int capacity = 16;
        while (capacity < 2 * this.snapshotSize) {
            capacity <<= 1;
        }
        if (this.cellHeads.length != capacity) {
            this.cellHeads = new int[capacity];
            this.cellKeys = new long[capacity];
        }
        this.cellMask = capacity - 1;
        Arrays.fill(this.cellHeads, -1);

        for (int index = 0; index < this.snapshotSize; index++) {
            final long key = cellKey(
                    this.cellOf(this.snapshotX[index]),
                    this.cellOf(this.snapshotY[index]),
                    this.cellOf(this.snapshotZ[index])
            );
            int slot = (int) (HashCommon.mix(key) & this.cellMask);
            while (this.cellHeads[slot] != -1 && this.cellKeys[slot] != key) {
                slot = (slot + 1) & this.cellMask;
            }
            if (this.cellHeads[slot] == -1) {
                this.cellKeys[slot] = key;
            }
            this.cellNext[index] = this.cellHeads[slot];
            this.cellHeads[slot] = index;
        }
    }

    private int cellOf(final double value) {
        return Mth.floor(value) >> this.cellShift;
    }

    private static long cellKey(final int cellX, final int cellY, final int cellZ) {
        return ((long) (cellX & 0xFFFFFF) << 40) | ((long) (cellY & 0xFFFF) << 24) | (cellZ & 0xFFFFFF);
    }

    private int cellStart(final long key) {
        int slot = (int) (HashCommon.mix(key) & this.cellMask);
        while (this.cellHeads[slot] != -1) {
            if (this.cellKeys[slot] == key) return this.cellHeads[slot];
            slot = (slot + 1) & this.cellMask;
        }
        return -1;
    }

    private boolean outsideBounds(final double x, final double y, final double z) {
        return x < this.boundsMinX || x > this.boundsMaxX
                || y < this.boundsMinY || y > this.boundsMaxY
                || z < this.boundsMinZ || z > this.boundsMaxZ;
    }

    public boolean isWarded(final Level level, final BlockPos pos) {
        if (this.wardedSpaces.isEmpty()) return false;
        if (this.wardedSpaces.containsKey(pos.asLong())) return true;

        this.refreshSnapshot(level);
        if (this.snapshotSize == 0) return false;

        JOMLConversion.atBottomCenterOf(pos, TEMP_POS_B);
        SableCompanion.INSTANCE.projectOutOfSubLevel(level, TEMP_POS_B, TEMP_POS_A);
        final double x = TEMP_POS_A.x;
        final double y = TEMP_POS_A.y;
        final double z = TEMP_POS_A.z;

        if (this.outsideBounds(x, y, z)) return false;

        if (!this.gridBuilt) {
            for (int index = 0; index < this.snapshotSize; index++) {
                if (this.withinRange(index, x, y, z)) return true;
            }
            return false;
        }

        final int minCellX = this.cellOf(x - this.maxRadius);
        final int maxCellX = this.cellOf(x + this.maxRadius);
        final int minCellY = this.cellOf(y - this.maxRadius);
        final int maxCellY = this.cellOf(y + this.maxRadius);
        final int minCellZ = this.cellOf(z - this.maxRadius);
        final int maxCellZ = this.cellOf(z + this.maxRadius);
        for (int cellX = minCellX; cellX <= maxCellX; cellX++) {
            for (int cellY = minCellY; cellY <= maxCellY; cellY++) {
                for (int cellZ = minCellZ; cellZ <= maxCellZ; cellZ++) {
                    int index = this.cellStart(cellKey(cellX, cellY, cellZ));
                    while (index != -1) {
                        if (this.withinRange(index, x, y, z)) return true;
                        index = this.cellNext[index];
                    }
                }
            }
        }

        return false;
    }

    private boolean withinRange(final int index, final double x, final double y, final double z) {
        final double dx = x - this.snapshotX[index];
        final double dy = y - this.snapshotY[index];
        final double dz = z - this.snapshotZ[index];
        return dx * dx + dy * dy + dz * dz <= this.snapshotRange[index];
    }

    private double rangeDistanceSquared(final int index, final double x, final double y, final double z) {
        final double dx = x - this.snapshotX[index];
        final double dy = y - this.snapshotY[index];
        final double dz = z - this.snapshotZ[index];
        final double distanceSquared = dx * dx + dy * dy + dz * dz;
        return distanceSquared <= this.snapshotRange[index] ? distanceSquared : Double.MAX_VALUE;
    }

    @Nullable
    public Pair<BlockPos, Integer> getAffectingEffigyAt(final Level level, final BlockPos pos) {
        if (this.wardedSpaces.containsKey(pos.asLong())) {
            return Pair.of(pos, this.wardedSpaces.get(pos.asLong()));
        }
        if (this.wardedSpaces.isEmpty()) return null;

        this.refreshSnapshot(level);
        if (this.snapshotSize == 0) return null;

        JOMLConversion.atBottomCenterOf(pos, TEMP_POS_B);
        SableCompanion.INSTANCE.projectOutOfSubLevel(level, TEMP_POS_B, TEMP_POS_A);
        final double x = TEMP_POS_A.x;
        final double y = TEMP_POS_A.y;
        final double z = TEMP_POS_A.z;

        if (this.outsideBounds(x, y, z)) return null;

        int closest = -1;
        double closestDistanceSquared = Double.MAX_VALUE;

        if (!this.gridBuilt) {
            for (int index = 0; index < this.snapshotSize; index++) {
                final double distanceSquared = this.rangeDistanceSquared(index, x, y, z);
                if (distanceSquared < closestDistanceSquared) {
                    closestDistanceSquared = distanceSquared;
                    closest = index;
                }
            }
        } else {
            final int minCellX = this.cellOf(x - this.maxRadius);
            final int maxCellX = this.cellOf(x + this.maxRadius);
            final int minCellY = this.cellOf(y - this.maxRadius);
            final int maxCellY = this.cellOf(y + this.maxRadius);
            final int minCellZ = this.cellOf(z - this.maxRadius);
            final int maxCellZ = this.cellOf(z + this.maxRadius);
            for (int cellX = minCellX; cellX <= maxCellX; cellX++) {
                for (int cellY = minCellY; cellY <= maxCellY; cellY++) {
                    for (int cellZ = minCellZ; cellZ <= maxCellZ; cellZ++) {
                        int index = this.cellStart(cellKey(cellX, cellY, cellZ));
                        while (index != -1) {
                            final double distanceSquared = this.rangeDistanceSquared(index, x, y, z);
                            if (distanceSquared < closestDistanceSquared) {
                                closestDistanceSquared = distanceSquared;
                                closest = index;
                            }
                            index = this.cellNext[index];
                        }
                    }
                }
            }
        }

        if (closest == -1) return null;
        return Pair.of(BlockPos.of(this.snapshotPos[closest]), this.snapshotRadius[closest]);
    }
}
