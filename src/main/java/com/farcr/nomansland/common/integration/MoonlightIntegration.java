package com.farcr.nomansland.common.integration;

import net.mehvahdjukaar.moonlight.api.block.ILightable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FireChargeItem;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class MoonlightIntegration {

    private MoonlightIntegration() {
    }

    public static boolean isLightable(BlockState state) {
        return state.getBlock() instanceof ILightable;
    }

    public static void toggle(Level level, BlockPos pos, BlockState state) {
        ILightable lightable = (ILightable) state.getBlock();
        if (lightable.isLitUp(state, level, pos)) {
            lightable.tryExtinguish(null, state, pos, level);
        } else {
            lightable.tryLightUp(null, state, pos, level, ILightable.FireSoundType.FLINT_AND_STEEL);
        }
    }

    public static boolean light(Level level, BlockPos pos, BlockState state, @Nullable Entity cause) {
        return ((ILightable) state.getBlock()).tryLightUp(cause, state, pos, level, ILightable.FireSoundType.FIRE_CHANGE);
    }

    public static boolean use(Level level, BlockPos pos, BlockState state, Player player, ItemStack stack, boolean extinguishing, boolean lighting) {
        if (!(state.getBlock() instanceof ILightable lightable)) return false;
        boolean lit = lightable.isLitUp(state, level, pos);
        if (lighting && !lit && !(stack.getItem() instanceof FlintAndSteelItem) && !(stack.getItem() instanceof FireChargeItem) && !stack.is(ILightable.FLINT_AND_STEELS)) {
            return lightable.tryLightUp(player, state, pos, level, ILightable.FireSoundType.FLINT_AND_STEEL);
        }
        if (extinguishing && lit && !lightable.canBeExtinguishedBy(stack)) {
            return lightable.tryExtinguish(player, state, pos, level);
        }
        return false;
    }
}
