package com.farcr.nomansland.common.mixin;

import com.farcr.nomansland.common.registry.items.NMLItems;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

@Mixin(BrewingStandBlockEntity.class)
public class BrewingStandBlockEntityMixin {

    @Unique
    private static Set<Integer> nml$emptyBandages = new HashSet<>();
    @Unique
    private static Map<Integer, PotionContents> nml$cachedBandage = new HashMap<>();
    @Unique
    private static List<MobEffectInstance> nml$cachedEffect = null;
    @Unique
    @Nullable
    private static Integer nml$cachedColor = null;

    @Unique
    private static boolean nml$isEmptyBandage(ItemStack stack) {
        if (!stack.is(NMLItems.BANDAGE)) return false;
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        return contents == null || !contents.getAllEffects().iterator().hasNext();
    }

    @Unique
    private static boolean nml$skipBandages(ItemStack stack) {
        if (!stack.is(Items.POTION)) return true;
        PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        boolean hasEffects = contents.getAllEffects().iterator().hasNext();
        return !hasEffects;
    }

    @Unique
    private static boolean nml$isUpgradedPotion(ItemStack stack) {
        if (!stack.is(Items.POTION) && !stack.is(Items.SPLASH_POTION) && !stack.is(Items.LINGERING_POTION)) return false;
        PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        return contents.potion().map(holder -> {
            String path = Objects.requireNonNull(holder.getKey()).location().getPath();
            return path.startsWith("strong_") || path.startsWith("long_");
        }).orElse(false);
    }

    @Unique
    private static Optional<Holder<Potion>> nml$getBasePotion(PotionContents contents) {
        return contents.potion().flatMap(holder -> {
            String path = Objects.requireNonNull(holder.getKey()).location().getPath();
            if (path.startsWith("strong_")) {
                return BuiltInRegistries.POTION.getHolder(ResourceLocation.withDefaultNamespace(path.substring("strong_".length())));
            }
            if (path.startsWith("long_")) {
                return BuiltInRegistries.POTION.getHolder(ResourceLocation.withDefaultNamespace(path.substring("long_".length())));
            }
            return Optional.of(holder);
        });
    }

    @Unique
    private static boolean nml$isWaterBottle(ItemStack stack) {
        if (!stack.is(Items.POTION)) return false;
        PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        return contents.potion().map(holder -> holder.is(Potions.WATER)).orElse(false);
    }

    @WrapOperation(method = "serverTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"))
    private static boolean nml$sulfurBrewingFuel(ItemStack stack, Item item, Operation<Boolean> original) {
        if (item == Items.BLAZE_POWDER && stack.is(NMLItems.SULFUR)) return true;
        return original.call(stack, item);
    }

    @Inject(method = "isBrewable", at = @At("HEAD"), cancellable = true)
    private static void nml$preventBrewing(PotionBrewing potionBrewing, NonNullList<ItemStack> items, CallbackInfoReturnable<Boolean> cir) {
        ItemStack ingredient = items.get(3);

        if (ingredient.is(NMLItems.AWKWARD_RESIDUE)) {
            for (int i = 0; i < 3; i++) {
                ItemStack slotItem = items.get(i);
                if (nml$isUpgradedPotion(slotItem) || nml$isWaterBottle(slotItem)) return;
            }
            cir.setReturnValue(false);
            return;
        }

        if (nml$skipBandages(ingredient)) return;

        for (int i = 0; i < 3; i++) {
            if (nml$isEmptyBandage(items.get(i))) return;
        }
        cir.setReturnValue(false);
    }

    @Inject(method = "doBrew", at = @At("HEAD"))
    private static void nml$cacheBandages(Level level, BlockPos pos, NonNullList<ItemStack> items, CallbackInfo ci) {
        nml$emptyBandages.clear();
        nml$cachedBandage.clear();
        nml$cachedEffect = null;
        nml$cachedColor = null;

        ItemStack ingredient = items.get(3);
        if (ingredient.is(NMLItems.AWKWARD_RESIDUE)) return;
        if (nml$skipBandages(ingredient)) return;

        for (int i = 0; i < 3; i++) {
            ItemStack slotItem = items.get(i);
            if (nml$isEmptyBandage(slotItem)) {
                nml$emptyBandages.add(i);
            } else if (slotItem.is(NMLItems.BANDAGE)) {
                PotionContents contents = slotItem.get(DataComponents.POTION_CONTENTS);
                if (contents != null) {
                    nml$cachedBandage.put(i, contents);
                }
            }
        }

        if (nml$emptyBandages.isEmpty()) return;

        PotionContents potionContents = ingredient.get(DataComponents.POTION_CONTENTS);
        if (potionContents == null) return;

        ArrayList<MobEffectInstance> effects = new ArrayList<>();
        Optional<Holder<Potion>> basePotion = nml$getBasePotion(potionContents);
        if (basePotion.isPresent()) {
            Potion base = basePotion.get().value();
            for (MobEffectInstance effect : base.getEffects()) {
                effects.add(new MobEffectInstance(
                        effect.getEffect(),
                        effect.getDuration() / 3,
                        0,
                        effect.isAmbient(),
                        effect.isVisible(),
                        effect.showIcon()
                ));
            }
        } else {
            for (MobEffectInstance effect : potionContents.getAllEffects()) {
                effects.add(new MobEffectInstance(
                        effect.getEffect(),
                        effect.getDuration() / 3,
                        0,
                        effect.isAmbient(),
                        effect.isVisible(),
                        effect.showIcon()
                ));
            }
        }

        nml$cachedEffect = effects;
        nml$cachedColor = potionContents.customColor().orElse(null);
    }

    @Inject(method = "doBrew", at = @At("TAIL"))
    private static void nml$infuseBandages(Level level, BlockPos pos, NonNullList<ItemStack> items, CallbackInfo ci) {
        for (Map.Entry<Integer, PotionContents> entry : nml$cachedBandage.entrySet()) {
            ItemStack stack = items.get(entry.getKey());
            if (stack.is(NMLItems.BANDAGE)) {
                stack.set(DataComponents.POTION_CONTENTS, entry.getValue());
            }
        }

        if (nml$cachedEffect == null || nml$cachedEffect.isEmpty()) {
            nml$emptyBandages.clear();
            nml$cachedBandage.clear();
            return;
        }

        for (int slot : nml$emptyBandages) {
            ItemStack stack = items.get(slot);
            if (stack.is(NMLItems.BANDAGE)) {
                stack.set(DataComponents.POTION_CONTENTS, new PotionContents(
                        Optional.empty(),
                        Optional.ofNullable(nml$cachedColor),
                        new ArrayList<>(nml$cachedEffect)
                ));
            }
        }

        nml$emptyBandages.clear();
        nml$cachedBandage.clear();
        nml$cachedEffect = null;
        nml$cachedColor = null;
    }
}
