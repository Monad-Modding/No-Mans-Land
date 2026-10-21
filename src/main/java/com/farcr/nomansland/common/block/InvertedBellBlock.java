package com.farcr.nomansland.common.block;

import com.farcr.nomansland.common.blockentity.InvertedBellControllerBlockEntity;
import com.farcr.nomansland.common.registry.NMLBlockEntities;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import com.farcr.nomansland.common.registry.items.NMLDataComponents;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class InvertedBellBlock extends BaseEntityBlock {
    public static DirectionProperty HORIZONTAL_FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static int CONTROLLER_PART = offsetToPart(0, 0, 0); // 13
    public static IntegerProperty PART = IntegerProperty.create("part", 0, 3 * 3 * 3 - 1);

    public InvertedBellBlock(final Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(HORIZONTAL_FACING, Direction.NORTH)
                .setValue(PART, 0)
        );
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(InvertedBellBlock::new);
    }

    public static final VoxelShape[] BELL_NS = new VoxelShape[3 * 3 * 3];
    public static final VoxelShape[] BELL_EW = new VoxelShape[3 * 3 * 3];

    static {
        final VoxelShape lowerRunOuter = Block.box(-10, -16, -10, 26, -14, 26);
        final VoxelShape lowerRunInner = Block.box(-5, -17, -5, 21, -13, 21);
        final VoxelShape lowerRun = Shapes.join(lowerRunOuter, lowerRunInner, BooleanOp.ONLY_FIRST);
        final VoxelShape mainBodyOuter = Block.box(-8, -16, -8, 24, 16, 24);
        final VoxelShape mainBodyInner = Block.box(-5, -17, -5, 21, 13, 21);
        final VoxelShape mainBody = Shapes.join(mainBodyOuter, mainBodyInner, BooleanOp.ONLY_FIRST);
        final VoxelShape topPlate = Block.box(-5, 16, -5, 21, 19, 21);
        final VoxelShape ornateNS = Block.box(7, 19, -9, 9, 28, 25);
        final VoxelShape ornateEW = Block.box(-9, 19, 7, 25, 28, 9);
        final VoxelShape beamNS = Block.box(6, 28, -16, 10, 32, 32);
        final VoxelShape beamEW = Block.box(-16, 28, 6, 32, 32, 10);

        final VoxelShape fullBellNS = Shapes.or(lowerRun, mainBody, topPlate, ornateNS, beamNS);
        final VoxelShape fullBellEW = Shapes.or(lowerRun, mainBody, topPlate, ornateEW, beamEW);
        for (int x = -1; x < 2; x++) {
            for (int z = -1; z < 2; z++) {
                for (int y = -1; y < 2; y++) {
                    final int i = offsetToPart(x, y, z);
                    final VoxelShape here = Block.box(x * 16, y * 16, z * 16, x * 16 + 16, y * 16 + 16, z * 16 + 16);
                    BELL_NS[i] = Shapes.join(fullBellNS, here, BooleanOp.AND).move(-x, -y, -z);
                    BELL_EW[i] = Shapes.join(fullBellEW, here, BooleanOp.AND).move(-x, -y, -z);
                }
            }
        }
    }

    public static Vec3i partToOffset(int part) {
        int dx = part % 3 - 1;
        int dz = (part / 3) % 3 - 1;
        int dy = part / 9 - 1;
        return new Vec3i(dx, dy, dz);
    }

    public static int offsetToPart(int dx, int dy, int dz) {
        return dx + dz * 3 + dy * 9 + 13;
    }

    public static int offsetToPart(Vec3i offset) {
        return offset.getX() + offset.getZ() * 3 + offset.getY() * 9 + 13;
    }

    @Override
    protected VoxelShape getShape(final BlockState state, final BlockGetter level, final BlockPos pos, final CollisionContext context) {
        if (state.getValue(HORIZONTAL_FACING).getAxis() == Direction.Axis.X) {
            return BELL_NS[state.getValue(PART)];
        } else {
            return BELL_EW[state.getValue(PART)];
        }
    }

    @Override
    public @Nullable BlockState getStateForPlacement(final BlockPlaceContext context) {
        final BlockPos blockpos = context.getClickedPos();
        final BlockPos.MutableBlockPos mutPos = new BlockPos.MutableBlockPos();
        final Level level = context.getLevel();
        for (int x = -1; x < 2; x++) {
            for (int z = -1; z < 2; z++) {
                for (int y = 0; y < 3; y++) {
                    mutPos.setWithOffset(blockpos, x, y, z);
                    if (!level.getBlockState(mutPos).canBeReplaced(context)) {
                        return null;
                    }
                }
            }
        }

        return this.defaultBlockState().setValue(HORIZONTAL_FACING, context.getHorizontalDirection());
    }

    public static void placeBell(final BlockPos bottomCenter, final Direction facing, final LevelWriter level) {
        final BlockState baseState = NMLBlocks.INVERTED_BELL.get().defaultBlockState().setValue(HORIZONTAL_FACING, facing);
        final BlockPos.MutableBlockPos mutPos = new BlockPos.MutableBlockPos();
        for (int x = -1; x < 2; x++) {
            for (int z = -1; z < 2; z++) {
                for (int y = 0; y < 3; y++) {
                    mutPos.setWithOffset(bottomCenter, x, y, z);
                    final int i = offsetToPart(x, y-1, z);
                    level.setBlock(mutPos, baseState.setValue(PART, i), 3);
                }
            }
        }
    }

    @Override
    public void setPlacedBy(final Level level, final BlockPos pos, final BlockState state, @Nullable final LivingEntity placer, final ItemStack stack) {
        placeBell(pos, state.getValue(HORIZONTAL_FACING), level);
        if (!level.isClientSide && level.getServer() != null) {
            GlobalPos target = stack.get(NMLDataComponents.INVERTED_BELL_TARGET.get());
            if (target != null && level.getBlockEntity(pos.above()) instanceof InvertedBellControllerBlockEntity ibbe) {
                ServerLevel targetLevel = level.getServer().getLevel(target.dimension());
                if (targetLevel != null && targetLevel.getBlockEntity(target.pos()) instanceof InvertedBellControllerBlockEntity ibbe2) {
                    ibbe.link(ibbe2, true);
                    if (placer != null) {
                        placer.sendSystemMessage(Component.literal("Successfully linked bell"));
                    }
                }
            }
            stack.set(NMLDataComponents.INVERTED_BELL_TARGET.get(), null);
        }
    }

    private boolean onHit(final Level level, final BlockState state, final BlockPos pos, final Vec3 lookAt) {
        final Direction front = state.getValue(HORIZONTAL_FACING);
        if (!level.isClientSide) {
            final InvertedBellControllerBlockEntity controller = getControllerBE(level, pos, state);
            if (controller != null && controller.ringCooldown <= 0) {
                double angle = lookAt.dot(new Vec3(front.getStepX(), front.getStepY(), front.getStepZ()));
                level.blockEvent(controller.getBlockPos(), controller.getBlockState().getBlock(), 1,
                        angle < 0 ? 2 : 0 // types are an unsigned byte...
                );
                return true;
            }
        }

        return false;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (NMLBlocks.INVERTED_BELL.asItem() == stack.getItem()) {
            InvertedBellControllerBlockEntity controller = getControllerBE(level, pos, state);
            if (controller != null) {
                stack.set(NMLDataComponents.INVERTED_BELL_TARGET.get(), GlobalPos.of(level.dimension(), controller.getBlockPos()));
                return ItemInteractionResult.SUCCESS;
            }
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected InteractionResult useWithoutItem(final BlockState state, final Level level, final BlockPos pos, final Player player, final BlockHitResult hitResult) {
        if (player.getMainHandItem().isEmpty()) {
            final InvertedBellControllerBlockEntity controller = getControllerBE(level, pos, state);
            if (controller != null && controller.targetBell == null) {
                if (level instanceof ServerLevel serverLevel) {
                    controller.attemptRecheck(serverLevel);
                }
                return InteractionResult.SUCCESS;
            }
            if (this.onHit(level, state, pos, player.getLookAngle())) {
                return InteractionResult.SUCCESS;
            }
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    @Override
    protected void onProjectileHit(final Level level, final BlockState state, final BlockHitResult hit, final Projectile projectile) {
        this.onHit(level, state, hit.getBlockPos(), projectile.getDeltaMovement());
    }

    @Override
    protected void onRemove(final BlockState state, final Level level, final BlockPos pos, final BlockState newState, final boolean movedByPiston) {
        final InvertedBellControllerBlockEntity ibbe = getControllerBE(level, pos, state);
        if (ibbe != null) {
            ibbe.destroyBell();
        }

        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    public static InvertedBellControllerBlockEntity getControllerBE(final Level level, final BlockPos pos, final BlockState ownState) {
        int part = ownState.getValue(PART);
        int rx = part % 3;
        int rz = (part / 3) % 3;
        int ry = part / 9;
        if (level.getBlockEntity(pos.offset(1-rx, 1-ry, 1-rz)) instanceof final InvertedBellControllerBlockEntity ibbe) {
            return ibbe;
        }

        return null;
    }

    @Override
    protected boolean propagatesSkylightDown(final BlockState state, final BlockGetter level, final BlockPos pos) {
        return true;
    }

    @Override
    protected boolean canBeReplaced(final BlockState state, final Fluid fluid) {
        return false;
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(final Level level, final BlockState state, final BlockEntityType<T> blockEntityType) {
        if (state.getValue(PART) == CONTROLLER_PART) {
            return createTickerHelper(blockEntityType, NMLBlockEntities.INVERTED_BELL.get(), InvertedBellControllerBlockEntity::tick);
        }
        return null;
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(HORIZONTAL_FACING).add(PART));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(final BlockPos blockPos, final BlockState blockState) {
        return blockState.getValue(PART) == CONTROLLER_PART ? new InvertedBellControllerBlockEntity(blockPos, blockState) : null;
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        Direction dir = state.getValue(HORIZONTAL_FACING);
        Vec3i offset = partToOffset(state.getValue(PART));
        offset = switch (rotation) {
            case NONE -> offset;
            case CLOCKWISE_90 -> new Vec3i(-offset.getZ(), offset.getY(), offset.getX());
            case CLOCKWISE_180 -> new Vec3i(-offset.getX(), offset.getY(), -offset.getZ());
            case COUNTERCLOCKWISE_90 -> new Vec3i(offset.getZ(), offset.getY(), -offset.getX());
        };
        return state.setValue(HORIZONTAL_FACING, rotation.rotate(dir)).setValue(PART, offsetToPart(offset));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        Direction dir = state.getValue(HORIZONTAL_FACING);
        Vec3i offset = partToOffset(state.getValue(PART));
        offset = switch (mirror) {
            case NONE -> offset;
            case LEFT_RIGHT -> new Vec3i(offset.getX(), offset.getY(), -offset.getZ());
            case FRONT_BACK -> new Vec3i(-offset.getX(), offset.getY(), offset.getZ());
        };
        return state.setValue(HORIZONTAL_FACING, mirror.mirror(dir)).setValue(PART, offsetToPart(offset));
    }
}
