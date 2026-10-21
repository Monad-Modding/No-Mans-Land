package com.farcr.nomansland.common.mixin;

import com.farcr.nomansland.common.entity.buddy.BuddyChunkAnchor;
import com.farcr.nomansland.common.extension.LevelChunkExtension;
import com.farcr.nomansland.common.mixin.plugin.annotation.IfModAbsent;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import com.farcr.nomansland.common.worldevent.SunDog;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ServerLevelData;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.farcr.nomansland.common.block.FrostedGrassBlock.SNOWLOGGED;
import static net.minecraft.world.level.block.SnowyDirtBlock.SNOWY;

@IfModAbsent("snowrealmagic")
@Mixin(value = ServerLevel.class)
public abstract class ServerLevelMixin {

    @Shadow public abstract ServerLevel getLevel();

    @Inject(method = "tickChunk", at = @At(value = "TAIL"))
    private void nml$tickChunk(LevelChunk chunk, int randomTickSpeed, CallbackInfo ci) {
        LevelChunkExtension extensionChunk = (LevelChunkExtension) chunk;
        if (extensionChunk.nml$shouldIgnoreBuddyAnchor()) return;

        BuddyChunkAnchor.getOrDefault(this.getLevel()).tickChunk(chunk);
    }

    @Shadow @Final private ServerLevelData serverLevelData;

    @Inject(method = "tickPrecipitation", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z", ordinal = 2, shift = At.Shift.BEFORE), cancellable = true)
    private void nml$tickPrecipitation(BlockPos pos, CallbackInfo ci) {
        ServerLevel level = this.getLevel();
        pos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos);
        BlockState state = level.getBlockState(pos);

        if (state.is(Blocks.SHORT_GRASS) || state.is(NMLBlocks.FROSTED_GRASS.get())) {
            level.setBlockAndUpdate(pos, NMLBlocks.FROSTED_GRASS.get().defaultBlockState().setValue(SNOWLOGGED, true));
            BlockPos posBelow = pos.below();
            BlockState stateUnder = level.getBlockState(posBelow);
            if (stateUnder.getBlock() instanceof SnowyDirtBlock)
                level.setBlockAndUpdate(posBelow, stateUnder.setValue(SNOWY, true));
            ci.cancel();
        }
    }

    @Inject(method = "advanceWeatherCycle",
            at = @At(value = "FIELD",
                    target = "Lnet/minecraft/network/protocol/game/ClientboundGameEventPacket;STOP_RAINING:Lnet/minecraft/network/protocol/game/ClientboundGameEventPacket$Type;",
                    opcode = Opcodes.GETSTATIC
            )
    )
    private void nml$advanceWeatherCycle(CallbackInfo ci) {
        SunDog.getOrDefault((ServerLevel) (Object) this).maybeStartFromStormEnd();
    }

    @Inject(method = "setDayTime",
            at = @At(value = "HEAD")
    )
    private void nml$setDayTime(long dayTime, CallbackInfo ci) {
        if (serverLevelData.getDayTime() != dayTime && (dayTime == 0 || dayTime == 24000L)) {
            SunDog.getOrDefault((ServerLevel) (Object) this).maybeStartFromMorning();
        }
    }
}
