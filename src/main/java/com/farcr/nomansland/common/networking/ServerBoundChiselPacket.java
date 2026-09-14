package com.farcr.nomansland.common.networking;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.carving.CarvingType;
import com.farcr.nomansland.common.item.ChiselItem;
import com.farcr.nomansland.common.registry.NMLRegistries;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerBoundChiselPacket(BlockPos start, BlockPos end, Direction direction, ResourceKey<CarvingType> carvingType) implements CustomPacketPayload {
    public static final StreamCodec<ByteBuf, ServerBoundChiselPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ServerBoundChiselPacket::start,
            BlockPos.STREAM_CODEC, ServerBoundChiselPacket::end,
            Direction.STREAM_CODEC, ServerBoundChiselPacket::direction,
            ResourceKey.streamCodec(NMLRegistries.CARVING_TYPE_KEY), ServerBoundChiselPacket::carvingType,
            ServerBoundChiselPacket::new
    );

    public static final Type<ServerBoundChiselPacket> TYPE = new Type<>(NoMansLand.location("server/chisel"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handleData(IPayloadContext context) {
        Player player = context.player();
        ServerLevel level = (ServerLevel) player.level();

        if(!ChiselItem.holdingChisel(player)) {
            return;
        }

        CarvingType type = NMLRegistries.CARVING_TYPE.get(this.carvingType);
        if(type == null) {
            return;
        }

        AABB placementBox = type.getPlacementBox(player, this.start, this.end, this.direction);
        BlockPos min = new BlockPos((int) Math.floor(placementBox.minX), (int) Math.floor(placementBox.minY), (int) Math.floor(placementBox.minZ));
        BlockPos max = new BlockPos((int) Math.floor(placementBox.maxX) - 1, (int) Math.floor(placementBox.maxY) - 1, (int) Math.floor(placementBox.maxZ) - 1);

        Vec3 playerPos = player.getEyePosition();
        SubLevelAccess subLevel = SableCompanion.INSTANCE.getContaining(level, placementBox.getCenter());
        if(subLevel != null) {
            playerPos = subLevel.logicalPose().transformPositionInverse(playerPos);
        }

        double interactionRange = player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE);
        if(Math.sqrt(AABB.encapsulatingFullBlocks(min, max).distanceToSqr(playerPos)) > interactionRange) {
            return;
        }

        if(type.placeBlocks((ServerPlayer) player, this.start, this.end, min, max, this.direction)) {
            return;
        }

        Iterable<BlockPos> iterator = BlockPos.betweenClosed(min, max);
        BlockParticleOption options = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState());
        for (BlockPos pos : iterator) {
            Vec3 center = pos.getCenter();
            level.sendParticles(options, center.x, center.y, center.z, 50, 0.15, 0.15, 0.15, 1.0f);
            sendOutlineParticles(level, options, pos);
        }

        Vec3 pos = placementBox.getCenter();
        level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.STONE_BREAK, SoundSource.BLOCKS);

        type.afterPlacement(player, min, max, this.direction);
    }

    private static void sendOutlineParticles(ServerLevel level, BlockParticleOption options, BlockPos pos) {
        for (int a = 0; a <= 1; a++) {
            for (int b = 0; b <= 1; b++) {
                level.sendParticles(options, pos.getX() + 0.5, pos.getY() + a, pos.getZ() + b, 4, 0.5, 0.02, 0.02, 0.5f);
                level.sendParticles(options, pos.getX() + a, pos.getY() + 0.5, pos.getZ() + b, 4, 0.02, 0.5, 0.02, 0.5f);
                level.sendParticles(options, pos.getX() + a, pos.getY() + b, pos.getZ() + 0.5, 4, 0.02, 0.02, 0.5, 0.5f);
            }
        }
    }
}
