package com.farcr.nomansland.common.item;

import com.farcr.nomansland.common.networking.alchemist_tools.ClientboundOathSwordParry;
import com.farcr.nomansland.common.registry.NMLParticleTypes;
import com.farcr.nomansland.common.registry.NMLSounds;
import com.farcr.nomansland.common.registry.NMLTags;
import com.farcr.nomansland.common.registry.entities.NMLEffects;
import com.farcr.nomansland.common.registry.items.NMLDataComponents;
import com.farcr.nomansland.common.registry.items.NMLItems;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.function.Supplier;

public class AncestralOathSwordItem extends SwordItem {
    public AncestralOathSwordItem(Tier tier, Properties properties) {
        super(tier, properties);
    }
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.BLOCK;
    }

    public static final float SWORD_PARRY_TICKS = 10f;
    private static final int STASIS_RANGE = 3;

    public static final int MAX_ANIMATE_TIME = 7;
    public static final int MAX_GLINT_ANIMATE = 45;
    public static final int MAX_PARRY_ANIMATE_TIME = 10;

    public static final int PARTICLE_AMOUNT = 5;

    public int getUseDuration(@NotNull ItemStack stack, @NotNull LivingEntity entity) {
        return 72000;
    }

    public static boolean canHurtUnderOath(Entity entity) {
        return (entity instanceof LivingEntity livingEntity
            && livingEntity.hasEffect(NMLEffects.STASIS))
            || entity.getType().is(NMLTags.MALEVOLENT_ENTITIES);
    }

    public static void setUseTime(LivingEntity livingEntity, int useTime, boolean mainHandOnly) {
        if (livingEntity.level().isClientSide) return;
        for (ItemStack itemStack : livingEntity.getHandSlots()) {
            if (!itemStack.is(NMLItems.ANCESTRAL_OATH_SWORD)) continue;
            // this seems like it could possibly cause problems in the future :p
            if (mainHandOnly && !itemStack.equals(livingEntity.getMainHandItem())) continue;
            else if (!mainHandOnly && !livingEntity.getUseItem().equals(itemStack)) continue;

            itemStack.set(NMLDataComponents.OATH_SWORD_USE_TIME, useTime);
        }
    }

    public static void updateUseTime(LivingEntity livingEntity) {
        if (livingEntity.level().isClientSide) return;
        for (ItemStack itemStack : livingEntity.getHandSlots()) {
            if (!itemStack.is(NMLItems.ANCESTRAL_OATH_SWORD)) continue;
            int useTime = itemStack.getOrDefault(NMLDataComponents.OATH_SWORD_USE_TIME, 0);
            if (useTime <= 0) continue;
            itemStack.set(NMLDataComponents.OATH_SWORD_USE_TIME, useTime - 1);
        }
    }

    @Override
    public boolean hurtEnemy(@NotNull ItemStack stack, @NotNull LivingEntity target, @NotNull LivingEntity attacker) {
        return canHurtUnderOath(target);
    }

    @Override public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack currentStack, boolean slotChanged) {
        return !oldStack.is(currentStack.getItem());
    }

    public float getParryTiming(ItemStack itemStack, LivingEntity livingEntity) {
        return itemStack.getUseDuration(livingEntity) - livingEntity.getUseItemRemainingTicks();
    }

    public boolean withinParryTiming(ItemStack itemStack, LivingEntity livingEntity) {
        return (getParryTiming(itemStack, livingEntity) <= SWORD_PARRY_TICKS);
    }

    // why is this not standardized anywhere in the base game's codebase I've had to manually do this like
    // 30 times now and im just getting tired of writing it manually so I'm just going to make this
    public static HumanoidArm getHumanoidArm(LivingEntity player, InteractionHand interactionHand) {
        HumanoidArm dominantHand = player.getMainArm();
        if (interactionHand == InteractionHand.OFF_HAND)
            return dominantHand.getOpposite();
        return dominantHand;
    }

    public void handleBlockingEvent(LivingIncomingDamageEvent event, ItemStack itemStack) {
        LivingEntity damagedEntity = event.getEntity();
        Entity directEntity = event.getSource().getDirectEntity();

        if (event.getSource().is(DamageTypeTags.BYPASSES_SHIELD)) return;

        Level level = damagedEntity.level();
        if (withinParryTiming(itemStack, damagedEntity)) {
            level.playSound(
                null, damagedEntity.blockPosition(),
                NMLSounds.OATH_PARRY.get(), SoundSource.PLAYERS
            );
            AncestralOathSwordItem.setUseTime(damagedEntity, MAX_GLINT_ANIMATE, false);
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(damagedEntity, new ClientboundOathSwordParry(
                damagedEntity.getId(), getHumanoidArm(damagedEntity, damagedEntity.getUsedItemHand()).getId()
            ));

            // deflect arrow
            if (directEntity instanceof Projectile projectile) {
                Vec3 newVelocity = damagedEntity.getForward().normalize()
                    .scale(projectile.getDeltaMovement().length());
                projectile.setDeltaMovement(newVelocity);
                projectile.setPos(projectile.position().add(projectile.getDeltaMovement()));
                createParticles(projectile, NMLParticleTypes.STASIS_HIT_PARRY, (PARTICLE_AMOUNT * 2));
            }
            for (LivingEntity livingEntity : level.getNearbyEntities(
                LivingEntity.class, TargetingConditions.forNonCombat().range(STASIS_RANGE),
                damagedEntity, damagedEntity.getBoundingBox().inflate(STASIS_RANGE)
            )) {
                this.applyStasisTicks(livingEntity, damagedEntity, 100 + (int) (20 * event.getOriginalAmount()));
                createParticles(livingEntity, NMLParticleTypes.STASIS_HIT_PARRY, (PARTICLE_AMOUNT * 2));
            }
            event.setCanceled(true);
            return;
        }

        // if damage is from an entity
        if (directEntity instanceof LivingEntity livingEntity) {
            // "parry miss" effect
            level.playSound(
                null, damagedEntity.blockPosition(),
                NMLSounds.OATH_BLOCK.get(), SoundSource.PLAYERS
            );
            applyStasisTicks(livingEntity, damagedEntity, 40 + (int) (20 * event.getOriginalAmount()));
            createParticles(livingEntity, NMLParticleTypes.STASIS_HIT, PARTICLE_AMOUNT);
            event.setAmount(event.getAmount() * .25f);
        }
    }

    public static void createParticles(Entity entity, Supplier<SimpleParticleType> supplier, int amount) {
        if (entity.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(supplier.get(),
                entity.getX(), entity.getY(0.5),
                entity.getZ(), amount, 0.1,
                0.0, 0.1, 0.2
            );
        }
    }

    private void applyStasisTicks(LivingEntity livingEntity, LivingEntity damagedEntity, int ticks) {
        livingEntity.knockback(0.25, damagedEntity.getX() - livingEntity.getX(), damagedEntity.getZ() - livingEntity.getZ());
        livingEntity.addEffect(new MobEffectInstance(
            NMLEffects.STASIS, ticks
        ));
    }

    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(itemstack);
    }

    public boolean canPerformAction(@NotNull ItemStack stack, @NotNull ItemAbility itemAbility) {
        return ItemAbilities.DEFAULT_SHIELD_ACTIONS.contains(itemAbility);
    }
}
