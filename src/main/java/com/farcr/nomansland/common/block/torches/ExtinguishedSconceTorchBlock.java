package com.farcr.nomansland.common.block.torches;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ExtinguishedSconceTorchBlock extends ExtinguishedTorchBlock {

    protected static final VoxelShape AABB = Block.box(6.0D, 0.0D, 6.0D, 10.0D, 12.0D, 10.0D);

    public ExtinguishedSconceTorchBlock(SimpleParticleType flameParticle, Properties properties) {
        super(flameParticle, properties);
    }

    public static final MapCodec<ExtinguishedSconceTorchBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            PARTICLE_OPTIONS_FIELD.forGetter(block -> block.flameParticle),
            propertiesCodec()
    ).apply(instance, ExtinguishedSconceTorchBlock::new));

    @Override
    public MapCodec<ExtinguishedSconceTorchBlock> codec() {
        return CODEC;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return AABB;
    }
}
