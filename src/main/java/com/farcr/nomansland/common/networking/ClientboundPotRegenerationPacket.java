package com.farcr.nomansland.common.networking;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.particle.PotShatterParticle;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;

import java.util.List;

public record ClientboundPotRegenerationPacket(BlockPos pos, ResourceLocation model, int ticks, List<AABB> boxes) implements CustomPacketPayload {

    private static final int MAX_BOXES = 64;
    private static final StreamCodec<ByteBuf, AABB> BOX_CODEC = StreamCodec.composite(
            ByteBufCodecs.VECTOR3F, box -> new Vector3f((float) box.minX, (float) box.minY, (float) box.minZ),
            ByteBufCodecs.VECTOR3F, box -> new Vector3f((float) box.maxX, (float) box.maxY, (float) box.maxZ),
            (min, max) -> new AABB(min.x(), min.y(), min.z(), max.x(), max.y(), max.z())
    );

    public static final StreamCodec<ByteBuf, ClientboundPotRegenerationPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ClientboundPotRegenerationPacket::pos,
            ResourceLocation.STREAM_CODEC, ClientboundPotRegenerationPacket::model,
            ByteBufCodecs.VAR_INT, ClientboundPotRegenerationPacket::ticks,
            BOX_CODEC.apply(ByteBufCodecs.list(MAX_BOXES)), ClientboundPotRegenerationPacket::boxes,
            ClientboundPotRegenerationPacket::new
    );

    public static final Type<ClientboundPotRegenerationPacket> TYPE = new Type<>(NoMansLand.location("client/pot_regeneration"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handleData(IPayloadContext context) {
        if (context.flow().isClientbound()) {
            context.enqueueWork(() -> PotShatterParticle.assemble(this.pos, this.model, this.ticks, this.boxes));
        }
    }
}
