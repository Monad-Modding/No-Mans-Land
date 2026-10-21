package com.farcr.nomansland.common.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.neoforged.neoforge.common.conditions.ICondition;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;

public record BooleanConfigCondition(String option, boolean enabledWhen) implements ICondition {

    public static final Map<String, BooleanSupplier> OPTIONS = new HashMap<>();

    public static final MapCodec<BooleanConfigCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("option").forGetter(BooleanConfigCondition::option),
            Codec.BOOL.fieldOf("enabled_when").forGetter(BooleanConfigCondition::enabledWhen)
    ).apply(instance, BooleanConfigCondition::new));

    @Override
    public boolean test(ICondition.IContext context) {
        BooleanSupplier supplier = OPTIONS.get(option);
        return supplier != null && supplier.getAsBoolean() == enabledWhen;
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
