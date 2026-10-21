package com.farcr.nomansland.common.commands;

import com.farcr.nomansland.common.dreams.DreamManager;
import com.farcr.nomansland.common.dreams.DreamType;
import com.farcr.nomansland.common.registry.NMLRegistries;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

public class DreamCommand {
    private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.nomansland.dream.clear.fail"));
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        dispatcher.register(Commands.literal("dream")
            .requires(stack -> stack.hasPermission(2))
                .then(
                    Commands.argument("targets", EntityArgument.players())
                        .then(
                            Commands.argument("dream_type", ResourceArgument.resource(context, NMLRegistries.DREAM_TYPE_KEY))
                                .then(Commands.literal("start").executes(DreamCommand::startDream))
                                .then(Commands.literal("clear").executes(DreamCommand::clearDream))
                        )
                )
        );
    }

    public static int startDream(CommandContext<CommandSourceStack> stackCommandContext) throws CommandSyntaxException {
        Collection<ServerPlayer> playerCollection = EntityArgument.getPlayers(stackCommandContext, "targets");
        Holder.Reference<DreamType> dreamTypeResult =
            ResourceArgument.getResource(stackCommandContext, "dream_type", NMLRegistries.DREAM_TYPE_KEY);
        DreamType dreamType = NMLRegistries.DREAM_TYPE.get(dreamTypeResult.key);
        int i = 0;
        for (ServerPlayer player : playerCollection) {
            DreamManager manager = DreamManager.getOrDefault(Objects.requireNonNull(player.getServer()));
            manager.forceNotify(dreamType, player);
            manager.flagForStart(dreamType, player);
            i++;
        }
        CommandSourceStack source = stackCommandContext.getSource();
        final int players = i;
        if (i > 1) {
            source.sendSuccess(() -> Component.translatable(
                "commands.nomansland.dream.start.pass_count",
                dreamTypeResult.key().location().toString(), players
            ), true);
        }
        Optional<ServerPlayer> player = playerCollection.stream().findFirst();
        player.ifPresent((serverPlayer) -> source.sendSuccess(() ->
            Component.translatable(
                "commands.nomansland.dream.start.pass",
                dreamTypeResult.key().location().toString(), serverPlayer.getDisplayName()
            ), true)
        );
        return players;
    }

    public static int clearDream(CommandContext<CommandSourceStack> stackCommandContext) throws CommandSyntaxException {
        Collection<ServerPlayer> playerCollection = EntityArgument.getPlayers(stackCommandContext, "targets");
        Holder.Reference<DreamType> dreamTypeResult =
            ResourceArgument.getResource(stackCommandContext, "dream_type", NMLRegistries.DREAM_TYPE_KEY);
        int i = (int) playerCollection.stream().filter(player ->
            DreamManager.getOrDefault(Objects.requireNonNull(player.getServer())).clearPlayerStorage(player,
                NMLRegistries.DREAM_TYPE.get(dreamTypeResult.key())) != null).count();
        CommandSourceStack source = stackCommandContext.getSource();

        if (i > 1) {
            source.sendSuccess(() -> Component.translatable(
                "commands.nomansland.dream.clear.pass_count",
                dreamTypeResult.key().location().toString(), i
            ), true);
            return i;
        }
        if (i > 0) {
            Optional<ServerPlayer> player = playerCollection.stream().findFirst();
            player.ifPresent((serverPlayer) -> source.sendSuccess(() ->
                Component.translatable(
                    "commands.nomansland.dream.clear.pass",
                    dreamTypeResult.key().location().toString(), serverPlayer.getDisplayName()
                ), true)
            );
            return i;
        }
        throw ERROR_FAILED.create();
    }
}
