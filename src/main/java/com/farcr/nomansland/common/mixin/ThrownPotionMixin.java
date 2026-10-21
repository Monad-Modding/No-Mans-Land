package com.farcr.nomansland.common.mixin;

import com.farcr.nomansland.common.block.torches.ExtinguishableBlockPairing;
import com.farcr.nomansland.common.registry.NMLRegistries;
import com.farcr.nomansland.common.registry.NMLSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThrownPotion.class)
public abstract class ThrownPotionMixin extends EntityMixin {

    @Inject(method = "dowseFire", at = @At("TAIL"))
    private void extinguishTorches(BlockPos pos, CallbackInfo ci) {
        BlockState state = level().getBlockState(pos);
        for (ExtinguishableBlockPairing pair : NMLRegistries.EXTINGUISHABLE_BLOCKS) {
            if (pair.isLitVersion(state)) {
                level().gameEvent(((ThrownPotion) (Object) this).getOwner(), GameEvent.BLOCK_CHANGE, pos);
                level().playSound(null, pos, NMLSounds.TORCH_EXTINGUISH.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                level().setBlock(pos, pair.extinguishedBlock().withPropertiesOf(state), 11);
                return;
            }
        }
    }
}
