package com.farcr.nomansland.common.entity.ai;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ProjectileWeaponItem;

public class WarningAttack {
    public static OneShot<Mob> create(int cooldownBetweenAttacks) {
        return BehaviorBuilder.create((instance) -> instance.group(instance.registered(MemoryModuleType.LOOK_TARGET), instance.present(MemoryModuleType.NEAREST_ATTACKABLE), instance.absent(MemoryModuleType.ATTACK_COOLING_DOWN), instance.present(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)).apply(instance, (muPositionTrackerMemoryAccessor, muLivingEntityMemoryAccessor, muBooleanMemoryAccessor, nearestVisibleLivingEntities) -> (serverLevel, mob, gameTime) -> {
            LivingEntity livingentity = instance.get(muLivingEntityMemoryAccessor);
            if (!isHoldingUsableProjectileWeapon(mob) && mob.isWithinMeleeAttackRange(livingentity) && instance.get(nearestVisibleLivingEntities).contains(livingentity)) {
                muPositionTrackerMemoryAccessor.set(new EntityTracker(livingentity, true));
                mob.swing(InteractionHand.MAIN_HAND);
                mob.doHurtTarget(livingentity);
                muBooleanMemoryAccessor.setWithExpiry(true, cooldownBetweenAttacks);
                return true;
            } else {
                return false;
            }
        }));
    }

    private static boolean isHoldingUsableProjectileWeapon(Mob mob) {
        return mob.isHolding(stack -> {
            Item item = stack.getItem();
            return item instanceof ProjectileWeaponItem && mob.canFireProjectileWeapon((ProjectileWeaponItem)item);
        });
    }
}
