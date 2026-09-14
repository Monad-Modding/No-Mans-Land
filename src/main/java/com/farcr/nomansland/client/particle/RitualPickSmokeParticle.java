package com.farcr.nomansland.client.particle;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;

public class RitualPickSmokeParticle extends TextureSheetParticle {
    public static ShaderInstance SHADER;
    public static final ParticleRenderType RENDER_TYPE = new ParticleRenderType() {
        @Override
        public BufferBuilder begin(Tesselator tesselator, TextureManager textureManager) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.depthMask(false);
            RenderSystem.disableDepthTest();
            RenderSystem.setShader(RitualPickSmokeParticle::getShader);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            return tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public String toString() {
            return "RITUAL_PICK_SMOKE";
        }
    };

    private final SpriteSet sprites;

    public RitualPickSmokeParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites) {
        super(level, x, y, z);
        this.sprites = sprites;
        this.setColor(1.0F, 1.0F, 1.0F);
        this.pickSprite(this.sprites);
        this.setLifetime(4 * 20 + level.random.nextIntBetweenInclusive(0, 3 * 20));
        this.hasPhysics = false;
        this.scale(5.0f);
        this.setAlpha(0.0f);
    }

    @Override
    public void tick() {
        super.tick();
        float life = ((float) this.age) / ((float) this.lifetime);
        this.setAlpha((float) (life / 2 * (1.0 - life)));
    }

    @Override
    public ParticleRenderType getRenderType() {
        return RENDER_TYPE;
    }

    public static ShaderInstance getShader() {
        return SHADER;
    }
}
