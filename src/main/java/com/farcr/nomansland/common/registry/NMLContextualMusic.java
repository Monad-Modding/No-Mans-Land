package com.farcr.nomansland.common.registry;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.music.condition.GuidanceMusicCondition;
import com.farcr.nomansland.client.music.condition.MusicCondition;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class NMLContextualMusic {
    public static final DeferredRegister<MusicCondition> CONTEXTUAL_MUSIC_REGISTRY = DeferredRegister.create(NMLRegistries.CONTEXTUAL_MUSIC, NoMansLand.MODID);

    public static final Supplier<GuidanceMusicCondition> GUIDANCE_CONTEXTUAL_MUSIC = CONTEXTUAL_MUSIC_REGISTRY.register("friend_moon_music", GuidanceMusicCondition::new);
}
