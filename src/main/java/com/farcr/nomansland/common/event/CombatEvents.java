package com.farcr.nomansland.common.event;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.effect.FlammableEffect;
import com.farcr.nomansland.common.registry.*;
import com.farcr.nomansland.common.registry.entities.NMLEffects;
import com.farcr.nomansland.common.registry.items.NMLArmorMaterials;
import com.farcr.nomansland.common.registry.items.NMLDataComponents;
import com.farcr.nomansland.common.registry.items.NMLItems;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.*;

@EventBusSubscriber(modid = NoMansLand.MODID)
public class CombatEvents {
    @SubscribeEvent
    public static void onHurt(LivingIncomingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        DamageSource source = event.getSource();
        float damage = event.getAmount();

        ItemStack chestplate = entity.getItemBySlot(EquipmentSlot.CHEST);
        if (chestplate.getItem() instanceof ArmorItem armorItem && armorItem.getMaterial().is(NMLArmorMaterials.TORTOISE)) {
            Vec3 vec32 = source.getSourcePosition();
            if (vec32 != null) {
                Vec3 vec3 = entity.calculateViewVector(0.0F, entity.getYHeadRot());
                Vec3 vec31 = vec32.vectorTo(entity.position());
                vec31 = new Vec3(vec31.x, 0.0, vec31.z).normalize();
                if (!source.is(DamageTypeTags.BYPASSES_SHIELD) && vec31.dot(vec3) > 0.0) {
                    if (chestplate.get(NMLDataComponents.TIME_WHEN_DISABLED) == null)
                        return;
                    if (entity instanceof Player playerReal)
                        playerReal.awardStat(Stats.ITEM_USED.get(chestplate.getItem()));
                    if (damage >= 3.0F) {
                        int damageToItem = 1 + Mth.floor(damage);
                        InteractionHand interactionhand = entity.getUsedItemHand();
                        if (isTortoiseShellDisabled(chestplate, entity))
                            chestplate.hurtAndBreak(damageToItem, entity, EquipmentSlot.CHEST);
                        if (chestplate.isEmpty()) {
                            entity.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
                            entity.level().playSound(null, entity.blockPosition(), SoundEvents.SHIELD_BREAK, SoundSource.NEUTRAL, 1.0F, 0.2F);
                        }
                    }
                    event.setCanceled(isTortoiseShellDisabled(chestplate, entity));
                    disableTortoiseShell(source, entity, chestplate);
                    if (event.isCanceled()) {
                        entity.level().playSound(null, entity.blockPosition(), SoundEvents.SHIELD_BLOCK, SoundSource.NEUTRAL, 1.0F, 0.2F);
                    }
                }
            }
        }

        ItemStack helmet = entity.getItemBySlot(EquipmentSlot.HEAD);
        if (!(entity instanceof Player || entity instanceof ArmorStand) && helmet.is(NMLItems.ANCIENT_BRONZE_MASK) && source.getEntity() instanceof Player) {
            int punchCount = helmet.getOrDefault(NMLDataComponents.PUNCH_COUNT, 0);
            if (punchCount >= 4) {
                entity.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
                entity.spawnAtLocation(helmet.copy());
            } else {
                helmet.set(NMLDataComponents.PUNCH_COUNT, punchCount + 1);
                helmet.set(NMLDataComponents.PUNCH_COOLDOWN, 100);
            }
        }
    }

    @SubscribeEvent
    public static void onFlammableIgnite(LivingDamageEvent.Post event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide())
            return;
        DamageSource source = event.getSource();
        if (entity.hasEffect(NMLEffects.FLAMMABLE) && (source.is(NMLTags.IGNITES_FLAMMABLE) || (source.getWeaponItem() != null && source.getWeaponItem().is(NMLTags.FIRESTARTERS)))) {
            if (source.getEntity() instanceof ServerPlayer serverPlayer)
                NMLCriteriaTriggers.IGNITE_FLAMMABLE_ENTITY.get().trigger(serverPlayer, entity, source);
            FlammableEffect.igniteFlammable(entity);
        }
    }

    @SubscribeEvent
    public static void onKnockback(LivingKnockBackEvent event) {
        LivingEntity entity = event.getEntity();
        ItemStack stack = entity.getItemBySlot(EquipmentSlot.CHEST);
        if (stack.getItem() instanceof ArmorItem armorItem && armorItem.getMaterial().is(NMLArmorMaterials.TORTOISE)) {
            if (entity.isCrouching()) {
                event.setStrength(0.0F);
            }
        }
    }

    public static void spawnItemParticles(int amount, RandomSource randomSource, Level level, ItemStack itemstack, LivingEntity entity) {
        for (int i = 0; i < amount; i++) {
            Vec3 vec3 = new Vec3(((double) randomSource.nextFloat() - 0.5) * 0.1, Math.random() * 0.1 + 0.1, 0.0);
            vec3 = vec3.xRot(-entity.getXRot() * (float) (Math.PI / 180.0));
            vec3 = vec3.yRot(-entity.getYRot() * (float) (Math.PI / 180.0));
            double d0 = (double) (-randomSource.nextFloat()) * 0.6 - 0.3;
            Vec3 vec31 = new Vec3(((double) randomSource.nextFloat() - 0.5) * 0.3, d0, 0.6);
            vec31 = vec31.xRot(-entity.getXRot() * (float) (Math.PI / 180.0));
            vec31 = vec31.yRot(-entity.getYRot() * (float) (Math.PI / 180.0));
            vec31 = vec31.add(entity.getX(), entity.getEyeY(), entity.getZ());
            if (!itemstack.isEmpty())
                ((ServerLevel) level).sendParticles(new ItemParticleOption(ParticleTypes.ITEM, itemstack), vec31.x, vec31.y, vec31.z, amount, vec3.x, vec3.y + 0.05, vec3.z, 0.5);
        }
    }

    public static void disableTortoiseShell(DamageSource source, LivingEntity entity, ItemStack stack) {
        if (stack.isEmpty() || !stack.isEmpty() && stack.get(NMLDataComponents.TIME_WHEN_DISABLED) != null && (entity.level().getGameTime() - stack.get(NMLDataComponents.TIME_WHEN_DISABLED)) < 100L)
            return;
        if (source.getEntity() instanceof LivingEntity living) {
            if (living.getItemBySlot(EquipmentSlot.MAINHAND).canDisableShield(living.getItemBySlot(EquipmentSlot.MAINHAND), entity, living)) {
                if (entity instanceof Player player)
                    player.getCooldowns().addCooldown(stack.getItem(), 100);
                stack.set(NMLDataComponents.TIME_WHEN_DISABLED.get(), entity.level().getGameTime());
                spawnItemParticles(5, living.getRandom(), living.level(), stack, living);
                entity.level().playSound(null, entity.blockPosition(), SoundEvents.SHIELD_BREAK, SoundSource.NEUTRAL, 1.0F, 0.2F);
            }
        }
    }

    public static boolean isTortoiseShellDisabled(ItemStack stack, Entity entity) {
        if (stack.get(NMLDataComponents.TIME_WHEN_DISABLED) == null)
            return false;
        return (entity.level().getGameTime() - stack.get(NMLDataComponents.TIME_WHEN_DISABLED)) > 100L;
    }
}
