package com.farcr.nomansland.common.event;

import com.farcr.nomansland.common.definitions.ItemLikeDefinition;
import com.farcr.nomansland.common.integration.FDIntegration;
import com.farcr.nomansland.common.integration.Mods;
import com.farcr.nomansland.common.integration.boatload.BoatloadIntegration;
import com.farcr.nomansland.common.integration.nirvana.NirvanaIntegration;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import com.farcr.nomansland.common.registry.items.NMLItems;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

import java.util.List;
import java.util.Optional;

import static com.farcr.nomansland.common.registry.blocks.NMLBlocks.*;
import static com.farcr.nomansland.common.registry.items.NMLItems.*;
import static net.minecraft.world.item.Items.*;

public class CreativeModeTabHandler {

    private BuildCreativeModeTabContentsEvent event;

    private void insertBefore(Item existingEntry, ItemLikeDefinition<?, ?> newEntry) {
        insertBeforeStack(existingEntry.getDefaultInstance(), newEntry.stack());
    }

    private void insertAfter(Item existingEntry, ItemLikeDefinition<?, ?> newEntry) {
        insertAfterStack(existingEntry.getDefaultInstance(), newEntry.stack());
    }

    private void insertBeforeStack(ItemStack anchor, ItemStack newStack) {
        event.remove(newStack, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        if (tabContains(event.getParentEntries(), anchor) && tabContains(event.getSearchEntries(), anchor)) {
            event.insertBefore(anchor, newStack, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        } else {
            event.accept(newStack, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }
    }

    private void insertAfterStack(ItemStack anchor, ItemStack newStack) {
        event.remove(newStack, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        if (tabContains(event.getParentEntries(), anchor) && tabContains(event.getSearchEntries(), anchor)) {
            event.insertAfter(anchor, newStack, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        } else {
            event.accept(newStack, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }
    }

    private static boolean tabContains(Iterable<ItemStack> entries, ItemStack anchor) {
        for (ItemStack stack : entries) {
            if (ItemStack.isSameItemSameComponents(stack, anchor)) return true;
        }
        return false;
    }

    private static void generateBandageEffectTypes(BuildCreativeModeTabContentsEvent output, HolderLookup.RegistryLookup<Potion> potions, Item item, FeatureFlagSet featureFlag) {
        ItemStack wardingBandage = WARDING_BANDAGE.stack();

        List<ItemStack> stacks = potions.listElements()
                .filter(h -> isValidPotionForBandage(h, featureFlag))
                .map(holder -> creatBandageEffectsStack(item, holder))
                .toList();

        // Inverted so insertAfter goes forward instead
        for (int i = stacks.size() - 1; i >= 0; i--) {
            output.insertAfter(wardingBandage, stacks.get(i), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }
    }

    //TODO: Should probably call this inside the Brewing Stand Mixin
    public static boolean isValidPotionForBandage(Holder.Reference<Potion> holder, FeatureFlagSet featureFlag) {
        if (!holder.value().isEnabled(featureFlag)) {
            return false;
        }
        var key = holder.key;
        var path = key.location().getPath();
        if (path.startsWith("strong_") || path.startsWith("long_")) {
            return false;
        }
        return !holder.is(Potions.WATER) && !holder.is(Potions.AWKWARD) && !holder.is(Potions.THICK) && !holder.is(Potions.MUNDANE);
    }

    private static ItemStack creatBandageEffectsStack(Item item, Holder<Potion> potion) {
        ItemStack stack = new ItemStack(item);
        stack.set(DataComponents.POTION_CONTENTS, new PotionContents(Optional.of(potion), Optional.empty(), List.of()));
        return stack;
    }

    @SubscribeEvent
    public void onBuildCreativeModeTabContents(final BuildCreativeModeTabContentsEvent event) {
        this.event = event;
        ResourceKey<CreativeModeTab> tab = event.getTabKey();

        //Note about the methods "insertBefore and insertAfter"
        //insertAfter reads from the bottom up, while insertBefore reads from up to bottom.
        //Might look messy, but trust me it makes sense I swear. 
        //-Farcr
        

        if (tab == CreativeModeTabs.BUILDING_BLOCKS) addBuildingBlocks(event);
        if (tab == CreativeModeTabs.NATURAL_BLOCKS) addNaturalBlocks(event);
        if (tab == CreativeModeTabs.FUNCTIONAL_BLOCKS) addFunctionalBlocks(event);
        if (tab == CreativeModeTabs.FOOD_AND_DRINKS) addFoodAndDrinks(event);
        if (tab == CreativeModeTabs.TOOLS_AND_UTILITIES) addToolsAndUtilities(event);
        if (tab == CreativeModeTabs.COMBAT) addCombat(event);
        if (tab == CreativeModeTabs.INGREDIENTS) addIngredients(event);
        if (tab == CreativeModeTabs.REDSTONE_BLOCKS) addRedstoneBlocks(event);
        if (tab == CreativeModeTabs.SPAWN_EGGS) addSpawnEggs(event);

       //if (tab == FDIntegration.TAB.get()); {
       //}

        if (Mods.CREATE.isLoaded())
            event.remove(Mods.CREATE.getItem("honeyed_apple").getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
    }

    private void addBuildingBlocks(final BuildCreativeModeTabContentsEvent event) {

        insertAfter(REINFORCED_DEEPSLATE, SOLAR_TILES);
        insertAfter(REINFORCED_DEEPSLATE, LARGE_SOLAR_TILE);
        insertAfter(REINFORCED_DEEPSLATE, LUNAR_TILES);
        insertAfter(REINFORCED_DEEPSLATE, LARGE_LUNAR_TILE);
        insertAfter(REINFORCED_DEEPSLATE, STELLAR_TILES);
        insertAfter(REINFORCED_DEEPSLATE, LARGE_STELLAR_TILE);

        insertAfter(STONE_BRICKS, FADED_STONE_BRICKS);
        insertBefore(STONE_BRICKS, POLISHED_STONE);
        insertBefore(STONE_BRICKS, POLISHED_STONE_STAIRS);
        insertBefore(STONE_BRICKS, POLISHED_STONE_SLAB);

        insertAfter(MOSSY_COBBLESTONE_WALL, MOSSY_COBBLESTONE_BRICK_WALL);
        insertAfter(MOSSY_COBBLESTONE_WALL, MOSSY_COBBLESTONE_BRICK_SLAB);
        insertAfter(MOSSY_COBBLESTONE_WALL, MOSSY_COBBLESTONE_BRICK_STAIRS);
        insertAfter(MOSSY_COBBLESTONE_WALL, MOSSY_COBBLESTONE_BRICKS);
        insertAfter(MOSSY_COBBLESTONE_WALL, CRACKED_COBBLESTONE_BRICKS);
        insertAfter(MOSSY_COBBLESTONE_WALL, COBBLESTONE_BRICK_WALL);
        insertAfter(MOSSY_COBBLESTONE_WALL, COBBLESTONE_BRICK_SLAB);
        insertAfter(MOSSY_COBBLESTONE_WALL, COBBLESTONE_BRICK_STAIRS);
        insertAfter(MOSSY_COBBLESTONE_WALL, COBBLESTONE_BRICKS);

        insertAfter(COBBLED_DEEPSLATE_WALL, COBBLED_DEEPSLATE_BRICK_WALL);
        insertAfter(COBBLED_DEEPSLATE_WALL, COBBLED_DEEPSLATE_BRICK_SLAB);
        insertAfter(COBBLED_DEEPSLATE_WALL, COBBLED_DEEPSLATE_BRICK_STAIRS);
        insertAfter(COBBLED_DEEPSLATE_WALL, COBBLED_DEEPSLATE_BRICKS);

        insertAfter(SMOOTH_STONE_SLAB, MUNDANE_TILE_SLAB);
        insertAfter(SMOOTH_STONE_SLAB, MUNDANE_TILE_STAIRS);
        insertAfter(SMOOTH_STONE_SLAB, MUNDANE_TILES);
        insertBefore(PACKED_MUD, EARTHEN_TILES);
        insertBefore(PACKED_MUD, EARTHEN_TILE_STAIRS);
        insertBefore(PACKED_MUD, EARTHEN_TILE_SLAB);
        insertBefore(PACKED_MUD, DROSS_TILES);
        insertBefore(PACKED_MUD, DROSS_TILE_STAIRS);
        insertBefore(PACKED_MUD, DROSS_TILE_SLAB);
        insertBefore(PACKED_MUD, SILT_BRICKS);
        insertBefore(PACKED_MUD, SILT_BRICK_STAIRS);
        insertBefore(PACKED_MUD, SILT_BRICK_SLAB);

        insertAfter(BRICK_WALL, MOSSY_COARSE_BRICK_WALL);
        insertAfter(BRICK_WALL, MOSSY_COARSE_BRICK_SLAB);
        insertAfter(BRICK_WALL, MOSSY_COARSE_BRICK_STAIRS);
        insertAfter(BRICK_WALL, MOSSY_COARSE_BRICKS);
        insertAfter(BRICK_WALL, COARSE_BRICK_WALL);
        insertAfter(BRICK_WALL, COARSE_BRICK_SLAB);
        insertAfter(BRICK_WALL, COARSE_BRICK_STAIRS);
        insertAfter(BRICK_WALL, COARSE_BRICKS);

        insertAfter(SPRUCE_BUTTON, PINE.button());
        insertAfter(SPRUCE_BUTTON, PINE.pressurePlate());
        insertAfter(SPRUCE_BUTTON, PINE.trapdoor());
        insertAfter(SPRUCE_BUTTON, PINE.door());
        insertAfter(SPRUCE_BUTTON, PINE.fenceGate());
        insertAfter(SPRUCE_BUTTON, PINE.fence());
        if (Mods.FARMERSDELIGHT.isLoaded()) insertAfter(SPRUCE_BUTTON, FDIntegration.PINE_CABINET);
        insertAfter(SPRUCE_BUTTON, PINE.trimmedPlanks());
        insertAfter(SPRUCE_BUTTON, PINE.bookshelf());
        insertAfter(SPRUCE_BUTTON, PINE.slab());
        insertAfter(SPRUCE_BUTTON, PINE.stairs());
        insertAfter(SPRUCE_BUTTON, PINE.planks());
        insertAfter(SPRUCE_BUTTON, PINE.strippedWood());
        insertAfter(SPRUCE_BUTTON, PINE.strippedLog());
        insertAfter(SPRUCE_BUTTON, PINE.wood());
        insertAfter(SPRUCE_BUTTON, PINE.log());

        insertAfter(DARK_OAK_BUTTON, WALNUT.button());
        insertAfter(DARK_OAK_BUTTON, WALNUT.pressurePlate());
        insertAfter(DARK_OAK_BUTTON, WALNUT.trapdoor());
        insertAfter(DARK_OAK_BUTTON, WALNUT.door());
        insertAfter(DARK_OAK_BUTTON, WALNUT.fenceGate());
        insertAfter(DARK_OAK_BUTTON, WALNUT.fence());
        if (Mods.FARMERSDELIGHT.isLoaded()) insertAfter(DARK_OAK_BUTTON, FDIntegration.WALNUT_CABINET);
        insertAfter(DARK_OAK_BUTTON, WALNUT.trimmedPlanks());
        insertAfter(DARK_OAK_BUTTON, WALNUT.bookshelf());
        insertAfter(DARK_OAK_BUTTON, WALNUT.slab());
        insertAfter(DARK_OAK_BUTTON, WALNUT.stairs());
        insertAfter(DARK_OAK_BUTTON, WALNUT.planks());
        insertAfter(DARK_OAK_BUTTON, WALNUT.strippedWood());
        insertAfter(DARK_OAK_BUTTON, WALNUT.strippedLog());
        insertAfter(DARK_OAK_BUTTON, WALNUT.wood());
        insertAfter(DARK_OAK_BUTTON, WALNUT.log());

        insertBefore(DARK_OAK_LOG, MAPLE.log());
        insertBefore(DARK_OAK_LOG, MAPLE.wood());
        insertBefore(DARK_OAK_LOG, MAPLE.strippedLog());
        insertBefore(DARK_OAK_LOG, MAPLE.strippedWood());
        insertBefore(DARK_OAK_LOG, MAPLE.planks());
        insertBefore(DARK_OAK_LOG, MAPLE.stairs());
        insertBefore(DARK_OAK_LOG, MAPLE.slab());
        insertBefore(DARK_OAK_LOG, MAPLE.bookshelf());
        insertBefore(DARK_OAK_LOG, MAPLE.trimmedPlanks());
        if (Mods.FARMERSDELIGHT.isLoaded()) insertBefore(DARK_OAK_LOG, FDIntegration.MAPLE_CABINET);
        insertBefore(DARK_OAK_LOG, MAPLE.fence());
        insertBefore(DARK_OAK_LOG, MAPLE.fenceGate());
        insertBefore(DARK_OAK_LOG, MAPLE.door());
        insertBefore(DARK_OAK_LOG, MAPLE.trapdoor());
        insertBefore(DARK_OAK_LOG, MAPLE.pressurePlate());
        insertBefore(DARK_OAK_LOG, MAPLE.button());

        insertBefore(MANGROVE_LOG, WILLOW.log());
        insertBefore(MANGROVE_LOG, WILLOW.wood());
        insertBefore(MANGROVE_LOG, WILLOW.strippedLog());
        insertBefore(MANGROVE_LOG, WILLOW.strippedWood());
        insertBefore(MANGROVE_LOG, WILLOW.planks());
        insertBefore(MANGROVE_LOG, WILLOW.stairs());
        insertBefore(MANGROVE_LOG, WILLOW.slab());
        insertBefore(MANGROVE_LOG, WILLOW.bookshelf());
        insertBefore(MANGROVE_LOG, WILLOW.trimmedPlanks());
        if (Mods.FARMERSDELIGHT.isLoaded()) insertBefore(MANGROVE_LOG, FDIntegration.WILLOW_CABINET);
        insertBefore(MANGROVE_LOG, WILLOW.fence());
        insertBefore(MANGROVE_LOG, WILLOW.fenceGate());
        insertBefore(MANGROVE_LOG, WILLOW.door());
        insertBefore(MANGROVE_LOG, WILLOW.trapdoor());
        insertBefore(MANGROVE_LOG, WILLOW.pressurePlate());
        insertBefore(MANGROVE_LOG, WILLOW.button());

        insertAfter(OAK_SLAB, TRIMMED_OAK_PLANKS);
        insertAfterStack(OAK_SLAB.getDefaultInstance(), BOOKSHELF.getDefaultInstance());
        insertAfter(SPRUCE_SLAB, TRIMMED_SPRUCE_PLANKS);
        insertAfter(SPRUCE_SLAB, SPRUCE_BOOKSHELF);
        insertAfter(BIRCH_SLAB, TRIMMED_BIRCH_PLANKS);
        insertAfter(BIRCH_SLAB, BIRCH_BOOKSHELF);
        insertAfter(JUNGLE_SLAB, TRIMMED_JUNGLE_PLANKS);
        insertAfter(JUNGLE_SLAB, JUNGLE_BOOKSHELF);
        insertAfter(ACACIA_SLAB, TRIMMED_ACACIA_PLANKS);
        insertAfter(ACACIA_SLAB, ACACIA_BOOKSHELF);
        insertAfter(DARK_OAK_SLAB, TRIMMED_DARK_OAK_PLANKS);
        insertAfter(DARK_OAK_SLAB, DARK_OAK_BOOKSHELF);
        insertAfter(CHERRY_SLAB, TRIMMED_CHERRY_PLANKS);
        insertAfter(CHERRY_SLAB, CHERRY_BOOKSHELF);
        insertAfter(MANGROVE_SLAB, TRIMMED_MANGROVE_PLANKS);
        insertAfter(MANGROVE_SLAB, MANGROVE_BOOKSHELF);
        insertAfter(CRIMSON_SLAB, TRIMMED_CRIMSON_PLANKS);
        insertAfter(CRIMSON_SLAB, CRIMSON_BOOKSHELF);
        insertAfter(WARPED_SLAB, TRIMMED_WARPED_PLANKS);
        insertAfter(WARPED_SLAB, WARPED_BOOKSHELF);
        insertAfter(BAMBOO_MOSAIC_SLAB, TRIMMED_BAMBOO_PLANKS);
        insertAfter(BAMBOO_MOSAIC_SLAB, BAMBOO_BOOKSHELF);

        insertBefore(AMETHYST_BLOCK, QUARTZITE);
        insertBefore(STONE, CRUDE_TRAPDOOR);
        insertBefore(STONE, CRUDE_DOOR);
        insertBefore(STONE, WOODEN_PLATFORM_STAIRS);
        insertBefore(STONE, WOODEN_PLATFORM);

        insertAfter(CHISELED_TUFF_BRICKS, CHISELED_SILTSTONE);
        insertAfter(CHISELED_TUFF_BRICKS, SILTSTONE_BRICK_WALL);
        insertAfter(CHISELED_TUFF_BRICKS, SILTSTONE_BRICK_SLAB);
        insertAfter(CHISELED_TUFF_BRICKS, SILTSTONE_BRICK_STAIRS);
        insertAfter(CHISELED_TUFF_BRICKS, SILTSTONE_BRICKS);
        insertAfter(CHISELED_TUFF_BRICKS, POLISHED_SILTSTONE_SLAB);
        insertAfter(CHISELED_TUFF_BRICKS, POLISHED_SILTSTONE_STAIRS);
        insertAfter(CHISELED_TUFF_BRICKS, POLISHED_SILTSTONE);
        insertAfter(CHISELED_TUFF_BRICKS, SILTSTONE_WALL);
        insertAfter(CHISELED_TUFF_BRICKS, SILTSTONE_SLAB);
        insertAfter(CHISELED_TUFF_BRICKS, SILTSTONE_STAIRS);
        insertAfter(CHISELED_TUFF_BRICKS, SILTSTONE);

        insertAfter(MUD_BRICK_WALL, THATCH_SLAB);
        insertAfter(MUD_BRICK_WALL, THATCH_STAIRS);
        insertAfter(MUD_BRICK_WALL, THATCH);
    }

    private void addNaturalBlocks(final BuildCreativeModeTabContentsEvent event) {
        insertAfter(SHORT_GRASS, NMLItems.FROSTED_GRASS);
        insertAfter(SHORT_GRASS, OAT_GRASS);
        insertAfter(SHORT_GRASS, GRASS_SPROUTS);
        insertAfter(FERN, FIDDLEHEAD);
        insertBefore(DEAD_BUSH, SHORT_BEACHGRASS);
        insertBefore(DEAD_BUSH, TALL_BEACHGRASS);
        insertBefore(DEAD_BUSH, CAVE_WEEDS);
        insertBefore(DEAD_BUSH, DRIED_GRASS);
        insertBefore(CRIMSON_ROOTS, MYCELIUM_GROWTHS);
        insertBefore(CRIMSON_ROOTS, MYCELIUM_SPROUTS);
        insertAfter(LARGE_FERN, CATTAIL);
        insertAfter(LARGE_FERN, REEDS);
        insertAfter(LILY_PAD, NMLItems.DUCKWEED);
        insertAfter(LILY_PAD, NMLItems.WATER_MOSAIC);
        insertAfter(HANGING_ROOTS, BEARD_MOSS);
        insertAfter(PINK_PETALS, GROUND_IVY);
        insertAfter(PINK_PETALS, CLOVER_PATCH);
        insertAfter(PINK_PETALS, RED_FLOWERBED);
        insertAfter(PINK_PETALS, YELLOW_FLOWERBED);
        insertAfter(PINK_PETALS, BLUE_FLOWERBED);
        insertAfter(PINK_PETALS, VIOLET_FLOWERBED);
        insertAfter(PINK_PETALS, WHITE_FLOWERBED);
        insertAfter(LILY_OF_THE_VALLEY, LAVENDER_BUSH);
        insertAfter(LILY_OF_THE_VALLEY, RED_LUPINE);
        insertAfter(LILY_OF_THE_VALLEY, BLUE_LUPINE);
        insertAfter(LILY_OF_THE_VALLEY, PINK_LUPINE);
        insertAfter(LILY_OF_THE_VALLEY, YELLOW_LUPINE);
        insertAfter(LILY_OF_THE_VALLEY, ACONITE);
        insertAfter(LILY_OF_THE_VALLEY, WILD_MINT);
        insertAfter(LILY_OF_THE_VALLEY, AUTUMN_CROCUS);
        insertAfter(LILY_OF_THE_VALLEY, THISTLE);
        insertAfter(LILY_OF_THE_VALLEY, STARFLOWER);
        insertAfter(SPORE_BLOSSOM, RAFFLESIA);
        insertAfter(DEAD_BUSH, BARREL_CACTUS);
        insertAfter(DEAD_BUSH, SUCCULENT);
        insertAfter(DEAD_BUSH, PICKLEWEED);
        insertAfter(WARPED_FUNGUS, PEBBLES);
        insertAfter(WARPED_FUNGUS, SEASHELLS);
        insertAfter(BIRCH_LEAVES, YELLOW_BIRCH_LEAVES);
        insertAfter(BIRCH_SAPLING, YELLOW_BIRCH_SAPLING);
        insertAfter(OAK_LEAVES, AUTUMNAL_OAK_LEAVES);
        insertAfter(OAK_SAPLING, AUTUMNAL_OAK_SAPLING);
        insertAfter(CHERRY_LEAVES, PALE_CHERRY_LEAVES);
        insertAfter(CHERRY_SAPLING, PALE_CHERRY_SAPLING);
        insertBefore(GRAVEL, SILT);
        insertBefore(GRAVEL, COARSE_SILT);
        insertBefore(GRAVEL, SILT_PATH);
        insertAfter(RED_MUSHROOM, SHELF_MUSHROOM);
        insertAfter(RED_MUSHROOM, NMLItems.FIELD_MUSHROOM);
        if (Mods.FARMERSDELIGHT.isLoaded()) insertAfter(RED_MUSHROOM, FDIntegration.FIELD_MUSHROOM_COLONY);
        insertAfter(RED_MUSHROOM_BLOCK, SHELF_MUSHROOM_BLOCK);
        insertAfter(RED_MUSHROOM_BLOCK, FIELD_MUSHROOM_BLOCK);
        insertAfter(SPRUCE_LEAVES, FROSTED_LEAVES);
        insertAfter(SAND, SAND_PATH);
        insertAfter(RED_SAND, RED_SAND_PATH);
        insertAfter(SNOW_BLOCK, SNOW_PATH);
        insertAfter(GRAVEL, GRAVEL_PATH);
        insertAfter(SNOW, SNOWY_GRASS_PATH);
        insertAfter(MYCELIUM, MYCELIUM_PATH);
        insertAfter(PODZOL, PODZOL_PATH);
        insertBefore(DIRT, NMLBlocks.DIRT_PATH);
        insertAfter(AMETHYST_CLUSTER, QUARTZITE_CLUSTER);
        insertAfter(AMETHYST_CLUSTER, LARGE_QUARTZITE_BUD);
        insertAfter(AMETHYST_CLUSTER, MEDIUM_QUARTZITE_BUD);
        insertAfter(AMETHYST_CLUSTER, SMALL_QUARTZITE_BUD);
        insertAfter(AMETHYST_CLUSTER, BUDDING_QUARTZITE);
        insertAfter(AMETHYST_CLUSTER, QUARTZITE);

        insertAfter(HAY_BLOCK, TROPICAL_FISH_BARREL);
        insertAfter(HAY_BLOCK, PUFFERFISH_BARREL);
        insertAfter(HAY_BLOCK, BILLHOOK_BASS_BARREL);
        insertAfter(HAY_BLOCK, SALMON_BARREL);
        insertAfter(HAY_BLOCK, COD_BARREL);
        insertAfter(HAY_BLOCK, PEAR_CRATE);
        insertAfter(HAY_BLOCK, APPLE_CRATE);

        insertAfter(TURTLE_EGG, TORTOISE_EGGS);

        insertAfter(OCHRE_FROGLIGHT, VERMILION_FROGLIGHT);

        insertAfter(ROSE_BUSH, ROSE_VINES);

        insertAfter(TUFF, SILTSTONE);


        insertAfter(BEDROCK, ANCESTRAL_CARVING);
        insertAfter(BEDROCK, ANCESTRAL_EFFIGY);

//            event.accept(REMAINS);

        insertAfter(SPRUCE_LOG, PINE.log());
        insertAfter(SPRUCE_LEAVES, PINE_LEAVES);
        insertAfter(SPRUCE_SAPLING, PINE_SAPLING);

        insertBefore(DARK_OAK_LOG, MAPLE.log());
        insertBefore(DARK_OAK_LEAVES, MAPLE_LEAVES);
        insertBefore(DARK_OAK_SAPLING, MAPLE_SAPLING);
        insertBefore(DARK_OAK_LEAVES, RED_MAPLE_LEAVES);
        insertBefore(DARK_OAK_SAPLING, RED_MAPLE_SAPLING);

        insertAfter(DARK_OAK_LOG, WALNUT.log());
        insertAfter(DARK_OAK_LEAVES, WALNUT_LEAVES);
        insertAfter(DARK_OAK_SAPLING, WALNUT_SAPLING);

        insertBefore(MANGROVE_LOG, WILLOW.log());
        insertBefore(MANGROVE_LEAVES, WILLOW_LEAVES);
        insertBefore(MANGROVE_PROPAGULE, WILLOW_SAPLING);

        insertAfter(BLUE_ICE, ICICLES);

    }

    private void addFunctionalBlocks(final BuildCreativeModeTabContentsEvent event) {
        insertAfter(BOOKSHELF, WARPED_BOOKSHELF);
        insertAfter(BOOKSHELF, CRIMSON_BOOKSHELF);
        insertAfter(BOOKSHELF, CHERRY_BOOKSHELF);
        insertAfter(BOOKSHELF, BAMBOO_BOOKSHELF);
        insertAfter(BOOKSHELF, MANGROVE_BOOKSHELF);
        insertAfter(BOOKSHELF, WILLOW.bookshelf());
        insertAfter(BOOKSHELF, WALNUT.bookshelf());
        insertAfter(BOOKSHELF, DARK_OAK_BOOKSHELF);
        insertAfter(BOOKSHELF, MAPLE.bookshelf());
        insertAfter(BOOKSHELF, ACACIA_BOOKSHELF);
        insertAfter(BOOKSHELF, JUNGLE_BOOKSHELF);
        insertAfter(BOOKSHELF, BIRCH_BOOKSHELF);
        insertAfter(BOOKSHELF, PINE.bookshelf());
        insertAfter(BOOKSHELF, SPRUCE_BOOKSHELF);
        insertAfter(SPRUCE_HANGING_SIGN, PINE_HANGING_SIGN);
        insertAfter(SPRUCE_HANGING_SIGN, PINE_SIGN);
        insertAfter(DARK_OAK_HANGING_SIGN, WALNUT_HANGING_SIGN);
        insertAfter(DARK_OAK_HANGING_SIGN, WALNUT_SIGN);
        insertBefore(DARK_OAK_SIGN, MAPLE_SIGN);
        insertBefore(DARK_OAK_SIGN, MAPLE_HANGING_SIGN);
        insertBefore(MANGROVE_SIGN, WILLOW_SIGN);
        insertBefore(MANGROVE_SIGN, WILLOW_HANGING_SIGN);
        insertAfter(REDSTONE_TORCH, NMLItems.SCONCE_SOUL_TORCH);
        insertAfter(REDSTONE_TORCH, NMLItems.SCONCE_TORCH);
        insertAfter(CAULDRON, TAP);
        insertBefore(SCAFFOLDING, NMLItems.WOODEN_SCAFFOLDING);
        insertAfter(CONDUIT, NMLItems.WARDING_EFFIGY);
        insertAfter(PINK_BED, STRAW_BED);

        insertAfter(OCHRE_FROGLIGHT, VERMILION_FROGLIGHT);

        if (Mods.NIRVANA.isLoaded()) insertBefore(Mods.NIRVANA.getItem("thc"), NirvanaIntegration.FAT_JOINT_ITEM);
    }

    private void addFoodAndDrinks(final BuildCreativeModeTabContentsEvent event) {
        insertAfter(COOKED_BEEF, HORSE_STEAK);
        insertAfter(COOKED_BEEF, RAW_HORSE);
        insertAfter(COOKED_MUTTON, COOKED_VENISON);
        insertAfter(COOKED_MUTTON, RAW_VENISON);
        insertAfter(COOKED_RABBIT, COOKED_FROG_LEG);
        insertAfter(COOKED_RABBIT, FROG_LEG);
        insertAfter(HONEY_BOTTLE, MAPLE_SYRUP_BOTTLE);
        insertAfter(PUMPKIN_PIE, PANCAKE);
        if (Mods.FARMERSDELIGHT.isLoaded()) {
            insertAfter(PUMPKIN_PIE, FDIntegration.PEAR_COBBLER_ITEM);
            insertAfter(CAKE, FDIntegration.FRUIT_CAKE);
            insertBefore(MILK_BUCKET, FDIntegration.PESTO_BOTTLE);
        }
        insertAfter(COOKIE, MAPLE_TART);
        insertAfter(COOKIE, SWEET_TART);
        insertAfter(ENCHANTED_GOLDEN_APPLE, SYRUPED_PEAR);
        insertAfter(ENCHANTED_GOLDEN_APPLE, PEAR);
        insertAfter(APPLE, HONEYED_APPLE);
        insertAfter(MELON_SLICE, TRAIL_MIX);
        insertAfter(MELON_SLICE, WALNUTS);
        insertAfter(MELON_SLICE, PINE_NUTS);
        insertAfter(MUSHROOM_STEW, MASHED_POTATOES_WITH_MUSHROOMS);
        insertAfter(POISONOUS_POTATO, GRILLED_MUSHROOMS);
        insertAfter(COOKED_SALMON, COOKED_BILLHOOK_BASS);
        insertAfter(COOKED_SALMON, BILLHOOK_BASS);
        insertAfter(SPIDER_EYE, HARDTACK);
//            insertAfter(TROPICAL_FISH, CAVE_CARP);
    }

    private void addToolsAndUtilities(final BuildCreativeModeTabContentsEvent event) {
        insertBefore(DARK_OAK_BOAT, MAPLE_BOAT);
        insertBefore(DARK_OAK_BOAT, MAPLE_CHEST_BOAT);
        insertBefore(BIRCH_BOAT, PINE_BOAT);
        insertBefore(BIRCH_BOAT, PINE_CHEST_BOAT);
        insertBefore(MANGROVE_BOAT, WALNUT_BOAT);
        insertBefore(MANGROVE_BOAT, WALNUT_CHEST_BOAT);

        if (Mods.BOATLOAD.isLoaded()) {
            insertBefore(BIRCH_BOAT, BoatloadIntegration.PINE_FURNACE_BOAT);
            insertBefore(BIRCH_BOAT, BoatloadIntegration.LARGE_PINE_BOAT);
            insertBefore(DARK_OAK_BOAT, BoatloadIntegration.MAPLE_FURNACE_BOAT);
            insertBefore(DARK_OAK_BOAT, BoatloadIntegration.LARGE_MAPLE_BOAT);

            insertBefore(MANGROVE_BOAT, BoatloadIntegration.WALNUT_FURNACE_BOAT);
            insertBefore(MANGROVE_BOAT, BoatloadIntegration.LARGE_WALNUT_BOAT);

            insertBefore(MANGROVE_BOAT, WILLOW_BOAT);
            insertBefore(MANGROVE_BOAT, WILLOW_CHEST_BOAT);
            insertBefore(MANGROVE_BOAT, BoatloadIntegration.WILLOW_FURNACE_BOAT);
            insertBefore(MANGROVE_BOAT, BoatloadIntegration.LARGE_WILLOW_BOAT);
        } else {
            insertBefore(MANGROVE_BOAT, WILLOW_BOAT);
            insertBefore(MANGROVE_BOAT, WILLOW_CHEST_BOAT);
        }

        insertAfter(SALMON_BUCKET, BILLHOOK_BASS_BUCKET);
        insertAfter(ELYTRA, NMLItems.WARDING_EFFIGY);
        insertAfter(ELYTRA, LIVING_URN);
        insertAfter(ELYTRA, ANCIENT_BRONZE_MASK);
        insertAfter(MUSIC_DISC_5, MUSIC_DISC_GUIDANCE);

        insertAfter(TNT_MINECART, WARDING_BANDAGE);
        insertAfter(TNT_MINECART, MEDICINAL_BANDAGE);
        insertAfter(TNT_MINECART, ANTIDOTE_BANDAGE);
        insertAfter(TNT_MINECART, BANDAGE);

        insertAfter(NETHERITE_HOE, ANCIENT_BRONZE_CHISEL);
        insertAfter(NETHERITE_HOE, RITUAL_PICK);

        event.getParameters().holders().lookup(Registries.POTION).ifPresent(
            potionLookup -> generateBandageEffectTypes(
                event,
                potionLookup,
                NMLItems.BANDAGE.get(),
                    event.getFlags()
            )
        );

//            insertAfter(TROPICAL_FISH_BUCKET, CAVE_CARP_BUCKET);
        if (!event.getFlags().contains(FeatureFlags.BUNDLE)) {
            insertBeforeStack(FLINT_AND_STEEL.getDefaultInstance(), BUNDLE.getDefaultInstance());
        }
    }

    private void addCombat(final BuildCreativeModeTabContentsEvent event) {
        insertAfter(WIND_CHARGE, INK_BOMB);
        insertAfter(WIND_CHARGE, FIREBOMB);
        insertAfter(SNOWBALL, LIVING_URN);
        insertBefore(TNT, EXPLOSIVE);
        insertAfter(EGG, RESIN_OIL_BOTTLE);
        insertAfter(TURTLE_HELMET, TORTOISE_SHELL);
        insertAfter(TURTLE_HELMET, ANCIENT_BRONZE_MASK);
        insertAfter(SPECTRAL_ARROW, INCENDIARY_ARROW);
        insertAfter(NETHERITE_SWORD, ANCESTRAL_OATH_SWORD);
    }

    private void addIngredients(final BuildCreativeModeTabContentsEvent event) {
        insertAfter(HONEYCOMB, RESIN);
        insertAfter(RESIN.asItem(), RESIN_OIL_BOTTLE);
        insertAfter(TURTLE_SCUTE, STURDY_SCUTE);
        insertAfter(NETHER_WART, AWKWARD_RESIDUE);
    }

    private void addRedstoneBlocks(final BuildCreativeModeTabContentsEvent event) {
        insertAfter(LIGHTNING_ROD, SPIKE_TRAP);
    }

    private void addSpawnEggs(final BuildCreativeModeTabContentsEvent event) {
        insertAfter(SPAWNER, MONSTER_ANCHOR);
        // TODO: moose
//            insertAfter(CREEPER_SPAWN_EGG, MOOSE_SPAWN_EGG);
        insertAfter(CREEPER_SPAWN_EGG, DEER_SPAWN_EGG);
        insertAfter(BEE_SPAWN_EGG, BILLHOOK_BASS_SPAWN_EGG);
        insertAfter(CHICKEN_SPAWN_EGG, GOOSE_SPAWN_EGG);
        insertBefore(TURTLE_SPAWN_EGG, TORTOISE_SPAWN_EGG);
        insertBefore(BREEZE_SPAWN_EGG, BUDDY_SPAWN_EGG);
    }
}
