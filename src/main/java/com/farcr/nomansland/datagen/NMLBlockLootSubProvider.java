package com.farcr.nomansland.datagen;

import com.farcr.nomansland.common.definitions.BlockDefinition;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import com.farcr.nomansland.datagen.loot.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.loot.CanItemPerformAbility;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

public class NMLBlockLootSubProvider extends BlockLootSubProvider {
    public NMLBlockLootSubProvider(HolderLookup.Provider provider) {
        super(new HashSet<>(), FeatureFlags.REGISTRY.allFlags(), provider);
    }

    protected static LootTable.@NotNull Builder createShearsOnlyDrop(ItemLike itemLike) {
        return LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .add(LootItem.lootTableItem(itemLike))
                        .when(CanItemPerformAbility.canItemPerformAbility(ItemAbilities.SHEARS_DIG)));
    }

    @Override
    protected @NotNull Iterable<Block> getKnownBlocks() {
        return NMLBlocks.BLOCK_DEFINITIONS.stream()
                .filter(blockDefinition -> !(blockDefinition.lootType() instanceof CustomBlockLootType))
                .map(BlockDefinition::block)
                .toList();
    }

    @Override
    protected void generate() {
        for (BlockDefinition<?> definition : NMLBlocks.BLOCK_DEFINITIONS) {
            Block block = definition.get();
            BlockLootType lootType = definition.lootType();

            if (lootType instanceof SelfBlockLootType)
                dropSelf(block);
            else if (lootType instanceof OtherBlockLootType otherBlockLootType)
                add(block, createSingleItemTable(otherBlockLootType.getBlock()));
            else if (lootType instanceof ShearsBlockLootType)
                add(block, createShearsOnlyDrop(block));
            else if (lootType instanceof OtherShearsBlockLootType otherShearsBlockLootType)
                add(block, createShearsOnlyDrop(otherShearsBlockLootType.getBlock()));
            else if (lootType instanceof SlabBlockLootType)
                add(block, createSlabItemTable(block));
            else if (lootType instanceof DoorBlockLootType)
                add(block, createDoorTable(block));
            else if (lootType instanceof BedBlockLootType)
                add(block, createSinglePropConditionTable(block, BedBlock.PART, BedPart.HEAD));
            else if (lootType instanceof CandleCakeBlockLootType candleCakeBlockLootType)
                add(block, createCandleCakeDrops(candleCakeBlockLootType.getCandle()));
            else if (lootType instanceof FlowerPotBlockLootType flowerPotBlockLootType)
                add(block, createPotFlowerItemTable(flowerPotBlockLootType.getPlant()));
            else if (lootType instanceof BookshelfBlockLootType)
                add(block, createSelfDropDispatchTable(block, hasSilkTouch(), this.applyExplosionDecay(block, LootItem.lootTableItem(Items.BOOK).apply(SetItemCountFunction.setCount(ConstantValue.exactly(3.0f))))));
            else if (lootType instanceof OreBlockLootType ore) {
                LootTable.Builder table = this.createSilkTouchDispatchTable(block, this.applyExplosionDecay(block,
                        LootItem.lootTableItem(ore.getDrop())
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(ore.getMin(), ore.getMax())))
                                .apply(ApplyBonusCount.addOreBonusCount(this.registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE)))));
                if (ore.getRareDrop() != null) {
                    table.withPool(LootPool.lootPool()
                            .add(LootItem.lootTableItem(ore.getRareDrop()))
                            .when(this.hasSilkTouch().invert())
                            .when(LootItemRandomChanceCondition.randomChance(ore.getRareChance())));
                }
                add(block, table);
            }
        }
    }
}
