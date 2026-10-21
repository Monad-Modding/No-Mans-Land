package com.farcr.nomansland.client;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.renderer.entity.PhysicsBarrelRenderer;
import com.farcr.nomansland.client.sable.SableBarrelGhosts;
import com.farcr.nomansland.common.block.PhysicsBarrelBlock;
import com.farcr.nomansland.common.integration.Mods;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@EventBusSubscriber(modid = NoMansLand.MODID, value = Dist.CLIENT)
public final class BarrelGhosts {
    private static final int READY_GRACE = 2;
    private static final int MAX_AGE = 40;
    private static final List<Ghost> GHOSTS = new ArrayList<>();

    private BarrelGhosts() {
    }

    public static void add(BlockPos world, BlockPos plot, BlockState state, long fuseEnd) {
        if (!Mods.SABLE.isLoaded()) return;
        GHOSTS.add(new Ghost(world, plot, state, fuseEnd));
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (GHOSTS.isEmpty()) return;
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            GHOSTS.clear();
            return;
        }
        Iterator<Ghost> iterator = GHOSTS.iterator();
        while (iterator.hasNext()) {
            Ghost ghost = iterator.next();
            ghost.age++;
            if (ghost.ready >= 0 || SableBarrelGhosts.isShown(level, ghost.plot)) {
                ghost.ready++;
            }
            if (ghost.ready > READY_GRACE || ghost.age > MAX_AGE) {
                iterator.remove();
            }
        }
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (GHOSTS.isEmpty() || event.getStage() != RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) return;
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) return;
        Vec3 camera = event.getCamera().getPosition();
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource buffer = minecraft.renderBuffers().bufferSource();
        for (Ghost ghost : GHOSTS) {
            poseStack.pushPose();
            Matrix4f transform = SableBarrelGhosts.transform(level, ghost.plot, camera, partialTick);
            if (transform == null) {
                transform = SableBarrelGhosts.transform(level, ghost.world, camera, partialTick);
            }
            if (transform != null) {
                poseStack.mulPose(transform);
            } else {
                poseStack.translate(ghost.world.getX() - camera.x, ghost.world.getY() - camera.y, ghost.world.getZ() - camera.z);
            }
            boolean lit = ghost.fuseEnd > 0 && ghost.state.hasProperty(PhysicsBarrelBlock.LIT) && ghost.state.getValue(PhysicsBarrelBlock.LIT);
            float fuseLeft = ghost.fuseEnd - (level.getGameTime() + partialTick);
            PhysicsBarrelRenderer.renderBarrel(minecraft.getBlockRenderer(), ghost.state, lit, fuseLeft, poseStack, buffer, LevelRenderer.getLightColor(level, ghost.world));
            poseStack.popPose();
        }
        buffer.endBatch();
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        GHOSTS.clear();
    }

    private static final class Ghost {
        private final BlockPos world;
        private final BlockPos plot;
        private final BlockState state;
        private final long fuseEnd;
        private int age;
        private int ready = -1;

        private Ghost(BlockPos world, BlockPos plot, BlockState state, long fuseEnd) {
            this.world = world;
            this.plot = plot;
            this.state = state;
            this.fuseEnd = fuseEnd;
        }
    }
}
