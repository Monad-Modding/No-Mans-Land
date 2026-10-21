package com.farcr.nomansland.client.sound;

import com.farcr.nomansland.common.item.BandageItem;
import com.farcr.nomansland.common.registry.NMLSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;

public class BandageSoundInstance extends AbstractTickableSoundInstance {
    private final Player player;

    public static void play(Player player) {
        Minecraft.getInstance().getSoundManager().play(new BandageSoundInstance(player));
    }

    public BandageSoundInstance(Player player) {
        super(NMLSounds.BANDAGE_WRAP.get(), SoundSource.PLAYERS, RandomSource.create());
        this.player = player;
        this.x = player.getX();
        this.y = player.getY();
        this.z = player.getZ();
        this.volume = 0.8F;
        this.pitch = 1;
    }

    @Override
    public void tick() {
        if (player.isRemoved() || !player.isAlive()) {
            this.stop();
            return;
        }

        if (!player.isUsingItem() || !(player.getUseItem().getItem() instanceof BandageItem)) {
            this.stop();
            return;
        }

        this.x = player.getX();
        this.y = player.getY();
        this.z = player.getZ();
    }
}
