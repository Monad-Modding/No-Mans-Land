package com.farcr.nomansland.common.block.moonlight;

import com.mojang.serialization.MapCodec;
import com.farcr.nomansland.client.renderer.FriendMoonRenderer;
import com.farcr.nomansland.common.friend.FriendMoon;
import com.farcr.nomansland.common.networking.ClientboundCandleLightPacket;
import com.farcr.nomansland.common.registry.NMLParticleTypes;
import com.mojang.math.Axis;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.AbstractCandleBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.function.ToIntFunction;

public class MoonlightCandleBlock extends Block implements SimpleWaterloggedBlock {

    public static final BooleanProperty CANDLE_LIT = AbstractCandleBlock.LIT;
    public static final int LIGHT_LEVEL = 12;

    public static final ToIntFunction<BlockState> LIGHT_EMISSION = (blockState) -> (blockState.getValue(CANDLE_LIT) ? LIGHT_LEVEL : 0);

    protected static final VoxelShape CANDLE_SHAPE = Block.box(6, 0, 6, 10, 9, 10);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public MoonlightCandleBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(
            this.stateDefinition.any().setValue(CANDLE_LIT, false)
                .setValue(WATERLOGGED, false)
        );
    }

    @Override
    public MapCodec<MoonlightCandleBlock> codec() {
        return simpleCodec(MoonlightCandleBlock::new);
    }

    public void lightSpark(BlockState state, Level level, BlockPos pos, RandomSource random) {
        level.setBlock(pos, state.setValue(MoonlightCandleBlock.CANDLE_LIT, true), 3);
        triggerSparkAnimation(state, level, pos, random);
    }

    public void triggerSparkAnimation(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (level.isClientSide)
            lightSparkAnimation(state, level, pos, random);
        else PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) level, new ChunkPos(pos), new ClientboundCandleLightPacket(pos));
    }

    public void lightSparkAnimation(BlockState state, Level level, BlockPos pos, RandomSource random) {
        Vec3 offset = pos.getCenter().add(state.getOffset(level, pos));
        for (int i = 0; i < random.nextInt(4, 8); i++) {
            level.addParticle(
                NMLParticleTypes.MOONLIGHT_SPARK.get(),
                offset.x, offset.y + 0.5f, offset.z,
                0f, 0f, 0f
            );
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (level.isClientSide() && state.getValue(CANDLE_LIT)) {
            Vec3 offset = pos.getCenter().add(state.getOffset(level, pos));
            float f = random.nextFloat();
            if (f < 0.4F) {
                FriendMoonRenderer renderer = FriendMoonRenderer.getInstance();
                Quaternionf rotationQuaternion = Axis.YP.rotationDegrees(renderer.friendMoonYawAngle)
                    .mul(Axis.XP.rotationDegrees(renderer.friendMoonPitchAngle));

                Vector3f worldPosition = new Vector3f(0f, FriendMoonRenderer.MOON_DISTANCE, 0f).rotate(rotationQuaternion);
                Vec3 directionCandle = new Vec3(worldPosition.normalize().mul(0.1f * renderer.getFriendMoonOpacity()));

                level.addParticle(
                    ParticleTypes.SMOKE,
                    offset.x, offset.y + 0.75f, offset.z,
                    directionCandle.x, directionCandle.y, directionCandle.z
                );
                if (f < 0.125F) {
                    level.playLocalSound(offset.x + 0.5F, offset.y + 0.5F, offset.z + 0.5F,
                        SoundEvents.CANDLE_AMBIENT, SoundSource.BLOCKS,
                        1.0F + random.nextFloat(), (random.nextFloat() * 0.7F) + 0.3F, false);
                }
            }
            f = random.nextFloat();
            if (f < 0.5f) {
                level.addParticle(
                    NMLParticleTypes.MOONLIGHT_FLAME.get(),
                    offset.x, offset.y + .275f, offset.z,
                    0f, 0f, 0f
                );
            }
        }
    }

    public void extinguish(Player player, BlockState state, Level level, BlockPos pos) {
        level.setBlock(pos, state.setValue(CANDLE_LIT, false), 11);
        level.playSound(null, pos, SoundEvents.CANDLE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
        if (level.isClientSide()) {
            for (int i = 0; i < 7; i++) {
                Vec3 offset = pos.getCenter().add(state.getOffset(level, pos));
                level.addParticle(
                    ParticleTypes.SMOKE,
                    offset.x, offset.y + 0.25f, offset.z,
                    0F, 0F, 0F
                );
            }
            lightSparkAnimation(state, level, pos, level.random);
        }
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(
        ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult
    ) {
        if (stack.isEmpty() && player.getAbilities().mayBuild && state.getValue(CANDLE_LIT)) {
            if (!isCommuning(level, player))
                return ItemInteractionResult.FAIL;
            extinguish(player, state, level, pos);
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    private static boolean isCommuning(Level level, Player player) {
        if (level.isClientSide) {
            BlockPos basinPos = FriendMoonRenderer.getInstance().clientBlockPos;
            return basinPos != null && FriendMoon.appearConditionsMet(player, basinPos);
        }
        if (player instanceof ServerPlayer serverPlayer)
            return FriendMoon.getOrDefault(serverPlayer.server.overworld()).playerHasFriendship(serverPlayer);
        return false;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED).add(CANDLE_LIT);
    }

    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Vec3 vec3 = state.getOffset(level, pos);
        return CANDLE_SHAPE.move(vec3.x, vec3.y, vec3.z);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        if (state.getValue(WATERLOGGED))
            level.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }
}