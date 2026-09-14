package com.farcr.nomansland.common.carving;

import com.farcr.nomansland.common.block.AncestralCarvingBlock;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class AncestralCarvingCarvingType implements CarvingType {

    @Override
    public ItemStack getIcon() {
        return NMLBlocks.ANCESTRAL_CARVING.stack();
    }

    @Override
    public boolean canReplace(BlockState state) {
        return state.is(Blocks.STONE) || state.is(Blocks.INFESTED_STONE) || state.is(NMLBlocks.ANCESTRAL_CARVING);
    }

    @Override
    public BlockState getStateForPlacement(Player player, BlockPos pos, BlockPos start, BlockPos end, Direction direction) {
        AncestralCarvingBlock block = getBlock();
        int rotation = 0;
        if(direction.getAxis() == Direction.Axis.Y) {
            rotation = block.getRotationForPlayer(new BlockPlaceContext(player, InteractionHand.MAIN_HAND, block.asItem().getDefaultInstance(), new BlockHitResult(pos.getCenter(), direction, pos, false)), direction);
        }

        return block.defaultBlockState()
                .setValue(AncestralCarvingBlock.FACING, direction)
                .setValue(AncestralCarvingBlock.ROTATION, rotation);
    }

    @Override
    public AABB getPlacementBox(Player player, BlockPos start, BlockPos end, Direction direction) {
        int maxSize = 2;
        BlockPos size = end.subtract(start);
        Vec3 position = player.getEyePosition();

        SubLevelAccess subLevel = SableCompanion.INSTANCE.getContaining(player.level(), start);
        if(subLevel != null) {
            position = subLevel.logicalPose().transformPositionInverse(position);
        }

        Vec3 center = start.getCenter();

        int xDir = (int) Math.signum(size.getX());
        if(xDir == 0) {
            xDir = (int) Math.signum(position.x - center.x);
        }

        int yDir = (int) Math.signum(size.getY());
        if(yDir == 0) {
            yDir = (int) Math.signum(position.y - center.y);
        }

        int zDir = (int) Math.signum(size.getZ());
        if(zDir == 0) {
            zDir = (int) Math.signum(position.z - center.z);
        }

        switch (direction.getAxis()) {
            case X -> {
                int expand = Math.max(Math.abs(size.getY()), Math.abs(size.getZ()));
                size = new BlockPos(
                        0,
                        Math.clamp((long) expand * yDir, -maxSize, maxSize),
                        Math.clamp((long) expand * zDir, -maxSize, maxSize)
                );
            }
            case Y -> {
                int expand = Math.max(Math.abs(size.getX()), Math.abs(size.getZ()));
                size = new BlockPos(
                        Math.clamp((long) expand * xDir, -maxSize, maxSize),
                        0,
                        Math.clamp((long) expand * zDir, -maxSize, maxSize)
                );
            }
            case Z -> {
                int expand = Math.max(Math.abs(size.getX()), Math.abs(size.getY()));
                size = new BlockPos(
                        Math.clamp((long) expand * xDir, -maxSize, maxSize),
                        Math.clamp((long) expand * yDir, -maxSize, maxSize),
                        0
                );
            }
        }

        return AABB.encapsulatingFullBlocks(BlockPos.ZERO, size).move(start);
    }

    @Override
    public void afterPlacement(Player player, BlockPos min, BlockPos max, Direction direction) {
        Vec3i forward = direction.getOpposite().getNormal();
        Vec3i up = new Vec3i(0, 1, 0);
        if(direction.getAxis() == Direction.Axis.Y) {
            BlockPlaceContext context = new BlockPlaceContext(player, InteractionHand.MAIN_HAND, getIcon(), new BlockHitResult(min.getCenter(), direction, min, false));

            Direction lookingDirection = context.getHorizontalDirection();
            up = (direction == Direction.DOWN ? lookingDirection.getOpposite() : lookingDirection).getNormal();
        }

        Vec3i right = up.cross(forward);
        BlockPos size = max.subtract(min);
        BlockPos corner = new BlockPos(
                (right.getX() + up.getX() + forward.getX()) * size.getX(),
                (right.getY() + up.getY() + forward.getY()) * size.getY(),
                (right.getZ() + up.getZ() + forward.getZ()) * size.getZ()
        ).offset(min);
        corner = BlockPos.min(BlockPos.max(corner, min), max);

        player.level().setBlock(corner, Blocks.AIR.defaultBlockState(), 1);
        player.level().setBlock(corner, getStateForPlacement(player, corner, min, max, direction), 3);
    }

    public AncestralCarvingBlock getBlock() {
        return NMLBlocks.ANCESTRAL_CARVING.get();
    }
}
