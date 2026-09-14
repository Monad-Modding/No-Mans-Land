package com.farcr.nomansland.datagen.tags;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.block.cauldrons.FourLayeredCauldronBlock;
import com.farcr.nomansland.common.block.cauldrons.WitchStewCauldron;
import com.farcr.nomansland.common.definitions.BlockDefinition;
import com.farcr.nomansland.common.integration.BlueprintIntegration;
import com.farcr.nomansland.common.registry.NMLTags;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.*;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class NMLBlockTagsProvider extends BlockTagsProvider {
    public NMLBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, NoMansLand.MODID, existingFileHelper);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void addTags(HolderLookup.Provider provider) {
        for (BlockDefinition<?> definition : NMLBlocks.BLOCK_DEFINITIONS) {
            if (definition.get() instanceof FlowerBlock)
                tag(BlockTags.SMALL_FLOWERS).add(definition.block());
            if (definition.get() instanceof WallBlock)
                tag(BlockTags.WALLS).add(definition.block());
            if (definition.get() instanceof SaplingBlock)
                tag(BlockTags.SAPLINGS).add(definition.block());
            if (definition.get() instanceof FourLayeredCauldronBlock && !(definition.get() instanceof WitchStewCauldron))
                tag(BlockTags.CAULDRONS).add(definition.block());
            if (definition.get() instanceof TorchBlock && !(definition.get() instanceof WallTorchBlock))
                tag(BlockTags.WALL_POST_OVERRIDE).add(definition.block());
            if (definition.get() instanceof LeavesBlock)
                tag(BlockTags.LEAVES).add(definition.block());
            if (definition.get() instanceof SlabBlock)
                tag(BlockTags.SLABS).add(definition.block());
            if (definition.get() instanceof StairBlock)
                tag(BlockTags.STAIRS).add(definition.block());
        }

        tag(BlockTags.CAULDRONS).addOptional(NoMansLand.location("pot_of_witch_stew"));

        for (NMLBlocks.Woodset woodset : NMLBlocks.WOODSETS) {
            tag(BlockTags.STANDING_SIGNS).add(woodset.sign().block());
            tag(BlockTags.WALL_SIGNS).add(woodset.wallSign().block());
            tag(BlockTags.CEILING_HANGING_SIGNS).add(woodset.hangingSign().block());
            tag(BlockTags.WALL_HANGING_SIGNS).add(woodset.hangingWallSign().block());
            tag(BlockTags.PLANKS).add(woodset.planks().block());
            tag(NMLTags.BOOKSHELVES.blockTag()).add(woodset.bookshelf().block());
            tag(Tags.Blocks.FENCE_GATES_WOODEN).add(woodset.fenceGate().block());
            tag(BlockTags.FENCE_GATES).add(woodset.fenceGate().block());
            tag(Tags.Blocks.FENCES_WOODEN).add(woodset.fence().block());
            tag(BlockTags.WOODEN_FENCES).add(woodset.fence().block());
            tag(BlockTags.WOODEN_STAIRS).add(woodset.stairs().block());
            tag(BlockTags.WOODEN_BUTTONS).add(woodset.button().block());
            tag(BlockTags.WOODEN_DOORS).add(woodset.door().block());
            tag(BlockTags.WOODEN_SLABS).add(woodset.slab().block());
            tag(BlockTags.WOODEN_PRESSURE_PLATES).add(woodset.pressurePlate().block());
            tag(BlockTags.WOODEN_TRAPDOORS).add(woodset.trapdoor().block());
            tag(BlockTags.LOGS).add(woodset.log().block());
            tag(Tags.Blocks.STRIPPED_LOGS).add(woodset.strippedLog().block());
            tag(Tags.Blocks.STRIPPED_WOODS).add(woodset.strippedWood().block());
        }

        tag(NMLTags.BOOKSHELVES.blockTag()).add(
                NMLBlocks.ACACIA_BOOKSHELF.block(),
                NMLBlocks.BAMBOO_BOOKSHELF.block(),
                NMLBlocks.BIRCH_BOOKSHELF.block(),
                NMLBlocks.CHERRY_BOOKSHELF.block(),
                NMLBlocks.CRIMSON_BOOKSHELF.block(),
                NMLBlocks.DARK_OAK_BOOKSHELF.block(),
                NMLBlocks.JUNGLE_BOOKSHELF.block(),
                NMLBlocks.MANGROVE_BOOKSHELF.block(),
                NMLBlocks.SPRUCE_BOOKSHELF.block(),
                NMLBlocks.WARPED_BOOKSHELF.block()
        );

        addToTags(NMLTags.BOOKSHELVES.blockTag(), Tags.Blocks.BOOKSHELVES, BlockTags.ENCHANTMENT_POWER_PROVIDER, BlueprintIntegration.woodenBookshelves().blockTag());
        tag(BlockTags.PLANKS).addTag(NMLTags.TRIMMED_PLANKS.blockTag());

        tag(BlockTags.LOGS_THAT_BURN).addTags(NMLTags.MAPLE_LOGS.blockTag(), NMLTags.PINE_LOGS.blockTag(), NMLTags.WALNUT_LOGS.blockTag(), NMLTags.WILLOW_LOGS.blockTag());
        tag(BlockTags.OVERWORLD_NATURAL_LOGS).add(
                NMLBlocks.MAPLE.log().block(),
                NMLBlocks.PINE.log().block(),
                NMLBlocks.WALNUT.log().block(),
                NMLBlocks.WILLOW.log().block()
        );

        addToTags(NMLBlocks.SILT.block(),
                BlockTags.OVERWORLD_CARVER_REPLACEABLES,
                BlockTags.LUSH_GROUND_REPLACEABLE,
                BlockTags.MOSS_REPLACEABLE,
                BlockTags.SCULK_REPLACEABLE,
                BlockTags.SCULK_REPLACEABLE_WORLD_GEN,
                BlockTags.CONVERTABLE_TO_MUD,
                BlockTags.DEAD_BUSH_MAY_PLACE_ON,
                BlockTags.BIG_DRIPLEAF_PLACEABLE,
                BlockTags.BAMBOO_PLANTABLE_ON
        );

        addToTags(NMLBlocks.SILTSTONE.block(),
                BlockTags.OVERWORLD_CARVER_REPLACEABLES,
                BlockTags.LUSH_GROUND_REPLACEABLE,
                BlockTags.MOSS_REPLACEABLE,
                BlockTags.SCULK_REPLACEABLE,
                BlockTags.SCULK_REPLACEABLE_WORLD_GEN,
                BlockTags.SNAPS_GOAT_HORN,
                BlockTags.BASE_STONE_OVERWORLD,
                BlockTags.GOATS_SPAWNABLE_ON,
                BlockTags.AZALEA_ROOT_REPLACEABLE,
                BlockTags.STONE_ORE_REPLACEABLES,
                BlockTags.DRIPSTONE_REPLACEABLE
        );

        addToTags(NMLBlocks.CRUDE_DOOR.block(),
                BlockTags.WOODEN_DOORS
        );

        addToTags(NMLBlocks.CRUDE_TRAPDOOR.block(),
                BlockTags.WOODEN_TRAPDOORS
        );

        addToTags(NMLBlocks.QUARTZITE_CLUSTER.block(),
                Tags.Blocks.CLUSTERS
        );

        addToTags(NMLBlocks.CUT_VINE.get(), BlockTags.MANGROVE_LOGS_CAN_GROW_THROUGH, BlockTags.MANGROVE_ROOTS_CAN_GROW_THROUGH);
        addToTags(NMLBlocks.SEASHELLS.get(), BlockTags.COMBINATION_STEP_SOUND_BLOCKS);
        addToTags(NMLBlocks.SMALL_QUARTZITE_BUD.get(), BlockTags.INSIDE_STEP_SOUND_BLOCKS);

        tag(NMLTags.ORE_VEIN_REPLACEABLE).add(Blocks.STONE, Blocks.DEEPSLATE, Blocks.STONE_STAIRS, Blocks.STONE_SLAB);
        tag(BlockTags.OVERWORLD_CARVER_REPLACEABLES).add(Blocks.COAL_BLOCK, Blocks.RAW_GOLD_BLOCK, NMLBlocks.SILTSTONE.get(), NMLBlocks.QUARTZITE.get(), Blocks.TUFF, Blocks.COAL_ORE, Blocks.SMOOTH_BASALT, Blocks.BASALT);

        tag(NMLTags.INCORRECT_FOR_RITUAL_TOOL).addTag(BlockTags.INCORRECT_FOR_STONE_TOOL);
        tag(NMLTags.RESONATES_WITH_RITUAL_PICKAXE).addTag(Tags.Blocks.ORES);
        tag(NMLTags.OCCLUDES_RITUAL_PICKAXE_RESONANCE).addTags(BlockTags.OCCLUDES_VIBRATION_SIGNALS);
    }

    @SafeVarargs
    protected final void addToTags(Block block, TagKey<Block>... blockTags) {
        List.of(blockTags).forEach(blockTag -> tag(blockTag).add(block));
    }

    @SafeVarargs
    protected final void addToTags(TagKey<Block> block, TagKey<Block>... blockTags) {
        List.of(blockTags).forEach(blockTag -> tag(blockTag).addTag(block));
    }
}
