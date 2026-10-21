package com.farcr.nomansland.common.registry.entities;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.dreams.dreamlevel.DreamPlayerSnapshot;
import com.farcr.nomansland.common.entity.goose.Goose;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class NMLEntityDataSerializers {
    public static final DeferredRegister<EntityDataSerializer<?>> ENTITY_DATA_SERIALIZERS = DeferredRegister.create(NeoForgeRegistries.ENTITY_DATA_SERIALIZERS, NoMansLand.MODID);

    public static final Supplier<EntityDataSerializer<Goose.State>> GOOSE_STATE = ENTITY_DATA_SERIALIZERS.register("goose_state", () -> EntityDataSerializer.forValueType(Goose.State.STREAM_CODEC));
    public static final Supplier<EntityDataSerializer<DreamPlayerSnapshot>> DREAM_PLAYER_SNAPSHOT = ENTITY_DATA_SERIALIZERS.register("dream_player_snapshot", () -> EntityDataSerializer.forValueType(DreamPlayerSnapshot.STREAM_CODEC));
}
