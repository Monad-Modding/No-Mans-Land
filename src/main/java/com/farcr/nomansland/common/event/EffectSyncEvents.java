package com.farcr.nomansland.common.event;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.registry.entities.NMLEffects;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = NoMansLand.MODID)
public class EffectSyncEvents {
    private static final List<Holder<MobEffect>> TRACKED_EFFECTS = List.of(
        NMLEffects.HAPPINESS, NMLEffects.STASIS
    );

    /*
    * To be clear, this exists because Minecraft doesn't actually sync mob effects
    * with the player. it just handles everything related to them on the server
    * including the spawning of particles. the only time an effect is applied is
    * when it is applied to the player itself or when the player is there to witness
    * the effect being applied. for this reason we must manually reapply the effect
    * on respawn or otherwise if the player wasn't there to witness its application
    */
    @SubscribeEvent
    public static void mobEffectTrackingPacket(PlayerEvent.StartTracking event) {
        if (!(event.getEntity() instanceof ServerPlayer tracker)) return;
        for (Holder<MobEffect> effect : TRACKED_EFFECTS) {
            if (event.getTarget() instanceof LivingEntity livingEntity && livingEntity.hasEffect(effect)) {
                MobEffectInstance effectInstance = livingEntity.getEffect(effect);
                tracker.connection.send(new ClientboundUpdateMobEffectPacket(livingEntity.getId(), effectInstance, false));
            }
        }
    }

    @SubscribeEvent
    public static void mobTrackedEffectAdded(MobEffectEvent.Added event) {
        if (event.getEntity() instanceof LivingEntity livingEntity
            && livingEntity.level() instanceof ServerLevel serverLevel
            && TRACKED_EFFECTS.contains(event.getEffectInstance().getEffect())
        ) {
            serverLevel.getChunkSource().broadcast(livingEntity,
                new ClientboundUpdateMobEffectPacket(livingEntity.getId(), event.getEffectInstance(), true));
        }
    }

    @SubscribeEvent
    public static void mobTrackedEffectRemoved(MobEffectEvent.Remove event) {
        if (event.getEntity() instanceof LivingEntity livingEntity
            && livingEntity.level() instanceof ServerLevel serverLevel
            && TRACKED_EFFECTS.contains(event.getEffect())
        ) {
            serverLevel.getChunkSource().broadcast(livingEntity,
                new ClientboundRemoveMobEffectPacket(livingEntity.getId(), event.getEffect()));
        }
    }
}
