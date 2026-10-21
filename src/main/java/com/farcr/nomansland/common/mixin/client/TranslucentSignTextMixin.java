package com.farcr.nomansland.common.mixin.client;

import com.farcr.nomansland.client.TranslucentSignFading;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.block.entity.SignText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Function;

@Mixin(SignText.class)
public class TranslucentSignTextMixin {

    @Inject(method = "getRenderMessages", at = @At("HEAD"))
    private void nml$markSignSide(boolean renderMessagesFiltered, Function<Component, FormattedCharSequence> formatter, CallbackInfoReturnable<FormattedCharSequence[]> cir) {
        TranslucentSignFading.side((SignText) (Object) this);
    }
}
