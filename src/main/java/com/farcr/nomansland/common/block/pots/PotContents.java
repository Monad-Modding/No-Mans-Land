package com.farcr.nomansland.common.block.pots;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

public final class PotContents {

    private static final String MODIFIERS_KEY = "Modifiers";
    private static final String POTION_TABLE_KEY = "PotionTable";
    private static final String POTION_TABLE_SEED_KEY = "PotionTableSeed";
    private static final String POTION_KEY = "StoredPotion";
    private static final String LOOT_TABLE_KEY = "LootTable";
    private static final String LOOT_TABLE_SEED_KEY = "LootTableSeed";

    private final EnumSet<PotModifier> modifiers = EnumSet.noneOf(PotModifier.class);
    private PotionContents potion = PotionContents.EMPTY;
    @Nullable
    private ResourceLocation potionTable;
    private long potionTableSeed;
    @Nullable
    private ResourceKey<LootTable> lootTable;
    private long lootTableSeed;
    private ItemStack item = ItemStack.EMPTY;

    public Set<PotModifier> getModifiers() {
        return this.modifiers;
    }

    public boolean hasModifier(PotModifier modifier) {
        return this.modifiers.contains(modifier);
    }

    public void setModifiers(Set<PotModifier> modifiers) {
        this.modifiers.clear();
        this.modifiers.addAll(modifiers);
    }

    public PotionContents getPotion(@Nullable Level level) {
        if (level != null) {
            this.unpackPotionTable(level);
        }
        return this.potion;
    }

    public void setPotion(PotionContents potion) {
        this.potion = potion;
        this.potionTable = null;
    }

    @Nullable
    public ResourceLocation getPotionTable() {
        return this.potionTable;
    }

    public long getPotionTableSeed() {
        return this.potionTableSeed;
    }

    public void setPotionTable(@Nullable ResourceLocation table, long seed) {
        this.potionTable = table;
        this.potionTableSeed = seed;
    }

    public boolean unpackPotionTable(Level level) {
        if (this.potionTable == null || level.isClientSide()) return false;
        PotionContents resolved = PotionTable.resolve(level, this.potionTable, this.potionTableSeed);
        if (resolved != null) {
            this.potion = resolved;
        }
        this.potionTable = null;
        return true;
    }

    @Nullable
    public ResourceKey<LootTable> getLootTable() {
        return this.lootTable;
    }

    public long getLootTableSeed() {
        return this.lootTableSeed;
    }

    public void setLootTable(@Nullable ResourceKey<LootTable> lootTable) {
        this.lootTable = lootTable;
    }

    public void setLootTableSeed(long seed) {
        this.lootTableSeed = seed;
    }

    public ItemStack getItem() {
        return this.item;
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }

    public void copyFrom(PotContents other) {
        this.setModifiers(other.modifiers);
        this.potion = other.potion;
        this.potionTable = other.potionTable;
        this.potionTableSeed = other.potionTableSeed;
        this.lootTable = other.lootTable;
        this.lootTableSeed = other.lootTableSeed;
        this.item = other.item.copy();
    }

    public void load(CompoundTag tag, HolderLookup.Provider registries, String itemKey) {
        this.setModifiers(readModifiers(tag));
        this.potion = PotionContents.EMPTY;
        this.potionTable = null;
        this.potionTableSeed = 0L;
        if (tag.contains(POTION_TABLE_KEY, Tag.TAG_STRING)) {
            this.potionTable = ResourceLocation.parse(tag.getString(POTION_TABLE_KEY));
            this.potionTableSeed = tag.getLong(POTION_TABLE_SEED_KEY);
        } else if (tag.contains(POTION_KEY)) {
            PotionContents.CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), tag.get(POTION_KEY)).resultOrPartial().ifPresent(parsed -> this.potion = parsed);
        }
        this.lootTable = null;
        this.lootTableSeed = 0L;
        this.item = ItemStack.EMPTY;
        if (tag.contains(LOOT_TABLE_KEY, Tag.TAG_STRING)) {
            this.lootTable = ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.parse(tag.getString(LOOT_TABLE_KEY)));
            this.lootTableSeed = tag.getLong(LOOT_TABLE_SEED_KEY);
        } else if (tag.contains(itemKey)) {
            this.item = ItemStack.parseOptional(registries, tag.getCompound(itemKey));
        }
    }

    public void save(CompoundTag tag, HolderLookup.Provider registries, String itemKey) {
        writeModifiers(tag, this.modifiers);
        if (this.potionTable != null) {
            tag.putString(POTION_TABLE_KEY, this.potionTable.toString());
            if (this.potionTableSeed != 0L) {
                tag.putLong(POTION_TABLE_SEED_KEY, this.potionTableSeed);
            }
        } else if (!this.potion.equals(PotionContents.EMPTY)) {
            PotionContents.CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), this.potion).result().ifPresent(encoded -> tag.put(POTION_KEY, encoded));
        }
        if (this.lootTable != null) {
            tag.putString(LOOT_TABLE_KEY, this.lootTable.location().toString());
            if (this.lootTableSeed != 0L) {
                tag.putLong(LOOT_TABLE_SEED_KEY, this.lootTableSeed);
            }
        } else if (!this.item.isEmpty()) {
            tag.put(itemKey, this.item.save(registries));
        }
    }

    public static EnumSet<PotModifier> readModifiers(CompoundTag tag) {
        EnumSet<PotModifier> modifiers = EnumSet.noneOf(PotModifier.class);
        if (!tag.contains(MODIFIERS_KEY, Tag.TAG_LIST)) return modifiers;
        ListTag list = tag.getList(MODIFIERS_KEY, Tag.TAG_STRING);
        for (int index = 0; index < list.size(); index++) {
            try {
                modifiers.add(PotModifier.valueOf(list.getString(index).toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return modifiers;
    }

    public static void writeModifiers(CompoundTag tag, Set<PotModifier> modifiers) {
        if (modifiers.isEmpty()) return;
        ListTag list = new ListTag();
        for (PotModifier modifier : modifiers) {
            list.add(StringTag.valueOf(modifier.getSerializedName()));
        }
        tag.put(MODIFIERS_KEY, list);
    }
}
