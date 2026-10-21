package com.farcr.nomansland.common.mixin;

import com.farcr.nomansland.common.extension.BeardifierExtension;
import com.farcr.nomansland.common.world.structure.MeetingPointStructure;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.Beardifier;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(Beardifier.class)
public abstract class BeardifierMixin implements BeardifierExtension {
    @Unique
    private static final double ALTAR_BLEND_PADDING = 16.0;
    @Unique
    private static final double ALTAR_BLEND_STRENGTH = 2.0;
    @Unique
    private static final double ALTAR_BLEND_BAND = 2.0;

    @Unique
    private List<AltarBeard> nomansland$altarBeards = List.of();

    @Unique
    private static final ThreadLocal<List<AltarBeard>> nomansland$pendingAltars =
        ThreadLocal.withInitial(ArrayList::new);

    @Override
    public void nomansland$setAltarBeards(List<AltarBeard> beards) {
        this.nomansland$altarBeards = beards;
    }

    @Override
    public List<AltarBeard> nomansland$altarBeards() {
        return this.nomansland$altarBeards;
    }

    @Inject(method = "forStructuresInChunk", at = @At("HEAD"))
    private static void nml$resetPendingAltars(
        StructureManager structureManager,
        ChunkPos chunkPos,
        CallbackInfoReturnable<Beardifier> cir
    ) {
        nomansland$pendingAltars.get().clear();
    }

    @WrapOperation(
        method = "lambda$forStructuresInChunk$2",
        at = @At(
            value = "NEW",
            target = "(Lnet/minecraft/world/level/levelgen/structure/BoundingBox;Lnet/minecraft/world/level/levelgen/structure/TerrainAdjustment;I)Lnet/minecraft/world/level/levelgen/Beardifier$Rigid;",
            ordinal = 1
        )
    )
    private static Beardifier.Rigid nml$processMeetingPointPiece(
        BoundingBox box,
        TerrainAdjustment adjustment,
        int groundLevelDelta,
        Operation<Beardifier.Rigid> original,
        @Local(argsOnly = true) StructureStart start,
        @Local StructurePiece piece
    ) {
        if (start.getStructure() instanceof MeetingPointStructure) {
            List<StructurePiece> pieces = start.getPieces();
            if (!pieces.isEmpty()) {
                if (piece == pieces.getFirst()) {
                    int xSpan = box.maxX() - box.minX() + 1;
                    int zSpan = box.maxZ() - box.minZ() + 1;
                    int centerX = (box.minX() + box.maxX()) / 2;
                    int centerZ = (box.minZ() + box.maxZ()) / 2;
                    int targetY = box.minY() + groundLevelDelta;
                    double radius = Math.max(xSpan, zSpan) / 2.0 + ALTAR_BLEND_PADDING;
                    nomansland$pendingAltars.get().add(new AltarBeard(centerX, centerZ, targetY, radius));
                    return original.call(box, TerrainAdjustment.NONE, groundLevelDelta);
                } else {
                    return original.call(box, TerrainAdjustment.BEARD_THIN, groundLevelDelta);
                }
            }
        }
        return original.call(box, adjustment, groundLevelDelta);
    }

    @ModifyReturnValue(method = "forStructuresInChunk", at = @At("RETURN"))
    private static Beardifier nml$attachAltarBeards(Beardifier beardifier) {
        List<AltarBeard> pending = nomansland$pendingAltars.get();
        if (!pending.isEmpty()) {
            ((BeardifierExtension) beardifier).nomansland$setAltarBeards(List.copyOf(pending));
            pending.clear();
        }
        return beardifier;
    }

    @ModifyReturnValue(method = "compute", at = @At("RETURN"))
    private double nml$applyAltarBlend(double original, DensityFunction.FunctionContext context) {
        List<AltarBeard> altars = this.nomansland$altarBeards;
        if (altars.isEmpty()) return original;
        int x = context.blockX();
        int y = context.blockY();
        int z = context.blockZ();
        double bias = 0.0;
        for (AltarBeard altar : altars) {
            double dx = x - altar.centerX();
            double dz = z - altar.centerZ();
            double r2 = dx * dx + dz * dz;
            double maxR2 = altar.radius() * altar.radius();
            if (r2 >= maxR2) continue;
            double t = Math.sqrt(r2) / altar.radius();
            double radialWeight = 1.0 - t * t * (3.0 - 2.0 * t);
            double depth = altar.targetY() - y;
            double verticalShape;
            if (depth >= ALTAR_BLEND_BAND) {
                verticalShape = 1.0;
            } else if (depth <= -ALTAR_BLEND_BAND) {
                verticalShape = -1.0;
            } else {
                double s = depth / ALTAR_BLEND_BAND;
                double a = Math.abs(s);
                verticalShape = Math.copySign(a * a * (3.0 - 2.0 * a), s);
            }
            bias += verticalShape * radialWeight * ALTAR_BLEND_STRENGTH;
        }
        return original + bias;
    }
}
