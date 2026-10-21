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
        if (bell.shaking) {
            float swing = Mth.sin((bell.ticks + partialTick) / Mth.PI) / (4.0F + (bell.ticks + partialTick) / 3.0F);
            Direction positiveDirection = switch (facing) {
                case NORTH -> Direction.SOUTH;
                case EAST -> Direction.EAST;
                case SOUTH -> Direction.NORTH;
                case WEST -> Direction.WEST;
                default -> facing;
            };
            float direction = bell.clickDirection == positiveDirection ? 1 : -1;
            xRotation = swing * direction;
        }

        poseStack.pushPose();
        int yRotation = switch (facing) { case EAST -> 90; case SOUTH -> 180; case WEST -> 270; default -> 0; };
        poseStack.rotateAround(Axis.YP.rotationDegrees(yRotation), 0.5F, 0.5F, 0.5F);
        poseStack.translate(0.5, 0.875, 0.5);
        poseStack.mulPose(Axis.XP.rotation(xRotation));
        poseStack.translate(-0.5, -0.875, -0.5);

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
