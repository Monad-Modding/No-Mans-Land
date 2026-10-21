package com.farcr.nomansland.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class SulfurOreBlock extends Block {
    private static final Set<BlockPos> PRIMED = new HashSet<>();
    private static final float EXPLOSION_POWER = 2.5F;

    public SulfurOreBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void onPlace(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState oldState, boolean isMoving) {
        if (level instanceof ServerLevel serverLevel) scheduleFireCheck(serverLevel, pos);
    }

    @Override
    public void neighborChanged(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Block block, @NotNull BlockPos fromPos, boolean isMoving) {
        if (level instanceof ServerLevel serverLevel) scheduleFireCheck(serverLevel, pos);
    }

    @Override
    public void tick(@NotNull BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        if (PRIMED.remove(pos)) {
            detonate(level, pos);
            return;
        }

        if (!hasAdjacentFire(level, pos)) return;

        for (Direction direction : Direction.values()) {
            BlockPos target = pos.relative(direction);
            if (ToxicGasBlock.canFlowInto(level.getBlockState(target)) && random.nextFloat() < 0.5F) {
                ToxicGasBlock.place(level, target);
                break;
            }
        }
        level.scheduleTick(pos, this, 20 + random.nextInt(20));
    }

    private void scheduleFireCheck(ServerLevel level, BlockPos pos) {
        if (hasAdjacentFire(level, pos) && !level.getBlockTicks().hasScheduledTick(pos, this)) {
            level.scheduleTick(pos, this, 20);
        }
    }

    private static boolean hasAdjacentFire(Level level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (level.getBlockState(pos.relative(direction)).is(BlockTags.FIRE)) return true;
        }
        return false;
    }

    public static boolean isSulfurOre(BlockState state) {
        return state.getBlock() instanceof SulfurOreBlock;
    }

    public static void prime(ServerLevel level, BlockPos pos) {
        if (!isSulfurOre(level.getBlockState(pos))) return;
        BlockPos immutable = pos.immutable();
        if (PRIMED.add(immutable)) {
            level.scheduleTick(immutable, level.getBlockState(immutable).getBlock(), 2 + level.random.nextInt(4));
        }
    }

    private static void detonate(ServerLevel level, BlockPos pos) {
        if (!isSulfurOre(level.getBlockState(pos))) return;
        RandomSource random = level.random;
        level.removeBlock(pos, false);
        level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, EXPLOSION_POWER, Level.ExplosionInteraction.BLOCK);

        if (random.nextFloat() < 0.7F) ToxicGasBlock.place(level, pos);
        for (Direction direction : Direction.values()) {
            if (random.nextFloat() < 0.3F) ToxicGasBlock.place(level, pos.relative(direction));
        }

        if (random.nextFloat() < 0.15F && level.getBlockState(pos).isAir()
                && level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)) {
            level.setBlock(pos, Blocks.FIRE.defaultBlockState(), 3);
        }
    }
}
