package com.farcr.nomansland.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.CritParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;

public class StasisHitProvider implements ParticleProvider<SimpleParticleType> {
    public static class StasisHitParticle extends CritParticle {
        private float freezeTime = 0.0f;
        protected StasisHitParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            super(level, x, y, z, xSpeed, ySpeed, zSpeed);
            this.gravity = 0.0f;
            this.friction = 1.0F;
            this.freezeTime = (float) ((Math.random() * 3F) + 10F);
            this.lifetime += (int) ((Math.random() * 10F) + 20F);
        }

        @Override public void tick() {
            super.tick();

            this.rCol = 1f;
            this.gCol = 1f;
            this.bCol = 1f;

            this.freezeTime--;
            if (this.freezeTime <= 0f) {
                this.hasPhysics = true;
                this.friction = 0.98F;
                this.gravity = Math.min(this.gravity + 0.05F, 0.5F);
            }
        }

        @Override
        protected int getLightColor(float partialTick) {
            return 240;
        }
    }

    private final SpriteSet sprite;

    public StasisHitProvider(SpriteSet sprites) {
        this.sprite = sprites;
    }

    public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        StasisHitParticle critparticle =
            new StasisHitParticle(level, x, y, z, xSpeed, ySpeed, zSpeed);
        critparticle.pickSprite(this.sprite);
        return critparticle;
    }
}
