package com.farcr.nomansland.common.block;

import com.mojang.serialization.MapCodec;
import com.farcr.nomansland.common.block.torches.ExtinguishableBlockPairing;
import com.farcr.nomansland.common.blockentity.AncientBronzeBellBlockEntity;
import com.farcr.nomansland.common.blockentity.PotBlockEntity;
import com.farcr.nomansland.common.registry.NMLBlockEntities;
import com.farcr.nomansland.common.registry.NMLRegistries;
import com.farcr.nomansland.common.registry.NMLSounds;
import net.mehvahdjukaar.moonlight.api.block.ILightable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

public class AncientBronzeBellBlock extends BaseEntityBlock {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty ATTACHED = BlockStateProperties.ATTACHED;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    private static final int RADIUS = 7;

    private static final VoxelShape BODY = Shapes.or(
            shape(4, 4, 4, 12, 14, 12),
            shape(3, 2, 3, 13, 4, 13),
            shape(7, 2, 7, 9, 10, 9),
            shape(7, 16, 7, 9, 18, 9));

    private static final VoxelShape BAR = Shapes.or(
            shape(0, 12, 6, 2, 16, 10),
            shape(14, 12, 6, 16, 16, 10),
            shape(2, 12, 7, 14, 16, 9));

    private static final VoxelShape POSTS = Shapes.or(
            shape(0, 0, 6, 2, 12, 10),
            shape(14, 0, 6, 16, 12, 10));

    private static final Map<Direction, VoxelShape> FLOOR_SHAPES =
            byFacing(Shapes.or(BODY, BAR, POSTS));
    private static final Map<Direction, VoxelShape> WALL_SHAPES =
            byFacing(Shapes.or(BODY, BAR));

    public AncientBronzeBellBlock(final Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(ATTACHED, false)
                .setValue(WATERLOGGED, false)
                .setValue(POWERED, false));
    }

    public static final MapCodec<AncientBronzeBellBlock> CODEC = simpleCodec(AncientBronzeBellBlock::new);
    private static final int RING_EVENT = 1;

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ATTACHED, WATERLOGGED, POWERED);
    }

    @Override
    protected VoxelShape getShape(final BlockState state, final BlockGetter level, final BlockPos pos,
                                  final CollisionContext context) {
        final Direction facing = state.getValue(FACING);
        return state.getValue(ATTACHED)
                ? WALL_SHAPES.get(facing)
                : FLOOR_SHAPES.get(facing);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context) {
        final Direction clicked = context.getClickedFace();
        final BlockPos pos = context.getClickedPos();
        final LevelReader level = context.getLevel();
        final boolean waterlogged = level.getFluidState(pos).getType() == Fluids.WATER;
        if (clicked.getAxis() == Direction.Axis.Y) {
            final BlockState floor = defaultBlockState()
                    .setValue(ATTACHED, false)
                    .setValue(WATERLOGGED, waterlogged)
                    .setValue(FACING, context.getHorizontalDirection());
            return floor.canSurvive(level, pos) ? floor : null;
        }
        final BlockState wall = defaultBlockState()
                .setValue(ATTACHED, true)
                .setValue(WATERLOGGED, waterlogged)
                .setValue(FACING, clicked.getCounterClockWise());
        return wall.canSurvive(level, pos) ? wall : null;
    }

    @Override
    protected boolean canSurvive(final BlockState state, final LevelReader level, final BlockPos pos) {
        if (state.getValue(ATTACHED)) {
            final Direction along = state.getValue(FACING);
            final Direction supportDirection = along.getClockWise();
            return sturdy(level, pos, supportDirection) && sturdy(level, pos, supportDirection.getOpposite());
        }
        final BlockPos below = pos.below();
        return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
    }

    @Override
    protected InteractionResult useWithoutItem(final BlockState state, final Level level, final BlockPos pos, final Player player, final BlockHitResult hit) {
        Direction facing = state.getValue(FACING);
        Direction hitDirection = hit.getDirection();
        if (hitDirection != facing && hitDirection != facing.getOpposite()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            ring(level, pos, state, hitDirection);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AncientBronzeBellBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, NMLBlockEntities.ANCIENT_BRONZE_BELL.get(), AncientBronzeBellBlockEntity::tick);
    }

    @Override
    public boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int data) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity != null && blockEntity.triggerEvent(id, data);
    }

    @Override
    protected void onProjectileHit(Level level, BlockState state, BlockHitResult hit, Projectile projectile) {
        Direction facing = state.getValue(FACING);
        Direction hitDirection = hit.getDirection();
        if (!level.isClientSide && (hitDirection == facing || hitDirection == facing.getOpposite())) {
            ring(level, hit.getBlockPos(), state, hitDirection);
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        boolean powered = level.hasNeighborSignal(pos);
        if (powered != state.getValue(POWERED)) {
            if (powered) {
                ring(level, pos, state, state.getValue(FACING));
            }
            level.setBlock(pos, state.setValue(POWERED, powered), 3);
        }
    }

    private void ring(Level level, BlockPos pos, BlockState state, Direction direction) {
        level.blockEvent(pos, this, RING_EVENT, direction.get3DDataValue());
        if (!state.getValue(WATERLOGGED)) {
            level.playSound(null, pos, SoundEvents.BELL_BLOCK, SoundSource.BLOCKS, 2.0F, 0.5F);
        }
        wakeNearbyLivingPots(level, pos);
        toggleNearbyLights(level, pos);
    }

    private static void wakeNearbyLivingPots(Level level, BlockPos center) {
        if (level.isClientSide) return;

        BlockPos.betweenClosed(center.offset(-RADIUS, -RADIUS, -RADIUS), center.offset(RADIUS, RADIUS, RADIUS)).forEach(pos -> {
            int dx = pos.getX() - center.getX();
            int dy = pos.getY() - center.getY();
            int dz = pos.getZ() - center.getZ();
            if (dx * dx + dy * dy + dz * dz > RADIUS * RADIUS) return;

            if (level.getBlockEntity(pos) instanceof PotBlockEntity pot && pot.isLiving()) {
                pot.wakeUp(null);
            }
        });
    }

    private static void toggleNearbyLights(Level level, BlockPos center) {
        BlockPos.betweenClosed(center.offset(-RADIUS, -RADIUS, -RADIUS), center.offset(RADIUS, RADIUS, RADIUS)).forEach(pos -> {
            int dx = pos.getX() - center.getX();
            int dy = pos.getY() - center.getY();
            int dz = pos.getZ() - center.getZ();
            if (dx * dx + dy * dy + dz * dz > RADIUS * RADIUS) return;

            BlockState target = level.getBlockState(pos);
            for (ExtinguishableBlockPairing pairing : NMLRegistries.EXTINGUISHABLE_BLOCKS) {
                if (pairing.isLitVersion(target)) {
                    playExtinguishSound(level, pos);
                    level.gameEvent(null, GameEvent.BLOCK_CHANGE, pos);
                    level.setBlockAndUpdate(pos, pairing.extinguishedBlock().withPropertiesOf(target));
                    return;
                }
                if (pairing.canRelight(target)) {
                    playLightSound(level, pos);
                    level.gameEvent(null, GameEvent.BLOCK_CHANGE, pos);
                    level.setBlockAndUpdate(pos, pairing.litBlock().withPropertiesOf(target));
                    return;
                }
            }

            if (target.getBlock() instanceof ILightable lightable) {
                if (lightable.isLitUp(target, level, pos)) {
                    lightable.tryExtinguish(null, target, pos, level);
                } else {
                    lightable.tryLightUp(null, target, pos, level, ILightable.FireSoundType.FLINT_AND_STEEL);
                }
                return;
            }

            if (target.is(BlockTags.CANDLES) || target.is(BlockTags.CANDLE_CAKES)) {
                if (AbstractCandleBlock.isLit(target)) {
                    level.setBlock(pos, target.setValue(BlockStateProperties.LIT, false), 11);
                    level.gameEvent(null, GameEvent.BLOCK_CHANGE, pos);
                    playExtinguishSound(level, pos);
                    if (level instanceof ServerLevel serverLevel) {
                        int particleCount = target.hasProperty(BlockStateProperties.CANDLES)
                                ? target.getValue(BlockStateProperties.CANDLES)
                                : 1;
                        serverLevel.sendParticles(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5,
                                particleCount, 0.15, 0.05, 0.15, 0.01);
                    }
                } else if (!target.getValue(BlockStateProperties.LIT)
                        && (!target.hasProperty(BlockStateProperties.WATERLOGGED)
                        || !target.getValue(BlockStateProperties.WATERLOGGED))) {
                    playLightSound(level, pos);
                    level.setBlock(pos, target.setValue(BlockStateProperties.LIT, true), 11);
                    level.gameEvent(null, GameEvent.BLOCK_CHANGE, pos);
                }
            } else if (target.is(BlockTags.CAMPFIRES) && target.hasProperty(BlockStateProperties.LIT)) {
                if (target.getValue(BlockStateProperties.LIT)) {
                    playExtinguishSound(level, pos);
                    CampfireBlock.dowse(null, level, pos, target);
                    level.setBlock(pos, target.setValue(BlockStateProperties.LIT, false), 3);
                } else if (CampfireBlock.canLight(target)) {
                    playLightSound(level, pos);
                    level.setBlock(pos, target.setValue(BlockStateProperties.LIT, true), 11);
                    level.gameEvent(null, GameEvent.BLOCK_CHANGE, pos);
                }
            }
        });
    }

    private static void playExtinguishSound(Level level, BlockPos pos) {
        level.playSound(null, pos, NMLSounds.TORCH_EXTINGUISH.get(), SoundSource.BLOCKS, 0.4F, 1.0F);
    }

    private static void playLightSound(Level level, BlockPos pos) {
        level.playSound(null, pos, NMLSounds.TORCH_LIGHT.get(), SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);
    }

    private static boolean sturdy(final LevelReader level, final BlockPos pos, final Direction side) {
        final BlockPos support = pos.relative(side);
        return level.getBlockState(support).isFaceSturdy(level, support, side.getOpposite());
    }

    @Override
    protected BlockState updateShape(final BlockState state, final Direction direction, final BlockState neighbour, final net.minecraft.world.level.LevelAccessor level, final BlockPos pos, final BlockPos neighbourPos) {
        if (!canSurvive(state, level, pos)) {
            return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        }
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, direction, neighbour, level, pos, neighbourPos);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    private static VoxelShape shape(final double x1, final double y1, final double z1,
                                  final double x2, final double y2, final double z2) {
        return Shapes.box(x1 / 16.0, y1 / 16.0, z1 / 16.0, x2 / 16.0, y2 / 16.0, z2 / 16.0);
    }

    private static Map<Direction, VoxelShape> byFacing(final VoxelShape north) {
        final Map<Direction, VoxelShape> shapes = new EnumMap<>(Direction.class);
        for (final Direction facing : Direction.Plane.HORIZONTAL) {
            shapes.put(facing, rotate(north, facing));
        }
        return shapes;
    }

    private static VoxelShape rotate(final VoxelShape shape, final Direction facing) {
        VoxelShape turned = shape;
        for (int step = 0; step < switch (facing) {
            case EAST -> 1;
            case SOUTH -> 2;
            case WEST -> 3;
            default -> 0;
        }; step++) {
            VoxelShape next = Shapes.empty();
            for (final var part : turned.toAabbs()) {
                next = Shapes.or(next, Shapes.box(
                        1.0 - part.maxZ, part.minY, part.minX,
                        1.0 - part.minZ, part.maxY, part.maxX));
            }
            turned = next;
        }
        return turned;
    }

    @Override
    protected BlockState rotate(final BlockState state, final Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(final BlockState state, final Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
}
