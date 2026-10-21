package com.farcr.nomansland.client.renderer;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.Meshes;
import com.farcr.nomansland.client.ambience.fogmodifiers.FriendMoonFogModifier;
import com.farcr.nomansland.client.renderer.context.MeetingPointRenderContext;
import com.farcr.nomansland.client.renderer.dreams.MoonlightDreamRenderer;
import com.farcr.nomansland.common.block.moonlight.MoonlightCandleBlock;
import com.farcr.nomansland.common.blockentity.MoonlightBasinBlockEntity;
import com.farcr.nomansland.common.friend.BuddyStar;
import com.farcr.nomansland.common.friend.FriendMoon;
import com.farcr.nomansland.common.friend.FriendMoonState;
import com.farcr.nomansland.common.friend.FriendMoonUpdate;
import com.farcr.nomansland.common.friend.dialogue.DialogueState;
import com.farcr.nomansland.common.friend.dialogue.DialogueUtil;
import com.farcr.nomansland.common.networking.dialogue.ClientboundDialoguePacket;
import com.farcr.nomansland.common.networking.friend.FriendMoonUpdatePacket;
import com.farcr.nomansland.common.registry.NMLBlockEntities;
import com.farcr.nomansland.common.registry.NMLRegistries;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

import java.util.*;
import java.util.function.IntFunction;

public class FriendMoonRenderer implements AutoCloseable {
    public static FriendMoonRenderer INSTANCE = new FriendMoonRenderer();
    public static FriendMoonRenderer getInstance() {
        if (INSTANCE == null)
            INSTANCE = new FriendMoonRenderer();
        return INSTANCE;
    }

    public static void destroy() {
        if (INSTANCE == null)
            return;
        INSTANCE.close();
        INSTANCE = null;
    }

    @Override
    public void close() {}

    private int ticksSinceMoonUpdate = 0;
    private static final int COOLDOWN_TICKS = 5;

    public void tickClientState() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.isPaused()) return;
        if (mc.level == null || mc.level.effects().skyType() != DimensionSpecialEffects.SkyType.NORMAL) {
            friendMoonOpacity = 0f;
            friendMoonDarkneningOpacity = 0f;
            friendShadowOpacity = 0f;
            friendShadowFaceOpacity = 0f;
            fogOpacity = 0f;
            animationProgress = 0f;
            badOmenWaitTime = 0f;
            return;
        }
        if (++ticksSinceMoonUpdate <= COOLDOWN_TICKS) return;

        float fadeSpeed = 1f / 25f;
        friendMoonOpacity = Math.max(friendMoonOpacity - fadeSpeed, 0f);
        friendMoonDarkneningOpacity = Mth.lerp(fadeSpeed, friendMoonDarkneningOpacity, 0f);
        friendShadowOpacity = Mth.lerp(fadeSpeed, friendShadowOpacity, 0f);
        friendShadowFaceOpacity = Mth.lerp(fadeSpeed, friendShadowFaceOpacity, 0f);
        fogOpacity = Mth.lerp(fadeSpeed, fogOpacity, 0f);
        if (friendMoonOpacity <= 0f) {
            animationProgress = 0f;
            badOmenWaitTime = 0f;
        }
    }

    public enum FriendMoonAnimation {
        TALKING(0, "talking", 1, 2),
        SURPRISED(1, "surprised", 1, 1),
        HIDDEN(2, "hidden", 1, 1),
        HIDDEN_2(3, "hidden_unfocused", 1, 1),
        PHASES(4, "phases", 6, 1),
        DREAM(5, "dream", 1, 1),
        DREAM_UNFOCUSED(6, "dream_unfocused", 1, 1);

        public static final String textureLocation = "textures/misc/friendmoon_";
        private final ResourceLocation location;

        private final int xFrames;
        private final int yFrames;
        private final int id;

        FriendMoonAnimation(int id, String name, int xFrames, int yFrames) {
            this.location = NoMansLand.location(textureLocation + name + ".png");

            this.id = id;
            this.xFrames = xFrames;
            this.yFrames = yFrames;
        }

        public int getFrames() {
            return (xFrames * yFrames) - 1;
        }

        public float[] getUV(int frame) {
            int horizontalIndex = frame % xFrames;
            int verticalIndex = (frame / xFrames) % yFrames;

            return new float[] {
                horizontalIndex / (float) xFrames, (verticalIndex + 1) / (float) yFrames,
                (horizontalIndex + 1) / (float) xFrames, verticalIndex / (float) yFrames
            };
        }

        public ResourceLocation getLocation() {
            return location;
        }

        public int getId() {
            return id;
        }

        public static final IntFunction<FriendMoonAnimation> BY_ID = ByIdMap.continuous(FriendMoonAnimation::getId, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
        public static final StreamCodec<ByteBuf, FriendMoonAnimation> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, FriendMoonAnimation::getId);
    }

    private float friendMoonDarkneningOpacity = 0.0f;
    public float getFriendMoonDarkeningStrength() { return this.friendMoonDarkneningOpacity * 0.3F; }


    public float modifyAmbientLightFactor(float ambientLight) {
        float darkeningAmount = 1 - this.friendMoonDarkneningOpacity;
        return ambientLight * darkeningAmount;
    }
    public void modifySkyLightColor(Vector3f color, int skyLightLevel) {
        color.mul(1 - this.friendMoonDarkneningOpacity);
    }
    public void modifyBlockLightColor(Vector3f color, int blockLightLevel) {
        if (this.friendMoonDarkneningOpacity <= 0) return;
        if (blockLightLevel < MoonlightCandleBlock.LIGHT_LEVEL) {
            float factor = Mth.map(blockLightLevel, 0, MoonlightCandleBlock.LIGHT_LEVEL, 0, 1);
            factor = (float) Math.pow(factor, Mth.lerp(this.friendMoonDarkneningOpacity, 1, 5));
            color.mul(factor);
        }
    }
    public Vec3 modifyCloudColor(Vec3 color) {
        float darkeningAmount = 1 - this.friendMoonDarkneningOpacity;
        return color.scale(darkeningAmount);
    }

    private float friendMoonOpacity = 0.0f;
    public float getFriendMoonOpacity() { return friendMoonOpacity; }
    private float friendShadowOpacity = 0.0f;
    private float friendShadowFaceOpacity = 0.0f;

    public float badOmenWaitTime = 0.0f;
    public static final int MAX_BAD_OMEN_WAIT_TIME = 60;

    public static final float MOON_SIZE = 20f;
    public static final float MOON_DISTANCE = 50f;
    public static final float MOON_UV_TRIM = 0.375f;
    public static final float LOOKING_AT_THRESHOLD = 0.1f;

    public FriendMoonAnimation MOON_ANIMATION = FriendMoonAnimation.TALKING;
    public void setFriendMoonAnimation(FriendMoonAnimation newAnimation) {
        if (MOON_ANIMATION != newAnimation)
            animationProgress = 0;
        MOON_ANIMATION = newAnimation;
    }
    public FriendMoonAnimation getFriendMoonEmotion(FriendMoon friendMoon) {
        if (friendMoon.getState() == FriendMoonState.NEGATIVE
        || friendMoon.getState() == FriendMoonState.UPSET)
            return FriendMoonAnimation.SURPRISED;
        return FriendMoonAnimation.TALKING;
    }

    public float friendMoonPitchAngle = 0f;
    public float friendMoonYawAngle = 0f;
    public float friendMoonPitchDirection = 0f;
    public float friendMoonYawDirection = 0f;
    public float friendMoonPitchStep = 0;
    public float friendMoonYawStep = 0;

    private final Map<Long, float[]> starAngles = new HashMap<>();

    public static boolean moonOnScreen(Minecraft mc, Matrix4f moonViewMatrix, Matrix4f projectionMatrix, float threshold) {
        Vector3f worldPosition = moonViewMatrix.transformPosition(0f, MOON_DISTANCE, 0F, new Vector3f());
        Vector4f clip = new Vector4f(worldPosition, 1f).mul(projectionMatrix);
        return (clip.w > 0.0f)
            && ((Math.abs(clip.x / clip.w) <= threshold)
            && (Math.abs(clip.y / clip.w) <= threshold));
    }

    // Retrieve Animation Type Logic
    private FriendMoonAnimation getFriendMoonAnimation() {
        return MOON_ANIMATION;
    }

    public float animationProgress = 0;
    public float talkAnimationProgress = 0; // separated for smoothness in talking

    public static boolean friendMoonIsOccluded(Minecraft mc, RandomSource random, Entity cameraEntity, Quaternionf moonRotation) {
        // Raycast for the Moon
        Vec3 start = cameraEntity.getEyePosition();
        Vector3f worldPosition = new Vector3f(0f, MOON_DISTANCE, 0f).rotate(moonRotation);
        Vec3 end = start.add(new Vec3(worldPosition));

        BlockHitResult cast = mc.level.clip(
            new ClipContext(
                start, end,
                ClipContext.Block.VISUAL,
                ClipContext.Fluid.NONE,
                CollisionContext.empty()
            )
        );
        return cast.getType() == HitResult.Type.BLOCK;
    }

    /*
    * Since the Friend Moon has a delay before leaving on the server
    * this delay has to be tracked on the client as well so I've hacked together
    * a solution that should work for now since the player receives one of these packets
    * whenever they "gain friendship" as well. oh how I miss the status effect
    */
    private float trackedBasinTime = 0f;
    public BlockPos clientBlockPos;
    public void setClientBlockPos(BlockPos clientBlockPos, boolean updateBasinTime) {
        this.clientBlockPos = clientBlockPos;
        if (updateBasinTime) trackedBasinTime = 0f;
    }

    public static final ResourceLocation NOBODY_CAME_LOCATION = NoMansLand.location("nobody_came");

    boolean setFriendMoonAnimations(FriendMoon friendMoonInstance, Player player, boolean moonIsVisible, float deltaTime) {
        DialogueState currentState = DialogueRenderer.getCurrentState();
        if (friendMoonInstance.getState().equals(FriendMoonState.UPSET)) {
            setFriendMoonAnimation(FriendMoonAnimation.SURPRISED);
            return true;
        }

        boolean hasBadOmen = friendMoonInstance.upsetWith.contains(player.getUUID());
        if (friendMoonInstance.cannotObtainFriendship(player) && !hasBadOmen)
            return true;

        if (!hasBadOmen) badOmenWaitTime = 0f;

        // bad omen wait time handle
        float turnAnimateSpeed = (deltaTime / 15f);
        boolean isAwake = friendMoonInstance.isAwake();
        if (hasBadOmen) {
            if (!moonIsVisible && badOmenWaitTime < MAX_BAD_OMEN_WAIT_TIME) {
                badOmenWaitTime = 0f;
                return true;
            }

            if (!getFriendMoonAnimation().equals(FriendMoonAnimation.PHASES)) {
                setFriendMoonAnimation(FriendMoonAnimation.PHASES);
                animationProgress = FriendMoonAnimation.PHASES.getFrames();
            }
            animationProgress = Math.max(0, animationProgress - (turnAnimateSpeed * 3f));

            if (badOmenWaitTime >= MAX_BAD_OMEN_WAIT_TIME) {
                if (friendMoonOpacity >= 1f) {
                    ClientboundDialoguePacket packet = ClientboundDialoguePacket.timedDialoguePacket(
                        NOBODY_CAME_LOCATION,
                        NMLRegistries.SPECIAL_DIALOGUE_KEY.location(),
                        Optional.of(player.getUUID()),
                        player.getRandom()
                    );
                    packet.applyPacket(player.level(), player);
                    DialogueRenderer.getCurrentState().setOverrideColor(DialogueUtil.NOBODY_CAME_TEXT_COLOR);
                    DialogueRenderer.getCurrentState().mute();
                }
                return true;
            } else {
                badOmenWaitTime += deltaTime;
                if (DialogueRenderer.isStateActive(currentState))
                    DialogueRenderer.getCurrentState().reset();
            }
            return false;
        }

        if (!isAwake) {
            if (!moonIsVisible || !FriendMoon.appearConditionsMet(player, clientBlockPos)) return true;
            // moon rotation
            if (friendMoonOpacity >= 1) {
                animationProgress = Math.min(animationProgress + turnAnimateSpeed, getFriendMoonAnimation().getFrames());
                if (animationProgress >= getFriendMoonAnimation().getFrames())
                    FriendMoonUpdatePacket.toServer(FriendMoonUpdate.ToServer.AWAKEN);
            } else {
                setFriendMoonAnimation(FriendMoonAnimation.PHASES);
                animationProgress = 0.0f;
            }
        } else {
            // bad omen ticks force surprised animation
            if (friendMoonInstance.getBadOmenInteractionTicks() > FriendMoon.BAD_OMEN_INTERACTION_LENGTH / 2) {
                setFriendMoonAnimation(FriendMoonAnimation.SURPRISED);
                return false;
            }

            // Set default animation to emotion (server chosen)
            setFriendMoonAnimation(getFriendMoonEmotion(friendMoonInstance));
            float talkSpeed = 1 / 3f;
            if (DialogueRenderer.isStateActive(currentState)) {
                if (currentState.canSpeakCurrently())
                    talkAnimationProgress = (talkAnimationProgress + (deltaTime * talkSpeed)) % (getFriendMoonAnimation().getFrames() + 1);
                animationProgress = talkAnimationProgress;
            } else {
                talkAnimationProgress = (float) Math.floor(talkAnimationProgress);
                animationProgress = 0.0f;
            }
        }
        return false;
    }

    public void updateFriendMoonPosition(
        Entity cameraEntity, Quaternionf moonRotation,
        Matrix4f moonViewMatrix, Matrix4f projectionMatrix, float partialTick
    ) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.isPaused())
            return;
        ticksSinceMoonUpdate = 0;

        LocalPlayer player = mc.player;
        boolean fadeOut = true;
        float deltaTime = mc.getTimer().getGameTimeDeltaTicks();
        trackedBasinTime = Math.min(trackedBasinTime + deltaTime, FriendMoon.LEAVE_TIME_THRESHOLD);

        float fadeSpeed = (deltaTime / 25f);

        boolean isAwake = false;
        boolean moonIsVisible = moonOnScreen(mc, moonViewMatrix, projectionMatrix, 0.5f);
        FriendMoon friendMoonInstance = getClientMoon();
        if (friendMoonInstance != null) {
            isAwake = friendMoonInstance.isAwake();
            fadeOut = setFriendMoonAnimations(friendMoonInstance, player, moonIsVisible, deltaTime);
        }
        if (fadeOut) {
            friendMoonOpacity = Math.max(friendMoonOpacity - fadeSpeed, 0);
            if (friendMoonOpacity <= 0) animationProgress = 0;
        } else friendMoonOpacity = Math.min(friendMoonOpacity + fadeSpeed, 1);

        float desiredDarkness = 0.0f;
        if (isAwake) desiredDarkness = friendMoonOpacity;
        friendMoonDarkneningOpacity = Mth.lerp(fadeSpeed, friendMoonDarkneningOpacity, desiredDarkness);

        RandomSource random = cameraEntity.getRandom();
        float pitch = cameraEntity.getViewXRot(partialTick) + 90;
        float yaw = -cameraEntity.getViewYRot(partialTick);
        float speed = deltaTime / 30;
        float starSpeed = speed;
        if (getFriendMoonOpacity() <= 0f)
            speed = 1;

        if (!isAwake) {
            speed /= 4f;
            starSpeed /= 4f;
        }

        float pitchClamp = 90;

        float minX = Math.min(pitch - 20, pitchClamp);
        float minY = yaw - 30;
        float maxX = pitch + 20;
        float maxY = yaw + 30;

        float desiredPitchStep = friendMoonPitchDirection;
        float desiredYawStep = friendMoonYawDirection;

        friendMoonPitchStep = Mth.lerp(speed, friendMoonPitchStep, desiredPitchStep);
        friendMoonYawStep = Mth.lerp(speed, friendMoonYawStep, desiredYawStep);

        float targetPitch = friendMoonPitchAngle + friendMoonPitchStep;
        float targetYaw = friendMoonYawAngle + friendMoonYawStep;

        if (targetPitch < minX || targetPitch > maxX) {
            friendMoonPitchDirection = friendMoonPitchDirection == 1 ? -1 : 1;
            if (random.nextFloat() < 0.1) friendMoonYawDirection = friendMoonYawDirection == 1 ? -1 : 1;
        }

        if (targetYaw < minY || targetYaw > maxY) {
            friendMoonYawDirection = friendMoonYawDirection == 1 ? -1 : 1;
            if (random.nextFloat() < 0.1) friendMoonPitchDirection = friendMoonPitchDirection == 1 ? -1 : 1;
        }

        boolean moonIsOccluded = false;
        if (moonIsVisible && friendMoonIsOccluded(mc, random, cameraEntity, moonRotation)) {
            moonIsOccluded = true;

            float escapeTime = 0.25f;
            float moonMaxSpeed = 25f;
            if (Math.abs(friendMoonPitchStep) < moonMaxSpeed)
                friendMoonPitchStep += (pitch - targetPitch) * escapeTime;
            if (Math.abs(friendMoonYawStep) < moonMaxSpeed)
                friendMoonYawStep += (yaw - targetYaw) * escapeTime;
            friendMoonPitchDirection = Math.signum(friendMoonPitchStep);
            friendMoonYawDirection = Math.signum(friendMoonYawStep);
        }

        friendMoonPitchAngle = Mth.lerp(speed, friendMoonPitchAngle, targetPitch);
        friendMoonYawAngle = Mth.lerp(speed, friendMoonYawAngle, targetYaw);

        if (!moonIsOccluded) {
            friendMoonPitchAngle = Mth.lerp(speed, friendMoonPitchAngle, Mth.clamp(friendMoonPitchAngle, minX, maxX));
            friendMoonYawAngle = Mth.lerp(speed, friendMoonYawAngle, Mth.clamp(friendMoonYawAngle, minY, maxY));
        }

        friendMoonPitchAngle = Math.min(friendMoonPitchAngle, pitchClamp - 25f);

        updateStarAngles(starSpeed);
    }

    private void updateStarAngles(float moonSpeed) {
        FriendMoon moon = null;
        if (clientBlockPos != null) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                Optional<MoonlightBasinBlockEntity> optionalBasin = player.level()
                    .getBlockEntity(clientBlockPos, NMLBlockEntities.MOONLIGHT_BASIN.get());
                if (optionalBasin.isPresent()) moon = optionalBasin.get().clientMoon;
            }
        }
        if (moon == null) {
            // Preserve cached angles so stars keep their orbital lag across transient null
            // states (fade cycles, chunk load glitches). Stale entries will be pruned by
            // retainAll below once a moon with fresh star seeds comes back.
            return;
        }
        List<BuddyStar> stars = moon.getBuddyStars();
        if (stars.isEmpty()) {
            if (!starAngles.isEmpty()) starAngles.clear();
            return;
        }

        Set<Long> seen = new HashSet<>(stars.size());
        for (BuddyStar star : stars) {
            long seed = star.seed();
            seen.add(seed);
            float[] angles = starAngles.get(seed);
            if (angles == null) {
                angles = new float[]{friendMoonPitchAngle, friendMoonYawAngle};
                starAngles.put(seed, angles);
                continue;
            }
            float t = Mth.clamp(moonSpeed * star.getStickiness(), 0f, 1f);
            angles[0] = Mth.lerp(t, angles[0], friendMoonPitchAngle);
            angles[1] = Mth.lerp(t, angles[1], friendMoonYawAngle);
        }
        if (starAngles.size() > seen.size()) starAngles.keySet().retainAll(seen);
    }

    public static void renderFriendMoonInternal(Tesselator tesselator, Matrix4f matrix4f1,
        FriendMoonAnimation moonAnimation, float opacity, int animationFrame, boolean trim
    ) {
        float[] shaderColor = RenderSystem.getShaderColor();
        float lastOpacity = shaderColor[3];
        RenderSystem.setShaderColor(shaderColor[0], shaderColor[1], shaderColor[2], opacity);

        RenderSystem.setShaderTexture(0, moonAnimation.getLocation());
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        float[] uvPositions = moonAnimation.getUV(animationFrame);
        float moonTrim = trim ? MOON_UV_TRIM : 0;
        float moonSize = MOON_SIZE * (1f - (moonTrim * 2f));
        buffer.addVertex(matrix4f1, -moonSize, MOON_DISTANCE, -moonSize).setUv(uvPositions[0] + moonTrim, uvPositions[3] + moonTrim);
        buffer.addVertex(matrix4f1, moonSize, MOON_DISTANCE, -moonSize).setUv(uvPositions[2] - moonTrim, uvPositions[3] + moonTrim);
        buffer.addVertex(matrix4f1, moonSize, MOON_DISTANCE, moonSize).setUv(uvPositions[2] - moonTrim, uvPositions[1] - moonTrim);
        buffer.addVertex(matrix4f1, -moonSize, MOON_DISTANCE, moonSize).setUv(uvPositions[0] + moonTrim, uvPositions[1] - moonTrim);

        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.setShaderColor(shaderColor[0], shaderColor[1], shaderColor[2], lastOpacity);
    }

    public MeetingPointRenderContext meetingPointContext = MeetingPointRenderContext.fromDefault();
    public void renderFriendShadow(
        Matrix4f frustumMatrix, Matrix4f projectionMatrix,
        Tesselator tesselator, PoseStack poseStack, float partialTick
    ) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        assert player != null;

        if (meetingPointContext.enabled()) {
            float deltaTime = mc.getTimer().getGameTimeDeltaTicks();
            if (mc.isPaused()) deltaTime = 0f;

            poseStack.mulPose(frustumMatrix);
            poseStack.pushPose();

            BlockPos meetingPointPosition = meetingPointContext.meetingPointPosition();
            float xDist = (float) (meetingPointPosition.getCenter().x - player.position().x);
            float yDist = (float) (meetingPointPosition.getCenter().z - player.position().z);
            float actualDegrees = (float) Math.toDegrees(Math.atan2(xDist, yDist));

            float totalDistance = (float) Math.sqrt(xDist * xDist + yDist * yDist);
            boolean visible = (FriendMoon.isNightTime(player.level())
                && !FriendMoon.appearConditionsMet(player, clientBlockPos));

            float moveSpeed = 0.2f;
            float t = (float) (1f - Math.exp(deltaTime * -moveSpeed));

            float moveTo = (visible ? 1 : 0);
            friendShadowOpacity = Mth.lerp(t, friendShadowOpacity, moveTo);
            float decreaseDistance = 32f;
            float compositeOpacity = Math.max(friendShadowOpacity - getFriendMoonOpacity()
                + Math.clamp((totalDistance - (decreaseDistance * 2)) / decreaseDistance, -1, 0), 0f);

            if (compositeOpacity <= 0.01f) return;

            poseStack.mulPose(Axis.YP.rotationDegrees(actualDegrees));
            poseStack.mulPose(Axis.XP.rotationDegrees(65));

            RenderSystem.disableCull();
            enableStencil();

            applyMultiplyBlendFunction();

            Matrix4f moonViewMatrix = poseStack.last().pose();
            drawWithColor(FastColor.ARGB32.color(255, 255, 255), compositeOpacity, () -> {
                boolean moonIsVisible = moonOnScreen(mc, moonViewMatrix, projectionMatrix, LOOKING_AT_THRESHOLD);
                friendShadowFaceOpacity = Mth.lerp(t, friendShadowFaceOpacity, (moonIsVisible ? 1 : 0));

                renderFriendMoonInternal(tesselator, moonViewMatrix, FriendMoonAnimation.HIDDEN_2, compositeOpacity, 0, false);

                applySkyBlendFunction();
                renderFriendMoonInternal(tesselator, moonViewMatrix, FriendMoonAnimation.HIDDEN, friendShadowFaceOpacity, 0, false);

                applyMultiplyBlendFunction();

                RenderSystem.colorMask(false, false, false, false);
                renderFriendMoonInternal(tesselator, moonViewMatrix, FriendMoonAnimation.HIDDEN_2, compositeOpacity, 0, false);
                RenderSystem.colorMask(true, true, true, true);

                stencilHideState();
            }, true);

            RenderSystem.defaultBlendFunc();
            RenderSystem.enableCull();

            poseStack.popPose();
            return;
        }
        friendShadowOpacity = 0.0f;
        friendShadowFaceOpacity = 0.0f;
    }

    public static void applySkyBlendFunction() {
        RenderSystem.blendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
            GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
    }

    public static void applyMultiplyBlendFunction() {
        RenderSystem.blendFuncSeparate(
            GlStateManager.SourceFactor.DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
    }

    private VertexBuffer skyMesh;
    private VertexBuffer getSkyMesh() {
        if (skyMesh == null)
            skyMesh = createSkyMesh();
        skyMesh.bind();
        return skyMesh;
    }

    public static VertexBuffer createSkyMesh() {
        VertexBuffer skyMesh = new VertexBuffer(VertexBuffer.Usage.STATIC);
        skyMesh.bind();
        skyMesh.upload(Meshes.texturelessHemisphere(Tesselator.getInstance(),
            24, 24, Mth.PI * 0.55F, 1,
            1F, 1F, 1F, 0F,
            0.3F, 0.3F, 0.4F, 1F,
            0.2F, -0.25F));
        VertexBuffer.unbind();
        return skyMesh;
    }

    public static void drawWithColor(int color, float alpha, Runnable runnable, boolean composite) {
        float c0 = RenderSystem.getShaderColor()[0];
        float c1 = RenderSystem.getShaderColor()[1];
        float c2 = RenderSystem.getShaderColor()[2];
        float c3 = RenderSystem.getShaderColor()[3];

        float compositeAlpha = composite ? alpha : 1;
        RenderSystem.setShaderColor(
            ((float) FastColor.ARGB32.red(color) / 255f) * compositeAlpha,
            ((float) FastColor.ARGB32.green(color) / 255f) * compositeAlpha,
            ((float) FastColor.ARGB32.blue(color) / 255f) * compositeAlpha,
            alpha
        );

        runnable.run();

        RenderSystem.setShaderColor(
            c0, c1, c2, c3
        );
    }

    public static int getGradientColor() {
        return FastColor.ARGB32.color(
            175, 220, 135
        );
    }

    private FriendMoon getClientMoon() {
        Player player = Minecraft.getInstance().player;
        if (player != null && clientBlockPos != null) {
            Optional<MoonlightBasinBlockEntity> optionalBasin = player.level()
                .getBlockEntity(clientBlockPos, NMLBlockEntities.MOONLIGHT_BASIN.get());
            boolean isOrWasPreviouslyMet = (FriendMoon.appearConditionsMet(player, clientBlockPos)
                || (trackedBasinTime < FriendMoon.LEAVE_TIME_THRESHOLD && FriendMoon.isNightTime(player.level()))
                || DialogueRenderer.isStateActive(DialogueRenderer.getCurrentState()));
            if (isOrWasPreviouslyMet && optionalBasin.isPresent() && (optionalBasin.get().clientMoon != null))
                return optionalBasin.get().clientMoon;
        }
        return null;
    }

    private boolean canRenderMoonEffects() {
        return (getClientMoon() != null) && (getClientMoon().getState() != FriendMoonState.UPSET);
    }

    float fogOpacity = 0.0f;
    FriendMoonFogModifier fogModifier = new FriendMoonFogModifier();
    public void renderFriendMoonFog(Matrix4f frustumMatrix, Matrix4f projectionMatrix, PoseStack poseStack) {
        poseStack.mulPose(frustumMatrix);

        // Renders separate from opacity
        Minecraft mc = Minecraft.getInstance();
        assert mc.player != null;

        boolean enabledFog = FriendMoon.appearConditionsMet(mc.player, clientBlockPos);
        float deltaTime = mc.getTimer().getGameTimeDeltaTicks() / 5f;
        if (mc.isPaused()) deltaTime = 0.0f;
        float t = (float) (1f - Math.exp(deltaTime * -.2f));
        fogOpacity = Mth.lerp(t, fogOpacity, (enabledFog ? (0.5f + (getFriendMoonOpacity() / 2f)) : 0));

        if (fogOpacity > 0.01f && MoonlightDreamRenderer.GRADIENT_SHADER != null) {
            poseStack.pushPose();
            // Render Sky Fog prior to skybox as well
            float divider = 1 / 24f;
            int fogColor = FastColor.ARGB32.colorFromFloat(
                1f,
                fogModifier.getFogRedMultiplier() * divider,
                fogModifier.getFogGreenMultiplier() * divider,
                fogModifier.getFogBlueMultiplier() * divider
            );
//            applyMultiplyBlendFunction();
            drawWithColor(fogColor, fogOpacity, () -> {
                poseStack.scale(100f, 100f, 100f);
                MoonlightDreamRenderer.GRADIENT_SHADER
                    .safeGetUniform("Slice").set(.15f);
                getSkyMesh().drawWithShader(
                    poseStack.last().pose(), projectionMatrix,
                    MoonlightDreamRenderer.GRADIENT_SHADER
                );

                VertexBuffer.unbind();
            }, true);

            applySkyBlendFunction();
            poseStack.popPose();
        }

        poseStack.popPose();
    }

    private static final int FRIEND_MOON_STENCIL_BIT = 0x80;
    private static StencilState previousStencilState;
    /*
    * I should do this for rendering in general in like a library or something that'd be a fun idea
    */
    private record StencilState(
        boolean enabled, int func,
        int ref, int valueMask,
        int writeMask, int fail,
        int passDepthFail,
        int passDepthPass
    ) {
        private static StencilState capture() {
            return new StencilState(
                GL11.glIsEnabled(GL11.GL_STENCIL_TEST),
                GL11.glGetInteger(GL11.GL_STENCIL_FUNC),
                GL11.glGetInteger(GL11.GL_STENCIL_REF),
                GL11.glGetInteger(GL11.GL_STENCIL_VALUE_MASK),
                GL11.glGetInteger(GL11.GL_STENCIL_WRITEMASK),
                GL11.glGetInteger(GL11.GL_STENCIL_FAIL),
                GL11.glGetInteger(GL11.GL_STENCIL_PASS_DEPTH_FAIL),
                GL11.glGetInteger(GL11.GL_STENCIL_PASS_DEPTH_PASS)
            );
        }

        private void restore() {
            if (enabled) GL11.glEnable(GL11.GL_STENCIL_TEST);
            else GL11.glDisable(GL11.GL_STENCIL_TEST);

            RenderSystem.stencilFunc(func, ref, valueMask);
            RenderSystem.stencilMask(writeMask);
            RenderSystem.stencilOp(fail, passDepthFail, passDepthPass);
        }
    }


    /*
     * encapsulated stencil calls to make sure that everything is clean
     * no more calling these individually, should help make
     * fixing issues related to stencil calls easier and fix aeronautics issues
     */
    public static void enableStencil() {
        if (previousStencilState == null)
            previousStencilState = StencilState.capture();

        GL11.glEnable(GL11.GL_STENCIL_TEST);
        RenderSystem.stencilMask(FRIEND_MOON_STENCIL_BIT);
        RenderSystem.clearStencil(0);
        RenderSystem.clear(GL11.GL_STENCIL_BUFFER_BIT, Minecraft.ON_OSX);
        RenderSystem.stencilFunc(GL11.GL_ALWAYS, FRIEND_MOON_STENCIL_BIT, FRIEND_MOON_STENCIL_BIT);
        RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
    }

    public static void stencilHideState() {
        RenderSystem.stencilFunc(GL11.GL_NOTEQUAL, FRIEND_MOON_STENCIL_BIT, FRIEND_MOON_STENCIL_BIT);
        RenderSystem.stencilMask(0x00);
        RenderSystem.stencilOp(
            GL11.GL_KEEP,
            GL11.GL_KEEP,
            GL11.GL_KEEP
        );
    }

    public static void disableStencil() {
        if (previousStencilState == null) return;

        RenderSystem.stencilFunc(GL11.GL_ALWAYS, 0, FRIEND_MOON_STENCIL_BIT);
        RenderSystem.stencilMask(FRIEND_MOON_STENCIL_BIT);
        RenderSystem.clearStencil(0);
        RenderSystem.clear(GL11.GL_STENCIL_BUFFER_BIT, Minecraft.ON_OSX);
        RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        RenderSystem.colorMask(true, true, true, true);

        previousStencilState.restore();
        previousStencilState = null;
    }

    public static void enableStencilTarget() {
        RenderTarget target = Minecraft.getInstance().getMainRenderTarget();
        if (!target.isStencilEnabled()) target.enableStencil();
    }

    /*
     * eventually with vulkan I know I will have to make things more render
     * agnostic and not use GL calls but for the time being this will have to do
     */
    public void renderFriendMoon(Matrix4f frustumMatrix, Matrix4f projectionMatrix, Tesselator tesselator, PoseStack poseStack, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        assert mc.level != null;

        poseStack.mulPose(frustumMatrix);

        Matrix4f skyMatrix = new Matrix4f(poseStack.last().pose());

        // Moon Rotation in the sky
        Quaternionf rotationQuaternion = Axis.YP.rotationDegrees(friendMoonYawAngle)
            .mul(Axis.XP.rotationDegrees(friendMoonPitchAngle));
        poseStack.mulPose(rotationQuaternion);

        Matrix4f matrix4f1 = poseStack.last().pose();

        RenderSystem.enableBlend();
        RenderSystem.enableCull();
        applySkyBlendFunction();

        // Update moon rotation / position
        updateFriendMoonPosition(mc.getCameraEntity(), rotationQuaternion, matrix4f1, projectionMatrix, partialTick);
        if (getFriendMoonOpacity() <= 0)
            return;

        RenderSystem.disableCull();

        // Render Friend Moon Afterwards
        poseStack.pushPose();

        applySkyBlendFunction();

        // Render Full Moon
        renderFriendMoonInternal(tesselator, matrix4f1, getFriendMoonAnimation(), getFriendMoonOpacity(), (int) animationProgress, false);

        enableStencil();

        // Rendering moon
        RenderSystem.colorMask(false, false, false, false);
        renderFriendMoonInternal(tesselator, matrix4f1, getFriendMoonAnimation(), getFriendMoonOpacity(), (int) animationProgress, true);
        RenderSystem.colorMask(true, true, true, true);

        renderBuddyStars(tesselator, skyMatrix, getFriendMoonOpacity());
        stencilHideState();

        RenderSystem.enableCull();
        poseStack.popPose();
    }

    private static final ResourceLocation BUDDY_STAR_TEXTURE = NoMansLand.location("textures/misc/buddy_star.png");
    public static final float STAR_SIZE = 1.0f;

    public void renderBuddyStars(Tesselator tesselator, Matrix4f skyMatrix, float opacity) {
        if (clientBlockPos == null || opacity <= 0)
            return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null)
            return;

        Optional<MoonlightBasinBlockEntity> optionalBasin = player.level().getBlockEntity(clientBlockPos, NMLBlockEntities.MOONLIGHT_BASIN.get());
        if (optionalBasin.isEmpty() || optionalBasin.get().clientMoon == null)
            return;

        List<BuddyStar> stars = optionalBasin.get().clientMoon.getBuddyStars();
        if (stars.isEmpty())
            return;

        RenderSystem.setShaderTexture(0, BUDDY_STAR_TEXTURE);
        long timeMs = System.currentTimeMillis();

        float[] prevColor = RenderSystem.getShaderColor();
        for (int i = 0; i < stars.size(); i++) {
            BuddyStar star = stars.get(i);
            float angleRad = (float) Math.toRadians(star.getAngle(timeMs));
            float dist = star.getDistance(i);

            float x = (float) (Math.sin(angleRad)) * dist;
            float y = MOON_DISTANCE;
            float z = (float) (Math.cos(angleRad)) * dist;

            float[] angles = starAngles.get(star.seed());
            float starPitch = angles != null ? angles[0] : friendMoonPitchAngle;
            float starYaw = angles != null ? angles[1] : friendMoonYawAngle;

            Matrix4f starMatrix = new Matrix4f(skyMatrix)
                .rotate(Axis.YP.rotationDegrees(starYaw))
                .rotate(Axis.XP.rotationDegrees(starPitch));

            float[] rgb = star.getRgb();
            float flicker = star.getFlickerAlpha(timeMs);
            RenderSystem.setShaderColor(rgb[0], rgb[1], rgb[2], opacity * flicker);

            BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            buffer.addVertex(starMatrix, x - STAR_SIZE, y, z - STAR_SIZE).setUv(0, 1);
            buffer.addVertex(starMatrix, x + STAR_SIZE, y, z - STAR_SIZE).setUv(1, 1);
            buffer.addVertex(starMatrix, x + STAR_SIZE, y, z + STAR_SIZE).setUv(1, 0);
            buffer.addVertex(starMatrix, x - STAR_SIZE, y, z + STAR_SIZE).setUv(0, 0);
            BufferUploader.drawWithShader(buffer.buildOrThrow());
        }
        RenderSystem.setShaderColor(prevColor[0], prevColor[1], prevColor[2], prevColor[3]);
    }

    public static void renderFinalize(Matrix4f frustumMatrix, Matrix4f projectionMatrix, Tesselator tesselator, float partialTick) {
        disableStencil();
    }
}
