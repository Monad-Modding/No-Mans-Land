package com.farcr.nomansland.common.mixin.integration;

import com.farcr.nomansland.common.integration.nirvana.NirvanaGasMaskHandler;
import com.farcr.nomansland.common.mixin.plugin.annotation.IfModPresent;
import galena.nirvana.world.item.SmokingItem;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@IfModPresent("nirvana")
@Mixin(SmokingItem.class)
public class SmokingItemMixin {

    @Inject(method = "applyEffect", at = @At("HEAD"), cancellable = true, remap = false)
    private static void nml$gasMaskFiltersSmoke(MobEffectInstance effect, ItemStack stack, LivingEntity target, LivingEntity source, CallbackInfo ci) {
        if (NirvanaGasMaskHandler.isAirborneEffect(effect) && NirvanaGasMaskHandler.filterSmoke(target)) ci.cancel();
    }
}
