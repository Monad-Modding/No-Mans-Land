package com.farcr.nomansland.common.block;

import com.farcr.nomansland.NMLConfig;
import com.farcr.nomansland.common.registry.NMLDamageTypes;
import com.farcr.nomansland.common.registry.NMLSounds;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public class SpikeTrapBlock extends DirectionalBlock implements SimpleWaterloggedBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    protected static final VoxelShape EAST_AABB = Block.box(0.0D, 0.0D, 0.0D, 2.0D, 16.0D, 16.0D);
    protected static final VoxelShape WEST_AABB = Block.box(14.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape SOUTH_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 2.0D);
    protected static final VoxelShape NORTH_AABB = Block.box(0.0D, 0.0D, 14.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape UP_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);
    protected static final VoxelShape DOWN_AABB = Block.box(0.0D, 14.0D, 0.0D, 16.0D, 16.0D, 16.0D);

    public SpikeTrapBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, Boolean.FALSE)
                .setValue(FACING, Direction.UP).setValue(POWERED, Boolean.TRUE));
    }

    @Override
    protected MapCodec<? extends DirectionalBlock> codec() {
        return simpleCodec(SpikeTrapBlock::new);
    }

    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case NORTH -> NORTH_AABB;
            case SOUTH -> SOUTH_AABB;
            case WEST -> WEST_AABB;
            case EAST -> EAST_AABB;
            case DOWN -> DOWN_AABB;
            default -> UP_AABB;
        };
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    public BlockState updateShape(BlockState state, Direction facing, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, facing, neighborState, level, pos, neighborPos);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED, WATERLOGGED);
    }

    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean isMoving) {
        if (!neighborBlock.equals(state.getBlock())) {
            if (!level.isClientSide) {
                this.checkIfPowered(level, pos, state);
            }
        }
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (entity instanceof LivingEntity livingEntity && livingEntity.isAlive()
                && !state.getValue(POWERED)
                && !(livingEntity instanceof Player player && player.getAbilities().invulnerable)
                && (!livingEntity.isShiftKeyDown() || !livingEntity.getPosition(0).equals(livingEntity.getPosition(1))))
            livingEntity.hurt(NMLDamageTypes.getSimpleDamageSource(level, NMLDamageTypes.SPIKE_POKE), NMLConfig.POKING_DAMAGE.get().floatValue());
    }

    private void checkIfPowered(Level level, BlockPos pos, BlockState state) {
        boolean hasSignal = false;
        for (Direction d : Direction.values()) {
            if (level.hasSignal(pos.relative(d), d)) {
                hasSignal = true;
                break;
            }
        }

        if (hasSignal && !state.getValue(POWERED)) {
            level.setBlockAndUpdate(pos, state.setValue(POWERED, true));
            level.playSound(null, pos, NMLSounds.SPIKE_TRAP_RETRACT.get(), SoundSource.BLOCKS, 0.5F, 1.0F);
            level.gameEvent(GameEvent.BLOCK_DEACTIVATE, pos, GameEvent.Context.of(state));
        } else if (!hasSignal && state.getValue(POWERED)) {
            level.setBlockAndUpdate(pos, state.setValue(POWERED, false));
            level.playSound(null, pos, NMLSounds.SPIKE_TRAP_EXTEND.get(), SoundSource.BLOCKS, 0.5F, 1.0F);
            level.gameEvent(GameEvent.BLOCK_ACTIVATE, pos, GameEvent.Context.of(state));
            Predicate<LivingEntity> livingEntityPredicate = LivingEntity::isAlive;
            List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, new AABB(pos), livingEntityPredicate);
            if (!entities.isEmpty())
                entities.forEach(entity -> entity.hurt(NMLDamageTypes.getSimpleDamageSource(level, NMLDamageTypes.SPIKE_IMPALE), NMLConfig.IMPALING_DAMAGE.get().floatValue()));
        }
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallingDistance) {
        if (state.getValue(FACING) == Direction.UP && !state.getValue(POWERED))
            entity.causeFallDamage(fallingDistance + 2.0F, NMLConfig.FALLING_DAMAGE.get().floatValue(), NMLDamageTypes.getSimpleDamageSource(level, NMLDamageTypes.SPIKE_FALL));
        else super.fallOn(level, state, pos, entity, fallingDistance);
    }

    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!oldState.is(state.getBlock()) && !level.isClientSide) {
            this.checkIfPowered(level, pos, state);
        }
        if (isMoving && !state.getValue(POWERED)) {
            Predicate<LivingEntity> livingEntityPredicate = LivingEntity::isAlive;
            List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, new AABB(pos), livingEntityPredicate);
            if (!entities.isEmpty())
                entities.forEach(entity -> entity.hurt(NMLDamageTypes.getSimpleDamageSource(level, NMLDamageTypes.SPIKE_SKEWER), NMLConfig.SKEWERING_DAMAGE.get().floatValue()));
        }
    }

    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction direction) {
        return true;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return state.getValue(POWERED);
    }
}
