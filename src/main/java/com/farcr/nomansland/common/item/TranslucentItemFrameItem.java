package com.farcr.nomansland.common.item;

import com.farcr.nomansland.common.entity.clod.TranslucentItemFrame;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HangingEntityItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public class TranslucentItemFrameItem extends HangingEntityItem {

    public TranslucentItemFrameItem(EntityType<? extends HangingEntity> type, Properties properties) {
        super(type, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockPos clicked = context.getClickedPos();
        Direction direction = context.getClickedFace();
        BlockPos placePos = clicked.relative(direction);
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        if (player != null && !this.mayPlace(player, direction, stack, placePos)) {
            return InteractionResult.FAIL;
        }

        Level level = context.getLevel();
        TranslucentItemFrame frame = new TranslucentItemFrame(level, placePos, direction);

        CustomData customData = stack.getOrDefault(DataComponents.ENTITY_DATA, CustomData.EMPTY);
        if (!customData.isEmpty()) {
            EntityType.updateCustomEntityTag(level, player, frame, customData);
        }

        if (frame.survives()) {
            if (!level.isClientSide) {
                frame.playPlacementSound();
                level.gameEvent(player, GameEvent.ENTITY_PLACE, frame.position());
                level.addFreshEntity(frame);
            }
            stack.shrink(1);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.CONSUME;
    }
}
