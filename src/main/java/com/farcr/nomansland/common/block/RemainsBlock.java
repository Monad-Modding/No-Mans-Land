package com.farcr.nomansland.common.block;

import com.farcr.nomansland.NMLConfig;
import com.farcr.nomansland.common.blockentity.RemainsBlockEntity;
import com.farcr.nomansland.common.entity.Buried;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BrushableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class RemainsBlock extends BrushableBlock {
    public RemainsBlock(Block turnsInto, SoundEvent brushSound, SoundEvent brushCompletedSound, Properties properties) {
        super(turnsInto, brushSound, brushCompletedSound, properties);
    }

    @Override
    public void spawnAfterBreak(BlockState state, ServerLevel level, BlockPos pos, ItemStack stack, boolean dropExperience) {
        if (level.random.nextFloat() < NMLConfig.BURIED_SPAWNING_CHANCE.get() * 4) {
            Buried.spawnFromRemains(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        }
    }

    @Override
    public void onBrokenAfterFall(Level level, BlockPos pos, FallingBlockEntity fallingBlockEntity) {
        if (level instanceof ServerLevel serverLevel && level.random.nextFloat() < NMLConfig.BURIED_SPAWNING_CHANCE.get() * 10) {
            Buried.spawnFromRemains(serverLevel, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        }

        super.onBrokenAfterFall(level, pos, fallingBlockEntity);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RemainsBlockEntity(pos, state);
    }
}
