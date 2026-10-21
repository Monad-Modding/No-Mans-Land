package com.farcr.nomansland.common.entity;

import com.farcr.nomansland.common.registry.NMLDamageTypes;
import com.farcr.nomansland.common.registry.NMLParticleTypes;
import com.farcr.nomansland.common.registry.entities.NMLEntities;
import com.farcr.nomansland.common.registry.items.NMLItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class ThrownAquaRegiaBottle extends ThrowableItemProjectile {

    private static final double RADIUS = 2.5;

    public ThrownAquaRegiaBottle(EntityType<? extends ThrownAquaRegiaBottle> entityType, Level level) {
        super(entityType, level);
    }

    public ThrownAquaRegiaBottle(Level level, LivingEntity thrower) {
        super(NMLEntities.AQUA_REGIA.get(), thrower, level);
    }

    public ThrownAquaRegiaBottle(Level level, double x, double y, double z) {
        super(NMLEntities.AQUA_REGIA.get(), x, y, z, level);
    }

    @Override
    protected @NotNull Item getDefaultItem() {
        return NMLItems.AQUA_REGIA.get();
    }

    @Override
    protected void onHit(@NotNull HitResult result) {
        super.onHit(result);
        Level level = level();
        if (level.isClientSide()) return;

        Vec3 center = result.getLocation();
        double radiusSq = RADIUS * RADIUS;

        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, new AABB(center, center).inflate(RADIUS))) {
            if (entity.distanceToSqr(center) > radiusSq) continue;

            ItemStack shield = ItemStack.EMPTY;
            if (entity instanceof Player player && player.isBlocking()) {
                shield = player.getUseItem();
                damageStackByPercent(shield, 0.5F);
                player.getCooldowns().addCooldown(Items.SHIELD, 200);
                player.stopUsingItem();
            }

            for (EquipmentSlot slot : EquipmentSlot.values()) {
                ItemStack equipped = entity.getItemBySlot(slot);
                if (equipped == shield) continue;
                damageStackByPercent(equipped, 0.1F);
            }

            entity.hurt(NMLDamageTypes.getSimpleDamageSource(level, NMLDamageTypes.CORROSION), 4.0F);
        }

        int range = Mth.ceil(RADIUS);
        BlockPos origin = BlockPos.containing(center);
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-range, -range, -range), origin.offset(range, range, range))) {
            if (pos.distToCenterSqr(center.x, center.y, center.z) > radiusSq) continue;
            BlockState state = level.getBlockState(pos);
            WeatheringCopper.getPrevious(state).ifPresent(once -> {
                BlockState twice = WeatheringCopper.getPrevious(once).orElse(once);
                level.setBlockAndUpdate(pos.immutable(), twice);
            });
        }

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(NMLParticleTypes.TOXIC_GAS.get(), center.x, center.y, center.z, 24, RADIUS / 2, RADIUS / 2, RADIUS / 2, 0.0);
        }
        level.playSound(null, blockPosition(), SoundEvents.SPLASH_POTION_BREAK, SoundSource.PLAYERS, 1.0F, 0.9F);
        discard();
    }

    private static void damageStackByPercent(ItemStack stack, float percent) {
        if (!stack.isDamageableItem()) return;
        int current = stack.getMaxDamage() - stack.getDamageValue();
        int amount = Math.max(1, Mth.ceil(current * percent));
        int newDamage = stack.getDamageValue() + amount;
        if (newDamage >= stack.getMaxDamage()) {
            stack.shrink(1);
        } else {
            stack.setDamageValue(newDamage);
        }
    }
}
