package com.farcr.nomansland.client.renderer.dreams;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class DreamAmbientSoundInstance extends AbstractTickableSoundInstance {
    public static final float TARGET_VOLUME = 0.2f;
    public static final float FADE_IN_RATE = TARGET_VOLUME / 40f;

    public DreamAmbientSoundInstance() {
        super(SoundEvents.AMBIENT_UNDERWATER_LOOP, SoundSource.AMBIENT, RandomSource.create());
        this.looping = true;
        this.delay = 0;
        this.volume = 0f;
        this.relative = true;
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }

    @Override
    public void tick() {
        if (!ClientDreamRenderer.getInstance().clientIsDreaming()) {
            this.stop();
            return;
        }
        if (this.volume < TARGET_VOLUME) {
            this.volume = Mth.lerp(FADE_IN_RATE, this.volume, TARGET_VOLUME);
        }
    }
}
