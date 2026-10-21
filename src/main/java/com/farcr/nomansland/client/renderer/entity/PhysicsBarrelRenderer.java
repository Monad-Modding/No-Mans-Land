package com.farcr.nomansland.client.renderer.entity;

import com.farcr.nomansland.common.block.PhysicsBarrelBlock;
import com.farcr.nomansland.common.entity.barrel.PhysicsBarrel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

public class PhysicsBarrelRenderer extends EntityRenderer<PhysicsBarrel> {
    public static final int FLASH_OVERLAY = OverlayTexture.pack(2, 10);
    private static final int FLASH_PERIOD = 5;
    private static final int SWELL_TICKS = 10;
    private static final float SWELL = 0.3F;

    private final BlockRenderDispatcher blockRenderer;

    public PhysicsBarrelRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.5F;
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    public static boolean isFlashing(float fuseLeft) {
        return Mth.floor(fuseLeft) / FLASH_PERIOD % 2 == 0;
    }

    public static boolean isSwelling(float fuseLeft) {
        return fuseLeft + 1.0F < SWELL_TICKS;
    }

    public static void renderBarrel(BlockRenderDispatcher blockRenderer, BlockState state, boolean lit, float fuseLeft, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        if (lit && isSwelling(fuseLeft)) {
            float progress = Mth.clamp(1.0F - (fuseLeft + 1.0F) / SWELL_TICKS, 0.0F, 1.0F);
            progress *= progress;
            progress *= progress;
            float scale = 1.0F + progress * SWELL;
            poseStack.scale(scale, scale, scale);
        }
        poseStack.translate(-0.5F, -0.5F, -0.5F);
        int overlay = lit && isFlashing(fuseLeft) ? FLASH_OVERLAY : OverlayTexture.NO_OVERLAY;
        BlockState shown = state.hasProperty(PhysicsBarrelBlock.LIT) ? state.setValue(PhysicsBarrelBlock.LIT, false) : state;
        blockRenderer.renderSingleBlock(shown, poseStack, bufferSource, packedLight, overlay);
        poseStack.popPose();
    }

    @Override
    public void render(PhysicsBarrel entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(-0.5F, 0.0F, -0.5F);
        renderBarrel(this.blockRenderer, entity.getBlockState(), entity.isLit(), entity.getFuse() - partialTick, poseStack, bufferSource, packedLight);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(PhysicsBarrel entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
