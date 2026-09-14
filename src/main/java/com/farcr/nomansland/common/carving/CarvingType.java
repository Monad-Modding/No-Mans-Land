package com.farcr.nomansland.common.carving;

import com.farcr.nomansland.common.registry.NMLRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.InfestedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

public interface CarvingType {
    ItemStack getIcon();

    boolean canReplace(BlockState state);

    BlockState getStateForPlacement(Player player, BlockPos pos, BlockPos start, BlockPos end, Direction direction);

    default AABB getPlacementBox(Player player, BlockPos start, BlockPos end, Direction direction) {
        return AABB.encapsulatingFullBlocks(start, end);
    }

    default AABB getPreviewBox(Player player, BlockPos start, BlockPos end, Direction direction) {
        return this.getPlacementBox(player, start, end, direction);
    }

    default boolean canCarve(Player player) {
        return true;
    }

    default boolean placeBlocks(ServerPlayer player, BlockPos start, BlockPos end, BlockPos min, BlockPos max, Direction direction) {
        ServerLevel level = (ServerLevel) player.level();
        Iterable<BlockPos> iterator = BlockPos.betweenClosed(min, max);
        for (BlockPos pos : iterator) {
            if(!this.canReplace(level.getBlockState(pos))) {
                return true;
            }
        }

        for (BlockPos pos : iterator) {
            if(level.getBlockState(pos).getBlock() instanceof InfestedBlock) {
                level.destroyBlock(pos, true, player);
                continue;
            }
            BlockState placementState = this.getStateForPlacement(player, pos, start, end, direction);
            level.setBlock(pos, placementState, 3);
        }

        return false;
    }

    default void afterPlacement(Player player, BlockPos min, BlockPos max, Direction direction) {

    }

    static List<CarvingType> forState(BlockState state) {
        List<CarvingType> types = new ArrayList<>();
        for (CarvingType type : NMLRegistries.CARVING_TYPE) {
            if(type.canReplace(state)) {
                types.add(type);
            }
        }
        return types;
    }

}
