package com.farcr.nomansland.common.event;

import com.farcr.nomansland.NMLConfig;
import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.block.SulfuricFumaroleBlock;
import com.farcr.nomansland.common.block.VentBlock;
import com.farcr.nomansland.common.entity.ai.WitchBowlStewGoal;
import com.farcr.nomansland.common.entity.frienderman.Frienderman;
import com.farcr.nomansland.common.integration.Mods;
import com.farcr.nomansland.common.registry.*;
import com.farcr.nomansland.common.registry.entities.NMLEntities;
import com.farcr.nomansland.common.world.saved_data.WardedSpacesData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.*;

@EventBusSubscriber(modid = NoMansLand.MODID)
public class MobSpawnEvents {
    @SubscribeEvent
    public static void onFinalizeMobSpawn(FinalizeSpawnEvent event) {
        ServerLevelAccessor level = event.getLevel();

        MobCategory category = event.getEntity().getType().getCategory();
        if ((category == MobCategory.WATER_CREATURE || category == MobCategory.WATER_AMBIENT
                || category == MobCategory.UNDERGROUND_WATER_CREATURE || category == MobCategory.AXOLOTLS)
                && (event.getSpawnType() == MobSpawnType.NATURAL || event.getSpawnType() == MobSpawnType.CHUNK_GENERATION)
                && VentBlock.isAffectedBy(level, event.getEntity().blockPosition(), SulfuricFumaroleBlock.class)) {
            event.setSpawnCancelled(true);
        }

        if (level instanceof ServerLevel serverLevel) {
            if (event.getSpawnType() == MobSpawnType.NATURAL
                    && event.getEntity() instanceof Enemy && !event.getEntity().getType().is(NMLTags.WARD_REPELLED_BLACKLIST)) {
                WardedSpacesData wardedSpacesData = WardedSpacesData.get(serverLevel);
                event.setSpawnCancelled(wardedSpacesData.isWarded(serverLevel, event.getEntity().blockPosition()));
            }

            if (event.getSpawnType() == MobSpawnType.NATURAL
                    && event.getEntity() instanceof EnderMan && !(event.getEntity() instanceof Frienderman)
                    && serverLevel.dimension() == Level.OVERWORLD && serverLevel.random.nextFloat() < 0.01f) {
                event.setSpawnCancelled(true);
                Frienderman frienderman = NMLEntities.FRIENDERMAN.get().create(serverLevel);
                if (frienderman != null) {
                    frienderman.moveTo(event.getEntity().position());
                    frienderman.setYRot(event.getEntity().getYRot());
                    serverLevel.addFreshEntity(frienderman);
                }
            }

            if (Mods.FARMERSDELIGHT.isLoaded() && event.getEntity() instanceof Witch witch && NMLConfig.WITCHES_EAT_STEW.get()) {
                witch.goalSelector.addGoal(2, new WitchBowlStewGoal(witch, 1.0));
            }

        }
    }
}
