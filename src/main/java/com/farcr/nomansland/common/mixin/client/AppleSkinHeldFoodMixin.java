package com.farcr.nomansland.common.mixin.client;

import com.farcr.nomansland.common.item.BandageHud;
import com.farcr.nomansland.common.mixin.plugin.annotation.IfModPresent;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import squeek.appleskin.client.HUDOverlayHandler;
import squeek.appleskin.helpers.FoodHelper;

@IfModPresent("appleskin")
@Mixin(HUDOverlayHandler.HeldFoodCache.class)
public class AppleSkinHeldFoodMixin {

    @ModifyReturnValue(method = "result", at = @At("RETURN"))
    private FoodHelper.QueriedFoodResult nml$bandageResult(FoodHelper.QueriedFoodResult original, int guiTick, Player player) {
        if (BandageHud.shouldShow(player)) {
            ItemStack bandage = BandageHud.held(player);
            FoodProperties food = BandageHud.food(bandage);
            return new FoodHelper.QueriedFoodResult(food, food, bandage);
        }
        return original;
    }
}
