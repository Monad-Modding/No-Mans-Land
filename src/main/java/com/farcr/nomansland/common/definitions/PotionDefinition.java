package com.farcr.nomansland.common.definitions;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.Objects;
import java.util.function.Supplier;

public class PotionDefinition extends Definition<Potion, Potion> {
    protected PotionDefinition(ResourceKey<Potion> key) {
        super(key);
    }

    public static PotionDefinition fromHolder(DeferredHolder<Potion, Potion> potion) {
        return new PotionDefinition(potion.getKey());
    }

    public String trimmedLangKey() {
        return this.langKey().substring(this.langKey().indexOf('.') + 1);
    }

    public String effectLanguageValue() {
        String[] words = defaultLanguageKey().split("_");
        StringBuilder localizedName = new StringBuilder();
        for (String word : words) localizedName.append(Character.toTitleCase(word.charAt(0)))
            .append(word.substring(1)).append(" ");
        return localizedName.toString().trim();
    }

    private String defaultLanguageKey() {
        String effectName = Objects.requireNonNull(BuiltInRegistries.POTION.get(key))
            .getEffects().getFirst().getEffect().getRegisteredName();
        return effectName.substring(effectName.indexOf(':') + 1);
    }
}