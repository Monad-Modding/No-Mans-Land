package com.farcr.nomansland.common.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CrudeTrapDoorBlock extends TrapDoorBlock {

    private static final VoxelShape BOTTOM_NORTH = Block.box(0, 0, 2, 16, 3, 16);
    private static final VoxelShape BOTTOM_EAST = Block.box(0, 0, 0, 14, 3, 16);
    private static final VoxelShape BOTTOM_SOUTH = Block.box(0, 0, 0, 16, 3, 14);
    private static final VoxelShape BOTTOM_WEST = Block.box(2, 0, 0, 16, 3, 16);

    private static final VoxelShape TOP_NORTH = Block.box(0, 13, 2, 16, 16, 16);
    private static final VoxelShape TOP_EAST = Block.box(0, 13, 0, 14, 16, 16);
    private static final VoxelShape TOP_SOUTH = Block.box(0, 13, 0, 16, 16, 14);
    private static final VoxelShape TOP_WEST = Block.box(2, 13, 0, 16, 16, 16);

    private static final VoxelShape OPEN_BOTTOM_NORTH = Block.box(0, 0, 13, 16, 14, 16);
    private static final VoxelShape OPEN_BOTTOM_EAST = Block.box(0, 0, 0, 3, 14, 16);
    private static final VoxelShape OPEN_BOTTOM_SOUTH = Block.box(0, 0, 0, 16, 14, 3);
    private static final VoxelShape OPEN_BOTTOM_WEST = Block.box(13, 0, 0, 16, 14, 16);

    private static final VoxelShape OPEN_TOP_NORTH = Block.box(0, 2, 13, 16, 16, 16);
    private static final VoxelShape OPEN_TOP_EAST = Block.box(0, 2, 0, 3, 16, 16);
    private static final VoxelShape OPEN_TOP_SOUTH = Block.box(0, 2, 0, 16, 16, 3);
    private static final VoxelShape OPEN_TOP_WEST = Block.box(13, 2, 0, 16, 16, 16);

    public CrudeTrapDoorBlock(BlockSetType type, Properties properties) {
        super(type, properties);
    }

    public static final MapCodec<CrudeTrapDoorBlock> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(BlockSetType.CODEC.fieldOf("block_set_type").forGetter(CrudeTrapDoorBlock::getType), propertiesCodec())
                    .apply(instance, CrudeTrapDoorBlock::new)
    );

    @Override
    public MapCodec<? extends TrapDoorBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);
        boolean top = state.getValue(HALF) == Half.TOP;
        if (state.getValue(OPEN)) {
            return switch (facing) {
                case SOUTH -> top ? OPEN_TOP_SOUTH : OPEN_BOTTOM_SOUTH;
                case EAST -> top ? OPEN_TOP_EAST : OPEN_BOTTOM_EAST;
                case WEST -> top ? OPEN_TOP_WEST : OPEN_BOTTOM_WEST;
                default -> top ? OPEN_TOP_NORTH : OPEN_BOTTOM_NORTH;
            };
        }
        return switch (facing) {
            case SOUTH -> top ? TOP_SOUTH : BOTTOM_SOUTH;
            case EAST -> top ? TOP_EAST : BOTTOM_EAST;
            case WEST -> top ? TOP_WEST : BOTTOM_WEST;
            default -> top ? TOP_NORTH : BOTTOM_NORTH;
        };
    }
}
