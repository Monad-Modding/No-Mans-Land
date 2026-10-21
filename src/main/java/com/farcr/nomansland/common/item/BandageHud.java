package com.farcr.nomansland.common.item;

import com.farcr.nomansland.NMLConfig;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;

public class BandageHud {

    public static ItemStack held(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.getItem() instanceof BandageItem) return mainHand;
        ItemStack offHand = player.getOffhandItem();
        if (offHand.getItem() instanceof BandageItem) return offHand;
        return ItemStack.EMPTY;
    }

    public static boolean shouldShow(Player player) {
        if (player.isSpectator() || player.getAbilities().instabuild) return false;
        return !held(player).isEmpty();
    }

    public static FoodProperties food(ItemStack stack) {
        float healthPoints = NMLConfig.BANDAGE_HEAL_AMOUNT.get().floatValue();

        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        if (contents != null) {
            for (MobEffectInstance effect : contents.getAllEffects()) {
                if (effect.is(MobEffects.REGENERATION)) {
                    healthPoints += (float) effect.getDuration() / Math.max(1, 50 >> effect.getAmplifier());
                } else if (effect.is(MobEffects.HEAL)) {
                    healthPoints += 4 << effect.getAmplifier();
                }
            }
        }
        return new FoodProperties.Builder().alwaysEdible().effect(regen(healthPoints), 1.0F).build();
    }

    private static MobEffectInstance regen(float healthPoints) {
        return new MobEffectInstance(MobEffects.REGENERATION, Math.max(1, Math.round(healthPoints * 50.0F)), 0);
    }
}
