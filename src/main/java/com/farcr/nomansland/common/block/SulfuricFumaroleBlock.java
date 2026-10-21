package com.farcr.nomansland.common.block;

import com.farcr.nomansland.common.registry.NMLParticleTypes;
import com.farcr.nomansland.common.registry.NMLTags;
import com.farcr.nomansland.common.registry.entities.NMLEffects;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DirectionalBlock;

public class SulfuricFumaroleBlock extends VentBlock {
    public static final MapCodec<SulfuricFumaroleBlock> CODEC = simpleCodec(SulfuricFumaroleBlock::new);
    private static final String SULFURIC_TICKS_KEY = "SulfuricFumaroleTicks";

    public SulfuricFumaroleBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends DirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    public ParticleOptions getVentParticle() {
        return NMLParticleTypes.SULFURIC_FUMAROLE.get();
    }

    @Override
    public void affectEntity(Level level, BlockPos ventPos, Entity entity, int distance) {
        if (entity instanceof FishingHook hook) {
            snapFishingLine(level, hook);
            return;
        }

        if (level.getGameTime() % 20 != 0) return;

        if (entity instanceof ItemEntity itemEntity) corrodeItemEntity(level, itemEntity, distance);
        else if (entity instanceof LivingEntity living) corrodeLivingEntity(living, distance);
    }

    private void corrodeLivingEntity(LivingEntity living, int distance) {
        if (!living.isInWater()) return;
        if (living instanceof Player player && (player.isCreative() || player.isSpectator())) return;

        int amplifier = Mth.clamp(RANGE - distance, 0, 4);
        living.addEffect(new MobEffectInstance(NMLEffects.CORROSION, 25, amplifier, true, true));

        float percent = 0.05F + 0.15F * (RANGE - distance) / (RANGE - 1);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = living.getItemBySlot(slot);
            if (stack.isEmpty() || !stack.isDamageableItem() || stack.is(NMLTags.DOES_NOT_CORRODE)) continue;
            damageStackByPercent(stack, percent);
            if (stack.isEmpty()) living.setItemSlot(slot, ItemStack.EMPTY);
        }
    }

    private void corrodeItemEntity(Level level, ItemEntity itemEntity, int distance) {
        if (!itemEntity.isInWater() || itemEntity.getItem().is(NMLTags.DOES_NOT_CORRODE)) return;
        if (!(level instanceof ServerLevel serverLevel)) return;

        serverLevel.sendParticles(NMLParticleTypes.VENT_BUBBLE.get(),
                itemEntity.getX(), itemEntity.getY() + 0.2, itemEntity.getZ(),
                8, 0.2, 0.2, 0.2, 0.1);

        int ticks = itemEntity.getPersistentData().getInt(SULFURIC_TICKS_KEY) + 20;
        itemEntity.getPersistentData().putInt(SULFURIC_TICKS_KEY, ticks);
        if (ticks >= 120 + 30 * (distance - 1)) {
            serverLevel.sendParticles(NMLParticleTypes.VENT_BUBBLE_POP.get(),
                    itemEntity.getX(), itemEntity.getY() + 0.2, itemEntity.getZ(),
                    12, 0.25, 0.25, 0.25, 0.05);
            level.playSound(null, itemEntity.blockPosition(), SoundEvents.GENERIC_BURN, SoundSource.BLOCKS, 0.5F, 1.4F);
            itemEntity.discard();
        }
    }

    private void snapFishingLine(Level level, FishingHook hook) {
        Player owner = hook.getPlayerOwner();
        if (owner != null) {
            ItemStack rod = owner.getMainHandItem().getItem() instanceof FishingRodItem
                    ? owner.getMainHandItem() : owner.getOffhandItem();
            if (rod.getItem() instanceof FishingRodItem) damageStackByPercent(rod, 0.5F);
            level.playSound(null, owner.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 0.8F, 0.8F);
        }
        level.playSound(null, hook.blockPosition(), SoundEvents.BUBBLE_COLUMN_BUBBLE_POP, SoundSource.BLOCKS, 1.0F, 0.7F);
        hook.discard();
    }

    public static void damageStackByPercent(ItemStack stack, float percent) {
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
