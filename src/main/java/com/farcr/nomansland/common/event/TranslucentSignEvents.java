package com.farcr.nomansland.common.event;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.extension.TranslucentSign;
import com.farcr.nomansland.common.registry.items.NMLItems;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;

@EventBusSubscriber(modid = NoMansLand.MODID)
public class TranslucentSignEvents {

    @SubscribeEvent
    public static void onRightClickSign(final UseItemOnBlockEvent event) {
        if (event.getUsePhase() != UseItemOnBlockEvent.UsePhase.BLOCK) return;

        Player player = event.getPlayer();
        if (player == null) return;
        ItemStack stack = event.getItemStack();
        if (!stack.is(NMLItems.TRANSLUCENT_SAC.get())) return;

        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof SignBlockEntity) || !(blockEntity instanceof TranslucentSign sign)) return;
        if (sign.nml$isTranslucent()) return;

        if (!level.isClientSide) {
            sign.nml$setTranslucent(true);
            blockEntity.setChanged();
            BlockState state = level.getBlockState(pos);
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
            stack.consume(1, player);
            level.playSound(null, pos, SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 0.6F, 1.4F);
        }

        event.cancelWithResult(ItemInteractionResult.sidedSuccess(level.isClientSide));
    }
}
