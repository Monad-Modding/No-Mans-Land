package com.farcr.nomansland.common.mixin.client;

import com.farcr.nomansland.client.particle.RitualPickDustParticle;
import com.farcr.nomansland.client.particle.RitualPickResonanceParticle;
import com.farcr.nomansland.client.particle.RitualPickSmokeParticle;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.particle.ParticleRenderType;
import net.neoforged.neoforge.client.ClientHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Comparator;
import java.util.List;

@Mixin(ClientHooks.class)
public abstract class ClientHooksMixin {

    @ModifyReturnValue(method = "makeParticleRenderTypeComparator", at = @At("RETURN"))
    private static Comparator<ParticleRenderType> nomansland$orderRitualPickParticles(Comparator<ParticleRenderType> comparator, List<ParticleRenderType> renderOrder) {
        return (typeOne, typeTwo) -> {
            int priorityOne = nomansland$ritualPickPriority(typeOne);
            int priorityTwo = nomansland$ritualPickPriority(typeTwo);
            if (priorityOne == -1 && priorityTwo == -1) return comparator.compare(typeOne, typeTwo);
            if (renderOrder.contains(typeOne) || renderOrder.contains(typeTwo)) return comparator.compare(typeOne, typeTwo);
            if (priorityOne != -1 && priorityTwo != -1) return Integer.compare(priorityOne, priorityTwo);
            return priorityOne != -1 ? -1 : 1;
        };
    }

    @Unique
    private static int nomansland$ritualPickPriority(ParticleRenderType type) {
        if (type == RitualPickSmokeParticle.RENDER_TYPE) return 0;
        if (type == RitualPickResonanceParticle.RENDER_TYPE) return 1;
        if (type == RitualPickDustParticle.RENDER_TYPE) return 2;
        return -1;
    }
}
