package com.farcr.nomansland.common.blockentity;

import com.farcr.nomansland.common.block.VentBlock;
import com.farcr.nomansland.common.registry.NMLBlockEntities;
import com.farcr.nomansland.common.registry.NMLParticleTypes;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class VentBlockEntity extends BlockEntity {
    private static final int RECOMPUTE_INTERVAL = 100;
    private static final int ENTITY_SCAN_INTERVAL = 5;
    private static final int MAX_AREA_BUBBLES = 3;
    private static final double PARTICLE_RANGE = 32.0;

    private final Long2IntOpenHashMap affectedWater = new Long2IntOpenHashMap();
    private final LongArrayList affectedCells = new LongArrayList();
    private final LongArrayList surfaceCells = new LongArrayList();
    private AABB bounds;
    private int recomputeCooldown;

    public VentBlockEntity(BlockPos pos, BlockState state) {
        super(NMLBlockEntities.VENT.get(), pos, state);
        affectedWater.defaultReturnValue(-1);
        recomputeCooldown = Math.floorMod(pos.hashCode(), RECOMPUTE_INTERVAL);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, VentBlockEntity vent) {
        if (!(state.getBlock() instanceof VentBlock ventBlock)) return;
        vent.refreshAffectedWater(level, pos, state);
        if (vent.affectedWater.isEmpty()) return;
        if (level.getGameTime() % ENTITY_SCAN_INTERVAL != 0) return;

        for (Entity entity : level.getEntitiesOfClass(Entity.class, vent.bounds)) {
            int distance = vent.touchDistance(entity);
            if (distance < 0) continue;
            ventBlock.affectEntity(level, pos, entity, distance);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, VentBlockEntity vent) {
        if (!(state.getBlock() instanceof VentBlock ventBlock)) return;
        vent.refreshAffectedWater(level, pos, state);
        if (vent.affectedWater.isEmpty()) return;
        if (level.getNearestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, PARTICLE_RANGE, false) == null) return;

        ParticleOptions bubble = NMLParticleTypes.VENT_BUBBLE.get();
        ParticleOptions ventSmoke = ventBlock.getVentParticle();
        RandomSource random = level.getRandom();
        Direction facing = state.getValue(VentBlock.FACING);

        if (vent.affectedWater.containsKey(pos.relative(facing).asLong())) {
            for (int i = 0; i < 2; i++) {
                if (random.nextFloat() >= 0.6F) continue;
                double x = pos.getX() + 0.5 + facing.getStepX() * 0.6 + (random.nextDouble() - 0.5) * 0.5;
                double y = pos.getY() + 0.5 + facing.getStepY() * 0.6 + (random.nextDouble() - 0.5) * 0.5;
                double z = pos.getZ() + 0.5 + facing.getStepZ() * 0.6 + (random.nextDouble() - 0.5) * 0.5;
                level.addParticle(bubble, x, y, z,
                        facing.getStepX() * 0.08 + (random.nextDouble() - 0.5) * 0.05,
                        0.06 + random.nextDouble() * 0.06 + facing.getStepY() * 0.08,
                        facing.getStepZ() * 0.08 + (random.nextDouble() - 0.5) * 0.05);
            }
        }

        if (ventSmoke != null && random.nextFloat() < 0.5F) {
            double x = pos.getX() + 0.5 + facing.getStepX() * 0.5 + (random.nextDouble() - 0.5) * 0.4;
            double y = pos.getY() + 0.5 + facing.getStepY() * 0.5 + (random.nextDouble() - 0.5) * 0.4;
            double z = pos.getZ() + 0.5 + facing.getStepZ() * 0.5 + (random.nextDouble() - 0.5) * 0.4;
            level.addParticle(ventSmoke, x, y, z, 0.0, 0.02, 0.0);
        }

        int bubbleCount = Math.min(MAX_AREA_BUBBLES, 1 + vent.affectedCells.size() / 48);
        for (int i = 0; i < bubbleCount; i++) {
            long cell = vent.affectedCells.getLong(random.nextInt(vent.affectedCells.size()));
            level.addParticle(bubble,
                    BlockPos.getX(cell) + random.nextDouble(), BlockPos.getY(cell) + random.nextDouble(), BlockPos.getZ(cell) + random.nextDouble(),
                    (random.nextDouble() - 0.5) * 0.03, 0.05 + random.nextDouble() * 0.05, (random.nextDouble() - 0.5) * 0.03);
        }

        if (!vent.surfaceCells.isEmpty()) {
            if (ventSmoke == null && random.nextFloat() < 0.3F) {
                long cell = vent.surfaceCells.getLong(random.nextInt(vent.surfaceCells.size()));
                level.addParticle(ParticleTypes.CLOUD,
                        BlockPos.getX(cell) + random.nextDouble(), BlockPos.getY(cell) + 1.0 + random.nextDouble() * 0.2, BlockPos.getZ(cell) + random.nextDouble(),
                        (random.nextDouble() - 0.5) * 0.02, 0.03 + random.nextDouble() * 0.03, (random.nextDouble() - 0.5) * 0.02);
            }

            if (random.nextInt(80) == 0) {
                long cell = vent.surfaceCells.getLong(random.nextInt(vent.surfaceCells.size()));
                level.playLocalSound(BlockPos.getX(cell) + 0.5, BlockPos.getY(cell) + 1.0, BlockPos.getZ(cell) + 0.5,
                        SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT, SoundSource.BLOCKS,
                        0.4F, 0.8F + random.nextFloat() * 0.4F, false);
            }
        }
    }

    private void refreshAffectedWater(Level level, BlockPos pos, BlockState state) {
        if (recomputeCooldown-- > 0) return;
        recomputeCooldown = RECOMPUTE_INTERVAL;

        affectedWater.clear();
        affectedCells.clear();
        surfaceCells.clear();
        bounds = null;
        VentBlock.computeAffectedWater(level, pos, state.getValue(VentBlock.FACING), affectedWater);
        if (affectedWater.isEmpty()) return;

        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        for (long cell : affectedWater.keySet()) {
            affectedCells.add(cell);
            int x = BlockPos.getX(cell), y = BlockPos.getY(cell), z = BlockPos.getZ(cell);
            if (x < minX) minX = x;
            if (y < minY) minY = y;
            if (z < minZ) minZ = z;
            if (x > maxX) maxX = x;
            if (y > maxY) maxY = y;
            if (z > maxZ) maxZ = z;
            cursor.set(x, y + 1, z);
            if (!VentBlock.isWater(level, cursor)) surfaceCells.add(cell);
        }
        bounds = new AABB(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1).inflate(0.5);
    }

    private int touchDistance(Entity entity) {
        int best = -1;
        AABB box = entity.getBoundingBox().deflate(0.001);
        int minX = Mth.floor(box.minX), minY = Mth.floor(box.minY), minZ = Mth.floor(box.minZ);
        int maxX = Mth.floor(box.maxX), maxY = Mth.floor(box.maxY), maxZ = Mth.floor(box.maxZ);
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    int distance = affectedWater.get(BlockPos.asLong(x, y, z));
                    if (distance >= 0 && (best < 0 || distance < best)) best = distance;
                }
            }
        }
        return best;
    }
}
