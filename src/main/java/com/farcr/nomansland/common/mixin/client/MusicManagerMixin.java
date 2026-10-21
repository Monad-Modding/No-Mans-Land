package com.farcr.nomansland.common.mixin.client;

import com.farcr.nomansland.client.music.AnachronisticMusicInjector;
import com.farcr.nomansland.client.music.ContextualMusicHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.sounds.Music;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MusicManager.class)
public abstract class MusicManagerMixin {

    /*
    * TODO: turn conditional music into its own embedded library mod eventually - liz :]
    */

    @Unique private final MusicManager NML$self = (MusicManager) (Object) this;
    @Shadow private SoundInstance currentMusic;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    public void NML$tickCustomMusic(CallbackInfo ci) {
        if (ContextualMusicHandler.tick(this.currentMusic, NML$self, Minecraft.getInstance().getSoundManager().soundEngine))
            ci.cancel();
    }

    @ModifyVariable(method = "startPlaying", at = @At("HEAD"), argsOnly = true)
    public Music NML$weighAnachronisticMusic(Music music) {
        return AnachronisticMusicInjector.select(music);
    }
}
