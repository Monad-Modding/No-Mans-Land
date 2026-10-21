package com.farcr.nomansland.common.world.structure;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.List;

public class StructureErosion {
    public static void buildFluidPositionSet(LongSet set,
                                             ServerLevelAccessor serverLevel,
                                             BlockPos offset, BlockPos pos,
                                             StructurePlaceSettings settings,
                                             RandomSource random,
                                             List<StructureTemplate.Palette> palettes,
                                             StructureTemplate template) {
        if (palettes.isEmpty()) return;
        for (StructureTemplate.StructureBlockInfo info : StructureTemplate.processBlockInfos(
                serverLevel, offset, pos, settings,
                settings.getRandomPalette(palettes, offset).blocks(),
                template
        )) {
            if (!info.state().getFluidState().isEmpty()) set.add(info.pos().asLong());
        }

    }

    private static final Direction[] FLUID_FLOW_DIRECTIONS = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.UP};
    public static boolean isContainingFluid(ServerLevelAccessor level, BlockPos pos, LongSet fluidHolders) {
        BlockPos.MutableBlockPos mutableBlockPos = pos.mutable();
        for (Direction fluidFlowDirection : FLUID_FLOW_DIRECTIONS) {
            mutableBlockPos.set(pos).move(fluidFlowDirection);
            // check neighbors for any fluids
            if (fluidHolders.contains(mutableBlockPos.asLong())) return true;
            if (!level.getBlockState(mutableBlockPos).getFluidState().isEmpty()) return true;
        }
        // and ourselves too
        if (fluidHolders.contains(mutableBlockPos.asLong())) return true;

        return false;
    }

    public static boolean shouldErode(Type erosionType, ServerLevelAccessor level, BlockPos pos, boolean containingFluid) {
        if (containingFluid) return false;
        switch (erosionType) {
            case CAVES_ONLY -> { return level.getBlockState(pos).is(Blocks.CAVE_AIR); }
            case ALL -> { return level.getBlockState(pos).isAir(); }
            default -> { return false; }
        }
    }

    public enum Type implements StringRepresentable {
        NONE("none"),
        CAVES_ONLY("caves_only"),
        ALL("all");

        public static Codec<StructureErosion.Type> CODEC = StringRepresentable.fromValues(StructureErosion.Type::values);
        private final String name;

        Type(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() { return this.name; }
    }
}
