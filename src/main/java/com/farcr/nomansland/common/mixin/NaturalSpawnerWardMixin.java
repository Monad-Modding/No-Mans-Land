package com.farcr.nomansland.common.mixin;

import com.farcr.nomansland.common.world.saved_data.WardedSpacesData;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(NaturalSpawner.class)
public class NaturalSpawnerWardMixin {

    @WrapOperation(
            method = "spawnCategoryForPosition(Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/ChunkAccess;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/NaturalSpawner$SpawnPredicate;Lnet/minecraft/world/level/NaturalSpawner$AfterSpawnCallback;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/NaturalSpawner;isRightDistanceToPlayerAndSpawnPoint(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/ChunkAccess;Lnet/minecraft/core/BlockPos$MutableBlockPos;D)Z")
    )
    private static boolean nomansland$skipWardedSpawnPositions(final ServerLevel level, final ChunkAccess chunk, final BlockPos.MutableBlockPos pos, final double distance, final Operation<Boolean> original, @Local(argsOnly = true) final MobCategory category) {
        if (category == MobCategory.MONSTER && WardedSpacesData.get(level).isWarded(level, pos)) {
            return false;
        }
        return original.call(level, chunk, pos, distance);
    }
}
