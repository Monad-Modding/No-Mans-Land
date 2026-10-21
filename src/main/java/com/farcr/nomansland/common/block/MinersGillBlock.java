package com.farcr.nomansland.common.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class MinersGillBlock extends BushBlock {
    public static final MapCodec<MinersGillBlock> CODEC = simpleCodec(MinersGillBlock::new);
    public static final int MAX_AGE = 3;
    public static final int MATURE_AGE = 1;
    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
    private static final VoxelShape SHAPE = box(3.0, 0.0, 3.0, 13.0, 11.0, 13.0);
    private static final int ABSORB_RANGE = 4;

    public MinersGillBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
    }

    @Override
    public @NotNull MapCodec<? extends BushBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) {
        return state.is(BlockTags.DIRT) || state.is(BlockTags.MUSHROOM_GROW_BLOCK) || state.is(BlockTags.BASE_STONE_OVERWORLD);
    }

    @Override
    public boolean isRandomlyTicking(@NotNull BlockState state) {
        return true;
    }

    @Override
    public void randomTick(@NotNull BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        int age = state.getValue(AGE);
        if (age >= MAX_AGE) return;

        if (!absorbNearestGas(level, pos, ABSORB_RANGE)) return;

        react(level, pos, state, random);
    }

    private static void react(ServerLevel level, BlockPos pos, BlockState state, RandomSource random) {
        level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5, 8, 0.3, 0.3, 0.3, 0.0);

        if (state.getBlock() instanceof MinersGillBlock && state.hasProperty(AGE)) {
            int age = state.getValue(AGE);
            if (age < MAX_AGE && random.nextInt(3) == 0) level.setBlock(pos, state.setValue(AGE, age + 1), 2);
        }
    }

    private static boolean canFeed(BlockState state) {
        if (state.getBlock() instanceof PottedMinersGillBlock) return true;
        if (state.getBlock() instanceof MinersGillBlock) return state.getValue(AGE) < MAX_AGE;
        return false;
    }

    public static BlockPos findFeedable(ServerLevel level, BlockPos origin, int range) {
        BlockPos nearest = null;
        double nearestSq = Double.MAX_VALUE;
        for (BlockPos candidate : BlockPos.betweenClosed(origin.offset(-range, -range, -range), origin.offset(range, range, range))) {
            if (canFeed(level.getBlockState(candidate))) {
                double distSq = candidate.distSqr(origin);
                if (distSq < nearestSq) {
                    nearestSq = distSq;
                    nearest = candidate.immutable();
                }
            }
        }
        return nearest;
    }

    public static boolean feed(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!canFeed(state)) return false;
        react(level, pos, state, level.random);
        return true;
    }

    @Override
    public @NotNull BlockState playerWillDestroy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull Player player) {
        if (level instanceof ServerLevel serverLevel && state.getValue(AGE) >= MATURE_AGE && !player.getMainHandItem().is(Items.SHEARS)) {
            sporeBurst(serverLevel, pos, state);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    private void sporeBurst(ServerLevel level, BlockPos pos, BlockState state) {
        level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 40, 0.6, 0.4, 0.6, 0.0);

        int count = 1 + level.random.nextInt(2) + (state.getValue(AGE) >= MAX_AGE ? 1 : 0);
        BlockState young = this.defaultBlockState();
        int placed = 0;
        for (int attempt = 0; attempt < 12 && placed < count; attempt++) {
            BlockPos target = pos.offset(level.random.nextInt(5) - 2, level.random.nextInt(3) - 1, level.random.nextInt(5) - 2);
            if (level.getBlockState(target).isAir() && young.canSurvive(level, target)) {
                level.setBlock(target, young, 3);
                placed++;
            }
        }
    }

    public static boolean absorbNearestGas(ServerLevel level, BlockPos pos, int range) {
        BlockPos nearest = null;
        double nearestSq = Double.MAX_VALUE;
        for (BlockPos candidate : BlockPos.betweenClosed(pos.offset(-range, -range, -range), pos.offset(range, range, range))) {
            if (ToxicGasBlock.isToxicGas(level.getBlockState(candidate))) {
                double distSq = candidate.distSqr(pos);
                if (distSq < nearestSq) {
                    nearestSq = distSq;
                    nearest = candidate.immutable();
                }
            }
        }
        if (nearest == null) return false;
        level.removeBlock(nearest, false);
        return true;
    }
}
