package com.farcr.nomansland.common.block;
import net.minecraft.core.registries.Registries;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.neoforged.neoforge.common.util.TriState;

public class SurfaceMushroomBlock extends MushroomBlock {
    private final ResourceKey<ConfiguredFeature<?, ?>> feature;

    public SurfaceMushroomBlock(ResourceKey<ConfiguredFeature<?, ?>> feature, Properties properties) {
        super(feature, properties);
        this.feature = feature;
    }

    public static final MapCodec<MushroomBlock> CODEC = RecordCodecBuilder.<SurfaceMushroomBlock>mapCodec(instance -> instance.group(
            ResourceKey.codec(Registries.CONFIGURED_FEATURE).fieldOf("feature").forGetter(block -> block.feature),
            propertiesCodec()
    ).apply(instance, SurfaceMushroomBlock::new)).xmap(block -> (MushroomBlock) block, block -> (SurfaceMushroomBlock) block);

    @Override
    public MapCodec<MushroomBlock> codec() {
        return CODEC;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(BlockTags.DIRT);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos lowerPos = pos.below();
        BlockState lowerState = level.getBlockState(lowerPos);
        TriState soilDecision = lowerState.canSustainPlant(level, lowerPos, Direction.UP, state);
        return lowerState.is(BlockTags.MUSHROOM_GROW_BLOCK) || (soilDecision.isDefault() ? this.mayPlaceOn(lowerState, level, lowerPos) : soilDecision.isTrue());
    }
}
