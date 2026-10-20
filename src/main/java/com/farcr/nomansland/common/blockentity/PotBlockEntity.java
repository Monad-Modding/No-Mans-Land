package com.farcr.nomansland.common.blockentity;

import com.farcr.nomansland.common.block.pots.*;
import com.farcr.nomansland.common.entity.living_pot.LivingPot;
import com.farcr.nomansland.common.registry.NMLBlockEntities;
import com.farcr.nomansland.common.registry.NMLRegistries;
import com.farcr.nomansland.common.registry.items.NMLDataComponents;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.*;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.ticks.ContainerSingleItem;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public class PotBlockEntity extends BlockEntity implements RandomizableContainer, ContainerSingleItem.BlockContainerSingleItem {

    private static final String ITEM_KEY = "Item";

    public PotVariant variant;
    private final PotContents contents = new PotContents();
    public boolean skipBreakEffects;
    public boolean shouldDropItems;
    public boolean preventRegen;
    public long wobbleStartedAtTick;
    public @Nullable DecoratedPotBlockEntity.WobbleStyle lastWobbleStyle;

    public PotBlockEntity(BlockPos pos, BlockState state) {
        super(NMLBlockEntities.POT.get(), pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (variant == null && level != null && !level.isClientSide && getBlockState().getBlock() instanceof PotBlock potBlock) {
            ensureVariant(potBlock.getSize());
        }
    }

    public void ensureVariant(PotSize size) {
        if (variant != null || level == null || level.isClientSide) return;
        Registry<PotVariant> registry = level.registryAccess().registryOrThrow(NMLRegistries.POT_VARIANT_KEY);
        List<Holder.Reference<PotVariant>> matching = registry.holders().filter(h -> h.value().size() == size).toList();
        variant = matching.get(level.getRandom().nextInt(matching.size())).value();
        level.blockEntityChanged(worldPosition);
    }

    public PotContents getContents() {
        return this.contents;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.variant != null && this.level != null) {
            ResourceLocation key = this.level.registryAccess().registryOrThrow(NMLRegistries.POT_VARIANT_KEY).getKey(this.variant);
            if (key != null) {
                tag.putString("Variant", key.toString());
            }
        }
        this.contents.save(tag, registries, ITEM_KEY);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Variant")) {
            registries.lookupOrThrow(NMLRegistries.POT_VARIANT_KEY).get(ResourceKey.create(NMLRegistries.POT_VARIANT_KEY, ResourceLocation.parse(tag.getString("Variant")))).ifPresent(holder -> this.variant = holder.value());
        }
        this.contents.load(tag, registries, ITEM_KEY);
    }

    public void restoreFromFallingBlock(CompoundTag tag, HolderLookup.Provider registries) {
        loadAdditional(tag, registries);
    }

    public ItemStack getPotAsItem() {
        ItemStack itemstack = getBlockState().getBlock().asItem().getDefaultInstance();
        itemstack.applyComponents(this.collectComponents());
        return itemstack;
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        if (level != null && variant != null) {
            Optional.ofNullable(level.registryAccess().registryOrThrow(NMLRegistries.POT_VARIANT_KEY).getKey(variant)).ifPresent(key -> {
                components.set(NMLDataComponents.POT_VARIANT, key);
            });
        }

        if (!this.contents.getModifiers().isEmpty()) {
            components.set(NMLDataComponents.POT_MODIFIERS, this.contents.getModifiers().stream().map(PotModifier::getSerializedName).toList());
        }

        if (this.contents.getPotionTable() != null) {
            components.set(NMLDataComponents.POT_POTION_TABLE, new SeededPotionTable(this.contents.getPotionTable(), this.contents.getPotionTableSeed()));
        } else if (!this.contents.getPotion(null).equals(PotionContents.EMPTY)) {
            components.set(DataComponents.POTION_CONTENTS, this.contents.getPotion(null));
        }
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput componentInput) {
        super.applyImplicitComponents(componentInput);

        Optional.ofNullable(componentInput.get(NMLDataComponents.POT_VARIANT)).ifPresent(key -> {
            level.registryAccess().registryOrThrow(NMLRegistries.POT_VARIANT_KEY)
                    .getOptional(ResourceKey.create(NMLRegistries.POT_VARIANT_KEY, key))
                    .ifPresent(v -> variant = v);
        });

        EnumSet<PotModifier> modifiers = EnumSet.noneOf(PotModifier.class);
        for (String name : componentInput.getOrDefault(NMLDataComponents.POT_MODIFIERS, List.<String>of())) {
            try {
                modifiers.add(PotModifier.valueOf(name.toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException ignored) {
            }
        }
        this.contents.setModifiers(modifiers);

        SeededPotionTable potionTableComponent = componentInput.get(NMLDataComponents.POT_POTION_TABLE);
        if (potionTableComponent != null) {
            this.contents.setPotionTable(potionTableComponent.potionTable(), potionTableComponent.seed());
        } else {
            PotionContents potion = componentInput.get(DataComponents.POTION_CONTENTS);
            if (potion != null) {
                this.contents.setPotion(potion);
            }
        }
    }

    @Override
    public void removeComponentsFromTag(CompoundTag tag) {
        super.removeComponentsFromTag(tag);
        tag.remove("Variant");
        tag.remove("Item");
        tag.remove("LootTable");
        tag.remove("LootTableSeed");
        tag.remove("PotionTable");
        tag.remove("PotionTableSeed");
        tag.remove("Modifiers");
        tag.remove("StoredPotion");
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveCustomOnly(registries);
    }

    public Direction getDirection() {
        return this.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
    }

    public void wobble(DecoratedPotBlockEntity.WobbleStyle style) {
        if (this.level != null && !this.level.isClientSide()) {
            this.level.blockEvent(this.getBlockPos(), this.getBlockState().getBlock(), 1, style.ordinal());
        }
    }

    public boolean triggerEvent(int id, int type) {
        if (this.level != null && id == 1 && type >= 0 && type < DecoratedPotBlockEntity.WobbleStyle.values().length) {
            this.wobbleStartedAtTick = this.level.getGameTime();
            this.lastWobbleStyle = DecoratedPotBlockEntity.WobbleStyle.values()[type];
            return true;
        } else {
            return super.triggerEvent(id, type);
        }
    }


    public ItemStack getTheItem() {
        this.unpackLootTable(null);
        return this.contents.getItem();
    }

    public ItemStack splitTheItem(int amount) {
        this.unpackLootTable(null);
        ItemStack split = this.contents.getItem().split(amount);
        if (this.contents.getItem().isEmpty()) {
            this.contents.setItem(ItemStack.EMPTY);
        }
        return split;
    }

    public void setTheItem(ItemStack item) {
        this.unpackLootTable(null);
        this.contents.setItem(item);
    }

    public void setFromItem(ItemStack item) {
        this.applyComponentsFromItemStack(item);
    }

    public BlockEntity getContainerBlockEntity() {
        return this;
    }

    public boolean insert(ItemStack stack) {
        this.unpackLootTable(null);
        ItemStack item = this.contents.getItem();
        if (item.isEmpty() || item.getMaxStackSize() > item.getCount() + stack.getCount() && ItemStack.isSameItemSameComponents(item, stack)) {
            this.setTheItem(stack.copyWithCount(item.getCount() + stack.getCount()));
            return true;
        }
        return false;
    }

    public boolean isLiving() {
        return this.contents.hasModifier(PotModifier.ALIVE);
    }

    public Set<PotModifier> getModifiers() {
        return this.contents.getModifiers();
    }

    public boolean hasModifier(PotModifier modifier) {
        return this.contents.hasModifier(modifier);
    }

    public void addModifier(PotModifier modifier) {
        this.contents.getModifiers().add(modifier);
        this.setChanged();
    }

    public void removeModifier(PotModifier modifier) {
        this.contents.getModifiers().remove(modifier);
        this.setChanged();
    }

    public PotionContents getStoredPotion() {
        if (this.level != null && this.contents.unpackPotionTable(this.level)) {
            this.setChanged();
        }
        return this.contents.getPotion(null);
    }

    public void setStoredPotion(PotionContents potion) {
        this.contents.setPotion(potion);
        this.setChanged();
    }

    public void setPotionTable(ResourceLocation potionTableId, long seed) {
        this.contents.setPotionTable(potionTableId, seed);
    }

    @Nullable
    public ResourceLocation getPotionTableId() {
        return this.contents.getPotionTable();
    }

    public long getPotionTableSeed() {
        return this.contents.getPotionTableSeed();
    }

    public void wakeUp(@Nullable LivingEntity disturber) {
        LivingPot pot = this.spawnLivingPot();
        if (pot == null) return;
        if (disturber instanceof Player player && player.canBeSeenAsEnemy()) {
            pot.angerAt(player);
        }
        pot.startWakeUp();
        BlockPos pos = this.getBlockPos();
        this.level.playSound(null, pos, SoundEvents.DECORATED_POT_STEP, SoundSource.HOSTILE, 1.0F, 0.8F);
        if (this.level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.DUST_PLUME, pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5, 8, 0.3, 0.05, 0.3, 0.02);
        }
    }

    public void wakeUpSilent() {
        this.spawnLivingPot();
    }

    @Nullable
    private LivingPot spawnLivingPot() {
        if (this.level == null || this.level.isClientSide) return null;
        LivingPot pot = LivingPot.fromPot(this);
        this.level.addFreshEntity(pot);
        this.skipBreakEffects = true;
        this.level.removeBlock(this.getBlockPos(), false);
        return pot;
    }

    public float getFullness() {
        this.unpackLootTable(null);
        ItemStack item = this.contents.getItem();
        return (float) item.getCount() / item.getMaxStackSize();
    }

    @Nullable
    @Override
    public ResourceKey<LootTable> getLootTable() {
        return this.contents.getLootTable();
    }

    @Override
    public void setLootTable(@Nullable ResourceKey<LootTable> lootTable) {
        this.contents.setLootTable(lootTable);
    }

    @Override
    public long getLootTableSeed() {
        return this.contents.getLootTableSeed();
    }

    @Override
    public void setLootTableSeed(long seed) {
        this.contents.setLootTableSeed(seed);
    }
}
