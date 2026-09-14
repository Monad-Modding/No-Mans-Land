package com.farcr.nomansland.common.block;

import com.mojang.serialization.MapCodec;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import org.jetbrains.annotations.Nullable;

public class LogBlock extends RotatedPillarBlock {

    public LogBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<LogBlock> codec() {
        return simpleCodec(LogBlock::new);
    }

    @Override
    public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return true;
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 5;
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 5;
    }

    @Override
    public @Nullable BlockState getToolModifiedState(BlockState state, UseOnContext context, ItemAbility itemAbility, boolean simulate) {
        if (itemAbility.equals(ItemAbilities.AXE_STRIP)) {
            BlockState checkedState;
            checkedState = NMLBlocks.PINE.checkLogStripping(state);
            if (checkedState != null) return checkedState;
            checkedState = NMLBlocks.MAPLE.checkLogStripping(state);
            if (checkedState != null) return checkedState;
            checkedState = NMLBlocks.WALNUT.checkLogStripping(state);
            if (checkedState != null) return checkedState;
            checkedState = NMLBlocks.WILLOW.checkLogStripping(state);
            if (checkedState != null) return checkedState;
        }

        return super.getToolModifiedState(state, context, itemAbility, simulate);
    }

}
