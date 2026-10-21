package com.farcr.nomansland.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;

public class SulfuricFumaroleParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    public SulfuricFumaroleParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites) {
        super(level, x, y, z);
        this.sprites = sprites;
        this.gravity = 0.0F;
        this.friction = 0.98F;
        this.lifetime = 70 + this.random.nextInt(40);
        this.quadSize *= 0.9F + this.random.nextFloat() * 0.5F;
        this.xd = (this.random.nextDouble() - 0.5) * 0.01;
        this.yd = 0.04 + this.random.nextDouble() * 0.03;
        this.zd = (this.random.nextDouble() - 0.5) * 0.01;
        this.alpha = 0.9F;
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
        if (this.yd < 0.03) this.yd = 0.03;
        this.quadSize += 0.003F;
        this.alpha -= 0.013F;
        if (this.alpha <= 0.0F
                || !this.level.getFluidState(BlockPos.containing(this.x, this.y, this.z)).is(FluidTags.WATER)) {
            this.remove();
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }
}
