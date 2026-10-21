package com.farcr.nomansland.common.blockentity;

import com.farcr.nomansland.common.registry.NMLBlockEntities;
import com.farcr.nomansland.common.world.saved_data.WardedSpacesData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import static com.farcr.nomansland.common.block.WardingEffigyBlock.getRange;

public class WardingEffigyBlockEntity extends BlockEntity {
    public WardingEffigyBlockEntity(final BlockPos pos, final BlockState state) {
        super(NMLBlockEntities.WARDING_EFFIGY.get(), pos, state);
    }

    @Override
    public void setLevel(final Level level) {
        if (level instanceof final ServerLevel serverLevel) {
            WardedSpacesData.get(serverLevel).addEffigy(this.getBlockPos(), getRange(this.getBlockState()));
        }

        super.setLevel(level);
    }
}
