package com.farcr.nomansland.common.handler;

import com.farcr.nomansland.common.block.ToxicGasBlock;
import com.farcr.nomansland.common.item.GasMaskItem;
import com.farcr.nomansland.common.registry.NMLTags;
import com.farcr.nomansland.common.registry.entities.NMLEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class ToxicGasHandler {
    private static final Map<Integer, int[]> EXPOSURE = new HashMap<>();

    public static void handle(LivingEntity entity) {
        if (entity.level().isClientSide()) return;

        int id = entity.getId();
        ItemStack mask = entity.getItemBySlot(EquipmentSlot.HEAD);
        boolean hasMask = mask.getItem() instanceof GasMaskItem;

        if (hasMask && GasMaskItem.getDuration(mask) > 0 && entity.isUnderWater()) {
            GasMaskItem.setDuration(mask, GasMaskItem.getDuration(mask) - 5);
        }

        if (entity.isRemoved() || !ToxicGasBlock.isBodyInGas(entity)) {
            EXPOSURE.remove(id);
            return;
        }

        boolean headInGas = ToxicGasBlock.isHeadInGas(entity);
        if (hasMask && headInGas && GasMaskItem.getDuration(mask) > 0) {
            GasMaskItem.setDuration(mask, GasMaskItem.getDuration(mask) - 1);
        }

        int[] exposure = EXPOSURE.computeIfAbsent(id, key -> new int[2]);
        exposure[0]++;
        if (exposure[0] < 20) return;
        exposure[0] = 0;

        corrodeEquipment(entity);

        boolean masked = hasMask && GasMaskItem.getDuration(mask) > 0;
        if (headInGas && !masked) {
            exposure[1]++;
            int duration = 20 + (exposure[1] / 2) * 20;
            entity.addEffect(new MobEffectInstance(NMLEffects.CORROSION, duration, 0, false, false, true));
        } else {
            exposure[1] = 0;
        }
    }

    private static void corrodeEquipment(LivingEntity entity) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = entity.getItemBySlot(slot);
            if (stack.isEmpty() || !stack.isDamageableItem() || stack.is(NMLTags.DOES_NOT_CORRODE)) continue;

            int amount = 1;
            if (stack.getMaxDamage() > 1000 && entity.getRandom().nextFloat() < 0.25F) amount = 2;

            int newDamage = Math.min(stack.getDamageValue() + amount, stack.getMaxDamage() - 1);
            if (newDamage > stack.getDamageValue()) stack.setDamageValue(newDamage);
        }
    }
}
