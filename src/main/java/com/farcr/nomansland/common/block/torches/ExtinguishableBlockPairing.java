package com.farcr.nomansland.common.block.torches;

import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

/**
 * A pair of lit and extinguished blocks
 *
 * @param litBlock
 * @param extinguishedBlock
 */
public record ExtinguishableBlockPairing(Block litBlock, Block extinguishedBlock) {

    /**
     * @return Whether the given block is the lit version of this pairing.
     */
    public boolean isLitVersion(final Block toCheck) {
        return toCheck.equals(this.litBlock);
    }

    /**
     * @return Whether the given block is the lit version of this pairing.
     */
    public boolean isLitVersion(final BlockState toCheck) {
        return toCheck.is(this.litBlock);
    }

    /**
     * @return Whether the given block is the extinguished version of this pairing.
     */
    public boolean isExtinguishedVersion(final Block toCheck) {
        return toCheck.equals(this.extinguishedBlock);
    }

    /**
     * @return Whether the given block is the extinguished version of this pairing.
     */
    public boolean isExtinguishedVersion(final BlockState toCheck) {
        return toCheck.is(this.extinguishedBlock);
    }

    public boolean canRelight(BlockState state) {
        return this.isExtinguishedVersion(state) && !isSubmerged(state);
    }

    public BlockState extinguishedIn(BlockState litState, FluidState fluid) {
        BlockState extinguished = this.extinguishedBlock.withPropertiesOf(litState);
        if (extinguished.hasProperty(BlockStateProperties.WATERLOGGED)) {
            extinguished = extinguished.setValue(BlockStateProperties.WATERLOGGED, fluid.getType() == Fluids.WATER);
        }
        return extinguished;
    }

    public static boolean isSubmerged(BlockState state) {
        return state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED);
    }

    public static boolean extinguishesIn(FluidState fluid) {
        return fluid.is(FluidTags.WATER);
    }
}
