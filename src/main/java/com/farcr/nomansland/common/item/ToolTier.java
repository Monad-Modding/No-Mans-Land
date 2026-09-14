package com.farcr.nomansland.common.item;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

public record ToolTier(TagKey<Block> incorrectBlockForDrops, int uses, float speed, float damage, int enchantmentValue, Supplier<Ingredient> repairIngredient) implements Tier {
    @Override public int getUses() { return this.uses(); }
    @Override public float getSpeed() { return this.speed(); }
    @Override public float getAttackDamageBonus() { return this.damage(); }
    @Override public TagKey<Block> getIncorrectBlocksForDrops() { return incorrectBlockForDrops; }
    @Override public int getEnchantmentValue() { return this.enchantmentValue(); }
    @Override public Ingredient getRepairIngredient() { return this.repairIngredient().get(); }
}
