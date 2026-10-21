package com.farcr.nomansland.client.extensions;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.renderer.effect.AccumulateZoomRenderer;
import com.farcr.nomansland.common.extension.LivingEntityExtension;
import com.farcr.nomansland.common.item.AncestralOathSwordItem;
import com.farcr.nomansland.common.registry.items.NMLDataComponents;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

import java.util.Objects;

import static com.farcr.nomansland.common.item.AncestralOathSwordItem.*;

public class AncestralOathSwordClientExtensions implements IClientItemExtensions {

    public static AccumulateZoomRenderer TRAIL_INSTANCE = new AccumulateZoomRenderer();
    static TextureTarget renderTarget = new TextureTarget(100, 100, true, false);
    public static RenderTarget getRenderTarget() { return renderTarget; }

    private static float getGlintAnimateTime(ItemStack itemStack, float partialTick) {
        return Math.max(itemStack.getOrDefault(NMLDataComponents.OATH_SWORD_USE_TIME, 0) - partialTick, 0f);
    }

    public float getGlintOpacity(ItemStack itemStack, LivingEntity livingEntity) {
        float deltaTicks = Minecraft.getInstance().getTimer().getGameTimeDeltaTicks();
        float baseOpacity = (getGlintAnimateTime(itemStack, deltaTicks) / MAX_GLINT_ANIMATE);
        if (itemStack.getItem() instanceof AncestralOathSwordItem oathSword) {
            float shieldOpacity = (AncestralOathSwordItem.SWORD_PARRY_TICKS
                - (oathSword.getParryTiming(itemStack, livingEntity) + deltaTicks))
                / AncestralOathSwordItem.SWORD_PARRY_TICKS;
            baseOpacity = Math.max(baseOpacity, shieldOpacity);
        }
        return baseOpacity;
    }

    private static final Minecraft minecraft = Minecraft.getInstance();
    public static void render(float partialTick) {
        // go here because the method below doesnt even run in anything but first person
        Player player = Minecraft.getInstance().player;
        if (!minecraft.options.getCameraType().isFirstPerson()) return;

        float glintAnimateTime = getGlintAnimateTime(player.getItemInHand(InteractionHand.MAIN_HAND), partialTick);
        // this is a mess i know im like starting to get really burnt out so please bare with me
        // apply time here because this only runs once and if the player is in first person anyways
        ((LivingEntityExtension) player).nml$setShakeAnimationTime(
            ((LivingEntityExtension) player).nml$getShakeAnimationTime() - partialTick
        );
        ((LivingEntityExtension) player).nml$updateParryAnimationTime(partialTick);

        if (true) return;

        if (glintAnimateTime <= 0f) {
            TRAIL_INSTANCE.persistentTarget.forceClear(false);
            renderTarget.clear(false);
            return;
        }

        Window window = minecraft.getWindow();
        if (renderTarget.width != window.getWidth() || renderTarget.height != window.getHeight())
            renderTarget.resize(window.getWidth(), window.getHeight(), false);

        renderTarget.setClearColor(0f, 0f, 0f, 0f);
        renderTarget.clear(false);
        renderTarget.bindWrite(false);

        renderItemToTarget(partialTick, glintAnimateTime);

        TRAIL_INSTANCE.renderConstant(
            minecraft, partialTick,
            1.0f, 1F - (glintAnimateTime / MAX_GLINT_ANIMATE) / 100f
        );
        drawRenderTarget((glintAnimateTime / MAX_GLINT_ANIMATE) * .5f);
    }

    // ough
    private static void drawRenderTarget(float alpha) {
        minecraft.getMainRenderTarget().bindWrite(true);

        Matrix4f lastProjection = new Matrix4f(RenderSystem.getProjectionMatrix());
        VertexSorting lastSorting = RenderSystem.getVertexSorting();
        Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();

        modelViewStack.pushMatrix();
        modelViewStack.identity();

        RenderSystem.applyModelViewMatrix();
        RenderSystem.setProjectionMatrix(new Matrix4f(), VertexSorting.ORTHOGRAPHIC_Z);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, renderTarget.getColorTextureId());

        BufferBuilder buffer = RenderSystem.renderThreadTesselator()
            .begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        buffer.addVertex(-1f, -1f, 0f)
            .setUv(0f, 0f).setColor(1f, 1f, 1f, alpha);
        buffer.addVertex( 1f, -1f, 0f)
            .setUv(1f, 0f).setColor(1f, 1f, 1f, alpha);
        buffer.addVertex( 1f,  1f, 0f)
            .setUv(1f, 1f).setColor(1f, 1f, 1f, alpha);
        buffer.addVertex(-1f,  1f, 0f)
            .setUv(0f, 1f).setColor(1f, 1f, 1f, alpha);

        BufferUploader.drawWithShader(buffer.buildOrThrow());

        RenderSystem.setProjectionMatrix(lastProjection, lastSorting);
        modelViewStack.popMatrix();
        RenderSystem.applyModelViewMatrix();
    }

    private static void renderItemToTarget(float partialTicks, float glintAnimateTime) {
        PoseStack poseStack = new PoseStack();

        minecraft.gameRenderer.bobHurt(poseStack, partialTicks);
        if (minecraft.options.bobView().get())
            minecraft.gameRenderer.bobView(poseStack, partialTicks);

        ItemInHandRenderer itemInHandRenderer = minecraft.gameRenderer.itemInHandRenderer;
        float time = (glintAnimateTime / MAX_GLINT_ANIMATE);
        poseStack.translate(0f, (1f - time) / 16f, 0f);
        float poseScale = 1.0f - (time * 0.05f);
        poseStack.scale(poseScale, poseScale, poseScale);

        int packedLight = minecraft.getEntityRenderDispatcher().getPackedLightCoords(minecraft.player, partialTicks);
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        itemInHandRenderer.renderHandsWithItems(partialTicks, poseStack, bufferSource, Minecraft.getInstance().player, packedLight);
        bufferSource.endBatch();
    }

    /*
    * Probably not good practice to reimplement this, but
    * I want to give the sword a cool shake animation so fuuuuuuuck
    */
    @Override
    public boolean applyForgeHandTransform(
        @NotNull PoseStack poseStack, @NotNull LocalPlayer player,
        @NotNull HumanoidArm arm, @NotNull ItemStack itemInHand,
        float partialTick, float equippedProgress, float swingProgress
    ) {
        ItemInHandRenderer itemInHandRenderer = Minecraft.getInstance().gameRenderer.itemInHandRenderer;
        InteractionHand hand = player.getMainArm() == arm ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        boolean rightHanded = arm == HumanoidArm.RIGHT;
        int invert = rightHanded ? 1 : -1;
        if (player.isUsingItem() && player.getUseItemRemainingTicks() > 0 && player.getUsedItemHand() == hand) {
            itemInHandRenderer.applyItemArmTransform(poseStack, arm, equippedProgress);
            poseStack.mulPose(Axis.XP.rotationDegrees(-102.25F));
            poseStack.mulPose(Axis.YP.rotationDegrees((float) invert * 13.365F));
            poseStack.mulPose(Axis.ZP.rotationDegrees((float) invert * 78.05F));
            //
            poseStack.mulPose(Axis.YP.rotationDegrees(
                swordRotationTransform(((LivingEntityExtension) player)
                    .nml$getParryAnimationTime(arm))
            ));
            poseStack.mulPose(Axis.ZN.rotationDegrees(
                swordRotationTransform(((LivingEntityExtension) player)
                    .nml$getParryAnimationTime(arm))
            ));
            return true;
        }
        swordShakeTransform(poseStack, ((LivingEntityExtension) player).nml$getShakeAnimationTime());
        return false;
    }

    // its more artistic if I do it like this (lazy
    private static final float[] SHAKE_ARRAY = new float[]{
        3, -3, 2, -1.75f, -2, 1.75f,
        -1.5f, -1.25f, -1, 1.5f, 1.25f,
        1, -.75f, -.66f, -.5f, -.33f,
        .75f, .66f, .5f, .33f, 0
    };
    public static float getShakePosition(float animateTime) {
        return SHAKE_ARRAY[(int) ((SHAKE_ARRAY.length - 1)
            * ((MAX_ANIMATE_TIME - animateTime) / MAX_ANIMATE_TIME))];
    }

    public static float swordRotationTransform(float animateTime) {
        double time = Math.pow(animateTime / MAX_PARRY_ANIMATE_TIME, 2.0D);
        return (float) (25f * time);
    }

    public static void swordShakeTransform(PoseStack poseStack, float animateTime) {
        poseStack.translate(getShakePosition(animateTime) / 200f, 0f, 0f);
    }
}
