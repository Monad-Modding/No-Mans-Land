package com.farcr.nomansland.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.tags.FluidTags;

import java.util.function.Supplier;

public class VentBubbleParticle extends BubbleParticle {
    public VentBubbleParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed,
                              SpriteSet spriteSet, Supplier<SimpleParticleType> popParticle) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed, spriteSet, popParticle);
        this.yd = Math.max(this.yd, 0.04 + Math.random() * 0.06);
        this.lifetime = 20 + this.random.nextInt(40);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.move(this.xd, this.yd, this.zd);
        this.yd += 0.002;
        this.xd *= 0.85;
        this.yd *= 0.85;
        this.zd *= 0.85;
        if (this.yd < 0.04) this.yd = 0.04;

        if (!this.level.getFluidState(BlockPos.containing(this.x, this.y, this.z)).is(FluidTags.WATER)) {
            this.lifetime = 0;
        }
        super.tick();
    }
}
