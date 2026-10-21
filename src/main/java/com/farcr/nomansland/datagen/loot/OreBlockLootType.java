package com.farcr.nomansland.datagen.loot;

import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class OreBlockLootType extends BlockLootType {
    private final Supplier<Item> drop;
    private final float min;
    private final float max;
    private final Supplier<Item> rareDrop;
    private final float rareChance;

    public OreBlockLootType(Supplier<Item> drop, float min, float max) {
        this(drop, min, max, null, 0.0F);
    }

    public OreBlockLootType(Supplier<Item> drop, float min, float max, @Nullable Supplier<Item> rareDrop, float rareChance) {
        this.drop = drop;
        this.min = min;
        this.max = max;
        this.rareDrop = rareDrop;
        this.rareChance = rareChance;
    }

    public Item getDrop() {
        return drop.get();
    }

    public float getMin() {
        return min;
    }

    public float getMax() {
        return max;
    }

    @Nullable
    public Item getRareDrop() {
        return rareDrop == null ? null : rareDrop.get();
    }

    public float getRareChance() {
        return rareChance;
    }
}
