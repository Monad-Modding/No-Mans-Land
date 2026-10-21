package com.farcr.nomansland.common.event;

import com.farcr.nomansland.NMLConfig;
import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import com.farcr.nomansland.common.registry.worldgen.NMLFeatures;
import java.util.Iterator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.BlockGrowFeatureEvent;

@EventBusSubscriber(modid = NoMansLand.MODID)
public class BlockGrowthEvents {
    @SubscribeEvent
    public static void onFarmlandTrample(BlockEvent.FarmlandTrampleEvent event) {
        if (!NMLConfig.TRAMPLING.get()) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onBlockGrow(BlockGrowFeatureEvent event) {
        if (event.getFeature() != null) {
            ResourceKey<ConfiguredFeature<?, ?>> feature = event.getFeature().getKey();
            List<ResourceKey<ConfiguredFeature<?, ?>>> regularOakFeatures = List.of(
                    TreeFeatures.OAK,
                    TreeFeatures.OAK_BEES_0002,
                    TreeFeatures.OAK_BEES_002,
                    TreeFeatures.OAK_BEES_005
            );
            List<ResourceKey<ConfiguredFeature<?, ?>>> fancyOakFeatures = List.of(
                    TreeFeatures.FANCY_OAK,
                    TreeFeatures.FANCY_OAK_BEES_0002,
                    TreeFeatures.FANCY_OAK_BEES_002,
                    TreeFeatures.FANCY_OAK_BEES_005
            );
            List<ResourceKey<ConfiguredFeature<?, ?>>> autumnalOakFeatures = List.of(
                    NMLFeatures.AUTUMNAL_OAK,
                    NMLFeatures.LARGE_AUTUMNAL_OAK
            );
            List<ResourceKey<ConfiguredFeature<?, ?>>> spruceFeatures = List.of(
                    TreeFeatures.SPRUCE,
                    TreeFeatures.MEGA_SPRUCE,
                    TreeFeatures.PINE,
                    TreeFeatures.MEGA_PINE
            );
            List<ResourceKey<ConfiguredFeature<?, ?>>> pineFeatures = List.of(
                    NMLFeatures.PINE,
                    NMLFeatures.LARGE_PINE
            );

            BlockPos pos = event.getPos();
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();
            LevelAccessor level = event.getLevel();
            int fruit = 0;
            RandomSource random = event.getRandom();

            boolean apple = regularOakFeatures.contains(feature) || fancyOakFeatures.contains(feature);
            boolean pear = autumnalOakFeatures.contains(feature);

            if (apple || pear) {
                Iterator<BlockPos> it = BlockPos.betweenClosedStream(x - 8, y - 12, z - 8, x + 8, y + 12, z + 8).iterator();
                while (it.hasNext()) {
                    BlockPos bp = it.next();
                    BlockState state = level.getBlockState(bp);
                    if (apple && state.is(NMLBlocks.APPLE_FRUIT.block())) fruit++;
                    if (pear && state.is(NMLBlocks.PEAR_FRUIT.block())) fruit++;
                }
                if (fruit >= 12) {
                    if (regularOakFeatures.contains(feature)) event.setFeature(NMLFeatures.OAK_APPLE_05);
                    if (fancyOakFeatures.contains(feature)) event.setFeature(NMLFeatures.FANCY_OAK_APPLE_05);
                    if (feature == NMLFeatures.AUTUMNAL_OAK) event.setFeature(NMLFeatures.AUTUMNAL_OAK_PEAR_05);
                    if (feature == NMLFeatures.LARGE_AUTUMNAL_OAK)
                        event.setFeature(NMLFeatures.LARGE_AUTUMNAL_OAK_PEAR_05);
                } else if (random.nextBoolean() && fruit >= 6) {
                    if (regularOakFeatures.contains(feature)) event.setFeature(NMLFeatures.OAK_APPLE_05);
                    if (fancyOakFeatures.contains(feature)) event.setFeature(NMLFeatures.FANCY_OAK_APPLE_05);
                    if (feature == NMLFeatures.AUTUMNAL_OAK) event.setFeature(NMLFeatures.AUTUMNAL_OAK_PEAR_05);
                    if (feature == NMLFeatures.LARGE_AUTUMNAL_OAK)
                        event.setFeature(NMLFeatures.LARGE_AUTUMNAL_OAK_PEAR_05);
                } else if (fruit > 0) {
                    if (regularOakFeatures.contains(feature)) event.setFeature(NMLFeatures.OAK_APPLE_01);
                    if (fancyOakFeatures.contains(feature)) event.setFeature(NMLFeatures.FANCY_OAK_APPLE_01);
                }
            }

            if ((spruceFeatures.contains(feature) || pineFeatures.contains(feature)) && level.getLevelData().isRaining() && !level.getBiome(pos).value().warmEnoughToRain(pos)) {
                if (spruceFeatures.contains(feature)) {
                    if (feature == TreeFeatures.SPRUCE) event.setFeature(NMLFeatures.FROSTED_SPRUCE);
                    if (feature == TreeFeatures.MEGA_SPRUCE) event.setFeature(NMLFeatures.MEGA_FROSTED_SPRUCE);
                    if (feature == TreeFeatures.PINE) event.setFeature(NMLFeatures.FROSTED_SPRUCE_ALT);
                    if (feature == TreeFeatures.MEGA_PINE) event.setFeature(NMLFeatures.MEGA_FROSTED_SPRUCE_ALT);
                }
                if (pineFeatures.contains(feature)) event.setFeature(NMLFeatures.FROSTED_PINE);
            }
        }
    }
}
