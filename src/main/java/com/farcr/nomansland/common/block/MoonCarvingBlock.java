package com.farcr.nomansland.common.block;

import com.farcr.nomansland.common.blockentity.MoonCarvingBlockEntity;
import com.farcr.nomansland.common.registry.NMLBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class MoonCarvingBlock extends AncestralCarvingBlock implements EntityBlock {

    public MoonCarvingBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends DirectionalBlock> codec() {
        return simpleCodec(MoonCarvingBlock::new);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (state.getValue(FORMATION) == CarvingFormation.THREE_1_1)
            return new MoonCarvingBlockEntity(pos, state);
        return null;
    }

    // Write an access transformer or something idk duplicate code sucks
    @Nullable
    protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(
        BlockEntityType<A> serverType, BlockEntityType<E> clientType, BlockEntityTicker<? super E> ticker) {
        return clientType == serverType ? (BlockEntityTicker<A>) ticker : null;
    }

    @Nullable @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, NMLBlockEntities.MOON_CARVING.get(), MoonCarvingBlockEntity::tick);
    }

    public boolean queryPositions(
        Level level, BlockPos placedPos, Direction facing,
        int rotation, Function<BlockState, Boolean> breakOutCondition
    ) {
        Direction right = getPlaneRight(facing, rotation);
        Direction down = getPlaneDown(facing, rotation);

        for (int col = -1; col <= 1; col++) {
            for (int row = -1; row <= 1; row++) {
                if (col == 0 && row == 0) continue;
                BlockPos pos = placedPos.relative(right, col).relative(down, row);
                if (breakOutCondition.apply(level.getBlockState(pos)))
                    return true;
            }
        }
        return false;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos placedPos = context.getClickedPos();

        Direction facing;
        int rotation;
        float pitch = context.getPlayer() != null ? context.getPlayer().getXRot() : 0;
        if (pitch > 60) {
            facing = Direction.UP;
            rotation = getRotationForPlayer(context, facing);
        } else if (pitch < -60) {
            facing = Direction.DOWN;
            rotation = getRotationForPlayer(context, facing);
        } else {
            facing = context.getHorizontalDirection().getOpposite();
            rotation = 0;
        }

        // sorry tazer I dont like duplicating code so i've condensed this here so I can use it in the blockentity as well !!!
        if (queryPositions(level, placedPos, facing, rotation, (blockState) -> !blockState.canBeReplaced(context)))
            return null;

        return this.defaultBlockState()
                .setValue(FACING, facing)
                .setValue(FORMATION, CarvingFormation.SINGLE)
                .setValue(ROTATION, rotation);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (level.isClientSide || oldState.is(this) || state.getValue(FORMATION) != CarvingFormation.SINGLE) return;

        Direction facing = state.getValue(FACING);
        int rotation = state.getValue(ROTATION);
        Direction right = getPlaneRight(facing, rotation);
        Direction down = getPlaneDown(facing, rotation);

        for (int col = -1; col <= 1; col++) {
            for (int row = -1; row <= 1; row++) {
                BlockPos target = pos.relative(right, col).relative(down, row);
                CarvingFormation formation = CarvingFormation.getForPosition(3, col + 1, row + 1);
                level.setBlock(target, this.defaultBlockState()
                        .setValue(FACING, facing)
                        .setValue(FORMATION, formation)
                        .setValue(ROTATION, rotation), 3);
            }
        }
    }
}
