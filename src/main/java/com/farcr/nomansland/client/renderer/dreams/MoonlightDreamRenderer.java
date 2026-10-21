package com.farcr.nomansland.client.renderer.dreams;

import com.farcr.nomansland.client.renderer.FriendMoonRenderer;
import com.farcr.nomansland.common.dreams.DreamType;
import com.farcr.nomansland.common.dreams.dreamtypes.MoonlightDreamType;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.math.Axis;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.awt.*;

public class MoonlightDreamRenderer implements IDreamRenderer {
    public static ShaderInstance DREAM_SKY_SHADER;
    public static ShaderInstance GRADIENT_SHADER;

    float speed = 1 / 40f;

    private DreamAmbientSoundInstance ambientSound;

    @Override
    public void tick() {
        if (ambientSound == null) {
            ambientSound = new DreamAmbientSoundInstance();
            Minecraft.getInstance().getSoundManager().play(ambientSound);
        }
    }

    public boolean render(
        LevelRenderer levelRenderer,
        PoseStack poseStack,
        DeltaTracker deltaTracker,
        Matrix4f frustumMatrix,
        Matrix4f projectionMatrix
    ) {
        if (GRADIENT_SHADER == null || DREAM_SKY_SHADER == null) return false;
        RenderSystem.depthMask(false);
        poseStack.mulPose(frustumMatrix);
        poseStack.pushPose();
        poseStack.mulPose(MoonlightDreamType.SKY_ROTATION);

        float starAlpha = getStarBrightness(0f, 0f);
        float partialTicks = deltaTracker.getGameTimeDeltaTicks();
        if (Minecraft.getInstance().isPaused()) partialTicks = 0.0F;

        float finalPartialTicks = partialTicks;
        FriendMoonRenderer.drawWithColor(FriendMoonRenderer.getGradientColor(), starAlpha, () -> {
            if (levelRenderer.starBuffer == null)
                levelRenderer.createStars();

            FriendMoonRenderer.applySkyBlendFunction();

            levelRenderer.starBuffer.bind();
            levelRenderer.starBuffer.drawWithShader(poseStack.last().pose(),
                projectionMatrix, GameRenderer.getPositionShader());
            VertexBuffer.unbind();

            renderDream(poseStack, projectionMatrix, finalPartialTicks);

            RenderSystem.setShaderColor(1, 1, 1, 1);

            renderMoon(poseStack, projectionMatrix);

            RenderSystem.defaultBlendFunc();

            poseStack.popPose();

            RenderSystem.setShaderColor(0f, 0f, 0f, 1f);
            poseStack.pushPose();

            poseStack.scale(100f, 100f, 100f);
            poseStack.mulPose(Axis.ZP.rotationDegrees(180));
            poseStack.translate(0, -0.125, 0);

            MoonlightDreamRenderer.GRADIENT_SHADER.safeGetUniform("Slice").set(1f / 6f);
            getSkyMesh().drawWithShader(poseStack.last().pose(), projectionMatrix, GRADIENT_SHADER);

            poseStack.popPose();
        }, true);

        levelRenderer.renderBuffers.bufferSource().endLastBatch();

        FriendMoonRenderer.applySkyBlendFunction();

        RenderSystem.applyModelViewMatrix();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        FogRenderer.setupNoFog();

        this.handleCamera(partialTicks);
        return true;
    }

    private void handleCamera(float partialTicks) {
        float timeWithDelta = dreamInstance.moonPresenceTime + partialTicks;
        if (hasSeenMoon) ticksSinceSeenMoon += partialTicks;
        timeWithDelta = Math.max(timeWithDelta - 1f, 0);

        if (timeWithDelta > 0) {
            Entity camera = Minecraft.getInstance().getCameraEntity();

            Vec3 camPos = camera.getEyePosition(partialTicks);
            Vector3f targetPosition = camPos.toVector3f().add(new Vector3f(0, 100, 0).rotate(MoonlightDreamType.SKY_ROTATION));

            Vec3 target = new Vec3(targetPosition.x, targetPosition.y, targetPosition.z);
            Vec3 dir = target.subtract(camPos).normalize();

            float yawTo = (float) Math.toDegrees(Math.atan2(dir.z, dir.x)) - 90f;
            float pitchTo = (float) Math.toDegrees(-Math.atan2(dir.y, Math.sqrt(dir.x * dir.x + dir.z * dir.z)));

            float yaw = camera.getYRot();
            float pitch = camera.getXRot();

            float rotateSpeed = 0.08f * (speed * timeWithDelta / 2f) * partialTicks;
            if (hasSeenMoon) rotateSpeed = Math.min(rotateSpeed, 0.5f);
            camera.setYRot(yaw + (Mth.wrapDegrees(yawTo - yaw) * rotateSpeed));
            camera.setXRot(pitch + ((pitchTo - pitch) * rotateSpeed));
        }
    }

    public float getStarBrightness(float partialTick, float originalBrightness) {
        DreamType dreamType = ClientDreamRenderer.getInstance().getDream();
        double distance = MoonlightDreamType.BASIN_POSITION.distSqr(
            new Vec3i(
                (int) dreamType.spawnPoint.x,
                (int) dreamType.spawnPoint.y,
                (int) dreamType.spawnPoint.z
            )
        );

        Vec3 playerPosition = Minecraft.getInstance().player.position();
        double currentDistance = MoonlightDreamType.BASIN_POSITION.distSqr(
            new Vec3i(
                (int) playerPosition.x,
                (int) playerPosition.y,
                (int) playerPosition.z
            )
        );

        return (1.f - (float) Math.clamp(currentDistance / distance, 0, 1));
    }

    private boolean hasSeenMoon = false;
    public void hasSeenMoon() {
        hasSeenMoon = true;
    }

    public float elapsedTime = 0.0f;
    private float ticksSinceSeenMoon = 0.0f;

    MoonlightDreamType.MoonlightDreamTypeInstance dreamInstance =
        (MoonlightDreamType.MoonlightDreamTypeInstance) ClientDreamRenderer.getInstance()
            .getDreamClientInstance();

    public void renderDream(
        PoseStack poseStack, Matrix4f projectionMatrix, float partialTicks
    ) {
        if (DREAM_SKY_SHADER == null) return;
        poseStack.pushPose();
        poseStack.scale(100f, 100f, 100f);

        RenderSystem.enableBlend();
        VertexBuffer skyBuffer = getSkyMesh();

        elapsedTime += (partialTicks / 40);
        float timeWithDelta = dreamInstance.moonPresenceTime + partialTicks;
        timeWithDelta = Math.max(timeWithDelta - 1f, 0);

        float distance = Math.max(0.25f, getStarBrightness(0f, 0f));
        DREAM_SKY_SHADER.safeGetUniform("CornerFade").set((timeWithDelta * speed));
        DREAM_SKY_SHADER.safeGetUniform("Intensity").set((.5f * distance) + (timeWithDelta * speed));
        DREAM_SKY_SHADER.safeGetUniform("Time").set(elapsedTime);

        skyBuffer.drawWithShader(poseStack.last().pose(), projectionMatrix, DREAM_SKY_SHADER);

        VertexBuffer.unbind();
        poseStack.popPose();
    }

    private static final int MOON_FADE_START_TIME = 50;
    private static final int STARE_AT_MOON_TICKS = 30;

    public static float BLOCKS_FROM_EDGE = 15;

    @Override public float getFadeAlpha(float originalAlpha) {
        if (hasSeenMoon && ticksSinceSeenMoon > 0f) {
            return Math.clamp(((ticksSinceSeenMoon - (MOON_FADE_START_TIME + STARE_AT_MOON_TICKS))
                / (MoonlightDreamType.MAX_MOON_GAZE_TIME - MOON_FADE_START_TIME)), 0, 1);
        }
        // distance from edge alpha
        Vec3 playerPosition = Minecraft.getInstance().player.position();
        AABB boundingBox = MoonlightDreamType.MoonlightDreamTypeInstance.DREAM_BOUNDING_BOX;
        float x = (float) Math.min(playerPosition.x - boundingBox.minX, boundingBox.maxX - playerPosition.x);
        float z = (float) Math.min(playerPosition.z - boundingBox.minZ, boundingBox.maxZ - playerPosition.z);
        float dist = 1f - Mth.clamp(Math.min(x, z) / BLOCKS_FROM_EDGE, 0f, 1f);
        return Math.max(originalAlpha, dist);
    }

    public void renderMoon(PoseStack poseStack, Matrix4f projectionMatrix) {
        RenderSystem.enableBlend();
        FriendMoonRenderer.applyMultiplyBlendFunction();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        Matrix4f moonViewMatrix = poseStack.last().pose();

//        if (FriendMoonRenderer.moonOnScreen(Minecraft.getInstance(), moonViewMatrix, projectionMatrix, FriendMoonRenderer.LOOKING_AT_THRESHOLD)
//        && dreamInstance.moonPresenceTime > 0) {
//            hasSeenMoon = true;
//        }
        float focusedOpacity = Math.clamp((ticksSinceSeenMoon - STARE_AT_MOON_TICKS) / 20f, 0, 1);
        FriendMoonRenderer.drawWithColor(
            Color.WHITE.getRGB(), 1f - focusedOpacity,
            () -> FriendMoonRenderer.renderFriendMoonInternal(Tesselator.getInstance(), moonViewMatrix,
                FriendMoonRenderer.FriendMoonAnimation.DREAM_UNFOCUSED, 1f - focusedOpacity, 0, false),
            true
        );
        FriendMoonRenderer.drawWithColor(
            Color.WHITE.getRGB(), focusedOpacity,
            () -> FriendMoonRenderer.renderFriendMoonInternal(Tesselator.getInstance(), moonViewMatrix,
                FriendMoonRenderer.FriendMoonAnimation.DREAM, focusedOpacity, 0, false),
            true
        );
    }

    private VertexBuffer skyMesh;
    private VertexBuffer getSkyMesh() {
        if (skyMesh == null)
            skyMesh = FriendMoonRenderer.createSkyMesh();
        skyMesh.bind();
        return skyMesh;
    }

    @Override
    public void close() {
        if (skyMesh != null) {
            skyMesh.close();
            skyMesh = null;
        }
    }
}
