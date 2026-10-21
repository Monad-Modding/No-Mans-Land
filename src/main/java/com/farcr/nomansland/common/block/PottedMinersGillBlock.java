package com.farcr.nomansland.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class PottedMinersGillBlock extends FlowerPotBlock {
    private static final int ABSORB_RANGE = 3;

    public PottedMinersGillBlock(Supplier<FlowerPotBlock> emptyPot, Supplier<? extends Block> potted, Properties properties) {
        super(emptyPot, potted, properties);
    }

    @Override
    public boolean isRandomlyTicking(@NotNull BlockState state) {
        return true;
    }

    @Override
    public void randomTick(@NotNull BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        if (MinersGillBlock.absorbNearestGas(level, pos, ABSORB_RANGE)) {
            level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, pos.getX() + 0.5, pos.getY() + 0.7, pos.getZ() + 0.5, 5, 0.2, 0.2, 0.2, 0.0);
        }
    }
}
