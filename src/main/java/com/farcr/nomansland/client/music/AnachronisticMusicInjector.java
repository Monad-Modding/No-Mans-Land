package com.farcr.nomansland.client.music;

import com.farcr.nomansland.client.music.condition.MusicCondition;
import com.farcr.nomansland.common.registry.NMLSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.Music;
import net.minecraft.util.RandomSource;

public class AnachronisticMusicInjector {
    public static final int ANACHRONISTIC_WEIGHT = 1;
    public static final int OTHER_MUSIC_WEIGHT = 9;

    public static final double MINIMUM_Y = 100.0;

    private static final Music ANACHRONISTIC_SONG = new Music(NMLSounds.ANACHRONISTIC_MUSIC, 12000, 24000, false);
    private static final RandomSource RANDOM = RandomSource.create();

    public static Music select(Music incoming) {
        if (incoming == null) return null;
        if (!conditionsMet()) return incoming;
        if (!NMLSounds.ANACHRONISTIC_MUSIC.isBound() || !incoming.getEvent().isBound()) return incoming;

        final ResourceLocation incomingLocation = incoming.getEvent().value().getLocation();
        if (incomingLocation.equals(NMLSounds.ANACHRONISTIC_MUSIC.getId())) return incoming;
        if (isContextual(incomingLocation)) return incoming;

        if (RANDOM.nextInt(ANACHRONISTIC_WEIGHT + OTHER_MUSIC_WEIGHT) < ANACHRONISTIC_WEIGHT)
            return ANACHRONISTIC_SONG;

        return incoming;
    }

    private static boolean conditionsMet() {
        final Minecraft minecraft = Minecraft.getInstance();
        final LocalPlayer player = minecraft.player;
        final ClientLevel level = minecraft.level;
        if (player == null || level == null) return false;
        return player.getY() > MINIMUM_Y && level.canSeeSky(player.blockPosition());
    }

    private static boolean isContextual(ResourceLocation incomingLocation) {
        if (!ContextualMusicHandler.builtContext) return false;
        for (MusicCondition.MusicConditionInstance instance : ContextualMusicHandler.instanceList) {
            final Music contextual = instance.getMusic();
            if (contextual != null && contextual.getEvent().isBound()
                && contextual.getEvent().value().getLocation().equals(incomingLocation))
                return true;
        }
        return false;
    }
}
