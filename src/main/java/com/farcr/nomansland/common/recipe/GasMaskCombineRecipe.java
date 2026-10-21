package com.farcr.nomansland.common.recipe;

import com.farcr.nomansland.common.item.GasMaskItem;
import com.farcr.nomansland.common.registry.NMLRecipeSerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class GasMaskCombineRecipe extends CustomRecipe {
    public GasMaskCombineRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, @NotNull Level level) {
        int masks = 0;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;
            if (stack.getItem() instanceof GasMaskItem) masks++;
            else return false;
        }
        return masks == 2;
    }

    @Override
    public @NotNull ItemStack assemble(CraftingInput input, HolderLookup.@NotNull Provider registries) {
        ItemStack first = ItemStack.EMPTY;
        int total = 0;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.getItem() instanceof GasMaskItem) {
                if (first.isEmpty()) first = stack;
                total += GasMaskItem.getDuration(stack);
            }
        }
        if (first.isEmpty()) return ItemStack.EMPTY;
        ItemStack result = first.copyWithCount(1);
        GasMaskItem.setDuration(result, total);
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return NMLRecipeSerializers.GAS_MASK_COMBINE.get();
    }
}
