package com.farcr.nomansland.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;

public class ToxicGasParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    public ToxicGasParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites) {
        super(level, x, y, z);
        this.sprites = sprites;
        this.gravity = 0.0F;
        this.lifetime = 40 + this.random.nextInt(40);
        this.quadSize *= 1.2F + this.random.nextFloat() * 0.5F;
        this.friction = 0.96F;
        this.xd = (this.random.nextDouble() - 0.5) * 0.005;
        this.yd = 0.005 + this.random.nextDouble() * 0.005;
        this.zd = (this.random.nextDouble() - 0.5) * 0.005;
        this.alpha = 0.85F;
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
        if (this.age > this.lifetime - 8) {
            this.alpha = Math.max(0.0F, this.alpha - 0.1F);
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }
}
