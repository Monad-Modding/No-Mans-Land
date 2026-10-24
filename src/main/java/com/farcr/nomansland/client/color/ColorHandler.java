package com.farcr.nomansland.client.color;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import com.farcr.nomansland.common.registry.items.NMLItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

@SuppressWarnings("unused")
@EventBusSubscriber(modid = NoMansLand.MODID, value = Dist.CLIENT)
public class ColorHandler {
    @SubscribeEvent
    public static void registerItemColorHandlers(RegisterColorHandlersEvent.Item event) {
        event.register((state, tintIndex) -> GrassColor.get(0.5D, 1),
                NMLBlocks.GRASS_SPROUTS.get(),
                NMLBlocks.FIDDLEHEAD.get(),
                NMLBlocks.GROUND_IVY.get());

        event.register((state, tintIndex) -> tintIndex == 0 ? GrassColor.get(0.5D, 1) : 0xFFFFFF,
                NMLBlocks.OAT_GRASS.get());

        event.register((stack, tintIndex) -> {
            if (tintIndex == 1) {
                PotionContents potionContents = stack.get(DataComponents.POTION_CONTENTS);
                if (potionContents != null && potionContents.getAllEffects().iterator().hasNext()) {
                    return potionContents.getColor();
                }
                return -1;
            }
            return -1;
        }, NMLItems.BANDAGE.get());

//        event.register((state, tintIndex) -> {
//            int grassColorPacked = GrassColor.get(0.5D, 1);
//            return packColor(unpackRed(grassColorPacked) / 0.556862745F,
//                    unpackGreen(grassColorPacked) / 0.745098039F,
//                    unpackBlue(grassColorPacked) / 0.705882353F);
//        }, NMLBlocks.OAT_GRASS.get());

        event.register((stack, index) -> FoliageColor.get(0.5D, 1),
                NMLBlocks.MAPLE_LEAVES.get(),
                NMLBlocks.WALNUT_LEAVES.get(),
                NMLBlocks.APPLE_FRUIT_LEAVES.get(),
                NMLBlocks.WILLOW_LEAVES.get()
        );
    }

    @SubscribeEvent
    public static void registerBlockColorHandlers(RegisterColorHandlersEvent.Block event) {
        event.register((state, level, pos, tintIndex) -> Minecraft.getInstance().getBlockColors().getColor(Blocks.SHORT_GRASS.defaultBlockState(), level, pos, tintIndex),
                NMLBlocks.GRASS_SPROUTS.get(),
                NMLBlocks.FIDDLEHEAD.get(),
                NMLBlocks.CUT_SUGAR_CANE.get(),
                NMLBlocks.FROSTED_GRASS.get(),
                NMLBlocks.OAT_GRASS.get(),
                NMLBlocks.GROUND_IVY.get()
        );
//        event.register((state, level, pos, tintIndex) -> {
//                    int grassColorPacked = GrassColor.get(0.5D, 1);
//                    if (level != null && pos != null) grassColorPacked = BiomeColors.getAverageGrassColor(level, pos);
//                    return packColor(unpackRed(grassColorPacked) / 0.556862745F,
//                            unpackGreen(grassColorPacked) / 0.745098039F,
//                            unpackBlue(grassColorPacked) / 0.705882353F);
//                },
//                NMLBlocks.OAT_GRASS.get()
//        );
        event.register((state, level, pos, tintIndex) -> level != null && pos != null ? BiomeColors.getAverageFoliageColor(level, pos) : GrassColor.get(0.5D, 1),
                NMLBlocks.MAPLE_LEAVES.get(),
                NMLBlocks.WALNUT_LEAVES.get(),
                NMLBlocks.APPLE_FRUIT_LEAVES.get(),
                NMLBlocks.DUCKWEED.get(),
                NMLBlocks.WILLOW_LEAVES.get(),
                NMLBlocks.CUT_VINE.get()
        );
    }

    private static float unpackRed(int rgb) {
        return ((rgb >> 16) & 0xFF) / 255F;
    }

    private static float unpackGreen(int rgb) {
        return ((rgb >> 8) & 0xFF) / 255F;
    }

    private static float unpackBlue(int rgb) {
        return ((rgb) & 0xFF) / 255F;
    }

    private static int packColor(float r, float g, float b) {
        return (((int) (Mth.clamp(r, 0, 1) * 255F) & 0xFF) << 16) |
                (((int) (Mth.clamp(g, 0, 1) * 255F) & 0xFF) << 8) |
                (((int) (Mth.clamp(b, 0, 1) * 255F) & 0xFF));
    }

}
