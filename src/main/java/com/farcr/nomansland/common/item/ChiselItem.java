package com.farcr.nomansland.common.item;

import com.farcr.nomansland.client.gui.CarvingSelectionScreen;
import com.farcr.nomansland.client.handler.CarvingClientHandler;
import com.farcr.nomansland.common.carving.CarvingType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class ChiselItem extends Item {
    public ChiselItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if(context.getLevel().isClientSide()) {
            CarvingClientHandler handler = CarvingClientHandler.instance;
            Player player = context.getPlayer();
            if(handler.isChiseling()) {
                if(player.isShiftKeyDown()) {
                    handler.clear();
                } else {
                    handler.endChisel();
                }
                return InteractionResult.SUCCESS;
            } else {
                BlockState state = context.getLevel().getBlockState(context.getClickedPos());
                List<CarvingType> types = CarvingType.forState(state)
                        .stream().filter(v -> v.canCarve(player))
                        .toList();
                if(!types.isEmpty()) {
                    CarvingSelectionScreen.open(context.getClickedPos(), context.getClickedFace(), context.getHand(), types);
                    return InteractionResult.SUCCESS;
                }
            }
        }

        return super.useOn(context);
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        return false;
    }

    public static boolean holdingChisel(Player player) {
        return player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof ChiselItem || player.getItemInHand(InteractionHand.OFF_HAND).getItem() instanceof ChiselItem;
    }

}
