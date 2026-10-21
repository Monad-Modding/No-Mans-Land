package com.farcr.nomansland.common.registry;

import com.farcr.nomansland.NMLConfig;
import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.condition.BooleanConfigCondition;
import com.mojang.serialization.MapCodec;
import com.teamabnormals.blueprint.core.api.conditions.ConfigValueCondition;
import com.teamabnormals.blueprint.core.util.DataUtil;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class NMLConditions {
    public static final DeferredRegister<MapCodec<? extends ICondition>> CONDITION_CODECS =
        DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, NoMansLand.MODID);

    public static final Supplier<MapCodec<BooleanConfigCondition>> BOOLEAN_CONFIG =
        CONDITION_CODECS.register("boolean_config", () -> BooleanConfigCondition.CODEC);
    public static final DeferredHolder<MapCodec<? extends ICondition>, ConfigValueCondition.Serializer> CONFIG =
        CONDITION_CODECS.register("config", () -> new ConfigValueCondition.Serializer(DataUtil.getConfigValues(NMLConfig.BIOMES)));
}
