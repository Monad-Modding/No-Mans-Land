package com.farcr.nomansland.common.registry;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.recipe.CauldronInteractionRecipe;
import com.farcr.nomansland.common.recipe.CauldronInteractionRecipeSerializer;
import com.farcr.nomansland.common.recipe.GasMaskCombineRecipe;
import com.farcr.nomansland.common.recipe.GasMaskRefuelRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class NMLRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, NoMansLand.MODID);

    public static final Supplier<RecipeSerializer<CauldronInteractionRecipe>> CAULDRON_INTERACTION_SERIALIZER =
            RECIPE_SERIALIZERS.register("cauldron_interaction", CauldronInteractionRecipeSerializer::new);

    public static final Supplier<RecipeSerializer<GasMaskRefuelRecipe>> GAS_MASK_REFUEL =
            RECIPE_SERIALIZERS.register("gas_mask_refuel", () -> new SimpleCraftingRecipeSerializer<>(GasMaskRefuelRecipe::new));

    public static final Supplier<RecipeSerializer<GasMaskCombineRecipe>> GAS_MASK_COMBINE =
            RECIPE_SERIALIZERS.register("gas_mask_combine", () -> new SimpleCraftingRecipeSerializer<>(GasMaskCombineRecipe::new));

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(BuiltInRegistries.RECIPE_TYPE, NoMansLand.MODID);

    public static final Supplier<RecipeType<CauldronInteractionRecipe>> CAULDRON_INTERACTION_RECIPE =
            RECIPE_TYPES.register(
                    "cauldron_interaction",
                    () -> RecipeType.simple(NoMansLand.location("cauldron_interaction"))
            );
}
