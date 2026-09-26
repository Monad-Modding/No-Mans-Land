package com.farcr.nomansland.client.renderer.entity;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.block.AncientBronzeBellBlock;
import com.farcr.nomansland.common.blockentity.AncientBronzeBellBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.client.RenderTypeHelper;
import net.neoforged.neoforge.client.model.data.ModelData;

public class AncientBronzeBellRenderer implements BlockEntityRenderer<AncientBronzeBellBlockEntity> {
    public static final ModelResourceLocation FLOOR_MOVING = model("ancient_bronze_bell_floor_moving");
    public static final ModelResourceLocation WALL_MOVING = model("ancient_bronze_bell_wall_moving");
    private final BlockRenderDispatcher blockRenderer;

    public AncientBronzeBellRenderer(BlockEntityRendererProvider.Context context) {
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    private static ModelResourceLocation model(String name) {
        return ModelResourceLocation.standalone(NoMansLand.location("block/dungeon/" + name));
    }

    @Override
    public void render(AncientBronzeBellBlockEntity bell, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int packedLight, int packedOverlay) {
        BlockState state = bell.getBlockState();
        Direction facing = state.getValue(AncientBronzeBellBlock.FACING);
        boolean wall = state.getValue(AncientBronzeBellBlock.ATTACHED);
        float xRotation = 0;
        float zRotation = 0;
        if (bell.shaking) {
            float swing = Mth.sin((bell.ticks + partialTick) / Mth.PI) / (4.0F + (bell.ticks + partialTick) / 3.0F);
            Direction direction = bell.clickDirection;
            if (facing.getAxis() == Direction.Axis.Z) {
                xRotation = direction == Direction.SOUTH ? swing : -swing;
            } else {
                zRotation = direction == Direction.WEST ? swing : -swing;
            }
        }

        poseStack.pushPose();
        poseStack.translate(0.5, 0.875, 0.5);
        poseStack.mulPose(Axis.XP.rotation(xRotation));
        poseStack.mulPose(Axis.ZP.rotation(zRotation));
        poseStack.translate(-0.5, -0.875, -0.5);
        int yRotation = wall
                ? switch (facing) { case NORTH -> 90; case EAST -> 180; case SOUTH -> 270; default -> 0; }
                : switch (facing) { case EAST -> 90; case SOUTH -> 180; case WEST -> 270; default -> 0; };
        poseStack.rotateAround(Axis.YP.rotationDegrees(yRotation), 0.5F, 0.5F, 0.5F);

        renderModel(bell, wall ? WALL_MOVING : FLOOR_MOVING, poseStack, buffers, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private void renderModel(AncientBronzeBellBlockEntity bell, ModelResourceLocation location, PoseStack poseStack, MultiBufferSource buffers, int packedLight, int packedOverlay) {
        BakedModel model = blockRenderer.getBlockModelShaper().getModelManager().getModel(location);
        for (RenderType renderType : model.getRenderTypes(bell.getBlockState(), RandomSource.create(), ModelData.EMPTY)) {
            blockRenderer.getModelRenderer().renderModel(poseStack.last(), buffers.getBuffer(RenderTypeHelper.getEntityRenderType(renderType, true)),
                    bell.getBlockState(), model, 1, 1, 1, packedLight, packedOverlay);
        }
    }

    @Override
    public AABB getRenderBoundingBox(AncientBronzeBellBlockEntity blockEntity) {
        return new AABB(blockEntity.getBlockPos()).inflate(1);
    }
}
