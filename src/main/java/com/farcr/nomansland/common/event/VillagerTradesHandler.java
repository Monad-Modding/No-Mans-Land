package com.farcr.nomansland.common.event;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.registry.NMLMapDecorationTypes;
import com.farcr.nomansland.common.registry.NMLTags;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.village.WandererTradesEvent;

@EventBusSubscriber(modid = NoMansLand.MODID)
public class VillagerTradesHandler {

//    @SubscribeEvent
//    public static void onVillagerTrades(VillagerTradesEvent event) {
//        if (event.getType() == VillagerProfession.CARTOGRAPHER) {
//            event.getTrades().get(5).add(new VillagerTrades.TreasureMapForEmeralds(
//                    20, NMLTags.BELL_SANCTUARY, "nomansland.filled_map.bell_sanctuary",
//                    NMLMapDecorationTypes.BELL_SANCTUARY, 1, 30
//            ));
//            event.getTrades().get(5).add(new VillagerTrades.TreasureMapForEmeralds(
//                    20, NMLTags.ALCHEMIST_RUINS, "nomansland.filled_map.alchemist_ruins",
//                    NMLMapDecorationTypes.ALCHEMIST_RUINS, 1, 30
//            ));
//        }
//    }

    @SubscribeEvent
    public static void onWandererTrades(WandererTradesEvent event) {
        event.getRareTrades().add(new VillagerTrades.TreasureMapForEmeralds(
                20, NMLTags.BELL_SANCTUARY, "nomansland.filled_map.bell_sanctuary",
                NMLMapDecorationTypes.BELL_SANCTUARY, 1, 30
        ));
        event.getRareTrades().add(new VillagerTrades.TreasureMapForEmeralds(
                25, NMLTags.ALCHEMIST_RUINS, "nomansland.filled_map.alchemist_ruins",
                NMLMapDecorationTypes.ALCHEMIST_RUINS, 1, 30
        ));
    }
}
