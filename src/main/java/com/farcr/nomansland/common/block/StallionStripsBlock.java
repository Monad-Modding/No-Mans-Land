package com.farcr.nomansland.common.block;

import com.mojang.serialization.MapCodec;
import com.farcr.nomansland.common.integration.FDIntegration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import vectorwing.farmersdelight.common.block.FeastBlock;

public class StallionStripsBlock extends FeastBlock {

    public static final IntegerProperty STRIP_SERVINGS = IntegerProperty.create("servings", 0, 5);

    protected static final VoxelShape PLATE_SHAPE_Z = Block.box(0, 0, 1, 16, 2, 15);
    protected static final VoxelShape FOOD_SHAPE_Z = Shapes.or(
            PLATE_SHAPE_Z,
            Block.box(0, 2, 1, 16, 4, 15)
    );
    protected static final VoxelShape PLATE_SHAPE_X = Block.box(1, 0, 0, 15, 2, 16);
    protected static final VoxelShape FOOD_SHAPE_X = Shapes.or(
            PLATE_SHAPE_X,
            Block.box(1, 2, 0, 15, 4, 16)
    );

    public StallionStripsBlock(BlockBehaviour.Properties properties) {
        super(properties, FDIntegration.STALLION_STRIP::item, true);
    }

    @Override
    public MapCodec<StallionStripsBlock> codec() {
        return simpleCodec(StallionStripsBlock::new);
    }

    public IntegerProperty getServingsProperty() {
        return STRIP_SERVINGS;
    }

    public int getMaxServings() {
        return 5;
    }

    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        boolean empty = state.getValue(getServingsProperty()) == 0;
        return switch (state.getValue(FACING)) {
            case NORTH, SOUTH -> empty ? PLATE_SHAPE_Z : FOOD_SHAPE_Z;
            default -> empty ? PLATE_SHAPE_X : FOOD_SHAPE_X;
        };
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, STRIP_SERVINGS);
    }
}
