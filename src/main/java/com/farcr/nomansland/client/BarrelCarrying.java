package com.farcr.nomansland.client;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.block.PhysicsBarrelBlock;
import com.farcr.nomansland.common.entity.barrel.PhysicsBarrel;
import com.farcr.nomansland.common.integration.Mods;
import com.farcr.nomansland.common.integration.sable.BarrelPhysics;
import com.farcr.nomansland.common.networking.barrel.ServerboundBarrelPunchPacket;
import com.farcr.nomansland.common.networking.barrel.ServerboundBarrelReleasePacket;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@EventBusSubscriber(modid = NoMansLand.MODID, value = Dist.CLIENT)
public final class BarrelCarrying {
    private static final int TAP_TICKS = 4;
    private static final IntOpenHashSet CARRIERS = new IntOpenHashSet();

    private static boolean holding;
    @Nullable
    private static BlockPos tapping;
    private static int tapTicks;
    @Nullable
    private static BlockPos mining;

    private BarrelCarrying() {
    }

    public static void setCarriers(List<Integer> players) {
        CARRIERS.clear();
        CARRIERS.addAll(players);
    }

    public static boolean isCarrying(Entity entity) {
        return CARRIERS.contains(entity.getId());
    }

    @SubscribeEvent(receiveCanceled = true)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getLevel().isClientSide() || event.getHand() != InteractionHand.MAIN_HAND || !event.getEntity().getMainHandItem().isEmpty()) return;
        if (event.getLevel().getBlockState(event.getPos()).getBlock() instanceof PhysicsBarrelBlock) {
            holding = true;
        }
    }

    @SubscribeEvent(receiveCanceled = true)
    public static void onRightClickEntity(PlayerInteractEvent.EntityInteract event) {
        if (!event.getLevel().isClientSide() || event.getHand() != InteractionHand.MAIN_HAND || !event.getEntity().getMainHandItem().isEmpty()) return;
        if (event.getTarget() instanceof PhysicsBarrel) {
            holding = true;
        }
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (!event.getLevel().isClientSide() || !Mods.SABLE.isLoaded() || event.getAction() != PlayerInteractEvent.LeftClickBlock.Action.START) return;
        BlockPos pos = event.getPos();
        if (pos.equals(mining) || !BarrelPhysics.isBarrelSubLevel(event.getLevel(), pos)) return;
        event.setCanceled(true);
        if (!pos.equals(tapping)) {
            tapping = pos.immutable();
            tapTicks = 0;
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        tickTap();
        tickHold();
    }

    private static void tickTap() {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        boolean attacking = minecraft.options.keyAttack.isDown();
        if (!attacking) {
            mining = null;
        }
        if (tapping == null) return;
        if (player == null || !(minecraft.hitResult instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK || !hit.getBlockPos().equals(tapping)) {
            tapping = null;
            return;
        }
        if (!attacking) {
            player.swing(InteractionHand.MAIN_HAND);
            PacketDistributor.sendToServer(new ServerboundBarrelPunchPacket(tapping));
            tapping = null;
            return;
        }
        if (++tapTicks > TAP_TICKS) {
            mining = tapping;
            tapping = null;
        }
    }

    private static void tickHold() {
        if (!holding) return;
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || !minecraft.options.keyUse.isDown() || !player.getMainHandItem().isEmpty()) {
            holding = false;
            PacketDistributor.sendToServer(ServerboundBarrelReleasePacket.INSTANCE);
            return;
        }
        if (!minecraft.options.getCameraType().isFirstPerson()) return;
        player.swingTime = 0;
        player.swinging = true;
        player.swingingArm = InteractionHand.MAIN_HAND;
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        CARRIERS.clear();
        holding = false;
    }
}
