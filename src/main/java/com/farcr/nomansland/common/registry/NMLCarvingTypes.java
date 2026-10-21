package com.farcr.nomansland.common.registry;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.carving.AncestralCarvingCarvingType;
import com.farcr.nomansland.common.carving.AncestralEffigyCarvingType;
import com.farcr.nomansland.common.carving.CarvingType;
import com.farcr.nomansland.common.carving.MoonCarvingCarvingType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class NMLCarvingTypes {
    public static final DeferredRegister<CarvingType> CARVING_TYPES = DeferredRegister.create(NMLRegistries.CARVING_TYPE_KEY, NoMansLand.MODID);

    public static final Supplier<AncestralCarvingCarvingType> ANCESTRAL_CARVING = CARVING_TYPES
            .register("ancestral_carving", AncestralCarvingCarvingType::new);

    public static final Supplier<AncestralEffigyCarvingType> ANCESTRAL_EFFIGY = CARVING_TYPES
            .register("ancestral_effigy", AncestralEffigyCarvingType::new);

    public static final Supplier<MoonCarvingCarvingType> MOON_CARVING = CARVING_TYPES
            .register("moon_carving", MoonCarvingCarvingType::new);

}
