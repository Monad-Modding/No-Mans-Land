package com.farcr.nomansland.common.event;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.block.IciclesBlock;
import com.farcr.nomansland.common.block.ToxicGasBlock;
import com.farcr.nomansland.common.effect.FlammableEffect;
import com.farcr.nomansland.common.handler.ToxicGasHandler;
import com.farcr.nomansland.common.item.AncestralOathSwordItem;
import com.farcr.nomansland.common.item.GasMaskItem;
import com.farcr.nomansland.common.registry.entities.NMLEffects;
import com.farcr.nomansland.common.registry.items.NMLDataComponents;
import com.farcr.nomansland.common.registry.items.NMLItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.AddAttributeTooltipsEvent;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = NoMansLand.MODID)
public class ItemBehaviorEvents {
    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof ItemEntity itemEntity && itemEntity.level() instanceof ServerLevel serverLevel
                && itemEntity.getItem().is(NMLItems.SULFUR) && (itemEntity.isInLava() || itemEntity.isOnFire())) {
            ToxicGasBlock.scatterAround(serverLevel, itemEntity.blockPosition().above(), itemEntity.getItem().getCount());
            itemEntity.discard();
            return;
        }

        if (event.getEntity() instanceof LivingEntity entity) {
            if (!entity.level().isClientSide()) {
                FlammableEffect.dampenWhenWet(entity);
                ToxicGasHandler.handle(entity);
                if (entity.isPassenger()) {
                    IciclesBlock.hitRider(entity);
                }
            }

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

    @SubscribeEvent
    public static void onGasMaskAnvil(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();
        if (left.getItem() instanceof GasMaskItem && right.getItem() instanceof GasMaskItem) {
            int total = Math.min(GasMaskItem.MAX_DURATION, GasMaskItem.getDuration(left) + GasMaskItem.getDuration(right));
            ItemStack output = left.copyWithCount(1);
            GasMaskItem.setDuration(output, total);
            event.setOutput(output);
            event.setCost(1);
        }
    }
}
