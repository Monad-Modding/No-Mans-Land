package com.farcr.nomansland.common.mixin;

import com.farcr.nomansland.common.block.ToxicGasBlock;
import com.farcr.nomansland.common.registry.items.NMLItems;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractFurnaceBlockEntity.class)
public class AbstractFurnaceBlockEntityMixin {

    @WrapOperation(method = "serverTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"))
    private static void nml$sulfurFuelGas(ItemStack fuel, int amount, Operation<Void> original, Level level, BlockPos pos, BlockState state, AbstractFurnaceBlockEntity blockEntity) {
        boolean wasSulfur = fuel.is(NMLItems.SULFUR);
        original.call(fuel, amount);
        if (wasSulfur && level instanceof ServerLevel serverLevel && state.hasProperty(BlockStateProperties.HORIZONTAL_FACING) && level.random.nextFloat() < 0.35) {
            ToxicGasBlock.emitInFront(serverLevel, pos, state.getValue(BlockStateProperties.HORIZONTAL_FACING));
        }
    }
}
