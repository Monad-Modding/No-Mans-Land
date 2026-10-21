package com.farcr.nomansland.common.mixin;

import com.farcr.nomansland.common.block.torches.ExtinguishableBlockPairing;
import com.farcr.nomansland.common.registry.NMLRegistries;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(StandingAndWallBlockItem.class)
public class StandingAndWallBlockItemMixin {

    @ModifyReturnValue(method = "getPlacementState", at = @At("RETURN"))
    private BlockState nomansland$extinguishUnderwater(BlockState original, BlockPlaceContext context) {
        if (original == null) return null;
        FluidState fluid = context.getLevel().getFluidState(context.getClickedPos());
        if (!ExtinguishableBlockPairing.extinguishesIn(fluid)) return original;
        for (ExtinguishableBlockPairing pair : NMLRegistries.EXTINGUISHABLE_BLOCKS) {
            if (pair.isLitVersion(original)) {
                return pair.extinguishedIn(original, fluid);
            }
        }
        return original;
    }
}
