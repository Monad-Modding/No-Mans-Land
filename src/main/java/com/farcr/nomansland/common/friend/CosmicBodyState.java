package com.farcr.nomansland.common.friend;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;

import java.util.UUID;

public record CosmicBodyState(
    UUID playerUUID,
    int daysCounted
) {
    public static final Codec<CosmicBodyState> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            UUIDUtil.CODEC.fieldOf("UUID").forGetter(CosmicBodyState::playerUUID),
            Codec.INT.fieldOf("Time").forGetter(CosmicBodyState::daysCounted)
        ).apply(instance, CosmicBodyState::new)
    );

    private static final int MAX_COSMIC_BODY_DAYS = 8;
    public boolean exceedsDays() {
        return (daysCounted() > (MAX_COSMIC_BODY_DAYS - 1));
    }
}
