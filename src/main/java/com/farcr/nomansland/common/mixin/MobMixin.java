package com.farcr.nomansland.common.mixin;

import com.farcr.nomansland.common.entity.billhook_bass.BillhookBass;
import com.farcr.nomansland.common.entity.cervidae.deer.Deer;
import com.farcr.nomansland.common.entity.cervidae.moose.Moose;
import com.farcr.nomansland.common.entity.clod.Clod;
import com.farcr.nomansland.common.entity.frienderman.Frienderman;
import com.farcr.nomansland.common.extension.EntityExtension;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public class MobMixin implements EntityExtension {

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;registerGoals()V"))
    private void noMansLand$registerExtraMobGoals(CallbackInfo ci) {
        Mob mob = (Mob)((Object)this);
        Deer.registerDeerRelatedGoals(mob);
        Moose.registerMooseRelatedGoals(mob);
        BillhookBass.registerBillhookBassRelatedGoals(mob);
        Frienderman.registerFriendermanRelatedGoals(mob);
        Clod.registerClodRelatedGoals(mob);
    }
}
