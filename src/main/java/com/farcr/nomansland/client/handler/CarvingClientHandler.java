package com.farcr.nomansland.client.handler;

import com.farcr.nomansland.client.renderer.rendertype.ChiselRenderTypes;
import com.farcr.nomansland.common.carving.CarvingType;
import com.farcr.nomansland.common.item.ChiselItem;
import com.farcr.nomansland.common.networking.ServerBoundChiselPacket;
import com.farcr.nomansland.common.registry.NMLRegistries;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.ryanhcode.sable.companion.ClientSubLevelAccess;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3d;

public class CarvingClientHandler {
    public static CarvingClientHandler instance = new CarvingClientHandler();
    private static final Vector3d GLOBAL_MIN = new Vector3d();
    private static final Quaternionf ORIENTATION = new Quaternionf();

    @Nullable
    private BlockPos startPos = null;
    @Nullable
    private BlockPos endPos = null;
    @Nullable
    private Direction direction = null;
    @Nullable
    private ResourceLocation id = null;
    @Nullable
    private InteractionHand hand = null;
    private boolean isChiseling = false;


    public void tick() {
        if(!this.isChiseling) {
            return;
        }

        LocalPlayer player = Minecraft.getInstance().player;
        if(player == null) {
            this.clear();
            return;
        }

        if(this.endPos != null) {
            AABB aabb = AABB.encapsulatingFullBlocks(this.startPos, this.endPos);
            Vec3 pos = player.getEyePosition();
            SubLevelAccess subLevel = SableCompanion.INSTANCE.getContaining(player.level(), aabb.getCenter());
            if(subLevel != null) {
                pos = subLevel.logicalPose().transformPositionInverse(pos);
            }

            double interactionRange = player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE);
            if(Math.sqrt(aabb.distanceToSqr(pos)) > interactionRange) {
                this.clear();
                return;
            }
        }

        if(!(player.getItemInHand(this.hand).getItem() instanceof ChiselItem)) {
            this.clear();
            return;
        }

        BlockHitResult result = (BlockHitResult) Minecraft.getInstance().hitResult;
        if(result.getType() == HitResult.Type.MISS) {
            return;
        }

        BlockPos newEndPos = result.getBlockPos();
        if(!newEndPos.equals(this.endPos)) {
            CarvingType type = NMLRegistries.CARVING_TYPE.get(this.id);
            AABB placementBox = type.getPlacementBox(player, this.startPos, newEndPos, this.direction);
            BlockPos min = new BlockPos((int) Math.floor(placementBox.minX), (int) Math.floor(placementBox.minY), (int) Math.floor(placementBox.minZ));
            BlockPos max = new BlockPos((int) Math.floor(placementBox.maxX) - 1, (int) Math.floor(placementBox.maxY) - 1, (int) Math.floor(placementBox.maxZ) - 1);

            Iterable<BlockPos> iterator = BlockPos.betweenClosed(min, max);
            for (BlockPos pos : iterator) {
                if(!type.canReplace(player.level().getBlockState(pos))) {
                    return;
                }
            }

            this.endPos = newEndPos;
        }

    }

    public void render(PoseStack poseStack, LevelRenderer levelRenderer, Camera camera, DeltaTracker deltaTracker) {
        if(this.endPos == null) return;
        LocalPlayer player = Minecraft.getInstance().player;
        CarvingType type = NMLRegistries.CARVING_TYPE.get(this.id);
        MultiBufferSource.BufferSource bufferSource = levelRenderer.renderBuffers.bufferSource();

        poseStack.pushPose();
        Vec3 cameraPosition = camera.getPosition();
        poseStack.translate(-cameraPosition.x, -cameraPosition.y, -cameraPosition.z);
        AABB previewBox = type.getPreviewBox(player, this.startPos, this.endPos, this.direction);

        Vec3 min = previewBox.getCenter().subtract(previewBox.getXsize() / 2, previewBox.getYsize() / 2, previewBox.getZsize() / 2);
        ClientSubLevelAccess subLevel = SableCompanion.INSTANCE.getContainingClient(min);
        if(subLevel != null) {
            Vector3d globalMin = subLevel.renderPose().transformPosition(GLOBAL_MIN.set(previewBox.minX, previewBox.minY, previewBox.minZ));
            previewBox = previewBox.move(-min.x, -min.y, -min.z);
            poseStack.translate(globalMin.x(), globalMin.y(), globalMin.z());
            poseStack.mulPose(subLevel.renderPose().orientation().get(ORIENTATION));
        }

        LevelRenderer.renderLineBox(poseStack, bufferSource.getBuffer(ChiselRenderTypes.LINES), previewBox.inflate(0.001f), 0.65f, 0.68f, 0.38f, 1.0f);
        int light = LevelRenderer.getLightColor(player.level(), this.startPos.relative(this.direction));
        renderBox(poseStack, previewBox, bufferSource, light);

        poseStack.popPose();
    }

    public void startChisel(BlockPos startPos, Direction direction, InteractionHand hand, ResourceLocation id) {
        this.startPos = startPos;
        this.direction = direction;
        this.id = id;
        this.hand = hand;
        this.isChiseling = true;

        LocalPlayer player = Minecraft.getInstance().player;
        if(player != null) {
            player.displayClientMessage(Component.translatable("nomansland.carving.hint"), true);
        }
    }

    public void endChisel() {
        if(this.isChiseling) {
            if(this.endPos != null) {
                PacketDistributor.sendToServer(new ServerBoundChiselPacket(this.startPos, this.endPos, this.direction, ResourceKey.create(NMLRegistries.CARVING_TYPE_KEY, this.id)));
            }
            this.clear();
        }
    }

    public void clear() {
        this.startPos = null;
        this.endPos = null;
        this.direction = null;
        this.id = null;
        this.hand = null;
        this.isChiseling = false;
    }

    public boolean isChiseling() {
        return isChiseling;
    }

    public @Nullable InteractionHand getHand() {
        return hand;
    }

    private void renderBox(PoseStack poseStack, AABB box, MultiBufferSource bufferSource, int light) {
        poseStack.pushPose();
        VertexConsumer builder = bufferSource.getBuffer(ChiselRenderTypes.OVERLAY);

        float minX = (float) box.minX;
        float minY = (float) box.minY;
        float minZ = (float) box.minZ;
        float maxX = (float) box.maxX;
        float maxY = (float) box.maxY;
        float maxZ = (float) box.maxZ;
        int color = 0xffffffff;
        float w = (float) box.getXsize();
        float h = (float) box.getYsize();
        float l = (float) box.getZsize();
        PoseStack.Pose pose = poseStack.last();

        // South
        builder.addVertex(pose, minX, minY, maxZ).setColor(color).setUv(0, h).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 0, 1);
        builder.addVertex(pose, maxX, minY, maxZ).setColor(color).setUv(w, h).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 0, 1);
        builder.addVertex(pose, maxX, maxY, maxZ).setColor(color).setUv(w, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 0, 1);
        builder.addVertex(pose, minX, maxY, maxZ).setColor(color).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 0, 1);

        // North
        builder.addVertex(pose, maxX, minY, minZ).setColor(color).setUv(0, h).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 0, -1);
        builder.addVertex(pose, minX, minY, minZ).setColor(color).setUv(w, h).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 0, -1);
        builder.addVertex(pose, minX, maxY, minZ).setColor(color).setUv(w, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 0, -1);
        builder.addVertex(pose, maxX, maxY, minZ).setColor(color).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 0, -1);

        // East
        builder.addVertex(pose, maxX, minY, maxZ).setColor(color).setUv(0, h).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 1, 0, 0);
        builder.addVertex(pose, maxX, minY, minZ).setColor(color).setUv(l, h).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 1, 0, 0);
        builder.addVertex(pose, maxX, maxY, minZ).setColor(color).setUv(l, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 1, 0, 0);
        builder.addVertex(pose, maxX, maxY, maxZ).setColor(color).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 1, 0, 0);

        // West
        builder.addVertex(pose, minX, minY, minZ).setColor(color).setUv(0, h).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, -1, 0, 0);
        builder.addVertex(pose, minX, minY, maxZ).setColor(color).setUv(l, h).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, -1, 0, 0);
        builder.addVertex(pose, minX, maxY, maxZ).setColor(color).setUv(l, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, -1, 0, 0);
        builder.addVertex(pose, minX, maxY, minZ).setColor(color).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, -1, 0, 0);

        // Up
        builder.addVertex(pose, maxX, maxY, minZ).setColor(color).setUv(0, l).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
        builder.addVertex(pose, minX, maxY, minZ).setColor(color).setUv(w, l).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
        builder.addVertex(pose, minX, maxY, maxZ).setColor(color).setUv(w, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
        builder.addVertex(pose, maxX, maxY, maxZ).setColor(color).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);

        // Down
        builder.addVertex(pose, maxX, minY, maxZ).setColor(color).setUv(0, l).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, -1, 0);
        builder.addVertex(pose, minX, minY, maxZ).setColor(color).setUv(w, l).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, -1, 0);
        builder.addVertex(pose, minX, minY, minZ).setColor(color).setUv(w, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, -1, 0);
        builder.addVertex(pose, maxX, minY, minZ).setColor(color).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, -1, 0);


        poseStack.popPose();
    }
}
