package com.farcr.nomansland.common.networking.dream;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.dreams.DreamType;
import com.farcr.nomansland.common.dreams.dreamlevel.DreamLevelHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public record ClientboundDimensionSyncPacket(
    Set<ResourceKey<Level>> newLevelSet
) implements CustomPacketPayload {
    public static final StreamCodec<ByteBuf, ClientboundDimensionSyncPacket> STREAM_CODEC = StreamCodec.composite(
        ResourceKey.streamCodec(Registries.DIMENSION).apply(
            ByteBufCodecs.collection(HashSet::new)
        ),
        ClientboundDimensionSyncPacket::newLevelSet,
        ClientboundDimensionSyncPacket::new
    );

    public static final Type<ClientboundDimensionSyncPacket> TYPE = new Type<>(NoMansLand.location("client/sync_dimension"));

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handleData(IPayloadContext context) {
        if (context.flow().isClientbound()) {
            context.enqueueWork(() -> {
                LocalPlayer player = Minecraft.getInstance().player;
                Set<ResourceKey<Level>> dimensionList = player.connection.levels();
                // remove any levels not in
                dimensionList.removeAll(dimensionList.stream().filter(
                    (levelKey) -> !newLevelSet.contains(levelKey)
                ).collect(Collectors.toSet()));
                // should add in order hopefully !!!
                for (ResourceKey<Level> level : newLevelSet) {
                    dimensionList.add(level);
                    // Handle Dimension Type Registration (if possible)
                    Optional<DreamType> potentialDream = DreamLevelHandler.keyToDream(level);
                    potentialDream.ifPresent(
                        (dreamType) -> {
                            DreamLevelHandler.registerDimensionType(
                                context.player().registryAccess(), dreamType,
                                context.player(), level.location()
                            );
                        }
                    );
                    /*
                        This can and will fail if other mods dynamically add dimensions without
                        properly registering themselves! but this is not my fault. just please
                        keep in mind you need to register both the dimension AND DimensionType
                        else desyncs will happen in multiplayer.

                        this is an important note to leave in case anyone is having compatibility issues / respawn crashes
                    */
                }
                PacketDistributor.sendToServer(new ServerboundDreamAcknowledgePacket());
            });
        }
    }
}
