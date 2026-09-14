package com.farcr.nomansland.common.registry.entities;

import com.farcr.nomansland.NoMansLand;
import com.mojang.serialization.Codec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class NMLEntityDataAttachments {
    public static final DeferredRegister<AttachmentType<?>> DATA_ATTACHMENTS =
        DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, NoMansLand.MODID);

    public static final Supplier<AttachmentType<Float>> STASIS_TICK_MULTIPLIER = DATA_ATTACHMENTS.register(
        "stasis_time", () -> AttachmentType.builder(() -> 1f)
            .sync(ByteBufCodecs.FLOAT).serialize(Codec.FLOAT).build()
    );
}
