package com.farcr.nomansland.common.registry.items;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.definitions.ItemDefinition;
import com.farcr.nomansland.common.definitions.PotionDefinition;
import com.farcr.nomansland.common.registry.entities.NMLEffects;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class NMLPotions {
    public static List<PotionDefinition> POTION_DEFINITIONS = new ArrayList<>();
    public static final DeferredRegister<Potion> POTION_REGISTRY
        = DeferredRegister.create(Registries.POTION, NoMansLand.MODID);

    public static PotionDefinition register(String identifier, Supplier<MobEffectInstance> effectSupplier) {
        DeferredHolder<Potion, Potion> potionHolder = POTION_REGISTRY.register(
            identifier, () -> new Potion(effectSupplier.get()));
        PotionDefinition definition = PotionDefinition.fromHolder(potionHolder);
        POTION_DEFINITIONS.add(definition);
        return definition;
    }

    public static final PotionDefinition STASIS = register("stasis",
        () -> new MobEffectInstance(NMLEffects.STASIS, 300, 0));
    public static final PotionDefinition LONG_STASIS = register("long_stasis",
        () -> new MobEffectInstance(NMLEffects.STASIS, 600, 0));
    public static final PotionDefinition CORROSION = register("corrosion",
            () -> new MobEffectInstance(NMLEffects.CORROSION, 160, 0));
    public static final PotionDefinition STRONG_CORROSION = register("strong_corrosion",
            () -> new MobEffectInstance(NMLEffects.CORROSION, 280, 1));
}
