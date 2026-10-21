package com.farcr.nomansland.client.renderer.entity;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.NMLModelLayers;
import com.farcr.nomansland.client.model.beetle.DungBallModel;
import com.farcr.nomansland.common.entity.beetle.DungBall;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Quaternionf;

@OnlyIn(Dist.CLIENT)
public class DungBallRenderer extends EntityRenderer<DungBall> {
    private static final ResourceLocation TEXTURE = NoMansLand.location("textures/entity/dung_ball.png");
    private static final float[] SCALE = {0.64F, 0.96F, 1.36F};
    private static final float CENTER = 0.3125F;
    private final DungBallModel model;

    public DungBallRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.4F;
        this.model = new DungBallModel(context.bakeLayer(NMLModelLayers.DUNG_BALL_LAYER));
    }

    @Override
    public void render(DungBall ball, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        float scale = SCALE[Mth.clamp(ball.getSize(), 0, SCALE.length - 1)];
        poseStack.scale(scale, scale, scale);

        double moveX = ball.getX() - ball.xOld;
        double moveZ = ball.getZ() - ball.zOld;
        double speed = Math.sqrt(moveX * moveX + moveZ * moveZ);
        if (speed > 1.0E-4) {
            float roll = Mth.lerp(partialTick, ball.rollO, ball.roll);
            float axisX = (float) (moveZ / speed);
            float axisZ = (float) (-moveX / speed);
            poseStack.translate(0.0F, CENTER, 0.0F);
            poseStack.mulPose(new Quaternionf().rotateAxis(roll, axisX, 0.0F, axisZ));
            poseStack.translate(0.0F, -CENTER, 0.0F);
        }

        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0.0F, -1.501F, 0.0F);
        VertexConsumer consumer = buffer.getBuffer(model.renderType(TEXTURE));
        model.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY, -1);
        poseStack.popPose();
        super.render(ball, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(DungBall ball) {
        return TEXTURE;
    }
}
