package com.farcr.nomansland.common.block.torches;

import com.farcr.nomansland.common.registry.NMLRegistries;
import net.mehvahdjukaar.moonlight.api.block.ILightable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

public final class Lightables {

    private Lightables() {
    }

    public static boolean light(Level level, BlockPos pos, BlockState state, @Nullable Entity cause) {
        for (ExtinguishableBlockPairing pair : NMLRegistries.EXTINGUISHABLE_BLOCKS) {
            if (pair.canRelight(state)) {
                level.setBlockAndUpdate(pos, pair.litBlock().withPropertiesOf(state));
                return true;
            }
        }
        if (state.getBlock() instanceof ILightable lightable) {
            return lightable.tryLightUp(cause, state, pos, level, ILightable.FireSoundType.FIRE_CHANGE);
        }
        if (CampfireBlock.canLight(state) || CandleBlock.canLight(state) || CandleCakeBlock.canLight(state)) {
            level.setBlock(pos, state.setValue(BlockStateProperties.LIT, true), 11);
            return true;
        }
        return false;
    }
}
