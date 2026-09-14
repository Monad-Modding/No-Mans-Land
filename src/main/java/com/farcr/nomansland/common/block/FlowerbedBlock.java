package com.farcr.nomansland.common.block;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FlowerbedBlock extends FlowerBlock {
    protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);

    public FlowerbedBlock(Holder<MobEffect> effect, float seconds, Properties properties) {
        super(effect, seconds, properties);
    }

    public static final MapCodec<FlowerbedBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            EFFECTS_FIELD.forGetter(FlowerBlock::getSuspiciousEffects),
            propertiesCodec()
    ).apply(instance, FlowerbedBlock::new));

    @Override
    public MapCodec<FlowerbedBlock> codec() {
        return CODEC;
    }

    public FlowerbedBlock(SuspiciousStewEffects suspiciousStewEffects, Properties properties) {
        super(suspiciousStewEffects, properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}
