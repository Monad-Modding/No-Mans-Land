package com.farcr.nomansland.common.event;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.effect.FlammableEffect;
import com.farcr.nomansland.common.item.AncestralOathSwordItem;
import com.farcr.nomansland.common.registry.entities.NMLEffects;
import com.farcr.nomansland.common.registry.items.NMLDataComponents;
import com.farcr.nomansland.common.registry.items.NMLItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.AddAttributeTooltipsEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = NoMansLand.MODID)
public class ItemBehaviorEvents {
    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof LivingEntity entity) {
            if (!entity.level().isClientSide())
                FlammableEffect.dampenWhenWet(entity);

            ItemStack stack = entity.getItemBySlot(EquipmentSlot.HEAD);
            if (stack.is(NMLItems.ANCIENT_BRONZE_MASK)) {
                int punchCooldown = stack.getOrDefault(NMLDataComponents.PUNCH_COOLDOWN, 0);
                if (punchCooldown > 0)
                    stack.set(NMLDataComponents.PUNCH_COOLDOWN, punchCooldown - 1);
                else if (stack.getOrDefault(NMLDataComponents.PUNCH_COUNT, 0) > 0)
                    stack.set(NMLDataComponents.PUNCH_COUNT, 0);

                if (entity instanceof Enemy) entity.addEffect(new MobEffectInstance(NMLEffects.PACIFIED, 200, 0, false, true, true));
            }
            
            AncestralOathSwordItem.updateUseTime(entity);
        }
    }

    @SubscribeEvent
    public static void onAddAttributeTooltips(AddAttributeTooltipsEvent event) {
        if (event.getStack().is(NMLItems.ANCIENT_BRONZE_MASK)) {
            event.addTooltipLines(Component.translatable("nomansland.tooltip.mask.regeneration").withStyle(ChatFormatting.BLUE));
        }
    }
}
