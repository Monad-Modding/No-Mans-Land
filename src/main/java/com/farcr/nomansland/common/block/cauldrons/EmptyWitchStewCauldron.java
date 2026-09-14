package com.farcr.nomansland.common.block.cauldrons;

import com.mojang.serialization.MapCodec;
import com.farcr.nomansland.common.registry.NMLSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class EmptyWitchStewCauldron extends Block {

    public static final VoxelShape SHAPE = Shapes.join(
            Shapes.block(),
            Shapes.or(
                    box(0.0, 0.0, 4.0, 16.0, 3.0, 12.0),
                    box(4.0, 0.0, 0.0, 12.0, 3.0, 16.0),
                    box(2.0, 0.0, 2.0, 14.0, 3.0, 14.0),
                    box(2.0, 4.0, 2.0, 14.0, 16.0, 14.0)
            ),
            BooleanOp.ONLY_FIRST);

    public EmptyWitchStewCauldron(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<EmptyWitchStewCauldron> codec() {
        return simpleCodec(EmptyWitchStewCauldron::new);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            level.setBlockAndUpdate(pos, Blocks.CAULDRON.defaultBlockState());
            player.addItem(Items.BONE_MEAL.getDefaultInstance());
            level.playSound(null, pos, NMLSounds.WITCH_STEW_CAULDRON_CLEAN.value(), SoundSource.BLOCKS);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}
