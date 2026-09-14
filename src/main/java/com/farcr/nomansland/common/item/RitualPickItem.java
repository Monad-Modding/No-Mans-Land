package com.farcr.nomansland.common.item;

import com.farcr.nomansland.common.networking.alchemist_tools.ServerboundRitualPickRequestPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.network.PacketDistributor;

public class RitualPickItem extends PickaxeItem {
    public static final int MAX_CHARGE_TIME = 20 * 3,
                            DEEP_CHARGE_TIME = (int) (MAX_CHARGE_TIME * (2.0 / 3.0));
    public RitualPickItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        BlockHitResult raycast = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
        ItemStack itemstack = player.getItemInHand(usedHand);
        // make sure you're looking at a block
        if (raycast.getType() != HitResult.Type.BLOCK) return InteractionResultHolder.fail(itemstack);

        player.startUsingItem(usedHand);
        return InteractionResultHolder.pass(itemstack);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        super.onUseTick(level, livingEntity, stack, remainingUseDuration);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        if (entity instanceof Player player) {
            entity.swing(entity.getUsedItemHand());
            boolean deep = timeCharged < MAX_CHARGE_TIME * (2.0 / 3.0);
            int radius = deep ? 1 : 2,
                depth = deep ? 17 : 6;
            BlockHitResult raycast = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
            if (raycast.getType() == HitResult.Type.BLOCK && level.isClientSide()) {
                PacketDistributor.sendToServer(
                        new ServerboundRitualPickRequestPacket(
                                raycast.getBlockPos(),
                                raycast.getDirection().getOpposite(),
                                radius, depth
                        )
                );
            }
        }
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return MAX_CHARGE_TIME;
    }
}
