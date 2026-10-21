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
    private boolean nml$translucentFront;
    @Unique
    private boolean nml$translucentBack;

    @Override
    public boolean nml$isTranslucent(boolean front) {
        return front ? this.nml$translucentFront : this.nml$translucentBack;
    }

    @Override
    public void nml$setTranslucent(boolean front, boolean translucent) {
        if (front) {
            this.nml$translucentFront = translucent;
        } else {
            this.nml$translucentBack = translucent;
        }
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void nml$save(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        if (this.nml$translucentFront) {
            tag.putBoolean("translucent_front", true);
        }
        if (this.nml$translucentBack) {
            tag.putBoolean("translucent_back", true);
        }
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void nml$load(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        this.nml$translucentFront = tag.getBoolean("translucent_front");
        this.nml$translucentBack = tag.getBoolean("translucent_back");
    }
}
