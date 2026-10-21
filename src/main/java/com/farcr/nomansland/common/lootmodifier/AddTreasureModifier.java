package com.farcr.nomansland.common.lootmodifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

import javax.annotation.Nonnull;

public class AddTreasureModifier extends LootModifier {

    public static MapCodec<AddTreasureModifier> CODEC = RecordCodecBuilder.mapCodec(
            instance -> codecStart(instance).and(instance.group(
                    LootPoolEntries.CODEC.fieldOf("entry").forGetter((m) -> m.entry),
                    Codec.FLOAT.optionalFieldOf("chance", 1.0F).forGetter((m) -> m.chance)
            )).apply(instance, AddTreasureModifier::new));

    private final LootPoolEntryContainer entry;
    private final float chance;

    protected AddTreasureModifier(LootItemCondition[] conditionsIn, LootPoolEntryContainer entry, float chance) {
        super(conditionsIn);
        this.entry = entry;
        this.chance = chance;
    }

    @Nonnull
    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (context.getRandom().nextFloat() >= this.chance) {
            return generatedLoot;
        }

        ObjectArrayList<ItemStack> treasure = new ObjectArrayList<>();
        this.entry.expand(context, choice -> choice.createItemStack(
                LootTable.createStackSplitter(context.getLevel(), treasure::add), context));

        if (!treasure.isEmpty()) {
            generatedLoot.clear();
            generatedLoot.addAll(treasure);
        }
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
