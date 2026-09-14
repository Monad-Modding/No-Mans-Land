package com.farcr.nomansland.common.networking.alchemist_tools;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.registry.NMLParticleTypes;
import com.farcr.nomansland.common.registry.NMLSounds;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record ClientboundRitualPickResponsePacket(List<BlockPos> positions, List<BlockPos> scannedPositions) implements CustomPacketPayload {
    public static final Type<ClientboundRitualPickResponsePacket> TYPE = new Type<>(NoMansLand.location("client/ritual_pick/response"));
    public static final StreamCodec<ByteBuf, ClientboundRitualPickResponsePacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list()), ClientboundRitualPickResponsePacket::positions,
            BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list()), ClientboundRitualPickResponsePacket::scannedPositions,
            ClientboundRitualPickResponsePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handleData(IPayloadContext context) {
        context.enqueueWork( () -> {
            ClientLevel level = Minecraft.getInstance().level;

            ObjectOpenHashSet<BlockPos> scannedSet = new ObjectOpenHashSet<>(this.scannedPositions);
            for (BlockPos position : scannedSet) {
                Vec3 center = position.getCenter();
                double smokeSpread = 0.8;
                for (int i = 0; i < 2; i++) {
                    double x = center.x() + Mth.randomBetween(level.random, -1.0f, 1.0f) * smokeSpread,
                           y = center.y() + Mth.randomBetween(level.random, -1.0f, 1.0f) * smokeSpread,
                           z = center.z() + Mth.randomBetween(level.random, -1.0f, 1.0f) * smokeSpread;

                    level.addAlwaysVisibleParticle(
                            NMLParticleTypes.RITUAL_PICK_SMOKE.get(),
                            x, y, z, 0, 0, 0
                    );
                }
            }

            ObjectOpenHashSet<BlockPos> positionsSet = new ObjectOpenHashSet<>(this.positions);
            for (BlockPos position : positionsSet) {
                Vec3 center = position.getCenter();
                double resonanceSpread = 0.3f;
                level.addAlwaysVisibleParticle(
                        NMLParticleTypes.RITUAL_PICK_RESONANCE.get(),
                        center.x + (level.random.nextFloat() - 0.5) * resonanceSpread,
                        center.y + (level.random.nextFloat() - 0.5) * resonanceSpread,
                        center.z + (level.random.nextFloat() - 0.5) * resonanceSpread,
                        0, 0, 0
                );

                level.playLocalSound(
                        center.x, center.y, center.z,
                        NMLSounds.RITUAL_PICK_GLINT.get(), SoundSource.PLAYERS,
                        0.6F, 0.9F + level.random.nextFloat() * 0.3F, false
                );

                double dustSpread = 0.4;
                for (int i = 0; i < 6; i++) {
                    double x = center.x() + Mth.randomBetween(level.random, -1.0f, 1.0f) * dustSpread,
                           y = center.y() + Mth.randomBetween(level.random, -1.0f, 1.0f) * dustSpread,
                           z = center.z() + Mth.randomBetween(level.random, -1.0f, 1.0f) * dustSpread;

                    level.addAlwaysVisibleParticle(
                            NMLParticleTypes.RITUAL_PICK_DUST.get(),
                            x, y, z, 0, 0, 0
                    );
                }
            }
        });
    }
}
