package com.farcr.nomansland.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class MoonlightSparkParticle extends TextureSheetParticle {
    private final SpriteSet spriteSet;
    private final int offsetIndex = this.random.nextInt(3);

    private int targetIntervalNext = 0;
    private Vec3 targetPosition;

    public MoonlightSparkParticle(
        ClientLevel level, double x, double y, double z,
        double xSpeed, double ySpeed, double zSpeed, SpriteSet spriteSet
    ) {
        super(level, x, y, z, 0f, 0f, 0f);
        this.spriteSet = spriteSet;
        this.lifetime = 45;

        this.setSprite(this.spriteSet.get(offsetIndex, 3));
    }

    @Override
    protected int getLightColor(float partialTick) {
        return 240;
    }

    @Override
    public void tick() {
        if (targetIntervalNext-- <= 0) {
            targetIntervalNext = this.random.nextInt(4, 8);
            targetPosition = new Vec3(
                (this.random.nextFloat() * 2f) - 1f,
                this.random.nextFloat() * 1.5f,
                (this.random.nextFloat() * 2f) - 1f
            );
        }

        float dividend = 1f / 12f;
        Vec3 addedPosition = targetPosition.multiply(dividend, dividend, dividend);
        this.setParticleSpeed(addedPosition.x, addedPosition.y, addedPosition.z);

        super.tick();

        this.setAlpha(1f - ((float) age / lifetime));
        this.setSprite(spriteSet.get((offsetIndex + (this.age / 8)) % 3, 3));
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }
}
