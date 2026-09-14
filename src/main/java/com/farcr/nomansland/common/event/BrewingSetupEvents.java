package com.farcr.nomansland.common.event;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.networking.*;
import com.farcr.nomansland.common.networking.alchemist_tools.*;
import com.farcr.nomansland.common.registry.items.NMLItems;
import com.farcr.nomansland.common.registry.items.NMLPotions;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.brewing.IBrewingRecipe;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(modid = NoMansLand.MODID)
public class BrewingSetupEvents {
    @SubscribeEvent
    public static void registerBrewingRecipes(RegisterBrewingRecipesEvent event) {
        event.getBuilder().addMix(Potions.WATER, NMLItems.AWKWARD_RESIDUE.get(), Potions.AWKWARD);

        event.getBuilder().addStartMix(Items.NAUTILUS_SHELL, NMLPotions.STASIS);
        event.getBuilder().addMix(NMLPotions.STASIS, Items.REDSTONE, NMLPotions.LONG_STASIS);

        event.getBuilder().addRecipe(new AwkwardResidueDowngradeRecipe());
        event.getBuilder().addRecipe(new BandageInfusionRecipe());
    }

    private static boolean isEmptyBandage(ItemStack stack) {
        if (!stack.is(NMLItems.BANDAGE)) return false;
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        return contents == null || !contents.getAllEffects().iterator().hasNext();
    }

    private static boolean isPotionWithEffects(ItemStack stack) {
        if (!stack.is(Items.POTION)) return false;
        PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        return contents.getAllEffects().iterator().hasNext();
    }

    private static boolean isUpgradedPotion(PotionContents contents) {
        return contents.potion().map(holder -> {
            String path = Objects.requireNonNull(holder.getKey()).location().getPath();
            return path.startsWith("strong_") || path.startsWith("long_");
        }).orElse(false);
    }

    private static Optional<Holder<Potion>> getBasePotionFromUpgraded(PotionContents contents) {
        return contents.potion().flatMap(holder -> {
            String path = Objects.requireNonNull(holder.getKey()).location().getPath();
            String basePath = null;
            if (path.startsWith("strong_")) {
                basePath = path.substring("strong_".length());
            } else if (path.startsWith("long_")) {
                basePath = path.substring("long_".length());
            }
            if (basePath != null) {
                ResourceLocation baseLocation = ResourceLocation.withDefaultNamespace(basePath);
                return BuiltInRegistries.POTION.getHolder(baseLocation);
            }
            return Optional.empty();
        });
    }

    private static class AwkwardResidueDowngradeRecipe implements IBrewingRecipe {
        @Override
        public boolean isInput(@NotNull ItemStack stack) {
            if (!stack.is(Items.POTION) && !stack.is(Items.SPLASH_POTION) && !stack.is(Items.LINGERING_POTION)) return false;
            PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
            return isUpgradedPotion(contents);
        }

        @Override
        public boolean isIngredient(@NotNull ItemStack stack) {
            return stack.is(NMLItems.AWKWARD_RESIDUE);
        }

        @Override
        public @NotNull ItemStack getOutput(@NotNull ItemStack input, @NotNull ItemStack ingredient) {
            if (!isInput(input) || !isIngredient(ingredient)) return ItemStack.EMPTY;
            PotionContents potionContents = input.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
            Optional<Holder<Potion>> basePotion = getBasePotionFromUpgraded(potionContents);
            if (basePotion.isEmpty()) return ItemStack.EMPTY;

            PotionContents newContents = new PotionContents(basePotion, potionContents.customColor(), potionContents.customEffects());
            ItemStack result = input.copy();
            result.set(DataComponents.POTION_CONTENTS, newContents);
            return result;
        }
    }

    private static class BandageInfusionRecipe implements IBrewingRecipe {
        @Override
        public boolean isInput(@NotNull ItemStack stack) {
            return stack.is(NMLItems.BANDAGE);
        }

        @Override
        public boolean isIngredient(@NotNull ItemStack stack) {
            return isPotionWithEffects(stack);
        }

        @Override
        public @NotNull ItemStack getOutput(@NotNull ItemStack input, @NotNull ItemStack ingredient) {
            if (!isEmptyBandage(input)) return ItemStack.EMPTY;

            PotionContents potionContents = ingredient.get(DataComponents.POTION_CONTENTS);
            if (potionContents == null) return ItemStack.EMPTY;

            ItemStack result = new ItemStack(NMLItems.BANDAGE.get());
            result.set(DataComponents.POTION_CONTENTS, potionContents);
            return result;
        }
    }
}
