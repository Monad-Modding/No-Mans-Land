package com.farcr.nomansland.common.registry.items;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Items;

public class NMLFoods {
    public static final FoodProperties MASHED_POTATOES_WITH_MUSHROOMS = new FoodProperties.Builder().nutrition(10).saturationModifier(0.9F).usingConvertsTo(Items.BOWL).build();
    public static final FoodProperties GRILLED_MUSHROOMS = new FoodProperties.Builder().nutrition(4).saturationModifier(0.5F).build();

    public static final FoodProperties FROG_LEG = new FoodProperties.Builder().nutrition(2).saturationModifier(0.2F).build();
    public static final FoodProperties COOKED_FROG_LEG = new FoodProperties.Builder().nutrition(6).saturationModifier(0.7F).build();

    public static final FoodProperties RAW_HORSE = new FoodProperties.Builder().nutrition(4).saturationModifier(0.3F).build();
    public static final FoodProperties HORSE_STEAK = new FoodProperties.Builder().nutrition(9).saturationModifier(0.8F).build();

    public static final FoodProperties RAW_VENISON = new FoodProperties.Builder().nutrition(3).saturationModifier(0.3F).build();
    public static final FoodProperties COOKED_VENISON = new FoodProperties.Builder().nutrition(6).saturationModifier(0.8F).build();

    public static final FoodProperties BILLHOOK_BASS = new FoodProperties.Builder().nutrition(3).saturationModifier(0.1F).build();
    public static final FoodProperties COOKED_BILLHOOK_BASS = new FoodProperties.Builder().nutrition(7).saturationModifier(0.8F).build();
    public static final FoodProperties CAVE_CARP = new FoodProperties.Builder().nutrition(3).saturationModifier(0.2F).build();

    public static final FoodProperties MAPLE_SYRUP_BOTTLE = new FoodProperties.Builder().nutrition(5).saturationModifier(0.2F).alwaysEdible().build();
    public static final FoodProperties MAPLE_TART = new FoodProperties.Builder().nutrition(7).saturationModifier(0.5F).alwaysEdible().build();

    public static final FoodProperties PEAR = new FoodProperties.Builder().nutrition(4).saturationModifier(0.3F).build();
    public static final FoodProperties SYRUPED_PEAR = new FoodProperties.Builder().nutrition(6).saturationModifier(0.4F).build();

    public static final FoodProperties PANCAKE = new FoodProperties.Builder().nutrition(8).saturationModifier(0.5F).build();

    public static final FoodProperties HONEYED_APPLE = new FoodProperties.Builder().nutrition(7).saturationModifier(0.5F).build();

    public static final FoodProperties WALNUTS = new FoodProperties.Builder().nutrition(2).saturationModifier(0.3F).fast().build();
    public static final FoodProperties TRAIL_MIX = new FoodProperties.Builder().nutrition(4).saturationModifier(0.5F).fast().build();

    public static final FoodProperties HARDTACK = new FoodProperties.Builder().nutrition(5).saturationModifier(0.8F).build();

    public static final FoodProperties PINE_NUTS = new FoodProperties.Builder().nutrition(2).saturationModifier(0.4F).fast().build();
    public static final FoodProperties SWEET_TART = new FoodProperties.Builder().nutrition(6).saturationModifier(0.5F).alwaysEdible().build();

    public static final FoodProperties FRUIT_CAKE_SLICE = new FoodProperties.Builder().nutrition(3).saturationModifier(0.6F).fast()
            .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 160, 0), 1).build();

    public static final FoodProperties AWKWARD_RESIDUE = new FoodProperties.Builder().nutrition(1).saturationModifier(0.1F).effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 100, 2), 0.8F).build();

    public static final FoodProperties BUG_MEAT = new FoodProperties.Builder().nutrition(2).saturationModifier(0.2F).build();
    public static final FoodProperties RAW_GRUB = new FoodProperties.Builder().nutrition(2).saturationModifier(0.1F).effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 80, 0), 0.3F).build();
    public static final FoodProperties FRIED_GRUB = new FoodProperties.Builder().nutrition(6).saturationModifier(0.7F).build();
}


