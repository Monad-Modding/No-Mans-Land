package com.farcr.nomansland.common.item;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.registry.items.NMLDataComponents;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GasMaskItem extends ArmorItem {
    public static final int MAX_DURATION = 12000;
    public static final int CRAFT_DURATION = 3000;
    private static final ResourceLocation TEXTURE = NoMansLand.location("textures/armor/gas_mask.png");

    public GasMaskItem(Holder<ArmorMaterial> material, Properties properties) {
        super(material, Type.HELMET, properties);
    }

    public static int getDuration(ItemStack stack) {
        return stack.getOrDefault(NMLDataComponents.GAS_MASK_DURATION, 0);
    }

    public static void setDuration(ItemStack stack, int duration) {
        stack.set(NMLDataComponents.GAS_MASK_DURATION, Mth.clamp(duration, 0, MAX_DURATION));
    }

    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        return getDuration(stack) < MAX_DURATION;
    }

    @Override
    public int getBarWidth(@NotNull ItemStack stack) {
        return Math.round(getDuration(stack) * 13.0F / MAX_DURATION);
    }

    @Override
    public int getBarColor(@NotNull ItemStack stack) {
        float f = Mth.clamp((float) getDuration(stack) / MAX_DURATION, 0.0F, 1.0F);
        int r = (int) Mth.lerp(f, 0x4B, 0x55);
        int g = (int) Mth.lerp(f, 0x00, 0xC8);
        int b = (int) Mth.lerp(f, 0x6E, 0xFF);
        return (r << 16) | (g << 8) | b;
    }

    @Override
    public @Nullable ResourceLocation getArmorTexture(@NotNull ItemStack stack, @NotNull Entity entity, @NotNull EquipmentSlot slot, ArmorMaterial.@NotNull Layer layer, boolean innerModel) {
        return TEXTURE;
    }
}
