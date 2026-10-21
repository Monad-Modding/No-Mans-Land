package com.farcr.nomansland.common.event;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.dreams.DreamManager;
import com.farcr.nomansland.common.effect.StasisEffect;
import com.farcr.nomansland.common.item.AncestralOathSwordItem;
import com.farcr.nomansland.common.registry.*;
import com.farcr.nomansland.common.registry.entities.NMLEffects;
import com.farcr.nomansland.common.registry.items.NMLItems;
import java.util.Objects;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = NoMansLand.MODID)
public class StasisEvents {
    @SubscribeEvent
    public static void onAttack(AttackEntityEvent event) {
        if (event.getEntity().getItemInHand(InteractionHand.MAIN_HAND).is(NMLItems.ANCESTRAL_OATH_SWORD)
        && !AncestralOathSwordItem.canHurtUnderOath(event.getTarget())) {
            if (event.getEntity() instanceof ServerPlayer serverPlayer) {
                AncestralOathSwordItem.setUseTime(serverPlayer, AncestralOathSwordItem.MAX_GLINT_ANIMATE, true);
                serverPlayer.connection.send(new ClientboundSetActionBarTextPacket(
                    Component.translatable("item.nomansland.ancestral_oath_sword.refuse")
                        .withColor(0xFFF8D473))
                );
            }
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingBlockEvent(LivingShieldBlockEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (!livingEntity.getUseItem().is(NMLItems.ANCESTRAL_OATH_SWORD)) return;
        event.setBlocked(false);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getTarget() instanceof LivingEntity livingEntity
        && livingEntity.hasEffect(NMLEffects.STASIS)) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void disableParticleEvent(EffectParticleModificationEvent event) {
        if (event.getEffect().is(NMLEffects.STASIS)) event.setVisible(false);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (event.getEntity().hasEffect(NMLEffects.STASIS)
        || DreamManager.getAmbiguousDreamTypeInstance(event.getEntity()) != null)
            event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onIncomingDamageStasis(LivingIncomingDamageEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (livingEntity.getUseItem().is(NMLItems.ANCESTRAL_OATH_SWORD)) {
            ((AncestralOathSwordItem) livingEntity.getUseItem().getItem())
                .handleBlockingEvent(event, livingEntity.getUseItem());
        }
        if (livingEntity.hasEffect(NMLEffects.STASIS)) {
            if (Objects.requireNonNull(livingEntity.getEffect(NMLEffects.STASIS)).getAmplifier() >= 1) {
                event.setCanceled(true);
                return;
            }
            if (NMLEffects.STASIS.get() instanceof StasisEffect effect
            && effect.immuneToDamage(event.getSource(), event.getEntity().level())) {
                event.setCanceled(true);
                return;
            }
            livingEntity.removeEffect(NMLEffects.STASIS);
            if (event.getSource().getWeaponItem() != null && event.getSource().getWeaponItem().is(NMLItems.ANCESTRAL_OATH_SWORD)) {
                livingEntity.addEffect(new MobEffectInstance(NMLEffects.PACIFIED, 300));
                AncestralOathSwordItem.createParticles(livingEntity,
                    NMLParticleTypes.STASIS_BREAK, (AncestralOathSwordItem.PARTICLE_AMOUNT * 2));
                event.setAmount(event.getAmount() * 1.5f);
                return;
            }
            livingEntity.addEffect(
                new MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN, 60, 2,
                    true, false, false
                )
            );
        }
    }
}
