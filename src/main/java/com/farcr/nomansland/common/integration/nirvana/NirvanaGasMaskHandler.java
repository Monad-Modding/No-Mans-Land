package com.farcr.nomansland.common.integration.nirvana;

import com.farcr.nomansland.common.item.GasMaskItem;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

public class NirvanaGasMaskHandler {
    public static final String NAMESPACE = "nirvana";
    public static final int SMOKE_COST = 20;
    public static final int CLOUD_COST = 20;

    public static boolean isAirborneEffect(MobEffectInstance instance) {
        if (instance == null) return false;
        Holder<MobEffect> effect = instance.getEffect();
        return effect.unwrapKey().map(ResourceKey::location)
            .map(location -> location.getNamespace().equals(NAMESPACE))
            .orElse(false);
    }

    public static boolean hasWorkingMask(LivingEntity entity) {
        if (entity == null) return false;
        ItemStack mask = entity.getItemBySlot(EquipmentSlot.HEAD);
        return mask.getItem() instanceof GasMaskItem && GasMaskItem.getDuration(mask) > 0;
    }

    private static void consume(LivingEntity entity, int ticks) {
        if (entity.level().isClientSide()) return;
        ItemStack mask = entity.getItemBySlot(EquipmentSlot.HEAD);
        if (mask.getItem() instanceof GasMaskItem) GasMaskItem.setDuration(mask, GasMaskItem.getDuration(mask) - ticks);
    }

    public static boolean filterSmoke(LivingEntity target) {
        if (!hasWorkingMask(target)) return false;
        consume(target, SMOKE_COST);
        return true;
    }

    public static void onEffectApplicable(MobEffectEvent.Applicable event) {
        Entity source = event.getEffectSource();
        if (!(source instanceof AreaEffectCloud)) return;
        if (!isAirborneEffect(event.getEffectInstance())) return;

        LivingEntity entity = event.getEntity();
        if (!hasWorkingMask(entity)) return;

        consume(entity, CLOUD_COST);
        event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
    }
}
