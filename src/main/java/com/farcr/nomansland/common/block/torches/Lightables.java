package com.farcr.nomansland.common.block.torches;

import com.farcr.nomansland.common.integration.Mods;
import com.farcr.nomansland.common.integration.MoonlightIntegration;
import com.farcr.nomansland.common.registry.NMLRegistries;
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
        if (Mods.MOONLIGHT.isLoaded() && MoonlightIntegration.isLightable(state)) {
            return MoonlightIntegration.light(level, pos, state, cause);
        }
        if (CampfireBlock.canLight(state) || CandleBlock.canLight(state) || CandleCakeBlock.canLight(state)) {
            level.setBlock(pos, state.setValue(BlockStateProperties.LIT, true), 11);
            return true;
        }
        return false;
    }
}
