package com.farcr.nomansland.common.mixin;

import com.farcr.nomansland.common.block.ToxicGasBlock;
import com.farcr.nomansland.common.registry.items.NMLItems;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CampfireBlockEntity.class)
public class CampfireBlockEntityMixin {

    @WrapOperation(method = "cookTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/Containers;dropItemStack(Lnet/minecraft/world/level/Level;DDDLnet/minecraft/world/item/ItemStack;)V"))
    private static void nml$sulfurCampfireGas(Level level, double x, double y, double z, ItemStack result, Operation<Void> original, @Local(ordinal = 0) ItemStack input) {
        if (input.is(NMLItems.SULFUR) && level instanceof ServerLevel serverLevel) {
            ToxicGasBlock.scatterAround(serverLevel, BlockPos.containing(x, y, z), 3 + serverLevel.random.nextInt(3));
            return;
        }
        original.call(level, x, y, z, result);
    }
}
