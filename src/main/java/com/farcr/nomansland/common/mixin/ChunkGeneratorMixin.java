package com.farcr.nomansland.common.mixin;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.extension.ChunkGeneratorExtension;
import com.farcr.nomansland.common.extension.ChunkGeneratorStructureStateExtension;
import com.farcr.nomansland.common.friend.FriendMoon;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkGenerator.class)
public abstract class ChunkGeneratorMixin implements ChunkGeneratorExtension {
    @Unique
    private ChunkGeneratorStructureState nomansland$structureState = null;

    @Inject(method = "createState", at = @At("HEAD"))
    private void nomansland$publishGeneratorForState(HolderLookup<StructureSet> structureSetLookup, RandomState randomState, long seed, CallbackInfoReturnable<ChunkGeneratorStructureState> cir) {
        ChunkGeneratorStructureStateExtension.CURRENT_GENERATOR.set((ChunkGenerator) (Object) this);
    }

    @Inject(method = "createState", at = @At("RETURN"))
    private void nomansland$attachSelfToState(HolderLookup<StructureSet> structureSetLookup, RandomState randomState, long seed, CallbackInfoReturnable<ChunkGeneratorStructureState> cir) {
        ChunkGeneratorStructureStateExtension.CURRENT_GENERATOR.remove();
        ChunkGeneratorStructureState state = cir.getReturnValue();
        if (state instanceof ChunkGeneratorStructureStateExtension extension) {
            extension.nomansland$setChunkGenerator((ChunkGenerator) (Object) this);
        }
        nomansland$structureState = state;
    }

    @Override
    public @Nullable ChunkGeneratorStructureState nomansland$structureState() {
        return nomansland$structureState;
    }

    @Override
    public void nomansland$setStructureState(ChunkGeneratorStructureState state) {
        this.nomansland$structureState = state;
    }

    @Inject(method = "findNearestMapStructure", at = @At("RETURN"), cancellable = true)
    private void nomansland$locateMeetingPoint(ServerLevel level, HolderSet<Structure> structures, BlockPos origin, int searchRadius, boolean skipKnownStructures, CallbackInfoReturnable<Pair<BlockPos, Holder<Structure>>> cir) {
        Holder<Structure> meetingPoint = null;
        for (Holder<Structure> holder : structures) {
            if (holder.is(NoMansLand.location("meeting_point"))) {
                meetingPoint = holder;
                break;
            }
        }
        if (meetingPoint == null) return;

        BlockPos meetingPointPosition = FriendMoon.getMeetingPointPosition(level);
        if (meetingPointPosition == null) return;

        Pair<BlockPos, Holder<Structure>> found = cir.getReturnValue();
        if (found != null && nomansland$horizontalDistanceSqr(origin, found.getFirst())
        <= nomansland$horizontalDistanceSqr(origin, meetingPointPosition)) return;

        cir.setReturnValue(Pair.of(meetingPointPosition, meetingPoint));
    }

    @Unique
    private static double nomansland$horizontalDistanceSqr(BlockPos from, BlockPos to) {
        double deltaX = from.getX() - to.getX();
        double deltaZ = from.getZ() - to.getZ();
        return (deltaX * deltaX) + (deltaZ * deltaZ);
    }
}
