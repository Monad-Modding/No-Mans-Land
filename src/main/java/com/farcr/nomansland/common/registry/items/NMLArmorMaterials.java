package com.farcr.nomansland.common.registry.items;

import com.farcr.nomansland.NMLConfig;
import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.registry.NMLSounds;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Supplier;

public class NMLArmorMaterials {
    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS = DeferredRegister.create(Registries.ARMOR_MATERIAL, NoMansLand.MODID);

    public static final Holder<ArmorMaterial> TORTOISE = register(
            "tortoise",
            ArmorItem.Type.CHESTPLATE,
            () -> NMLConfig.ARMOR_VALUE.get(),
            9,
            NMLSounds.TORTOISE_ARMOR_EQUIP,
            () -> Ingredient.of(NMLItems.STURDY_SCUTE),
            "tortoise",
            () -> NMLConfig.ARMOR_TOUGHNESS_VALUE.get().floatValue(),
            () -> NMLConfig.KNOCKBACK_RESISTANCE_VALUE.get().floatValue()
    );

    public static final Holder<ArmorMaterial> ANCIENT_BRONZE_MASK = register(
            "ancient_bronze_mask",
            ArmorItem.Type.HELMET,
            () -> 3,
            20,
            NMLSounds.ANCIENT_BRONZE_MASK_EQUIP,
            () -> Ingredient.EMPTY,
            "ancient_bronze_mask",
            () -> 0F,
            () -> 0F
    );

    public static Holder<ArmorMaterial> register(String name, ArmorItem.Type armorType, Supplier<Integer> armorValue, int enchantmnetValue, Holder<SoundEvent> soundEvent, Supplier<Ingredient> repairIngredient, String armorLayer, Supplier<Float> toughness, Supplier<Float> knockbackResistance) {
        return register(name, () -> new ArmorMaterial(
                Util.make(new EnumMap<>(ArmorItem.Type.class), map -> map.put(armorType, armorValue.get())), enchantmnetValue, soundEvent, repairIngredient,
                List.of(
                        new ArmorMaterial.Layer(NoMansLand.location(armorLayer)),
                        new ArmorMaterial.Layer(NoMansLand.location(armorLayer), "_overlay", false)
                ), toughness.get(), knockbackResistance.get()
        ));
    }

    public static Holder<ArmorMaterial> register(String name, Supplier<ArmorMaterial> armorMaterial) {
        return ARMOR_MATERIALS.register(name, armorMaterial);
    }
}