package com.farcr.nomansland.common.block.moonlight;

import com.farcr.nomansland.common.blockentity.MoonlightBasinBlockEntity;
import com.farcr.nomansland.common.registry.NMLBlockEntities;
import com.mojang.math.OctahedralGroup;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Vector3f;

import java.util.List;

public class MoonlightBasinBlock extends BaseEntityBlock implements SimpleWaterloggedBlock
{

	@Nullable @Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
		return createTickerHelper(blockEntityType, NMLBlockEntities.MOONLIGHT_BASIN.get(), MoonlightBasinBlockEntity::tick);
	}

	public static final VoxelShape BASIN_CENTER = Block.box(0, 4, 0, 16, 12, 16);
	public static final VoxelShape BASIN_TOP = Block.box(0, 12, 0, 16, 16, 4);
	public static final VoxelShape BASIN_BOTTOM = Block.box(0, 0, 4, 16, 4, 16);

	public static final int MULTIBLOCK_SIZE = 3;
	public static final int MULTIBLOCK_CENTER = ((MULTIBLOCK_SIZE * MULTIBLOCK_SIZE) / 2);

	/*
	 * Apologies for this UNHOLY code
	 */
	public static VoxelShape rotateBoundingBox(VoxelShape baseShape, int times) {
		List<AABB> boxes = baseShape.toAabbs();
		VoxelShape rotatedShape = Shapes.empty();

		for (AABB box : boxes) {
			double minX = box.minX;
			double minY = box.minY;
			double minZ = box.minZ;
			double maxX = box.maxX;
			double maxY = box.maxY;
			double maxZ = box.maxZ;

			for (int i = 0; i < times; i++) {
				double rMinX = 1.0 - maxZ;
				double rMinZ = minX;
				double rMaxX = 1.0 - minZ;
				double rMaxZ = maxX;

				minX = Math.min(rMinX, rMaxX);
				maxX = Math.max(rMinX, rMaxX);
				minZ = Math.min(rMinZ, rMaxZ);
				maxZ = Math.max(rMinZ, rMaxZ);
			}

			if (minX >= maxX || minZ >= maxZ || minY >= maxY)
				continue;

			rotatedShape = Shapes.or(rotatedShape, Shapes.box(minX, minY, minZ, maxX, maxY, maxZ));
		}
		return rotatedShape;
	}

	public static final VoxelShape BASIN_RIDGE_TOP = Shapes.or(BASIN_TOP, BASIN_CENTER, BASIN_BOTTOM);

	public static final VoxelShape BASIN_TOP_LEFT = Block.box(0, 12, 0, 4, 16, 16);
	public static final VoxelShape BASIN_RIDGE_TOP_LEFT = Shapes.or(
			BASIN_TOP, BASIN_CENTER, BASIN_TOP_LEFT, Block.box(4, 0, 4, 16, 4, 16)
	);
	public static final VoxelShape BASIN_CENTER_BOTTOM = Block.box(0, 0, 0, 16, 4, 16);
	public static final VoxelShape BASIN_CENTER_COMPOSITE = Shapes.or(BASIN_CENTER, BASIN_CENTER_BOTTOM);

	public static final VoxelShape[] VOXEL_SHAPE_MAP = new VoxelShape[]{
			BASIN_RIDGE_TOP_LEFT, BASIN_RIDGE_TOP, rotateBoundingBox(BASIN_RIDGE_TOP_LEFT, 1),
			rotateBoundingBox(BASIN_RIDGE_TOP, 3), BASIN_CENTER_COMPOSITE, rotateBoundingBox(BASIN_RIDGE_TOP, 1),
			rotateBoundingBox(BASIN_RIDGE_TOP_LEFT, 3), rotateBoundingBox(BASIN_RIDGE_TOP, 2), rotateBoundingBox(BASIN_RIDGE_TOP_LEFT, 2)
	};

	public static final IntegerProperty PART = IntegerProperty.create("part", 0, 8);
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

	public MoonlightBasinBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(
				this.stateDefinition.any()
                    .setValue(PART, MULTIBLOCK_CENTER)
                    .setValue(WATERLOGGED, false)
                    .setValue(MoonlightCandleBlock.CANDLE_LIT, false)
		);
	}
	public static final MapCodec<MoonlightBasinBlock> CODEC = simpleCodec(MoonlightBasinBlock::new);
	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}

	@Nullable
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockPos blockpos = context.getClickedPos();
		Level level = context.getLevel();
		for (int i = 0; i < MULTIBLOCK_SIZE; i++) {
			for (int j = 0; j < MULTIBLOCK_SIZE; j++) {
				BlockPos newPosition = blockpos.offset(new Vec3i(i - 1, 0, j - 1));
				if (!level.getBlockState(newPosition).canBeReplaced(context))
					return null;
			}
		}
		return this.defaultBlockState();
	}

    public static float calculateToCenter() {
        return (MULTIBLOCK_SIZE - 1) / 2f;
    }

	public static BlockPos calculateCenterPosition(BlockPos pos, BlockState state) {
		int position = state.getValue(PART);
		int i = (position % MULTIBLOCK_SIZE);
		int j = (position / MULTIBLOCK_SIZE);
		return pos.offset(new Vec3i((int) (calculateToCenter() - i), 0, (int) (calculateToCenter() - j)));
	}

	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		BlockPos centerPosition = calculateCenterPosition(pos, state);
		for (int i = 0; i < MULTIBLOCK_SIZE; i++) {
			for (int j = 0; j < MULTIBLOCK_SIZE; j++) {
				BlockPos newPosition = centerPosition.offset(new Vec3i(i - 1, 0, j - 1));
				if (!newPosition.equals(pos)) {
					level.setBlock(newPosition, level.getFluidState(newPosition).createLegacyBlock(), 35);
					level.levelEvent(player, 2001, newPosition, Block.getId(state));
				}
			}
		}
		return super.playerWillDestroy(level, pos, state, player);
	}

	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		for (int i = 0; i < MULTIBLOCK_SIZE; i++) {
			for (int j = 0; j < MULTIBLOCK_SIZE; j++) {
				BlockPos newPosition = pos.offset(new Vec3i(i - 1, 0, j - 1));
				if (!newPosition.equals(pos))
					level.setBlock(newPosition, (BlockState) state.setValue(PART, ((j * 3) + i)), 3);
			}
		}
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(WATERLOGGED).add(PART).add(MoonlightCandleBlock.CANDLE_LIT);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return VOXEL_SHAPE_MAP[state.getOptionalValue(PART).orElse(MULTIBLOCK_CENTER)];
	}

    /*
    * Meant to use this as a generalized function for applying octahedrals but
    * im probably doing something wrong because clockwise and counter clockwise 90 degree
    * doesnt work and its pissing me off so I just threw together something similar to the
    * way the bounding box rotates
    */
    public BlockState applyOctahedral(BlockState blockState, OctahedralGroup octahedralGroup) {
        int position = blockState.getValue(PART);
        int x = (position % MULTIBLOCK_SIZE);
        int z = (position / MULTIBLOCK_SIZE);

        float toCenter = calculateToCenter();
        float centeredX = x - toCenter;
        float centeredZ = z - toCenter;

        Matrix3f transform = octahedralGroup.transformation();
        Vector3f vec = new Vector3f(centeredX, 0f, centeredZ)
            .mul(transform);
        vec.add(toCenter, 0f, toCenter);

        return blockState.setValue(PART, (int) (vec.z() * MULTIBLOCK_SIZE + vec.x()));
    }

    // see above, code pisses me off but it works, whatever
    @Override
    protected @NotNull BlockState rotate(BlockState state, Rotation rotation) {
        int position = state.getValue(PART);
        int x = (position % MULTIBLOCK_SIZE);
        int z = (position / MULTIBLOCK_SIZE);

        int toCenter = (int) calculateToCenter();
        int centeredX = x - toCenter;
        int centeredZ = z - toCenter;

        int rotatedX = centeredX;
        int rotatedZ = centeredZ;
        switch (rotation) {
            case CLOCKWISE_90 -> {
                rotatedX = -centeredZ;
                rotatedZ = centeredX;
            }
            case CLOCKWISE_180 -> {
                rotatedX = -centeredX;
                rotatedZ = -centeredZ;
            }
            case COUNTERCLOCKWISE_90 -> {
                rotatedX = centeredZ;
                rotatedZ = -centeredX;
            }
        }
        rotatedX += toCenter;
        rotatedZ += toCenter;
        return state.setValue(PART, (rotatedZ * MULTIBLOCK_SIZE) + rotatedX);
    }

    @Override
    protected @NotNull BlockState mirror(BlockState state, Mirror mirror) {
        return applyOctahedral(state, mirror.rotation());
    }

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		if (state.getValue(PART) == MULTIBLOCK_CENTER)
			return RenderShape.MODEL;
		return RenderShape.INVISIBLE;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
		if (blockState.getValue(PART) == MULTIBLOCK_CENTER)
			return new MoonlightBasinBlockEntity(blockPos, blockState);
		return null;
	}

	protected FluidState getFluidState(BlockState state) {
		return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
	}

	protected BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
		if (state.getValue(WATERLOGGED))
			level.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
		return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (level.random.nextIntBetweenInclusive(0, 4) == 0) {
			Vec3 center = pos.getCenter();
//			level.addParticle(
//					NMLParticleTypes.MOONLIGHT_RAY.get(),
//					center.x + (level.random.nextFloat() * 2f - 1f),
//					center.y,
//					center.z + (level.random.nextFloat() * 2f - 1f),
//					0,
//					0,
//					0
//			);
		}
	}
}