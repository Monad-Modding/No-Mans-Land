package com.farcr.nomansland.common.blockentity;

import com.farcr.nomansland.common.registry.NMLBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class AncientBronzeBellBlockEntity extends BlockEntity {
    public int ticks;
    public boolean shaking;
    public Direction clickDirection;

    public AncientBronzeBellBlockEntity(BlockPos pos, BlockState state) {
        super(NMLBlockEntities.ANCIENT_BRONZE_BELL.get(), pos, state);
    }

    public boolean triggerEvent(int id, int data) {
        if (id != 1) return super.triggerEvent(id, data);
        this.clickDirection = Direction.from3DDataValue(data);
        this.ticks = 0;
        this.shaking = true;
        return true;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, AncientBronzeBellBlockEntity bell) {
        if (bell.shaking && ++bell.ticks >= 50) {
            bell.shaking = false;
            bell.ticks = 0;
        }
    }
}
