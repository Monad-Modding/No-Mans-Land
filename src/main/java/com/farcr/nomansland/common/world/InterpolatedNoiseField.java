package com.farcr.nomansland.common.world;

import net.minecraft.util.Mth;

import java.util.BitSet;

public final class InterpolatedNoiseField {
    public final int xzCellScale, xzCellSize, xzCellMask, xzCellCount, xzCellStride;
    public final int yCellScale, yCellSize, yCellMask, yCellCount;
    final double invXZCellSize;
    final double invYCellSize;
    final BitSet filledLayers, fillMask;
    public final double[] field;

    public InterpolatedNoiseField(int chunkHeight, int xzCellScale, int yCellScale) {
        this.xzCellScale = xzCellScale;
        this.xzCellSize = 1 << xzCellScale;
        this.xzCellMask = this.xzCellSize - 1;
        this.invXZCellSize = 1.0 / this.xzCellSize;
        this.xzCellCount = (16 >> xzCellScale) + 1;
        this.xzCellStride = this.xzCellCount * this.xzCellCount;

        this.yCellScale = yCellScale;
        this.yCellSize = 1 << yCellScale;
        this.yCellMask = this.yCellSize - 1;
        this.invYCellSize = 1.0 / this.yCellSize;
        this.yCellCount = (chunkHeight >> yCellScale) + 1;

        this.field = new double[this.xzCellStride * this.yCellCount];
        this.filledLayers = new BitSet(this.yCellCount);
        this.fillMask = new BitSet(this.yCellCount);
    }

    public double retrieve(int x, int y, int z) {
        int cellX = x >> xzCellScale,
            cellY = y >> yCellScale,
            cellZ = z >> xzCellScale;
        int localX = x & xzCellMask,
            localY = y & yCellMask,
            localZ = z & xzCellMask;
        double interpX = localX * invXZCellSize,
               interpY = localY * invYCellSize,
               interpZ = localZ * invXZCellSize;
        int nextX = cellX + ((localX | -localX) >>> 31),
            nextY = cellY + ((localY | -localY) >>> 31),
            nextZ = cellZ + ((localZ | -localZ) >>> 31);
        return Mth.lerp3(interpX, interpZ, interpY,
                field[cellX + cellZ * xzCellCount + cellY * xzCellStride], field[nextX + cellZ * xzCellCount + cellY * xzCellStride],
                field[cellX + nextZ * xzCellCount + cellY * xzCellStride], field[nextX + nextZ * xzCellCount + cellY * xzCellStride],
                field[cellX + cellZ * xzCellCount + nextY * xzCellStride], field[nextX + cellZ * xzCellCount + nextY * xzCellStride],
                field[cellX + nextZ * xzCellCount + nextY * xzCellStride], field[nextX + nextZ * xzCellCount + nextY * xzCellStride]
        );
    }

    public void fill(int startY, int endY, int minX, int minY, int minZ, NoiseFieldFiller filler) {
        // find unfilled layers
        fillMask.clear();
        fillMask.set(Math.max(0, startY >> yCellScale), Math.min(yCellCount - 1, endY >> yCellScale) + 1);
        fillMask.andNot(filledLayers);
        // fill them
        int startCellY = this.fillMask.nextSetBit(0);
        while (startCellY >= 0) {
            int endCellY = this.fillMask.nextClearBit(startCellY);
            this.fillInternal(startCellY, endCellY - 1, minX, minY, minZ, filler);
            startCellY = this.fillMask.nextSetBit(endCellY);
        }
        // finally, set filled layers
        filledLayers.or(fillMask);
    }

    public void fillInternal(int startCellY, int endCellY, int minX, int minY, int minZ, NoiseFieldFiller filler) {
        int index = startCellY * xzCellStride;
        for (int cellY = startCellY; cellY <= endCellY; cellY++) {
            int globalY = (cellY << yCellScale) + minY;
            for (int cellZ = 0; cellZ < xzCellCount; cellZ++) {
                int globalZ = (cellZ << xzCellScale) + minZ;
                for (int cellX = 0; cellX < xzCellCount; cellX++) {
                    int globalX = (cellX << xzCellScale) + minX;
                    field[index++] = filler.compute(globalX, globalY, globalZ);
                }
            }
        }
    }
}
