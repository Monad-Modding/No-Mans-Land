package com.farcr.nomansland.client.extensions;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.NMLArmorModels;
import com.farcr.nomansland.common.registry.NMLFluids;
import com.farcr.nomansland.common.registry.items.NMLItems;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

public class NMLClientExtensions {

    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerFluidType(new IClientFluidTypeExtensions() {
            private static final ResourceLocation STILL_RESIN_OIL = NoMansLand.location("block/fluid/resin_oil");
            private static final ResourceLocation FLOWING_RESIN_OIL = NoMansLand.location("block/fluid/flowing_resin_oil");

            public ResourceLocation getStillTexture() {
                return STILL_RESIN_OIL;
            }

            public ResourceLocation getFlowingTexture() {
                return FLOWING_RESIN_OIL;
            }
        }, NMLFluids.RESIN_OIL_TYPE.get());

        event.registerItem(new LodestoneArmorClientItemExtensions(()->NMLArmorModels.ANCIENT_BRONZE_MASK), NMLItems.ANCIENT_BRONZE_MASK.get());
        event.registerItem(new LodestoneArmorClientItemExtensions(()->NMLArmorModels.TORTOISE_SHELL), NMLItems.TORTOISE_SHELL.get());
        event.registerItem(new AncestralOathSwordClientExtensions(), NMLItems.ANCESTRAL_OATH_SWORD.get());
        event.registerItem(new LodestoneArmorClientItemExtensions(()->NMLArmorModels.GAS_MASK), NMLItems.GAS_MASK.get());
        event.registerItem(new PotClientItemExtensions(), NMLItems.ANCIENT_POT.get());
        event.registerItem(new PotClientItemExtensions(), NMLItems.LARGE_ANCIENT_POT.get());
        event.registerItem(new BandageClientItemExtensions(), NMLItems.BANDAGE.get());
        event.registerItem(new BandageClientItemExtensions(), NMLItems.MEDICINAL_BANDAGE.get());
        event.registerItem(new BandageClientItemExtensions(), NMLItems.ANTIDOTE_BANDAGE.get());
        event.registerItem(new BandageClientItemExtensions(), NMLItems.WARDING_BANDAGE.get());
    }
}
