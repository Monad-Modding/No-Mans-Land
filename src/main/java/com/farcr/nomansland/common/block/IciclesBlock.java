package com.farcr.nomansland.common.block;

import com.mojang.serialization.MapCodec;
import com.farcr.nomansland.common.registry.NMLDamageTypes;
import com.farcr.nomansland.common.registry.NMLSounds;
import com.farcr.nomansland.common.registry.NMLTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Fallable;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class IciclesBlock extends Block implements Fallable {

    public static final DirectionProperty TIP_DIRECTION = BlockStateProperties.VERTICAL_DIRECTION;

    public IciclesBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(TIP_DIRECTION, Direction.DOWN));
    }

    @Override
    public MapCodec<IciclesBlock> codec() {
        return simpleCodec(IciclesBlock::new);
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        if (state.getValue(TIP_DIRECTION) == Direction.UP) {
            entity.causeFallDamage(fallDistance + 2, 1, NMLDamageTypes.getSimpleDamageSource(level, NMLDamageTypes.ICICLE_PIERCE));
            if (entity instanceof LivingEntity livingEntity) livingEntity.setTicksFrozen(40);
            level.destroyBlock(pos, false);

        } else super.fallOn(level, state, pos, entity, fallDistance);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction placementDirection = context.getNearestLookingVerticalDirection().getOpposite();
        return defaultBlockState().setValue(TIP_DIRECTION, placementDirection);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TIP_DIRECTION);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        boolean aboveSupportsIcicle = level.getBlockState(pos.above()).is(NMLTags.SUPPORTS_ICICLE) && state.getValue(TIP_DIRECTION) == Direction.DOWN;
        boolean belowSupportsIcicle = level.getBlockState(pos.below()).is(NMLTags.SUPPORTS_ICICLE) && state.getValue(TIP_DIRECTION) == Direction.UP;

        return aboveSupportsIcicle || belowSupportsIcicle;
    }

    @NotNull
    private static final VoxelShape UPWARD_SHAPE = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 7.0D, 14.0D);
    private static final VoxelShape DOWNWARD_SHAPE = Block.box(3.0D, 9.0D, 3.0D, 13.0D, 16.0D, 14.0D);

    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Vec3 offset = state.getOffset(level, pos);
        VoxelShape shape = state.getValue(TIP_DIRECTION) == Direction.UP ? UPWARD_SHAPE : DOWNWARD_SHAPE;
        return shape.move(offset.x, offset.y, offset.z);
    }

    @Override
    public DamageSource getFallDamageSource(Entity entity) {
        return NMLDamageTypes.getSimpleDamageSource(entity.level(), NMLDamageTypes.ICICLE_PIERCE);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(TIP_DIRECTION) == Direction.UP) {
            level.destroyBlock(pos, false);
        } else if (!this.canSurvive(state, level, pos)) {
            FallingBlockEntity fallingBlockEntity = FallingBlockEntity.fall(level, pos, state);
            fallingBlockEntity.setHurtsEntities(2, 40);
            fallingBlockEntity.disableDrop();
        }
    }

    @NotNull
    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (!this.canSurvive(state, level, pos)) {
            level.scheduleTick(pos, this, 2);
        }

        super.updateShape(state, direction, neighborState, level, pos, neighborPos);
        return state;
    }

    @Override
    public void onBrokenAfterFall(Level level, BlockPos pos, FallingBlockEntity fallingBlock) {
        if (!fallingBlock.isSilent() && level instanceof ServerLevel serverLevel) {
            serverLevel.playSound(fallingBlock, pos, NMLSounds.ICICLE_SHATTER.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
        }
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        // If entity is moving upwards and icicle is pointed downwards
        if (entity instanceof LivingEntity livingEntity && !level.isClientSide()) {
            if (Direction.getNearest(livingEntity.getDeltaMovement()) == Direction.UP && state.getValue(TIP_DIRECTION) == Direction.DOWN) {
                level.destroyBlock(pos, false);
                livingEntity.setTicksFrozen(40);
                livingEntity.hurt(NMLDamageTypes.getSimpleDamageSource(level, NMLDamageTypes.ICICLE_PIERCE), 4);
            }
        }
    }
}
