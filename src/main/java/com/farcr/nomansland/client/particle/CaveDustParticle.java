package com.farcr.nomansland.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;

public class CaveDustParticle extends TextureSheetParticle {

    private static final float TEXEL_INSET = 0.5F;

    public CaveDustParticle(ClientLevel level, double x, double y, double z, SpriteSet spriteSet) {
        super(level, x, y, z);
        this.setSprite(spriteSet.get(this.random));
        this.gravity = 0.01F;
        this.lifetime = (int) (64.0 / (Math.random() * 0.8 + 0.2));
        int width = this.sprite.contents().width();
        this.quadSize *= (width - 2.0F * TEXEL_INSET) / width;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    protected float getU0() {
        return this.sprite.getU(this.getInsetU());
    }

    @Override
    protected float getU1() {
        return this.sprite.getU(1.0F - this.getInsetU());
    }

    @Override
    protected float getV0() {
        return this.sprite.getV(this.getInsetV());
    }

    @Override
    protected float getV1() {
        return this.sprite.getV(1.0F - this.getInsetV());
    }

    private float getInsetU() {
        return TEXEL_INSET / this.sprite.contents().width();
    }

    private float getInsetV() {
        return TEXEL_INSET / this.sprite.contents().height();
    }
}
