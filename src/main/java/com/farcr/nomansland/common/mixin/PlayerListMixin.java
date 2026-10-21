package com.farcr.nomansland.common.mixin;

import com.farcr.nomansland.common.dreams.dreamlevel.DreamLevelHandler;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerList.class)
public class PlayerListMixin {
    @WrapOperation(method = "placeNewPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getLevel(Lnet/minecraft/resources/ResourceKey;)Lnet/minecraft/server/level/ServerLevel;"))
    private ServerLevel nml$restoreFromDreamOnRejoin(MinecraftServer server, ResourceKey<Level> resourceKey, Operation<ServerLevel> original, @Local(argsOnly = true) ServerPlayer player) {
        if (DreamLevelHandler.keyToDream(resourceKey).isEmpty()) return original.call(server, resourceKey);
        return DreamLevelHandler.playerLoadFallback(player);
    }
}
