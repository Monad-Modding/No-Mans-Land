package com.farcr.nomansland.common.networking.alchemist_tools;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.item.RitualPickItem;
import com.farcr.nomansland.common.registry.NMLSounds;
import com.farcr.nomansland.common.registry.NMLTags;
import com.farcr.nomansland.common.registry.items.NMLItems;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.List;

public record ServerboundRitualPickRequestPacket(BlockPos pos, Direction direction, int radius, int depth) implements CustomPacketPayload {
    public static final Type<ServerboundRitualPickRequestPacket> TYPE = new Type<>(NoMansLand.location("client/ritual_pick/request"));
    public static final StreamCodec<ByteBuf, ServerboundRitualPickRequestPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ServerboundRitualPickRequestPacket::pos,
            Direction.STREAM_CODEC, ServerboundRitualPickRequestPacket::direction,
            ByteBufCodecs.VAR_INT, ServerboundRitualPickRequestPacket::radius,
            ByteBufCodecs.VAR_INT, ServerboundRitualPickRequestPacket::depth,
            ServerboundRitualPickRequestPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    boolean validate(Player player) {
        // make sure the numbers are valid
        if (radius > 2 || depth > 17) return false;

        // make sure the item isn't on cooldown!!
        if (player.getCooldowns().isOnCooldown(NMLItems.RITUAL_PICK.get()))
            return false;

        // make sure the player is actually holding the ritual pickaxe
        if (!(player.getItemBySlot(EquipmentSlot.MAINHAND).getItem() instanceof RitualPickItem))
            return false;

        // make sure the player can reach the spot, roughly.
        double distance = Mth.sqrt((float) player.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()));
        double approximateReachDistance = player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE) + 2;
        if (distance >= approximateReachDistance)
            return false;

        return true;
    }

    List<BlockPos> locateOreBlocks(Level level, List<BlockPos> scannedPositions) {
        List<BlockPos> positions = new ArrayList<>(radius * radius * 4 * depth);
        BlockPos.MutableBlockPos mPos = new BlockPos.MutableBlockPos();

        // fetch axis by direction...
        Direction uAxis = switch (direction.getAxis()) {
            case X -> Direction.SOUTH;
            case Z -> Direction.EAST;
            case Y -> Direction.EAST;
        };
        Direction vAxis = switch (direction.getAxis()) {
            case X, Z -> Direction.DOWN;
            case Y -> Direction.SOUTH;
        };

        // look through blocks
        for (int u = -radius; u <= radius; u++) {
            for (int v = -radius; v <= radius; v++) {
                mPos.set(pos).move(uAxis, u).move(vAxis, v);

                // depth search!
                boolean hasTouchedSolidThisColumn = false;
                for (int d = 0; d < depth; d++) {
                    BlockState state = level.getBlockState(mPos);

                    if (!state.isAir()) hasTouchedSolidThisColumn = true;

                    // search doesn't propagate through blocks that don't resonate!
                    // can hide objects or discover caves or something like this.
                    // it also won't resonate through air IF ITS FOUND A SOLID BLOCK THUS FAR!
                    if (state.is(NMLTags.OCCLUDES_RITUAL_PICKAXE_RESONANCE) || (state.isAir() && hasTouchedSolidThisColumn))
                        break;

                    if (!state.isAir()) scannedPositions.add(mPos.immutable());
                    if (state.is(NMLTags.RESONATES_WITH_RITUAL_PICKAXE))
                        positions.add(mPos.immutable());
                    mPos.move(direction);
                }
            }
        }
        return positions;
    }

    public void handleData(IPayloadContext context) {
        context.enqueueWork( () -> {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            // validate state stuff
            if (server == null) return;
            if (!validate(context.player())) return;

            ServerLevel level = server.getLevel(context.player().level().dimension());
            if (level == null) return;

            BlockState struck = level.getBlockState(pos);
            SoundType soundType = struck.getSoundType(level, pos, context.player());
            level.playSound(null, pos, soundType.getHitSound(), SoundSource.BLOCKS, 0.9F, soundType.getPitch() * 0.9F);
            level.playSound(null, pos, NMLSounds.RITUAL_PICK_LOCATE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);

            List<BlockPos> scannedBlocks = new ArrayList<>(radius * radius * 4 * depth);
            List<BlockPos> locatedBlocks = locateOreBlocks(level, scannedBlocks);
            context.player().getCooldowns().addCooldown(NMLItems.RITUAL_PICK.item(), 40);
            PacketDistributor.sendToPlayer(
                    (ServerPlayer) context.player(),
                    new ClientboundRitualPickResponsePacket(locatedBlocks, scannedBlocks)
            );
        });
    }
}
