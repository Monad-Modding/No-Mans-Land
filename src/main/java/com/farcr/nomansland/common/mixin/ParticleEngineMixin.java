package com.farcr.nomansland.common.mixin;

import com.farcr.nomansland.client.particle.RitualPickDustParticle;
import com.farcr.nomansland.client.particle.RitualPickResonanceParticle;
import com.farcr.nomansland.client.particle.RitualPickSmokeParticle;
import com.google.common.collect.EvictingQueue;
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

import java.util.Map;
import java.util.Queue;

@Mixin(ParticleEngine.class)
public class ParticleEngineMixin {
    @Shadow
    @Final
    private Map<ParticleRenderType, Queue<Particle>> particles;

    @Unique
    private boolean nomansland$addedRenderTypes = false;

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
