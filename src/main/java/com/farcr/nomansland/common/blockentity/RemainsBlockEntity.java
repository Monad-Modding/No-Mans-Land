package com.farcr.nomansland.common.blockentity;

import com.farcr.nomansland.NMLConfig;
import com.farcr.nomansland.common.entity.Buried;
import com.farcr.nomansland.common.registry.NMLBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class RemainsBlockEntity extends BrushableBlockEntity {
    public RemainsBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Override
    public boolean isValidBlockState(BlockState blockState) {
        return NMLBlockEntities.REMAINS.get().isValid(blockState);
    }

    @Override
    public BlockEntityType<?> getType() {
        return NMLBlockEntities.REMAINS.get();
    }

    @Override
    public void brushingCompleted(Player player) {
        if (level instanceof ServerLevel serverLevel && serverLevel.random.nextFloat() < NMLConfig.BURIED_SPAWNING_CHANCE.get()) {
            Direction direction = getHitDirection() == null ? Direction.UP : getHitDirection();
            BlockPos spawnPos = worldPosition.relative(direction);
            if (serverLevel.getBlockState(spawnPos).isAir()) {
                Vec3 center = spawnPos.getCenter();
                Buried.spawnFromRemains(serverLevel, center.x, center.y, center.z);
            } else {
                Buried.spawnFromRemains(serverLevel,
                        (player.getX() + worldPosition.getX()) / 2,
                        (player.getY() + worldPosition.getY()) / 2,
                        (player.getZ() + worldPosition.getZ()) / 2);
            }
        }

        super.brushingCompleted(player);
    }
}
