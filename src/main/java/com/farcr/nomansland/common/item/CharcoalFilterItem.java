package com.farcr.nomansland.common.item;

import com.farcr.nomansland.common.block.ToxicGasBlock;
import com.farcr.nomansland.common.registry.NMLSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public class CharcoalFilterItem extends Item {

    public static final int MAX_USES = 150;
    private static final int USES_PER_SECOND = 15;
    private static final int TICKS_PER_USE_SECOND = 20;
    private static final int USE_DURATION = 72000;
    private static final int HISS_INTERVAL = 10;

    public CharcoalFilterItem(Properties properties) {
        super(properties);
    }

    public static int remainingTicks(ItemStack stack) {
        return Math.max(0, stack.getMaxDamage() - stack.getDamageValue()) * TICKS_PER_USE_SECOND;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getDamageValue() >= stack.getMaxDamage()) return InteractionResultHolder.fail(stack);
        player.startUsingItem(hand);
        play(level, player, NMLSounds.CHARCOAL_FILTER_CLICK.get());
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return USE_DURATION;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remaining) {
        if (!(level instanceof ServerLevel server)) return;
        int elapsed = USE_DURATION - remaining;
        ToxicGasBlock.clearTouching(server, entity);
        if (elapsed % HISS_INTERVAL == 0) {
            play(level, entity, NMLSounds.CHARCOAL_FILTER_HISS.get());
        }
        int wear = elapsed * USES_PER_SECOND / TICKS_PER_USE_SECOND - (elapsed - 1) * USES_PER_SECOND / TICKS_PER_USE_SECOND;
        if (wear <= 0) return;
        if (stack.getDamageValue() + wear >= stack.getMaxDamage()) {
            play(level, entity, NMLSounds.CHARCOAL_FILTER_CLICK.get());
        }
        stack.hurtAndBreak(wear, entity, LivingEntity.getSlotForHand(entity.getUsedItemHand()));
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        play(level, entity, NMLSounds.CHARCOAL_FILTER_CLICK.get());
    }

    private static void play(Level level, LivingEntity entity, SoundEvent sound) {
        if (level.isClientSide()) return;
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), sound, SoundSource.PLAYERS, 1.0F, 0.9F + level.getRandom().nextFloat() * 0.2F);
    }
}
