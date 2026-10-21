package com.farcr.nomansland.client.renderer;

import com.farcr.nomansland.client.renderer.entity.PhysicsBarrelRenderer;
import com.farcr.nomansland.common.block.PhysicsBarrelBlock;
import com.farcr.nomansland.common.blockentity.PhysicsBarrelBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

public class PhysicsBarrelBlockEntityRenderer implements BlockEntityRenderer<PhysicsBarrelBlockEntity> {
    private final BlockRenderDispatcher blockRenderer;

    public PhysicsBarrelBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(PhysicsBarrelBlockEntity barrel, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        BlockState state = barrel.getBlockState();
        if (!state.getValue(PhysicsBarrelBlock.LIT) || barrel.getLevel() == null) return;
        boolean timed = barrel.getFuseEnd() > 0;
        float fuseLeft = timed ? barrel.getFuseEnd() - (barrel.getLevel().getGameTime() + partialTick) : Float.MAX_VALUE;
        PhysicsBarrelRenderer.renderBarrel(this.blockRenderer, state, timed, fuseLeft, poseStack, bufferSource, packedLight);
    }
}
