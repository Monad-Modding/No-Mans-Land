package com.farcr.nomansland.common.entity.clod;

import com.farcr.nomansland.common.registry.entities.NMLEntities;
import com.farcr.nomansland.common.registry.items.NMLItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class TranslucentItemFrame extends ItemFrame {

    public TranslucentItemFrame(EntityType<? extends ItemFrame> type, Level level) {
        super(type, level);
    }

    public TranslucentItemFrame(Level level, BlockPos pos, Direction direction) {
        super(NMLEntities.TRANSLUCENT_ITEM_FRAME.get(), level, pos, direction);
    }

    @Override
    public void tick() {
        super.tick();
        boolean hasItem = !this.getItem().isEmpty();
        if (this.isInvisible() != hasItem) {
            this.setInvisible(hasItem);
        }
    }

    @Override
    protected ItemStack getFrameItemStack() {
        return new ItemStack(NMLItems.TRANSLUCENT_ITEM_FRAME.get());
    }
}
