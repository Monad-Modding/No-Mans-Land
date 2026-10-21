package com.farcr.nomansland.common.mixin;

import com.farcr.nomansland.common.registry.items.NMLItems;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.world.inventory.BrewingStandMenu$FuelSlot")
public class BrewingStandMenuMixin {

    @Inject(method = "mayPlaceItem", at = @At("HEAD"), cancellable = true)
    private static void nml$sulfurIsFuel(ItemStack itemStack, CallbackInfoReturnable<Boolean> cir) {
        if (itemStack.is(NMLItems.SULFUR)) cir.setReturnValue(true);
    }
}
