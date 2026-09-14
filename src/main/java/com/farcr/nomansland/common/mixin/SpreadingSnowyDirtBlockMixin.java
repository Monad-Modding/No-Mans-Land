package com.farcr.nomansland.common.mixin;

import com.farcr.nomansland.NMLConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SpreadingSnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SpreadingSnowyDirtBlock.class)
public abstract class SpreadingSnowyDirtBlockMixin {

    @Inject(method = "canPropagate", at = @At(value = "HEAD"), cancellable = true)
    private static void injected(BlockState state, LevelReader level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (state.getBlock().equals(Blocks.GRASS_BLOCK) && !NMLConfig.GRASS_SPREADS.get()) cir.setReturnValue(false);
        if (state.getBlock().equals(Blocks.MYCELIUM) && !NMLConfig.MYCELIUM_SPREADS.get()) cir.setReturnValue(false);
    }
}
