package com.farcr.nomansland.datagen.tags;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.definitions.BlockDefinition;
import com.farcr.nomansland.common.definitions.ItemDefinition;
import com.farcr.nomansland.common.integration.BlueprintIntegration;
import com.farcr.nomansland.common.registry.NMLTags;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import com.farcr.nomansland.common.registry.items.NMLItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class NMLItemTagsProvider extends ItemTagsProvider {


    public NMLItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, CompletableFuture<TagLookup<Block>> blockTags, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTags, NoMansLand.MODID, existingFileHelper);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void addTags(HolderLookup.Provider provider) {
        for (BlockDefinition<?> definition : NMLBlocks.BLOCK_DEFINITIONS) {
            if (definition.hasItem()) {
                if (definition.get() instanceof FlowerBlock)
                    tag(ItemTags.SMALL_FLOWERS).add(definition.item());
                if (definition.get() instanceof WallBlock)
                    tag(ItemTags.WALLS).add(definition.item());
                if (definition.get() instanceof SaplingBlock)
                    tag(ItemTags.SAPLINGS).add(definition.item());
                if (definition.get() instanceof LeavesBlock)
                    tag(ItemTags.LEAVES).add(definition.item());
                if (definition.get() instanceof SlabBlock)
                    tag(ItemTags.SLABS).add(definition.item());

            }
        }

        for (ItemDefinition<?> definition : NMLItems.ITEM_DEFINITIONS) {
            if (definition.hasItem()) {
                if (definition.get() instanceof BoatItem)
                    tag(ItemTags.BOATS).add(definition.item());
            }
        }

        for (NMLBlocks.Woodset woodset : NMLBlocks.WOODSETS) {
            tag(ItemTags.SIGNS).add(woodset.sign().item());
            tag(ItemTags.HANGING_SIGNS).add(woodset.hangingSign().item());
            tag(ItemTags.PLANKS).add(woodset.planks().item());
            tag(ItemTags.PLANKS).add(woodset.trimmedPlanks().item());
            tag(NMLTags.BOOKSHELVES.itemTag()).add(woodset.bookshelf().item());
            tag(Tags.Items.FENCE_GATES_WOODEN).add(woodset.fenceGate().item());
            tag(Tags.Items.FENCES_WOODEN).add(woodset.fence().item());
            tag(ItemTags.WOODEN_FENCES).add(woodset.fence().item());
            tag(ItemTags.WOODEN_STAIRS).add(woodset.stairs().item());
            tag(ItemTags.WOODEN_BUTTONS).add(woodset.button().item());
            tag(ItemTags.WOODEN_DOORS).add(woodset.door().item());
            tag(ItemTags.WOODEN_SLABS).add(woodset.slab().item());
            tag(ItemTags.WOODEN_PRESSURE_PLATES).add(woodset.pressurePlate().item());
            tag(ItemTags.WOODEN_TRAPDOORS).add(woodset.trapdoor().item());
            tag(ItemTags.LOGS).add(woodset.log().item());
            tag(Tags.Items.STRIPPED_LOGS).add(woodset.strippedLog().item());
            tag(Tags.Items.STRIPPED_WOODS).add(woodset.strippedWood().item());
        }

        tag(NMLTags.BOOKSHELVES.itemTag()).add(
                NMLBlocks.ACACIA_BOOKSHELF.item(),
                NMLBlocks.BAMBOO_BOOKSHELF.item(),
                NMLBlocks.BIRCH_BOOKSHELF.item(),
                NMLBlocks.CHERRY_BOOKSHELF.item(),
                NMLBlocks.CRIMSON_BOOKSHELF.item(),
                NMLBlocks.DARK_OAK_BOOKSHELF.item(),
                NMLBlocks.JUNGLE_BOOKSHELF.item(),
                NMLBlocks.MANGROVE_BOOKSHELF.item(),
                NMLBlocks.SPRUCE_BOOKSHELF.item(),
                NMLBlocks.WARPED_BOOKSHELF.item()
        );

        tag(ItemTags.HEAD_ARMOR).add(NMLItems.ANCIENT_BRONZE_MASK.get());
        tag(ItemTags.HEAD_ARMOR_ENCHANTABLE).add(NMLItems.ANCIENT_BRONZE_MASK.get());
        tag(ItemTags.EQUIPPABLE_ENCHANTABLE).add(NMLItems.ANCIENT_BRONZE_MASK.get());
        tag(ItemTags.EQUIPPABLE_ENCHANTABLE).add(NMLItems.ANCIENT_BRONZE_MASK.get());
        tag(ItemTags.CHEST_ARMOR).add(NMLItems.TORTOISE_SHELL.get());
        tag(ItemTags.CHEST_ARMOR_ENCHANTABLE).add(NMLItems.TORTOISE_SHELL.get());
        tag(ItemTags.FISHES).add(NMLItems.COOKED_BILLHOOK_BASS.get(), NMLItems.BILLHOOK_BASS.get());

        addToTags(NMLTags.BOOKSHELVES.itemTag(), Tags.Items.BOOKSHELVES, BlueprintIntegration.woodenBookshelves().itemTag());
        tag(ItemTags.PLANKS).addTag(NMLTags.TRIMMED_PLANKS.itemTag());
        tag(ItemTags.LOGS_THAT_BURN).addTags(NMLTags.MAPLE_LOGS.itemTag(), NMLTags.PINE_LOGS.itemTag(), NMLTags.WALNUT_LOGS.itemTag(), NMLTags.WILLOW_LOGS.itemTag());
    }

    @SafeVarargs
    protected final void addToTags(Item item, TagKey<Item>... itemTags) {
        List.of(itemTags).forEach(itemTag -> tag(itemTag).add(item));
    }

    @SafeVarargs
    protected final void addToTags(TagKey<Item> item, TagKey<Item>... itemTags) {
        List.of(itemTags).forEach(itemTag -> tag(itemTag).addTag(item));
    }
}
