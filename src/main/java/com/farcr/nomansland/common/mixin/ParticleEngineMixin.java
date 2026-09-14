package com.farcr.nomansland.common.mixin;

import com.farcr.nomansland.client.particle.RitualPickDustParticle;
import com.farcr.nomansland.client.particle.RitualPickResonanceParticle;
import com.farcr.nomansland.client.particle.RitualPickSmokeParticle;
import com.google.common.collect.EvictingQueue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Queue;

@Mixin(ParticleEngine.class)
public class ParticleEngineMixin {
    @Shadow
    @Final
    private Map<ParticleRenderType, Queue<Particle>> particles;

    @Unique
    private boolean nomansland$addedRenderTypes = false;

    @Unique
    private static int nomansland$ritualPickPriority(ParticleRenderType type) {
        if (type == RitualPickSmokeParticle.RENDER_TYPE) return 0;
        if (type == RitualPickResonanceParticle.RENDER_TYPE) return 1;
        if (type == RitualPickDustParticle.RENDER_TYPE) return 2;
        return -1;
    }

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/client/ClientHooks;makeParticleRenderTypeComparator(Ljava/util/List;)Ljava/util/Comparator;"))
    private Comparator<ParticleRenderType> nomansland$orderRitualPickParticles(List<ParticleRenderType> renderOrder, Operation<Comparator<ParticleRenderType>> original) {
        Comparator<ParticleRenderType> comparator = original.call(renderOrder);
        return (typeOne, typeTwo) -> {
            int priorityOne = nomansland$ritualPickPriority(typeOne);
            int priorityTwo = nomansland$ritualPickPriority(typeTwo);
            if (priorityOne == -1 && priorityTwo == -1) return comparator.compare(typeOne, typeTwo);
            if (renderOrder.contains(typeOne) || renderOrder.contains(typeTwo)) return comparator.compare(typeOne, typeTwo);
            if (priorityOne != -1 && priorityTwo != -1) return Integer.compare(priorityOne, priorityTwo);
            return priorityOne != -1 ? -1 : 1;
        };
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void nomansland$tick(CallbackInfo ci) {
        if(!this.nomansland$addedRenderTypes) {
            this.particles.computeIfAbsent(RitualPickSmokeParticle.RENDER_TYPE, type -> EvictingQueue.create(16384));
            this.particles.computeIfAbsent(RitualPickResonanceParticle.RENDER_TYPE, type -> EvictingQueue.create(16384));
            this.particles.computeIfAbsent(RitualPickDustParticle.RENDER_TYPE, type -> EvictingQueue.create(16384));
            this.nomansland$addedRenderTypes = true;
        }
    }

}
