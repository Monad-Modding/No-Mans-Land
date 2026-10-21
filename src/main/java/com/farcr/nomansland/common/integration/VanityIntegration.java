package com.farcr.nomansland.common.integration;

import com.farcr.nomansland.NoMansLand;
import net.minecraft.world.item.ItemStack;
import tech.thatgravyboat.vanity.common.item.DesignHelper;

public class VanityIntegration {
    public static void applyWarpWornDesign(ItemStack stack) {
        DesignHelper.setDesignAndStyle(stack, NoMansLand.location("warp_worn_mask"), "warp_worn");
    }

    public static boolean hasDesign(ItemStack stack) {
        return DesignHelper.getDesign(stack) != null;
    }
}
