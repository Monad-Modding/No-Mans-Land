package com.farcr.nomansland.common.commands;

import com.farcr.nomansland.common.extension.ChunkGeneratorStructureStateExtension;
import com.farcr.nomansland.common.friend.FriendMoon;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import org.jetbrains.annotations.Nullable;

public class MeetingPointCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("meetingpoint")
            .requires(stack -> stack.hasPermission(2))
                .executes(MeetingPointCommand::query)
                .then(Commands.literal("set")
                    .then(Commands.argument("pos", BlockPosArgument.blockPos())
                        .executes(context -> set(context, BlockPosArgument.getLoadedBlockPos(context, "pos")))))
                .then(Commands.literal("reset")
                    .executes(MeetingPointCommand::reset))
        );
    }

    private static int query(CommandContext<CommandSourceStack> context) {
        BlockPos current = FriendMoon.getMeetingPointPosition(context.getSource().getLevel());
        if (current == null) {
            context.getSource().sendSuccess(() -> Component.literal("No meeting point exists in this world"), false);
            return 0;
        }
        context.getSource().sendSuccess(() -> Component.literal(
            "Meeting point is at " + current.getX() + ", " + current.getZ()), false);
        return 1;
    }

    private static int set(CommandContext<CommandSourceStack> context, BlockPos pos) {
        ChunkPos chunkPos = new ChunkPos(pos);
        apply(context.getSource().getServer(), chunkPos);

        BlockPos center = chunkPos.getMiddleBlockPosition(0);
        context.getSource().sendSuccess(() -> Component.literal(
            "Meeting point moved to " + center.getX() + ", " + center.getZ()), true);
        return 1;
    }

    private static int reset(CommandContext<CommandSourceStack> context) {
        apply(context.getSource().getServer(), null);
        context.getSource().sendSuccess(() -> Component.literal("Meeting point reset to its generated position"), true);
        return 1;
    }

    private static void apply(MinecraftServer server, @Nullable ChunkPos pos) {
        ServerLevel overworld = server.overworld();
        FriendMoon.getOrDefault(overworld).setMeetingPointOverride(pos);
        extension(overworld).nomansland$setMeetingPointPosition(pos);
        refreshClients(overworld);
    }

    public static void applyPersistedOverride(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        ChunkPos override = FriendMoon.getOrDefault(overworld).getMeetingPointOverride();
        if (override != null) {
            extension(overworld).nomansland$setMeetingPointPosition(override);
        }
    }

    private static ChunkGeneratorStructureStateExtension extension(ServerLevel level) {
        ChunkGeneratorStructureState state = level.getChunkSource().getGeneratorState();
        return (ChunkGeneratorStructureStateExtension) state;
    }

    private static void refreshClients(ServerLevel overworld) {
        FriendMoon friendMoon = FriendMoon.getOrDefault(overworld);
        overworld.players().forEach(friendMoon::playerSendShadowPacket);
    }
}
