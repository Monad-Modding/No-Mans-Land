package com.farcr.nomansland.common.mixin;

import com.farcr.nomansland.common.mixin.plugin.annotation.IfModAbsent;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.SnowAndFreezeFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static com.farcr.nomansland.common.block.FrostedGrassBlock.SNOWLOGGED;

@IfModAbsent("snowrealmagic")
@Mixin(SnowAndFreezeFeature.class)
public class SnowAndFreezeFeatureMixin {

    @WrapOperation(
            method = "place",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/WorldGenLevel;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z")
    )
    private boolean nml$snowlogFrostedGrass(WorldGenLevel level, BlockPos pos, BlockState state, int flags, Operation<Boolean> original) {
        if (state.is(Blocks.SNOW) && level.getBlockState(pos).is(NMLBlocks.FROSTED_GRASS.block())) {
            return original.call(level, pos, NMLBlocks.FROSTED_GRASS.get().defaultBlockState().setValue(SNOWLOGGED, true), flags);
        }
        return original.call(level, pos, state, flags);
    }
}
