package com.farcr.nomansland.common.recipe;

import com.farcr.nomansland.common.item.CharcoalFilterItem;
import com.farcr.nomansland.common.item.GasMaskItem;
import com.farcr.nomansland.common.registry.NMLRecipeSerializers;
import com.farcr.nomansland.common.registry.items.NMLItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class GasMaskRefuelRecipe extends CustomRecipe {
    public GasMaskRefuelRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, @NotNull Level level) {
        ItemStack mask = ItemStack.EMPTY;
        int filters = 0;
        int lastFilter = 0;
        int added = 0;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;
            if (stack.getItem() instanceof GasMaskItem) {
                if (!mask.isEmpty()) return false;
                mask = stack;
            } else if (stack.is(NMLItems.CHARCOAL_FILTER)) {
                filters++;
                lastFilter = CharcoalFilterItem.remainingTicks(stack);
                added += lastFilter;
            } else {
                return false;
            }
        }
        if (mask.isEmpty() || filters < 1 || added <= 0) return false;
        return GasMaskItem.getDuration(mask) + added - lastFilter < GasMaskItem.MAX_DURATION;
    }

    @Override
    public @NotNull ItemStack assemble(CraftingInput input, HolderLookup.@NotNull Provider registries) {
        ItemStack mask = ItemStack.EMPTY;
        int added = 0;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.getItem() instanceof GasMaskItem) mask = stack;
            else if (stack.is(NMLItems.CHARCOAL_FILTER)) added += CharcoalFilterItem.remainingTicks(stack);
        }
        if (mask.isEmpty()) return ItemStack.EMPTY;
        ItemStack result = mask.copyWithCount(1);
        GasMaskItem.setDuration(result, GasMaskItem.getDuration(mask) + added);
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return NMLRecipeSerializers.GAS_MASK_REFUEL.get();
    }
}
