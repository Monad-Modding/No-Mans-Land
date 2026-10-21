package com.farcr.nomansland.common.block.torches;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.MapCodec;

import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.BiConsumer;

public class SconceTorchBlock extends TorchBlock {
    protected static final VoxelShape AABB = Block.box(6, 0, 6, 10, 12, 10);

    public SconceTorchBlock(SimpleParticleType flameParticle, Properties properties) {
        super(flameParticle, properties);

    }

    public static final MapCodec<SconceTorchBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            PARTICLE_OPTIONS_FIELD.forGetter(block -> block.flameParticle),
            propertiesCodec()
    ).apply(instance, SconceTorchBlock::new));

    @Override
    public MapCodec<SconceTorchBlock> codec() {
        return CODEC;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return AABB;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        double d0 = (double) pos.getX() + 0.5D;
        double d1 = (double) pos.getY() + 0.8D;
        double d2 = (double) pos.getZ() + 0.5D;
        level.addParticle(ParticleTypes.SMOKE, d0, d1, d2, 0, 0, 0);
        level.addParticle(this.flameParticle, d0, d1, d2, 0, 0, 0);
    }

    @Override
    protected void onExplosionHit(BlockState state, Level level, BlockPos pos, Explosion explosion, BiConsumer<ItemStack, BlockPos> dropConsumer) {
        if (explosion.canTriggerBlocks()) {
            level.setBlockAndUpdate(pos, state.is(NMLBlocks.SCONCE_SOUL_TORCH.block()) ? NMLBlocks.EXTINGUISHED_SCONCE_SOUL_TORCH.get().withPropertiesOf(state) : NMLBlocks.EXTINGUISHED_SCONCE_TORCH.get().withPropertiesOf(state));
        }

        super.onExplosionHit(state, level, pos, explosion, dropConsumer);
    }
}
