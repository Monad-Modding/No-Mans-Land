package com.farcr.nomansland.client.particle;

import com.farcr.nomansland.client.model.utils.AnimUtil;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class DeepSleepParticle extends TextureSheetParticle {
    private final SpriteSet spriteSet;
    private final Vec3 startingPosition;

    public DeepSleepParticle(
        ClientLevel level, double x, double y, double z,
        double xSpeed, double ySpeed, double zSpeed, SpriteSet spriteSet
    ) {
        super(level, x, y + 0.25f, z, 0, ySpeed, 0);
        startingPosition = new Vec3(x, y, z);
        this.setSprite(spriteSet.get(1, 1));
        this.setSize(1f, 1f);
        this.spriteSet = spriteSet;
        this.lifetime = 80;
    }

    @Override public float getQuadSize(float scaleFactor) {
        float scale = (float) this.age / (float) this.lifetime;
        return this.quadSize * scale;
    }

    @Override
    public void tick() {
        float displacement = AnimUtil.wave((float) age / lifetime) / 2f;
        Vec3 displacedPos = new Vec3(
            startingPosition.x + displacement,
            y + 0.25f,
            startingPosition.z + displacement
        );
        float dividend = 1 / 64f;
        Vec3 addedPosition = displacedPos.subtract(this.startingPosition).multiply(dividend, dividend, dividend);
        this.setParticleSpeed(addedPosition.x, addedPosition.y, addedPosition.z);

        int fadeTicks = 20;
        float alpha = ((float) ((age + fadeTicks) - lifetime) / fadeTicks);
        this.setAlpha(1.0f - Math.max(alpha, 0));
        super.tick();
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }
}
