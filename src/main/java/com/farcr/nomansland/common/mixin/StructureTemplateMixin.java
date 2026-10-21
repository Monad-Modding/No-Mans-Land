package com.farcr.nomansland.common.mixin;

import com.farcr.nomansland.common.world.structure.StructureErosion;
import com.farcr.nomansland.common.world.structure.StructurePlaceSettingsErosionHolder;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(StructureTemplate.class)
public class StructureTemplateMixin {
    @Final @Shadow
    private List<StructureTemplate.Palette> palettes;
    @Unique
    private static final ThreadLocal<LongSet> mod$fluidPositions =
            ThreadLocal.withInitial(LongOpenHashSet::new);

    @Inject(method = "placeInWorld", at = @At("HEAD"))
    private void mod$buildFluidSet(
            ServerLevelAccessor serverLevel,
            BlockPos offset,
            BlockPos pos,
            StructurePlaceSettings settings,
            RandomSource random,
            int flags,
            CallbackInfoReturnable<Boolean> cir
    ) {
        StructureErosion.Type erosionType = ((StructurePlaceSettingsErosionHolder) settings).nml$getErosionType();
        LongSet set = mod$fluidPositions.get();
        set.clear();
        if (erosionType == StructureErosion.Type.NONE || this.palettes.isEmpty()) return;
        StructureErosion.buildFluidPositionSet(
                set,
                serverLevel, offset, pos, settings, random,
                this.palettes, (StructureTemplate) (Object) this
        );
    }

    @WrapOperation(
            method = "placeInWorld",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/ServerLevelAccessor;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z",
                    ordinal = 1
            )
    )
    private boolean mod$erodeBlock(
            ServerLevelAccessor level,
            BlockPos pos,
            BlockState state,
            int flags,
            Operation<Boolean> original,
            @Local(argsOnly = true) StructurePlaceSettings settings
    ) {
        StructureErosion.Type erosionType = ((StructurePlaceSettingsErosionHolder) settings).nml$getErosionType();
        if (erosionType != StructureErosion.Type.NONE) {
            boolean containingFluid = StructureErosion.isContainingFluid(level, pos, mod$fluidPositions.get());
            if (StructureErosion.shouldErode(erosionType, level, pos, containingFluid)) return false;
        }
        return original.call(level, pos, state, flags);
    }
}
