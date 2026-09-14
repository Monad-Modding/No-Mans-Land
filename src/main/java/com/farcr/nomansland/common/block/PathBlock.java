package com.farcr.nomansland.common.block;
import net.minecraft.core.registries.BuiltInRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirtPathBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

import javax.annotation.Nullable;

public class PathBlock extends DirtPathBlock {
    public final Block mainBlock;
    public final boolean hasGravity;

    public PathBlock(Properties properties, Block mainBlock, boolean hasGravity) {
        super(properties);
        this.mainBlock = mainBlock;
        this.hasGravity = hasGravity;
    }

    public static final MapCodec<DirtPathBlock> CODEC = RecordCodecBuilder.<PathBlock>mapCodec(instance -> instance.group(
            propertiesCodec(),
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("main_block").forGetter(block -> block.mainBlock),
            Codec.BOOL.fieldOf("has_gravity").forGetter(block -> block.hasGravity)
    ).apply(instance, PathBlock::new)).xmap(block -> (DirtPathBlock) block, block -> (PathBlock) block);

    @Override
    public MapCodec<DirtPathBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return !this.defaultBlockState().canSurvive(context.getLevel(), context.getClickedPos()) ? Block.pushEntitiesUp(this.defaultBlockState(), mainBlock.defaultBlockState(), context.getLevel(), context.getClickedPos()) : super.getStateForPlacement(context);
    }

    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (this.hasGravity && FallingBlock.isFree(level.getBlockState(pos.below()))) {
            level.scheduleTick(pos, this, 2);
        }
    }

    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        if (facing == Direction.UP && !state.canSurvive(level, currentPos)) {
            level.scheduleTick(currentPos, this, 1);
        }

        if (this.hasGravity && FallingBlock.isFree(level.getBlockState(currentPos.below()))) {
            level.scheduleTick(currentPos, this, 2);
        }

        return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource source) {
        turnToBlock(null, state, level, pos);
    }

    public void turnToBlock(@Nullable Entity entity, BlockState state, Level level, BlockPos pos) {
        BlockState blockstate = pushEntitiesUp(state, mainBlock.defaultBlockState(), level, pos);
        level.setBlockAndUpdate(pos, blockstate);
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(entity, blockstate));
    }

}
