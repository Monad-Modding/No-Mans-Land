package com.farcr.nomansland.common.registry.worldgen;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.world.structure.MeetingPointStructurePlacement;
import com.farcr.nomansland.common.world.structure.bell_sanctuary.BellSanctuaryStructurePlacement;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class NMLStructurePlacements {
    public static final DeferredRegister<StructurePlacementType<?>> STRUCTURE_PLACEMENTS =
            DeferredRegister.create(Registries.STRUCTURE_PLACEMENT, NoMansLand.MODID);

    public static final Supplier<StructurePlacementType<MeetingPointStructurePlacement>> MEETING_POINT = register("meeting_point", MeetingPointStructurePlacement.CODEC);

    public static final Supplier<StructurePlacementType<BellSanctuaryStructurePlacement>> BELL_SANCTUARY = register("bell_sanctuary", BellSanctuaryStructurePlacement.CODEC);

    private static <P extends StructurePlacement> DeferredHolder<StructurePlacementType<?>, StructurePlacementType<P>> register (String name, MapCodec<P> codec) {
        return STRUCTURE_PLACEMENTS.register(name, () -> () -> codec);
    }
}
