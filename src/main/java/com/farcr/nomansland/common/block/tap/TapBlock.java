package com.farcr.nomansland.common.block.tap;

import com.farcr.nomansland.common.block.cauldrons.FourLayeredCauldronBlock;
import com.farcr.nomansland.common.blockentity.TapBlockEntity;
import com.farcr.nomansland.common.registry.NMLBlockEntities;
import com.farcr.nomansland.common.registry.NMLRegistries;
import com.farcr.nomansland.common.registry.NMLSounds;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static net.minecraft.world.level.block.LayeredCauldronBlock.LEVEL;

public class TapBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    private static final Map<Direction, VoxelShape> AABBS = Maps.newEnumMap(ImmutableMap.of(
            Direction.NORTH, Block.box(6.0D, 3.0D, 10.0D, 10.0D, 8.0D, 16.0D),
            Direction.SOUTH, Block.box(6.0D, 3.0D, 0.0D, 10.0D, 8.0D, 6.0D),
            Direction.WEST, Block.box(10.0D, 3.0D, 6.0D, 16.0D, 8.0D, 10.0D),
            Direction.EAST, Block.box(0.0D, 3.0D, 6.0D, 6.0D, 8.0D, 10.0D)
    ));

    public static final BooleanProperty CLOSED = BooleanProperty.create("closed");

    public TapBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(TapBlock::new);
    }

    public static VoxelShape getShape(BlockState state) {
        return AABBS.get(state.getValue(FACING));
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    public static BlockState getBlockStateBehind(Level level, BlockPos pos, BlockState state) {
        return level.getBlockState(pos.relative(state.getValue(FACING).getOpposite()));
    }

    public static BlockPos getCauldronPos(Level level, BlockPos tapPos) {
        // Get the position and state of the closest cauldron downwards (up to a max of 3 blocks down)
        for (int i = 0; i <= 3; i++) {
            BlockPos posBelow = tapPos.below(i);
            BlockState stateBelow = level.getBlockState(posBelow);
            if (stateBelow.getBlock() instanceof AbstractCauldronBlock) {
                return posBelow;
            } else if (stateBelow.isCollisionShapeFullBlock(level, posBelow)) return null;
        }
        return null;
    }

    public static void spawnDrippingParticles(Level level, BlockPos pos, BlockState state, ParticleType<?> particleType) {
        double x = pos.getX();
        double y = pos.getY();
        double z = pos.getZ();
        switch (state.getValue(FACING)) {
            case NORTH: {
                x = (double) pos.getX() + 0.5;
                y = (double) ((float) (pos.getY() + 1) - 0.75F) - 0.09;
                z = (double) pos.getZ() + 0.68;
                break;
            }
            case SOUTH: {
                x = (double) pos.getX() + 0.5;
                y = (double) ((float) (pos.getY() + 1) - 0.75F) - 0.09;
                z = (double) pos.getZ() + 0.32;
                break;
            }
            case EAST: {
                x = (double) pos.getX() + 0.32;
                y = (double) ((float) (pos.getY() + 1) - 0.75F) - 0.09;
                z = (double) pos.getZ() + 0.5;
                break;
            }
            case WEST: {
                x = (double) pos.getX() + 0.68;
                y = (double) ((float) (pos.getY() + 1) - 0.75F) - 0.09;
                z = (double) pos.getZ() + 0.5;
                break;
            }
        }

        if (level.isClientSide) level.addParticle((ParticleOptions) particleType, x, y, z, 0, 0,0);
        else ((ServerLevel) (level)).sendParticles((ParticleOptions) particleType, x, y, z, 1, 0, 0,0,0);
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState blockstate = this.defaultBlockState();
        LevelReader levelreader = context.getLevel();
        BlockPos blockpos = context.getClickedPos();
        Direction[] adirection = context.getNearestLookingDirections();

        blockstate = blockstate.setValue(CLOSED, false);

        for (Direction direction : adirection) {
            if (direction.getAxis().isHorizontal()) {
                Direction direction1 = direction.getOpposite();
                blockstate = blockstate.setValue(FACING, direction1);
                if (blockstate.canSurvive(levelreader, blockpos)) {
                    return blockstate;
                }
            }
        }

        return null;
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        builder.add(CLOSED);
    }

    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction direction = state.getValue(FACING).getOpposite();
        BlockPos posBehind = pos.relative(state.getValue(FACING).getOpposite());
        BlockState stateBehind = level.getBlockState(posBehind);
        return MultifaceBlock.canAttachTo(level, direction, posBehind, stateBehind) || stateBehind.getBlock() instanceof AbstractCauldronBlock;
    }

    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        return facing.getOpposite() == state.getValue(FACING) && !state.canSurvive(level, currentPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if(state.getValue(CLOSED)) return;

        BlockPos cauldronPos = getCauldronPos(level, pos);
        if (cauldronPos == null) return;
        BlockState cauldronState = level.getBlockState(cauldronPos);

        BlockState stateBehind = getBlockStateBehind(level, pos, state);
        List<Holder.Reference<TapInteraction>> allTapInteractions = level.registryAccess().registryOrThrow(NMLRegistries.TAP_INTERACTION_KEY).holders().filter(tapInteractionReference -> tapInteractionReference.value().particleType().isPresent()).toList();

        for (Holder.Reference<TapInteraction> tapInteractionReference : allTapInteractions) {
            boolean hasBlock = false;
            for (BlockStateProvider blockStateProvider : tapInteractionReference.value().sources()) {
                BlockState neededState = blockStateProvider.getState(level.random, pos.relative(state.getValue(FACING).getOpposite()));
                if (stateBehind == neededState) {
                    if (neededState.is(BlockTags.LOGS)) {
                        if (stateBehind == getBlockStateBehind(level, pos.above(), state) && stateBehind == getBlockStateBehind(level, pos.below(), state))
                            hasBlock = true;
                    } else hasBlock = true;
                    break;
                }
            }

            if (hasBlock && random.nextFloat() < 0.05F * tapInteractionReference.value().rate()) {
                tryFill(cauldronState, cauldronPos, level, tapInteractionReference.value());
                break;
            }
        }
    }

    public static void tryFill(BlockState cauldronState, BlockPos cauldronPos, Level level, TapInteraction tapInteraction) {
        if (cauldronState.getBlock() == tapInteraction.cauldron() && cauldronState.getBlock() instanceof AbstractCauldronBlock cauldron && !cauldron.isFull(cauldronState)) {
            BlockState newState;

            if (cauldronState.getBlock() instanceof FourLayeredCauldronBlock)
                newState = cauldronState.setValue(FourLayeredCauldronBlock.LEVEL, cauldronState.getValue(FourLayeredCauldronBlock.LEVEL) + 1);
            else
                newState = cauldronState.setValue(LEVEL, cauldronState.getValue(LEVEL) + 1);

            level.setBlockAndUpdate(cauldronPos, newState);
            level.gameEvent(GameEvent.BLOCK_CHANGE, cauldronPos, GameEvent.Context.of(newState));
        } else if (cauldronState.is(Blocks.CAULDRON)) {
            level.setBlockAndUpdate(cauldronPos, tapInteraction.cauldron().defaultBlockState());
            level.gameEvent(GameEvent.BLOCK_CHANGE, cauldronPos, GameEvent.Context.of(tapInteraction.cauldron().defaultBlockState()));
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if(state.getValue(CLOSED)) return;

        List<Holder.Reference<TapInteraction>> allTapInteractions = level.registryAccess().registryOrThrow(NMLRegistries.TAP_INTERACTION_KEY).holders().filter(tapInteractionReference -> tapInteractionReference.value().particleType().isPresent()).toList();
        BlockState stateBehind = getBlockStateBehind(level, pos, state);

        for (Holder.Reference<TapInteraction> tapInteractionReference : allTapInteractions) {
            boolean hasBlock = false;
            for (BlockStateProvider blockStateProvider : tapInteractionReference.value().sources()) {
                BlockState neededState = blockStateProvider.getState(level.random, pos.relative(state.getValue(FACING).getOpposite()));
                if (stateBehind == neededState) {
                    if (neededState.is(BlockTags.LOGS)) {
                        if (stateBehind == getBlockStateBehind(level, pos.above(), state) && stateBehind == getBlockStateBehind(level, pos.below(), state))
                            hasBlock = true;
                    } else hasBlock = true;
                    break;
                }
            }

            if (hasBlock && random.nextFloat() < 0.05F) {
                spawnDrippingParticles(level, pos, state, (SimpleParticleType) tapInteractionReference.value().particleType().get());
                break;
            }
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TapBlockEntity(pos, state);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntity) {
        return level.isClientSide ? null : createTickerHelper(blockEntity, NMLBlockEntities.TAP.get(), TapBlockEntity::tick);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            this.toggle(state, level, pos, null);
            return InteractionResult.CONSUME;
        }
    }

    @Override
    protected void onExplosionHit(BlockState state, Level level, BlockPos pos, Explosion explosion, BiConsumer<ItemStack, BlockPos> dropConsumer) {
        if (explosion.canTriggerBlocks()) {
            this.toggle(state, level, pos, null);
        }

        super.onExplosionHit(state, level, pos, explosion, dropConsumer);
    }

    public void toggle(BlockState state, Level level, BlockPos pos, @Nullable Player player) {
        state = state.cycle(CLOSED);
        level.setBlock(pos, state, 3);
        playSound(player, level, pos, state);
        level.gameEvent(player, state.getValue(CLOSED) ? GameEvent.BLOCK_DEACTIVATE : GameEvent.BLOCK_ACTIVATE, pos);
    }

    protected static void playSound(@Nullable Player player, LevelAccessor level, BlockPos pos, BlockState state) {
        float f = state.getValue(CLOSED) ? 0.6F : 0.5F;
        level.playSound(player, pos, NMLSounds.TAP_TURNS.get(), SoundSource.BLOCKS, 0.3F, f);
    }
}
