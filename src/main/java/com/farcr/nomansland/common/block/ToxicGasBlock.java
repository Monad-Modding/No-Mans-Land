package com.farcr.nomansland.common.block;

import com.farcr.nomansland.common.extension.LevelChunkExtension;
import com.farcr.nomansland.common.registry.NMLParticleTypes;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

public class ToxicGasBlock extends Block {
    public static final double HITBOX_HALF = 1.25;
    private static final int HITBOX_REACH = 2;
    private static final int PARTICLE_DELAY = 5;
    private static final int MAX_FLOW_CLUMP = 1024;
    private static final float OPEN_DISSIPATE_CHANCE = 0.01F;
    private static final float DISSIPATE_CHANCE = 0.005F;
    private static final float THIN_DISSIPATE_CHANCE = 0.0025F;
    private static final float SLIP_CHANCE = 0.5F;
    private static final float DISPLACED_VANISH_CHANCE = 0.5F;
    private static final int GILL_FEED_RANGE = 5;

    public ToxicGasBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public boolean propagatesSkylightDown(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) {
        return true;
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public void onPlace(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState oldState, boolean isMoving) {
        markChunk(level, pos);
        if (!oldState.is(this)) level.scheduleTick(pos, this, PARTICLE_DELAY);
    }

    private static void markChunk(final Level level, final BlockPos pos) {
        final ChunkAccess chunk = level.getChunk(SectionPos.blockToSectionCoord(pos.getX()),
                SectionPos.blockToSectionCoord(pos.getZ()), ChunkStatus.FULL, false);
        if (chunk instanceof LevelChunkExtension extension) {
            extension.nml$setMayHaveToxicGas(true);
        }
    }

    private static boolean chunkMayHaveGas(final ChunkAccess chunk) {
        if (!(chunk instanceof LevelChunkExtension extension)) return true;
        if (!extension.nml$toxicGasScanned()) {
            boolean present = false;
            for (final LevelChunkSection section : chunk.getSections()) {
                if (!section.hasOnlyAir() && section.maybeHas(ToxicGasBlock::isToxicGas)) {
                    present = true;
                    break;
                }
            }
            extension.nml$setMayHaveToxicGas(present);
        }
        return extension.nml$mayHaveToxicGas();
    }

    @Override
    public void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide() && !newState.is(this) && !newState.isAir() && newState.getFluidState().isEmpty() && level instanceof ServerLevel serverLevel && serverLevel.getRandom().nextFloat() >= DISPLACED_VANISH_CHANCE) {
            place(serverLevel, pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public void tick(@NotNull BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        if (canFlowInto(level.getBlockState(pos.below()))) {
            level.removeBlock(pos, false);
            place(level, pos.below());
            return;
        }

        BlockPos slip = random.nextFloat() < SLIP_CHANCE ? findSlip(level, pos, random) : null;
        if (slip != null) {
            level.removeBlock(pos, false);
            place(level, slip);
            return;
        }

        int air = countOpenNeighbors(level, pos);
        if (air >= 2 && random.nextFloat() < (air >= 4 ? OPEN_DISSIPATE_CHANCE : air == 3 ? DISSIPATE_CHANCE : THIN_DISSIPATE_CHANCE)) {
            level.removeBlock(pos, false);
            return;
        }

        level.sendParticles(NMLParticleTypes.TOXIC_GAS.get(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1, 0.45, 0.45, 0.45, 0.0);
        level.scheduleTick(pos, this, PARTICLE_DELAY + random.nextInt(PARTICLE_DELAY));
    }

    private static BlockPos findSlip(ServerLevel level, BlockPos pos, RandomSource random) {
        boolean onGas = isToxicGas(level.getBlockState(pos.below()));
        int start = random.nextInt(4);
        BlockPos sideways = null;
        for (int i = 0; i < 4; i++) {
            Direction direction = Direction.from2DDataValue(start + i);
            BlockPos side = pos.relative(direction);
            if (!canFlowInto(level.getBlockState(side))) continue;
            if (canFlowInto(level.getBlockState(side.below()))) return side.below();
            if (onGas && sideways == null) {
                sideways = side;
            }
        }
        return sideways;
    }

    private static int countOpenNeighbors(ServerLevel level, BlockPos pos) {
        int count = 0;
        for (Direction direction : Direction.values()) {
            BlockPos neighbor = pos.relative(direction);
            BlockState state = level.getBlockState(neighbor);
            if (!isToxicGas(state) && !state.isCollisionShapeFullBlock(level, neighbor)) count++;
        }
        return count;
    }

    public static boolean isToxicGas(BlockState state) {
        return state.getBlock() instanceof ToxicGasBlock;
    }

    public static boolean canFlowInto(BlockState state) {
        if (isToxicGas(state)) return false;
        if (state.isAir()) return true;
        return state.canBeReplaced() && state.getFluidState().isEmpty();
    }

    private static void setGas(LevelAccessor level, BlockPos pos) {
        if (!level.getBlockState(pos).isAir() && level instanceof Level realLevel) {
            realLevel.destroyBlock(pos, false);
        }
        Block block = NMLBlocks.TOXIC_GAS.get();
        level.setBlock(pos, block.defaultBlockState(), 3);
        level.scheduleTick(pos, block, PARTICLE_DELAY);
    }

    public static void place(LevelAccessor level, BlockPos pos) {
        if (canFlowInto(level.getBlockState(pos))) {
            setGas(level, pos.immutable());
            return;
        }
        BlockPos free = findFreeSpot(level, pos);
        if (free != null) setGas(level, free);
    }

    private static BlockPos findFreeSpot(LevelAccessor level, BlockPos origin) {
        Set<BlockPos> clump = new HashSet<>();
        Deque<BlockPos> queue = new ArrayDeque<>();
        if (isToxicGas(level.getBlockState(origin))) {
            BlockPos start = origin.immutable();
            clump.add(start);
            queue.add(start);
        } else {
            for (Direction direction : Direction.values()) {
                BlockPos neighbor = origin.relative(direction).immutable();
                if (isToxicGas(level.getBlockState(neighbor)) && clump.add(neighbor)) queue.add(neighbor);
            }
        }
        if (clump.isEmpty()) return null;

        while (!queue.isEmpty() && clump.size() < MAX_FLOW_CLUMP) {
            BlockPos current = queue.poll();
            for (Direction direction : Direction.values()) {
                BlockPos neighbor = current.relative(direction).immutable();
                if (isToxicGas(level.getBlockState(neighbor)) && clump.add(neighbor)) queue.add(neighbor);
            }
        }

        for (BlockPos p : clump) {
            BlockPos below = p.below();
            if (canFlowInto(level.getBlockState(below))) return below;
        }
        for (BlockPos p : clump) {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos side = p.relative(direction);
                if (canFlowInto(level.getBlockState(side))) return side;
            }
        }
        for (BlockPos p : clump) {
            BlockPos above = p.above();
            if (canFlowInto(level.getBlockState(above))) return above;
        }
        return null;
    }

    public static void scatterAround(ServerLevel level, BlockPos center, int count) {
        BlockPos gill = MinersGillBlock.findFeedable(level, center, GILL_FEED_RANGE);
        for (int i = 0; i < count; i++) {
            if (gill != null && MinersGillBlock.feed(level, gill)) continue;
            gill = null;
            place(level, center);
        }
    }

    public static void emitInFront(ServerLevel level, BlockPos containerPos, Direction facing) {
        BlockPos gill = MinersGillBlock.findFeedable(level, containerPos, GILL_FEED_RANGE);
        if (gill != null && MinersGillBlock.feed(level, gill)) return;
        place(level, containerPos.relative(facing));
    }

    public static int clearTouching(ServerLevel level, LivingEntity entity) {
        AABB box = entity.getBoundingBox();
        if (!mightHaveGasNear(level, box)) return 0;
        int cleared = 0;
        for (BlockPos pos : BlockPos.betweenClosed(BlockPos.containing(box.minX - HITBOX_REACH, box.minY - HITBOX_REACH, box.minZ - HITBOX_REACH), BlockPos.containing(box.maxX + HITBOX_REACH, box.maxY + HITBOX_REACH, box.maxZ + HITBOX_REACH))) {
            if (isToxicGas(level.getBlockState(pos)) && gasBox(pos).intersects(box)) {
                level.removeBlock(pos, false);
                cleared++;
            }
        }
        return cleared;
    }

    public static void clearArea(ServerLevel level, Vec3 center, double radius) {
        int r = Mth.ceil(radius);
        double radiusSq = radius * radius;
        BlockPos origin = BlockPos.containing(center);
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-r, -r, -r), origin.offset(r, r, r))) {
            if (isToxicGas(level.getBlockState(pos)) && pos.distToCenterSqr(center.x, center.y, center.z) <= radiusSq) {
                level.removeBlock(pos, false);
            }
        }
    }

    private static AABB gasBox(BlockPos pos) {
        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 0.5;
        double cz = pos.getZ() + 0.5;
        return new AABB(cx - HITBOX_HALF, cy - HITBOX_HALF, cz - HITBOX_HALF, cx + HITBOX_HALF, cy + HITBOX_HALF, cz + HITBOX_HALF);
    }

    private static boolean mightHaveGasNear(final Level level, final AABB box) {
        final int minSectionX = SectionPos.blockToSectionCoord(Mth.floor(box.minX - HITBOX_REACH));
        final int maxSectionX = SectionPos.blockToSectionCoord(Mth.floor(box.maxX + HITBOX_REACH));
        final int minSectionY = SectionPos.blockToSectionCoord(Mth.floor(box.minY - HITBOX_REACH));
        final int maxSectionY = SectionPos.blockToSectionCoord(Mth.floor(box.maxY + HITBOX_REACH));
        final int minSectionZ = SectionPos.blockToSectionCoord(Mth.floor(box.minZ - HITBOX_REACH));
        final int maxSectionZ = SectionPos.blockToSectionCoord(Mth.floor(box.maxZ + HITBOX_REACH));

        for (int sectionX = minSectionX; sectionX <= maxSectionX; sectionX++) {
            for (int sectionZ = minSectionZ; sectionZ <= maxSectionZ; sectionZ++) {
                final ChunkAccess chunk = level.getChunk(sectionX, sectionZ, ChunkStatus.FULL, false);
                if (chunk == null) return false;
                if (!chunkMayHaveGas(chunk)) continue;

                final LevelChunkSection[] sections = chunk.getSections();
                for (int sectionY = minSectionY; sectionY <= maxSectionY; sectionY++) {
                    final int index = chunk.getSectionIndexFromSectionY(sectionY);
                    if (index < 0 || index >= sections.length) continue;

                    final LevelChunkSection section = sections[index];
                    if (section.hasOnlyAir()) continue;
                    if (section.maybeHas(ToxicGasBlock::isToxicGas)) return true;
                }
            }
        }

        return false;
    }

    public static boolean isBodyInGas(final LivingEntity entity) {
        final Level level = entity.level();
        final AABB box = entity.getBoundingBox();

        if (!mightHaveGasNear(level, box)) return false;

        for (final BlockPos pos : BlockPos.betweenClosed(
                BlockPos.containing(box.minX - HITBOX_REACH, box.minY - HITBOX_REACH, box.minZ - HITBOX_REACH),
                BlockPos.containing(box.maxX + HITBOX_REACH, box.maxY + HITBOX_REACH, box.maxZ + HITBOX_REACH))) {
            if (isToxicGas(level.getBlockState(pos)) && gasBox(pos).intersects(box)) return true;
        }

        return false;
    }

    public static boolean isHeadInGas(LivingEntity entity) {
        Level level = entity.level();
        Vec3 eye = entity.getEyePosition();
        if (!mightHaveGasNear(level, new AABB(eye, eye))) return false;

        BlockPos origin = BlockPos.containing(eye);
        for (int x = -HITBOX_REACH; x <= HITBOX_REACH; x++) {
            for (int y = -HITBOX_REACH; y <= HITBOX_REACH; y++) {
                for (int z = -HITBOX_REACH; z <= HITBOX_REACH; z++) {
                    BlockPos pos = origin.offset(x, y, z);
                    if (isToxicGas(level.getBlockState(pos)) && gasBox(pos).contains(eye)) return true;
                }
            }
        }
        return false;
    }
}
