package com.farcr.nomansland.common.block;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.blockentity.PhysicsBarrelBlockEntity;
import com.farcr.nomansland.common.entity.barrel.PhysicsBarrel;
import com.farcr.nomansland.common.integration.sable.BarrelPhysics;
import com.farcr.nomansland.common.networking.barrel.ClientboundBarrelCarriersPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = NoMansLand.MODID)
public final class PhysicsBarrelCarriers {
    public static final double HOLD_DISTANCE = 2.0;
    private static final double HOLD_RANGE = 6.0;
    private static final int NO_ENTITY = -1;
    private static final Map<UUID, Holding> HOLDERS = new HashMap<>();

    private PhysicsBarrelCarriers() {
    }

    public static void grab(Player player, BlockPos barrel) {
        if (!(player instanceof ServerPlayer server)) return;
        HOLDERS.put(server.getUUID(), new Holding(barrel.immutable(), NO_ENTITY, server.level().dimension()));
        broadcast(server.server);
    }

    public static void grab(Player player, PhysicsBarrel barrel) {
        if (!(player instanceof ServerPlayer server)) return;
        HOLDERS.put(server.getUUID(), new Holding(null, barrel.getId(), server.level().dimension()));
        broadcast(server.server);
    }

    public static void release(ServerPlayer player) {
        if (HOLDERS.remove(player.getUUID()) != null) {
            broadcast(player.server);
        }
    }

    @Nullable
    public static Player holderOf(Level level, BlockPos barrel) {
        for (Map.Entry<UUID, Holding> entry : HOLDERS.entrySet()) {
            if (barrel.equals(entry.getValue().barrel()) && entry.getValue().dimension() == level.dimension()) return level.getPlayerByUUID(entry.getKey());
        }
        return null;
    }

    public static boolean isCarried(PhysicsBarrel barrel) {
        for (Holding holding : HOLDERS.values()) {
            if (holding.entity() == barrel.getId() && holding.dimension() == barrel.level().dimension()) return true;
        }
        return false;
    }

    private static void broadcast(MinecraftServer server) {
        PacketDistributor.sendToAllPlayers(payload(server));
    }

    private static ClientboundBarrelCarriersPacket payload(MinecraftServer server) {
        List<Integer> ids = new ArrayList<>();
        for (UUID id : HOLDERS.keySet()) {
            ServerPlayer player = server.getPlayerList().getPlayer(id);
            if (player != null) {
                ids.add(player.getId());
            }
        }
        return new ClientboundBarrelCarriersPacket(ids);
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (HOLDERS.isEmpty()) return;
        MinecraftServer server = event.getServer();
        boolean changed = false;
        Iterator<Map.Entry<UUID, Holding>> iterator = HOLDERS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Holding> entry = iterator.next();
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            Holding holding = entry.getValue();
            if (!stillHolding(player, holding)) {
                iterator.remove();
                changed = true;
                continue;
            }
            if (holding.barrel() != null) {
                BarrelPhysics.steer((ServerLevel) player.level(), holding.barrel(), player);
            } else if (player.level().getEntity(holding.entity()) instanceof PhysicsBarrel barrel) {
                barrel.steer(player);
            }
        }
        if (changed) {
            broadcast(server);
        }
    }

    private static boolean stillHolding(@Nullable ServerPlayer player, Holding holding) {
        if (player == null || !player.isAlive() || player.isSpectator() || !player.getMainHandItem().isEmpty() || player.level().dimension() != holding.dimension()) return false;
        Level level = player.level();
        Vec3 centre;
        if (holding.barrel() != null) {
            if (!level.isLoaded(holding.barrel()) || !(level.getBlockState(holding.barrel()).getBlock() instanceof PhysicsBarrelBlock)) return false;
            centre = PhysicsBarrelBlockEntity.worldCentre(level, holding.barrel());
        } else {
            if (!(level.getEntity(holding.entity()) instanceof PhysicsBarrel barrel) || barrel.isRemoved()) return false;
            centre = barrel.position().add(0.0, 0.5, 0.0);
        }
        return player.getEyePosition().distanceToSqr(centre) <= HOLD_RANGE * HOLD_RANGE;
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PacketDistributor.sendToPlayer(player, payload(player.server));
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            release(player);
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        HOLDERS.clear();
    }

    private record Holding(@Nullable BlockPos barrel, int entity, ResourceKey<Level> dimension) {
    }
}
