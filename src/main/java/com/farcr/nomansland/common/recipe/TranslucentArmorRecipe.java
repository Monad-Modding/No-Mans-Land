package com.farcr.nomansland.common.recipe;

import com.farcr.nomansland.common.registry.NMLRecipeSerializers;
import com.farcr.nomansland.common.registry.items.NMLDataComponents;
import com.farcr.nomansland.common.registry.items.NMLItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class TranslucentArmorRecipe extends CustomRecipe {

    public TranslucentArmorRecipe(CraftingBookCategory category) {
        super(category);
    }

    private static boolean isArmor(ItemStack stack) {
        return stack.getItem() instanceof ArmorItem;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        ItemStack armor = ItemStack.EMPTY;
        boolean hasSac = false;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;
            if (stack.is(NMLItems.TRANSLUCENT_SAC.get())) {
                hasSac = true;
            } else if (isArmor(stack) && !stack.has(NMLDataComponents.TRANSLUCENT.get())) {
                if (!armor.isEmpty()) return false;
                armor = stack;
            } else {
                return false;
            }
        }
        return hasSac && !armor.isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider provider) {
        ItemStack armor = ItemStack.EMPTY;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;
            if (isArmor(stack)) {
                armor = stack;
            }
        }
        if (armor.isEmpty()) return ItemStack.EMPTY;
        ItemStack result = armor.copyWithCount(1);
        result.set(NMLDataComponents.TRANSLUCENT.get(), Unit.INSTANCE);
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return NMLRecipeSerializers.TRANSLUCENT_ARMOR_SERIALIZER.get();
    }
}
