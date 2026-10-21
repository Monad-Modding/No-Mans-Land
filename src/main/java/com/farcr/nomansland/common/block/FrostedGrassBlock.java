package com.farcr.nomansland.common.block;

import net.minecraft.world.level.gameevent.GameEvent;
import com.farcr.nomansland.NMLConfig;
import com.farcr.nomansland.common.integration.Mods;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import static net.minecraft.world.level.block.SnowyDirtBlock.SNOWY;

public class FrostedGrassBlock extends BushBlock implements BonemealableBlock {
    public static final BooleanProperty SNOWLOGGED = BooleanProperty.create("snowlogged");

    protected static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 13.0, 14.0);
    protected static final VoxelShape SNOWLOGGED_SHAPE = Shapes.or(SHAPE, Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0));

    public FrostedGrassBlock(Properties properties) {
        super(properties);
        registerDefaultState(this.stateDefinition.any().setValue(SNOWLOGGED, Boolean.FALSE));
    }

    @Override
    protected MapCodec<? extends BushBlock> codec() {
        return simpleCodec(FrostedGrassBlock::new);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (player.getMainHandItem().is(Blocks.SNOW.asItem()) && !state.getValue(SNOWLOGGED) && NMLConfig.GRASS_FROSTING.get() && !Mods.SNOWREALMAGIC.isLoaded()) {
            BlockState frosted = state.setValue(SNOWLOGGED, true);
            level.setBlockAndUpdate(pos, frosted);
            stack.consume(1, player);
            level.playSound(player, pos, SoundEvents.SNOW_PLACE, SoundSource.PLAYERS, 1.0F, (level.random.nextFloat() - level.random.nextFloat()) * 0.6F + 1.2F);
            level.gameEvent(GameEvent.BLOCK_PLACE, pos, GameEvent.Context.of(player, frosted));
            BlockPos posBelow = pos.below();
            BlockState stateUnder = level.getBlockState(posBelow);
            if (stateUnder.getBlock() instanceof SnowyDirtBlock)
                level.setBlockAndUpdate(posBelow, stateUnder.setValue(SNOWY, true));
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected boolean canBeReplaced(BlockState state, BlockPlaceContext useContext) {
        if (useContext.getItemInHand().is(Blocks.SNOW.asItem()) || useContext.getItemInHand().is(Blocks.SHORT_GRASS.asItem())) {
            return false;
        }
        return super.canBeReplaced(state, useContext);
    }

    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Vec3 offset = state.getOffset(level, pos);
        return (state.getValue(SNOWLOGGED) ? SNOWLOGGED_SHAPE : SHAPE).move(offset.x, offset.y, offset.z);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SNOWLOGGED);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel serverLevel, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
        int x = blockPos.getX();
        int y = blockPos.getY();
        int z = blockPos.getZ();

        for (BlockPos pos : BlockPos.betweenClosed(x - 3, y - 1, z - 3, x + 3, y + 2, z + 3)) {
            if (randomSource.nextFloat() < 0.3F && blockState.canSurvive(serverLevel, pos) && serverLevel.isEmptyBlock(pos)) {
                serverLevel.setBlockAndUpdate(pos, NMLBlocks.GRASS_SPROUTS.block().defaultBlockState());
            }
        }
    }
}
