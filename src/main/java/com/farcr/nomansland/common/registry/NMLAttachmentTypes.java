package com.farcr.nomansland.common.registry;

import com.farcr.nomansland.NoMansLand;
import com.mojang.serialization.Codec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class NMLAttachmentTypes {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, NoMansLand.MODID);

    public static final Supplier<AttachmentType<Long>> LAST_MOON_CARVING_INTERACTION = ATTACHMENT_TYPES.register(
            "last_moon_carving_interaction", () -> AttachmentType.builder(() -> -1L).serialize(Codec.LONG).sync(ByteBufCodecs.VAR_LONG).build()
    );

}
