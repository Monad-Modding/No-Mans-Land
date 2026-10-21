package com.farcr.nomansland.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.Mth;

public class EntropyDustParticle extends TextureSheetParticle {
    float initialQuadSize, pQuadSize;
    public EntropyDustParticle(ClientLevel level, double x, double y, double z, SpriteSet spriteSet) {
        super(level, x, y, z);
        this.setSprite(spriteSet.get(this.random.nextInt(4), 4));
        this.gravity = 0.01F;
        this.lifetime = (int) (48.0 / (Math.random() * 0.8 + 0.2));

        this.initialQuadSize = this.quadSize;
        this.pQuadSize = this.quadSize;
    }

    @Override
    public void tick() {
        this.pQuadSize = this.quadSize;
        this.quadSize = Mth.clampedMap(this.age, this.lifetime - 20, this.lifetime, this.initialQuadSize, 0);
        super.tick();
    }

    @Override
    public void render(VertexConsumer buffer, Camera renderInfo, float partialTicks) {
        float brightness = Mth.clampedMap(this.age + partialTicks, 0, 50, 0, 1);
        this.setColor(brightness, brightness, brightness);
        super.render(buffer, renderInfo, partialTicks);
    }

    @Override
    public float getQuadSize(float partialTicks) {
        return Mth.lerp(partialTicks, pQuadSize, quadSize);
    }

    @Override
    protected int getLightColor(float partialTicks) {
        return LightTexture.pack(2, 0);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_LIT;
    }
}
