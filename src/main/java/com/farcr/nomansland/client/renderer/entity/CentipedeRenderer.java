package com.farcr.nomansland.client.renderer.entity;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.NMLModelLayers;
import com.farcr.nomansland.client.model.centipede.CentipedeHeadModel;
import com.farcr.nomansland.client.model.centipede.CentipedeSegmentModel;
import com.farcr.nomansland.client.model.centipede.CentipedeTailModel;
import com.farcr.nomansland.common.entity.centipede.Centipede;
import dev.tazer.mixed_litter.VariantUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class CentipedeRenderer extends EntityRenderer<Centipede> {
    private static final ResourceLocation BASE_TEXTURE = NoMansLand.location("textures/entity/centipede/centipede.png");

    private final CentipedeHeadModel headModel;
    private final CentipedeSegmentModel frontModel;
    private final CentipedeSegmentModel middleModel;
    private final CentipedeTailModel tailModel;

    public CentipedeRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.35F;
        this.headModel = new CentipedeHeadModel(context.bakeLayer(NMLModelLayers.CENTIPEDE_HEAD));
        this.frontModel = new CentipedeSegmentModel(context.bakeLayer(NMLModelLayers.CENTIPEDE_FRONT));
        this.middleModel = new CentipedeSegmentModel(context.bakeLayer(NMLModelLayers.CENTIPEDE_MIDDLE));
        this.tailModel = new CentipedeTailModel(context.bakeLayer(NMLModelLayers.CENTIPEDE_TAIL));
    }

    @Override
    public void render(Centipede entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(entity)));
        int overlay = OverlayTexture.pack(OverlayTexture.u(0.0F), OverlayTexture.v(entity.hurtTime > 0 || entity.deathTime > 0));

        double ex = Mth.lerp(partialTick, entity.xOld, entity.getX());
        double ey = Mth.lerp(partialTick, entity.yOld, entity.getY());
        double ez = Mth.lerp(partialTick, entity.zOld, entity.getZ());

        float ageInTicks = entity.tickCount + partialTick;
        float bodyYaw = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
        float headYaw = Mth.rotLerp(partialTick, entity.yHeadRotO, entity.yHeadRot);
        float headPitch = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());

        int segments = entity.getSegments();
        int pieces = entity.getPieceCount();

        float bellyHalf = entity.getBbHeight() * 0.5F;

        double p0x = entity.getPieceRenderX(0, partialTick);
        double p0y = entity.getPieceRenderY(0, partialTick);
        double p0z = entity.getPieceRenderZ(0, partialTick);

        Vector3f headUp = new Vector3f(entity.getHeadRenderNX(partialTick), entity.getHeadRenderNY(partialTick), entity.getHeadRenderNZ(partialTick));

        Vector3f headForward = new Vector3f((float) (ex - p0x), (float) (ey - p0y), (float) (ez - p0z));
        if (headForward.lengthSquared() < 1.0E-6F) {
            float r = bodyYaw * Mth.DEG_TO_RAD;
            headForward.set(-Mth.sin(r), 0.0F, Mth.cos(r));
        }
        Vector3f chainFwd = new Vector3f(headForward).normalize();
        Vector3f chainUp = seedUp(headForward, headUp);

        poseStack.pushPose();
        poseStack.translate(-headUp.x * bellyHalf, bellyHalf - headUp.y * bellyHalf, -headUp.z * bellyHalf);
        applyOrientation(poseStack, headForward, chainUp);
        this.headModel.setupHead(ageInTicks, Mth.wrapDegrees(headYaw - bodyYaw), headPitch);
        this.headModel.render(poseStack, vertexConsumer, packedLight, overlay);
        poseStack.popPose();

        float limbSwingAmount = Math.min(1.0F, entity.getHeadSpeed() * 5.0F);
        float limbSwing = entity.getHeadGaitPhase(partialTick);

        for (int i = 0; i < pieces; i++) {
            double sx = entity.getPieceRenderX(i, partialTick);
            double sy = entity.getPieceRenderY(i, partialTick);
            double sz = entity.getPieceRenderZ(i, partialTick);

            double frontX;
            double frontY;
            double frontZ;
            if (i == 0) {
                frontX = ex;
                frontY = ey;
                frontZ = ez;
            } else {
                frontX = entity.getPieceRenderX(i - 1, partialTick);
                frontY = entity.getPieceRenderY(i - 1, partialTick);
                frontZ = entity.getPieceRenderZ(i - 1, partialTick);
            }

            Vector3f forward = new Vector3f((float) (frontX - sx), (float) (frontY - sy), (float) (frontZ - sz));
            if (forward.lengthSquared() < 1.0E-6F) {
                float r = bodyYaw * Mth.DEG_TO_RAD;
                forward.set(-Mth.sin(r), 0.0F, Mth.cos(r));
            }
            Vector3f fwdN = new Vector3f(forward).normalize();
            float pnx = entity.getPieceRenderNX(i, partialTick);
            float pny = entity.getPieceRenderNY(i, partialTick);
            float pnz = entity.getPieceRenderNZ(i, partialTick);
            Vector3f surfN = new Vector3f(pnx, pny, pnz);

            chainUp = transport(chainFwd, fwdN, chainUp);
            if (surfN.lengthSquared() > 1.0E-6F) {
                surfN.normalize();
                if (Math.abs(surfN.dot(fwdN)) < 0.85F) {
                    chainUp.lerp(surfN, 0.65F);
                }
            }
            float od = chainUp.dot(fwdN);
            chainUp.sub(fwdN.x * od, fwdN.y * od, fwdN.z * od);
            if (chainUp.lengthSquared() < 1.0E-5F) {
                chainUp = seedUp(fwdN, surfN);
            } else {
                chainUp.normalize();
            }
            chainFwd = fwdN;

            poseStack.pushPose();
            poseStack.translate(sx - ex - pnx * bellyHalf, sy - ey + bellyHalf - pny * bellyHalf, sz - ez - pnz * bellyHalf);
            applyOrientation(poseStack, forward, chainUp);

            if (i >= segments) {
                this.tailModel.setupSegment(i, limbSwing, limbSwingAmount);
                this.tailModel.render(poseStack, vertexConsumer, packedLight, overlay);
            } else if (i == 0) {
                this.frontModel.setupSegment(i, limbSwing, limbSwingAmount);
                this.frontModel.render(poseStack, vertexConsumer, packedLight, overlay);
            } else {
                this.middleModel.setupSegment(i, limbSwing, limbSwingAmount);
                this.middleModel.render(poseStack, vertexConsumer, packedLight, overlay);
            }
            poseStack.popPose();
        }
    }

    private static Vector3f seedUp(Vector3f forward, Vector3f surfaceNormal) {
        Vector3f f = new Vector3f(forward);
        if (f.lengthSquared() < 1.0E-6F) f.set(0.0F, 0.0F, 1.0F);
        else f.normalize();
        Vector3f up = new Vector3f(surfaceNormal);
        if (up.lengthSquared() < 1.0E-6F) up.set(0.0F, 1.0F, 0.0F);
        else up.normalize();
        float d = up.dot(f);
        up.sub(f.x * d, f.y * d, f.z * d);
        if (up.lengthSquared() < 1.0E-4F) {
            up.set(0.0F, 1.0F, 0.0F);
            d = up.dot(f);
            up.sub(f.x * d, f.y * d, f.z * d);
            if (up.lengthSquared() < 1.0E-4F) {
                up.set(1.0F, 0.0F, 0.0F);
                d = up.dot(f);
                up.sub(f.x * d, f.y * d, f.z * d);
            }
        }
        return up.normalize();
    }

    private static Vector3f transport(Vector3f fromForward, Vector3f toForward, Vector3f up) {
        Vector3f a = new Vector3f(fromForward);
        Vector3f b = new Vector3f(toForward);
        if (a.lengthSquared() < 1.0E-8F || b.lengthSquared() < 1.0E-8F) return new Vector3f(up);
        a.normalize();
        b.normalize();
        Vector3f axis = new Vector3f(a).cross(b);
        float sinT = axis.length();
        float cosT = a.dot(b);
        if (sinT < 1.0E-5F) return new Vector3f(up);
        axis.div(sinT);
        Vector3f axv = new Vector3f(axis).cross(up);
        float ad = axis.dot(up);
        return new Vector3f(
                up.x * cosT + axv.x * sinT + axis.x * ad * (1.0F - cosT),
                up.y * cosT + axv.y * sinT + axis.y * ad * (1.0F - cosT),
                up.z * cosT + axv.z * sinT + axis.z * ad * (1.0F - cosT)
        );
    }

    private static void applyOrientation(PoseStack poseStack, Vector3f forward, Vector3f normal) {
        Vector3f up = new Vector3f(normal);
        if (up.lengthSquared() < 1.0E-6F) {
            up.set(0.0F, 1.0F, 0.0F);
        } else {
            up.normalize();
        }

        float dot = forward.dot(up);
        Vector3f fwd = new Vector3f(forward).sub(up.x * dot, up.y * dot, up.z * dot);
        if (fwd.lengthSquared() < 1.0E-6F) {
            fwd.set(up.y, up.z, up.x);
            dot = fwd.dot(up);
            fwd.sub(up.x * dot, up.y * dot, up.z * dot);
        }
        fwd.normalize();

        Vector3f modelY = new Vector3f(up).mul(-1.0F);
        Vector3f modelZ = new Vector3f(fwd).mul(-1.0F);
        Vector3f modelX = new Vector3f(modelY).cross(modelZ).normalize();

        Matrix3f matrix = new Matrix3f();
        matrix.setColumn(0, modelX);
        matrix.setColumn(1, modelY);
        matrix.setColumn(2, modelZ);
        poseStack.mulPose(matrix.getNormalizedRotation(new Quaternionf()));
    }

    @Override
    public ResourceLocation getTextureLocation(Centipede entity) {
        return VariantUtil.resolveTexture(entity, BASE_TEXTURE, false);
    }
}
