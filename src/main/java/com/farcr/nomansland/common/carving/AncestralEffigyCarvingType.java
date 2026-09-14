package com.farcr.nomansland.common.carving;

import com.farcr.nomansland.common.block.AncestralEffigyBlock;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class AncestralEffigyCarvingType implements CarvingType {

    @Override
    public ItemStack getIcon() {
        return NMLBlocks.ANCESTRAL_EFFIGY.stack();
    }

    @Override
    public boolean canReplace(BlockState state) {
        return state.is(Blocks.STONE) || state.is(Blocks.INFESTED_STONE) || state.is(NMLBlocks.ANCESTRAL_CARVING);
    }

    @Override
    public BlockState getStateForPlacement(Player player, BlockPos pos, BlockPos start, BlockPos end, Direction direction) {
        if(direction.getAxis() == Direction.Axis.Y) {
            direction = player.getDirection().getOpposite();
        }
        return NMLBlocks.ANCESTRAL_EFFIGY.block().defaultBlockState()
                .setValue(AncestralEffigyBlock.FACING, direction.getOpposite());
    }

    @Override
    public AABB getPlacementBox(Player player, BlockPos start, BlockPos end, Direction direction) {
        end = new BlockPos(start.getX(), Math.clamp(end.getY(), start.getY() - 7, start.getY() + 7), start.getZ());
        return CarvingType.super.getPlacementBox(player, start, end, direction);
    }

    @Override
    public AABB getPreviewBox(Player player, BlockPos start, BlockPos end, Direction direction) {
        BlockState state = NMLBlocks.ANCESTRAL_EFFIGY.block().defaultBlockState();
        Vec3 offset = state.getOffset(player.level(), start);
        return CarvingType.super.getPreviewBox(player, start, end, direction).deflate(0.25, 0.0, 0.25).move(offset);
    }
}
