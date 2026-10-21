package com.farcr.nomansland.client.animation;

import com.farcr.nomansland.common.item.BandageItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class BandageAnimations {

    private static final float LENGTH = 1.0F;
    private static final float TICKS_PER_SECOND = 20.0F;
    private static final float MODEL_TOP = 1.501F;
    private static final float[] TIMES = {0.0F, 1.0F / 12.0F, 2.0F / 12.0F, 3.0F / 12.0F, 4.0F / 12.0F, 5.0F / 12.0F, 6.0F / 12.0F, 7.0F / 12.0F, 8.0F / 12.0F, 9.0F / 12.0F, 10.0F / 12.0F, 11.0F / 12.0F, 1.0F};
    private static final float[][] FIRST_PERSON_LEFT = {{85, -2.5F, 5}, {80, -7.5F, 7.5F}, {70, -11.16F, 9.33F}, {65, -12.5F, 10}, {70, -11.16F, 9.33F}, {80, -7.5F, 7.5F}, {85, -2.5F, 5}, {80, 2.5F, 2.5F}, {70, 6.16F, 0.67F}, {65, 7.5F, 0}, {70, 6.16F, 0.67F}, {80, 2.5F, 2.5F}, {85, -2.5F, 5}};
    private static final float[][] FIRST_PERSON_RIGHT = {{65, 2.5F, -5}, {70, 7.5F, -2.5F}, {80, 11.16F, -0.67F}, {85, 12.5F, 0}, {80, 11.16F, -0.67F}, {70, 7.5F, -2.5F}, {65, 2.5F, -5}, {70, -2.5F, -7.5F}, {80, -6.16F, -9.33F}, {85, -7.5F, -10}, {80, -6.16F, -9.33F}, {70, -2.5F, -7.5F}, {65, 2.5F, -5}};
    private static final float[][] THIRD_PERSON_LEFT = {{70, -2.5F, 5}, {65, -7.5F, 7.5F}, {55, -11.16F, 9.33F}, {50, -12.5F, 10}, {55, -11.16F, 9.33F}, {65, -7.5F, 7.5F}, {70, -2.5F, 5}, {65, 2.5F, 2.5F}, {55, 6.16F, 0.67F}, {50, 7.5F, 0}, {55, 6.16F, 0.67F}, {65, 2.5F, 2.5F}, {70, -2.5F, 5}};
    private static final float[][] THIRD_PERSON_RIGHT = {{50, 2.5F, -5}, {55, 7.5F, -2.5F}, {65, 11.16F, -0.67F}, {70, 12.5F, 0}, {65, 11.16F, -0.67F}, {55, 7.5F, -2.5F}, {50, 2.5F, -5}, {55, -2.5F, -7.5F}, {65, -6.16F, -9.33F}, {70, -7.5F, -10}, {65, -6.16F, -9.33F}, {55, -2.5F, -7.5F}, {50, 2.5F, -5}};
    private static final float[] FIRST_PERSON_ARM_OFFSET = {0, 0, -1.5F};
    private static final float[] THIRD_PERSON_OFFSET = {0, -1, -3};
    private static final float THIRD_PERSON_LEAN = -15.0F;
    private static final float[] SELF_WRAPPED_TIMES = {0.0F, 1.0F / 6.0F, 2.0F / 6.0F, 3.0F / 6.0F, 4.0F / 6.0F, 5.0F / 6.0F, 1.0F};
    private static final float[][] SELF_WRAPPED = {{65, -32.5F, 4.5F}, {65, -33.75F, 8.83F}, {65, -36.25F, 8.83F}, {65, -37.5F, 4.5F}, {65, -36.25F, 0.17F}, {65, -33.75F, 0.17F}, {65, -32.5F, 4.5F}};
    private static final float[] SELF_WRAPPED_OFFSET = {0, 0, -2};
    private static final float[] SELF_WRAPPING_TIMES = {0.0F, 1.0F / 12.0F, 2.0F / 12.0F, 4.0F / 12.0F, 5.0F / 12.0F, 6.0F / 12.0F, 7.0F / 12.0F, 8.0F / 12.0F, 10.0F / 12.0F, 11.0F / 12.0F, 1.0F};
    private static final float[][] SELF_WRAPPING = {{85, 70, -15}, {77.5F, 70, -15}, {55, 70, -15}, {62.5F, 70, -15}, {77.5F, 70, -15}, {85, 70, -15}, {77.5F, 70, -15}, {55, 70, -15}, {62.5F, 70, -15}, {77.5F, 70, -15}, {85, 70, -15}};
    private static final float[][] SELF_WRAPPING_OFFSET = {{0, 1, -1}, {-2, 0.5F, -1}, {0, -1, -1}, {0.87F, -0.5F, -1}, {0.87F, 0.5F, -1}, {0, 1, -1}, {-2, 0.5F, -1}, {0, -1, -1}, {0.87F, -0.5F, -1}, {0.87F, 0.5F, -1}, {0, 1, -1}};
    private static final Int2IntOpenHashMap TARGETS = new Int2IntOpenHashMap();

    private static MultiBufferSource handBuffers;
    private static int handLight;

    private BandageAnimations() {
    }

    public static void setHandContext(MultiBufferSource buffers, int light) {
        handBuffers = buffers;
        handLight = light;
    }

    public static void setTarget(int player, int target) {
        TARGETS.put(player, target);
    }

    public static boolean isHealingOther(LivingEntity entity) {
        if (!(entity instanceof Player player)) return false;
        if (!player.isUsingItem() || !(player.getUseItem().getItem() instanceof BandageItem)) {
            TARGETS.remove(player.getId());
            return false;
        }
        return TARGETS.getOrDefault(player.getId(), -1) >= 0;
    }

    public static boolean isHealingSelf(LivingEntity entity) {
        if (!(entity instanceof Player player) || !player.isUsingItem() || !(player.getUseItem().getItem() instanceof BandageItem)) return false;
        return TARGETS.getOrDefault(player.getId(), -1) < 0;
    }

    public static void poseThirdPersonSelf(HumanoidModel<?> model, Player player, float partialTick) {
        float time = time(player, partialTick);
        HumanoidArm wrapping = player.getUsedItemHand() == InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
        boolean mirrored = wrapping == HumanoidArm.LEFT;
        posePart(mirrored ? model.leftArm : model.rightArm, sample(SELF_WRAPPING_TIMES, SELF_WRAPPING, time), sample(SELF_WRAPPING_TIMES, SELF_WRAPPING_OFFSET, time), mirrored);
        posePart(mirrored ? model.rightArm : model.leftArm, sample(SELF_WRAPPED_TIMES, SELF_WRAPPED, time), SELF_WRAPPED_OFFSET, mirrored);
    }

    private static void posePart(ModelPart part, float[] rotation, float[] position, boolean mirrored) {
        if (mirrored) {
            rotate(part, new float[]{rotation[0], -rotation[1], -rotation[2]});
            offset(part, new float[]{-position[0], position[1], position[2]});
        } else {
            rotate(part, rotation);
            offset(part, position);
        }
    }

    public static void poseThirdPerson(HumanoidModel<?> model, Player player, float partialTick) {
        float time = time(player, partialTick);
        model.head.xRot -= THIRD_PERSON_LEAN * Mth.DEG_TO_RAD;
        offset(model.head, THIRD_PERSON_OFFSET);
        model.hat.copyFrom(model.head);
        model.body.xRot -= THIRD_PERSON_LEAN * Mth.DEG_TO_RAD;
        offset(model.body, THIRD_PERSON_OFFSET);
        rotate(model.leftArm, sample(TIMES, THIRD_PERSON_LEFT, time));
        offset(model.leftArm, THIRD_PERSON_OFFSET);
        rotate(model.rightArm, sample(TIMES, THIRD_PERSON_RIGHT, time));
        offset(model.rightArm, THIRD_PERSON_OFFSET);
    }

    public static void renderFirstPersonSelf(PoseStack pose, AbstractClientPlayer player, HumanoidArm arm, ItemStack stack, float partialTick) {
        float flip = arm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        float progress = selfProgress(player, stack, partialTick);
        renderSelfOffHand(pose, player, flip, progress, partialTick);
        renderSelfMainHand(pose, player, flip, progress, partialTick);
        swaySelf(pose, flip, progress);
        pose.translate(-0.1F * flip, 0.3F, 0.3F);
        pose.mulPose(Axis.XP.rotationDegrees(-90.0F));
    }

    private static void renderSelfMainHand(PoseStack pose, AbstractClientPlayer player, float flip, float progress, float partialTick) {
        pose.pushPose();
        swaySelf(pose, flip, progress);
        pose.translate(0.3F * flip, -0.5F, 0.0F);
        pose.mulPose(Axis.ZP.rotationDegrees(-6.0F * flip));
        renderVanillaHand(pose, player, flip > 0.0F, partialTick);
        pose.popPose();
    }

    private static void renderSelfOffHand(PoseStack pose, AbstractClientPlayer player, float flip, float progress, float partialTick) {
        pose.pushPose();
        pose.translate(-0.1F * flip, -0.66F + Mth.sin(progress * 25.0F + Mth.PI) / 30.0F, -0.6F + Mth.cos(progress * 25.0F + Mth.PI) / 30.0F);
        pose.mulPose(Axis.ZP.rotationDegrees(-93.0F));
        pose.mulPose(Axis.XP.rotationDegrees(-5.0F));
        pose.translate(-0.4F * flip, -0.4F, 0.0F);
        pose.mulPose(Axis.ZP.rotationDegrees(6.0F * flip));
        renderVanillaHand(pose, player, flip < 0.0F, partialTick);
        pose.popPose();
    }

    private static void swaySelf(PoseStack pose, float flip, float progress) {
        float raise = Mth.clamp(progress * 10.0F, 0.0F, 1.0F);
        pose.translate(0.4F * flip, -0.9F + raise * 0.4F + Mth.sin(progress * 25.0F) / 7.0F, -0.6F + Mth.cos(progress * 25.0F) / 5.0F);
        pose.mulPose(Axis.ZP.rotationDegrees((90.0F * raise + Mth.sin(progress * 25.0F + 6.0F) * 8.0F) * flip));
        pose.mulPose(Axis.XP.rotationDegrees(-20.0F + Mth.sin(progress * 25.0F + 6.0F) * 8.0F));
    }

    private static float selfProgress(AbstractClientPlayer player, ItemStack stack, float partialTick) {
        float remaining = player.getUseItemRemainingTicks() - partialTick + 1.0F;
        return 1.0F - remaining / stack.getUseDuration(player);
    }

    private static void renderVanillaHand(PoseStack pose, AbstractClientPlayer player, boolean right, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        PlayerRenderer renderer = (PlayerRenderer) minecraft.getEntityRenderDispatcher().getRenderer(player);
        MultiBufferSource buffers = handBuffers != null ? handBuffers : minecraft.renderBuffers().bufferSource();
        int light = handBuffers != null ? handLight : minecraft.getEntityRenderDispatcher().getPackedLightCoords(player, partialTick);
        if (right) {
            renderer.renderRightHand(pose, buffers, light, player);
        } else {
            renderer.renderLeftHand(pose, buffers, light, player);
        }
    }

    public static void renderFirstPersonOther(PoseStack pose, AbstractClientPlayer player, HumanoidArm arm, ItemStack stack, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        PlayerRenderer renderer = (PlayerRenderer) minecraft.getEntityRenderDispatcher().getRenderer(player);
        PlayerModel<AbstractClientPlayer> model = renderer.getModel();
        MultiBufferSource buffers = handBuffers != null ? handBuffers : minecraft.renderBuffers().bufferSource();
        int light = handBuffers != null ? handLight : minecraft.getEntityRenderDispatcher().getPackedLightCoords(player, partialTick);
        float time = time(player, partialTick);
        pose.pushPose();
        pose.translate(0.0F, MODEL_TOP - player.getEyeHeight(), 0.0F);
        pose.scale(-1.0F, -1.0F, 1.0F);
        renderArm(pose, buffers, light, player, model.rightArm, model.rightSleeve, PlayerModelPart.RIGHT_SLEEVE, sample(TIMES, FIRST_PERSON_RIGHT, time));
        renderArm(pose, buffers, light, player, model.leftArm, model.leftSleeve, PlayerModelPart.LEFT_SLEEVE, sample(TIMES, FIRST_PERSON_LEFT, time));
        boolean left = arm == HumanoidArm.LEFT;
        ModelPart hand = left ? model.leftArm : model.rightArm;
        hand.translateAndRotate(pose);
        pose.mulPose(Axis.XP.rotationDegrees(-90.0F));
        pose.mulPose(Axis.YP.rotationDegrees(180.0F));
        pose.translate((left ? -1.0F : 1.0F) / 16.0F, 0.125F, -0.625F);
        minecraft.getItemRenderer().renderStatic(player, stack, left ? ItemDisplayContext.THIRD_PERSON_LEFT_HAND : ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, left, pose, buffers, player.level(), light, OverlayTexture.NO_OVERLAY, player.getId());
        pose.popPose();
        model.rightArm.resetPose();
        model.leftArm.resetPose();
        model.rightSleeve.resetPose();
        model.leftSleeve.resetPose();
        pose.scale(0.0F, 0.0F, 0.0F);
    }

    private static void renderArm(PoseStack pose, MultiBufferSource buffers, int light, AbstractClientPlayer player, ModelPart arm, ModelPart sleeve, PlayerModelPart part, float[] rotation) {
        ResourceLocation skin = player.getSkin().texture();
        arm.resetPose();
        arm.visible = true;
        rotate(arm, rotation);
        offset(arm, FIRST_PERSON_ARM_OFFSET);
        sleeve.copyFrom(arm);
        sleeve.visible = player.isModelPartShown(part);
        arm.render(pose, buffers.getBuffer(RenderType.entitySolid(skin)), light, OverlayTexture.NO_OVERLAY);
        if (sleeve.visible) {
            sleeve.render(pose, buffers.getBuffer(RenderType.entityTranslucent(skin)), light, OverlayTexture.NO_OVERLAY);
        }
    }

    private static float time(Player player, float partialTick) {
        float seconds = (player.getTicksUsingItem() + partialTick) / TICKS_PER_SECOND;
        return seconds % LENGTH;
    }

    private static void rotate(ModelPart part, float[] rotation) {
        part.xRot = -rotation[0] * Mth.DEG_TO_RAD;
        part.yRot = -rotation[1] * Mth.DEG_TO_RAD;
        part.zRot = rotation[2] * Mth.DEG_TO_RAD;
    }

    private static void offset(ModelPart part, float[] position) {
        part.x -= position[0];
        part.y -= position[1];
        part.z += position[2];
    }

    private static float[] sample(float[] times, float[][] keys, float time) {
        int last = times.length - 1;
        int index = 0;
        while (index < last - 1 && time >= times[index + 1]) {
            index++;
        }
        float span = times[index + 1] - times[index];
        float u = Mth.clamp((time - times[index]) / span, 0.0F, 1.0F);
        float[] p0 = keys[Math.max(index - 1, 0)];
        float[] p1 = keys[index];
        float[] p2 = keys[index + 1];
        float[] p3 = keys[Math.min(index + 2, last)];
        float[] out = new float[3];
        for (int axis = 0; axis < 3; axis++) {
            out[axis] = catmullRom(p0[axis], p1[axis], p2[axis], p3[axis], u);
        }
        return out;
    }

    private static float catmullRom(float p0, float p1, float p2, float p3, float u) {
        float u2 = u * u;
        float u3 = u2 * u;
        return 0.5F * (2.0F * p1 + (p2 - p0) * u + (2.0F * p0 - 5.0F * p1 + 4.0F * p2 - p3) * u2 + (3.0F * p1 - p0 - 3.0F * p2 + p3) * u3);
    }
}
