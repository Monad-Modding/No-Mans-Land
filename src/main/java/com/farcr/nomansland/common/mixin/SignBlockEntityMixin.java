package com.farcr.nomansland.common.mixin;

import com.farcr.nomansland.common.extension.TranslucentSign;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SignBlockEntity.class)
public class SignBlockEntityMixin implements TranslucentSign {

    @Unique
    private boolean nml$translucent = false;

    @Override
    public boolean nml$isTranslucent() {
        return this.nml$translucent;
    }

    @Override
    public void nml$setTranslucent(boolean translucent) {
        this.nml$translucent = translucent;
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void nml$save(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        if (this.nml$translucent) {
            tag.putBoolean("translucent", true);
        }
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void nml$load(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        this.nml$translucent = tag.getBoolean("translucent");
    }
}
