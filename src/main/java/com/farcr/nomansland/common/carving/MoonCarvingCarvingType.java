package com.farcr.nomansland.common.carving;

import com.farcr.nomansland.common.block.AncestralCarvingBlock;
import com.farcr.nomansland.common.registry.NMLAttachmentTypes;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.InfestedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class MoonCarvingCarvingType extends AncestralCarvingCarvingType {
    @Override
    public ItemStack getIcon() {
        return NMLBlocks.MOON_CARVING.stack();
    }

    @Override
    public boolean canCarve(Player player) {
        long lastInteraction = player.getData(NMLAttachmentTypes.LAST_MOON_CARVING_INTERACTION.get());
        long timeElapsed = player.level().getGameTime() - lastInteraction;
        return lastInteraction > -1 && timeElapsed <= 72000;
    }

    @Override
    public AABB getPlacementBox(Player player, BlockPos start, BlockPos end, Direction direction) {
        return switch (direction.getAxis()) {
            case X -> AABB.encapsulatingFullBlocks(start.below().north(), start.above().south());
            case Y -> AABB.encapsulatingFullBlocks(start.north().west(), start.south().east());
            case Z -> AABB.encapsulatingFullBlocks(start.below().west(), start.above().east());
        };
    }

    @Override
    public boolean placeBlocks(ServerPlayer player, BlockPos start, BlockPos end, BlockPos min, BlockPos max, Direction direction) {
        ServerLevel level = (ServerLevel) player.level();
        Iterable<BlockPos> iterator = BlockPos.betweenClosed(min, max);
        for (BlockPos pos : iterator) {
            if(!this.canReplace(level.getBlockState(pos))) {
                return true;
            }
        }

        BlockPos center = BlockPos.containing(max.subtract(min).getCenter().scale(0.5)).offset(min);
        if(level.getBlockState(center).getBlock() instanceof InfestedBlock) {
            level.destroyBlock(center, true, player);
            return false;
        }
        BlockState state = this.getStateForPlacement(player, center, start, end, direction);
        level.setBlock(center, state, 3);
        return false;
    }

    @Override
    public void afterPlacement(Player player, BlockPos min, BlockPos max, Direction direction) {}

    @Override
    public AncestralCarvingBlock getBlock() {
        return NMLBlocks.MOON_CARVING.get();
    }
}
