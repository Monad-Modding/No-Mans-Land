package com.farcr.nomansland.common.world.feature;

import com.farcr.nomansland.common.block.ToxicGasBlock;
import com.farcr.nomansland.common.block.VentBlock;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import com.farcr.nomansland.common.world.generation.GeothermalSampler;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UndergroundPoolFeature extends Feature<UndergroundPoolConfiguration> {
    private static final double NO_VENT_MAX = -0.25;
    private static final double THERMAL_MAX = 0.5;
    private static final double LAKE_DIAMETER_FRACTION = 0.5;
    private static final double SHORE_SLOPE = 0.18;
    private static final int BOTTOM_OPEN_TOLERANCE = 2;
    private static final double TOP_OPEN_FRACTION_MAX = 0.4;
    private static final int WATER_PER_EXTRA_VENT = 80;
    private static final int MAX_VENTS = 3;

    public UndergroundPoolFeature(Codec<UndergroundPoolConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<UndergroundPoolConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        UndergroundPoolConfiguration config = context.config();

        if (!level.getBlockState(origin).isSolid() || !level.getBlockState(origin.below()).isSolid()) return false;

        double geothermality = GeothermalSampler.sample(level, origin);
        boolean sulfuric = geothermality >= THERMAL_MAX;
        boolean thermal = geothermality >= NO_VENT_MAX && geothermality < THERMAL_MAX;

        double radius = config.cavernDiameter().sample(random) / 2.0;
        int cavernHeight = config.cavernHeight().sample(random);
        int lakeDepth = config.lakeDepth().sample(random);
        int waterHeight = config.waterHeight().sample(random);
        double lakeRadius = radius * LAKE_DIAMETER_FRACTION;

        double pA = random.nextDouble() * Math.PI * 2.0;
        double pB = random.nextDouble() * Math.PI * 2.0;
        double pC = random.nextDouble() * Math.PI * 2.0;
        double pD = random.nextDouble() * Math.PI * 2.0;
        double pE = random.nextDouble() * Math.PI * 2.0;
        double pF = random.nextDouble() * Math.PI * 2.0;
        double pG = random.nextDouble() * Math.PI * 2.0;
        double pH = random.nextDouble() * Math.PI * 2.0;
        double pI = random.nextDouble() * Math.PI * 2.0;

        int cx = origin.getX();
        int floorY = origin.getY();
        int cz = origin.getZ();
        int midY = floorY + cavernHeight / 2;
        int rCeil = Mth.ceil(radius * 1.35) + 2;
        int waterLevelY = Mth.clamp(floorY - lakeDepth + waterHeight - 1, floorY - lakeDepth, floorY);

        List<BlockPos> waterList = new ArrayList<>();
        Set<BlockPos> waterSet = new HashSet<>();
        List<BlockPos> airList = new ArrayList<>();
        int bottomOpen = 0;
        int topOpen = 0;
        int topTotal = 0;

        for (int dx = -rCeil; dx <= rCeil; dx++) {
            for (int dz = -rCeil; dz <= rCeil; dz++) {
                double d = Math.sqrt((double) dx * dx + (double) dz * dz);
                double theta = Math.atan2(dz, dx);

                double boundary = radius * (1.0 + 0.14 * Math.sin(theta + pA) + 0.10 * Math.sin(2.0 * theta + pB) + 0.06 * Math.sin(3.0 * theta + pC));
                if (d > boundary) continue;

                double q = Mth.clamp(d / boundary, 0.0, 1.0);
                double dome = Math.sqrt(Math.max(0.0, 1.0 - q * q));
                int ceilingY = floorY + (int) Math.round(cavernHeight * dome)
                        + (int) Math.round(1.3 * Math.sin((cx + dx) * 0.28 + pF) * Math.cos((cz + dz) * 0.31 + pG));
                if (ceilingY <= floorY) continue;

                double lakeBoundary = lakeRadius * (1.0 + 0.20 * Math.sin(2.0 * theta + pD) + 0.12 * Math.sin(3.0 * theta + pE));

                int airStart;
                if (lakeBoundary > 0.0 && d <= lakeBoundary) {
                    double lakeQ = Mth.clamp(d / lakeBoundary, 0.0, 1.0);
                    int basinBottomY = floorY - (int) Math.round(lakeDepth * (1.0 - lakeQ * lakeQ));
                    if (basinBottomY <= waterLevelY) {
                        for (int y = basinBottomY; y <= waterLevelY; y++) {
                            BlockPos pos = new BlockPos(cx + dx, y, cz + dz);
                            waterList.add(pos);
                            waterSet.add(pos);
                            if (!level.getBlockState(pos).isSolid()) bottomOpen++;
                        }
                        airStart = waterLevelY + 1;
                    } else {
                        airStart = basinBottomY;
                    }
                } else {
                    int shoreTop = floorY + (int) Math.round(SHORE_SLOPE * (d - lakeRadius))
                            + (int) Math.round(0.7 * Math.sin((cx + dx) * 0.3 + pH) * Math.cos((cz + dz) * 0.33 + pI));
                    if (shoreTop < floorY) shoreTop = floorY;
                    airStart = shoreTop + 1;
                }

                for (int y = airStart; y <= ceilingY; y++) {
                    BlockPos pos = new BlockPos(cx + dx, y, cz + dz);
                    airList.add(pos);
                    boolean open = !level.getBlockState(pos).isSolid();
                    if (y < midY) {
                        if (open) bottomOpen++;
                    } else {
                        topTotal++;
                        if (open) topOpen++;
                    }
                }
            }
        }

        if (airList.isEmpty()) return false;
        if (bottomOpen > BOTTOM_OPEN_TOLERANCE) return false;
        if (topTotal > 0 && (double) topOpen / topTotal > TOP_OPEN_FRACTION_MAX) return false;

        for (BlockPos pos : waterList) level.setBlock(pos, Blocks.WATER.defaultBlockState(), 2);
        for (BlockPos pos : airList) level.setBlock(pos, Blocks.CAVE_AIR.defaultBlockState(), 2);

        if (sulfuric || thermal) {
            Block ventBlock = sulfuric ? NMLBlocks.SULFURIC_FUMAROLE.get() : NMLBlocks.THERMAL_VENT.get();
            int ventCount = Mth.clamp(1 + waterSet.size() / WATER_PER_EXTRA_VENT, 1, MAX_VENTS);
            placeVents(level, waterSet, ventBlock, ventCount, lakeRadius, random);
        }

        if (sulfuric) {
            placeExposedVeins(context, waterSet, airList, midY, geothermality);
            double gasFraction = Mth.clamp((geothermality - THERMAL_MAX) / (1.0 - THERMAL_MAX), 0.0, 1.0);
            int gasTop = waterLevelY + 1 + (int) Math.round(gasFraction * (config.maxGasLayers() - 1));
            for (BlockPos pos : airList) {
                if (pos.getY() > waterLevelY && pos.getY() <= gasTop && level.getBlockState(pos).isAir()) {
                    ToxicGasBlock.place(level, pos);
                }
            }
        }

        return true;
    }

    private static void placeVents(WorldGenLevel level, Set<BlockPos> waterCells, Block ventBlock, int ventCount, double lakeRadius, RandomSource random) {
        List<BlockPos> seats = new ArrayList<>();
        for (BlockPos water : waterCells) {
            if (waterCells.contains(water.above()) && level.getBlockState(water.below()).isSolid()) seats.add(water);
        }
        if (seats.isEmpty()) return;

        double minSepSq = Math.max(3.0, lakeRadius * 0.6);
        minSepSq *= minSepSq;
        List<BlockPos> chosen = new ArrayList<>();
        int maxAttempts = seats.size() * 8 + 16;
        for (int attempt = 0; chosen.size() < ventCount && attempt < maxAttempts; attempt++) {
            BlockPos candidate = seats.get(random.nextInt(seats.size()));
            if (chosen.contains(candidate)) continue;
            boolean ok = true;
            for (BlockPos c : chosen) {
                int dx = candidate.getX() - c.getX();
                int dz = candidate.getZ() - c.getZ();
                if ((double) dx * dx + (double) dz * dz < minSepSq) {
                    ok = false;
                    break;
                }
            }
            if (ok) chosen.add(candidate);
        }
        if (chosen.isEmpty()) chosen.add(seats.get(random.nextInt(seats.size())));

        for (BlockPos seat : chosen) {
            level.setBlock(seat, ventBlock.defaultBlockState()
                    .setValue(DirectionalBlock.FACING, Direction.UP)
                    .setValue(VentBlock.WATERLOGGED, true), 2);
            waterCells.remove(seat);
        }
    }

    private static void placeExposedVeins(FeaturePlaceContext<UndergroundPoolConfiguration> context, Set<BlockPos> waterCells, List<BlockPos> airCells, int midY, double geothermality) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();

        List<BlockPos> floorShell = new ArrayList<>();
        for (BlockPos water : waterCells) {
            for (Direction direction : Direction.values()) {
                BlockPos neighbor = water.relative(direction);
                if (!waterCells.contains(neighbor) && level.getBlockState(neighbor).isSolid()) floorShell.add(neighbor);
            }
        }

        List<BlockPos> upperShell = new ArrayList<>();
        for (BlockPos air : airCells) {
            if (air.getY() < midY) continue;
            for (Direction direction : Direction.values()) {
                BlockPos neighbor = air.relative(direction);
                if (level.getBlockState(neighbor).isSolid()) upperShell.add(neighbor);
            }
        }

        int veinCount = context.config().veinCount().sample(random);
        double veinChance = Mth.clamp(geothermality, 0.0, 1.0);
        placeVeinsOn(context, floorShell, veinCount, veinChance, random);
        placeVeinsOn(context, upperShell, veinCount, veinChance, random);
    }

    private static void placeVeinsOn(FeaturePlaceContext<UndergroundPoolConfiguration> context, List<BlockPos> shell, int count, double veinChance, RandomSource random) {
        if (shell.isEmpty()) return;
        for (int i = 0; i < count; i++) {
            if (random.nextDouble() >= veinChance) continue;
            BlockPos spot = shell.get(random.nextInt(shell.size()));
            context.config().veinFeature().value().place(context.level(), context.chunkGenerator(), random, spot);
        }
    }
}
