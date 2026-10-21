package com.farcr.nomansland.common.registry.entities;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.entity.variant_action.SetAntlerLayer;
import com.farcr.nomansland.common.entity.variant_action.SetBuddyMushroom;
import com.farcr.nomansland.common.entity.variant_action.SetGrubBug;
import com.farcr.nomansland.common.entity.variant_action.SetPatternLayer;
import dev.tazer.mixed_litter.MLRegistries;
import dev.tazer.mixed_litter.actions.VariantActionType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class NMLVariantActions {
    public static final DeferredRegister<VariantActionType> ACTIONS = DeferredRegister.create(MLRegistries.VARIANT_ACTION_TYPES, NoMansLand.MODID);

    public static final Supplier<SetAntlerLayer> SET_ANTLER_LAYER = ACTIONS.register("set_antler_layer", SetAntlerLayer::new);
    public static final Supplier<SetPatternLayer> SET_PATTERN_LAYER = ACTIONS.register("set_pattern_layer", SetPatternLayer::new);
    public static final Supplier<SetBuddyMushroom> SET_BUDDY_MUSHROOM = ACTIONS.register("set_buddy_mushroom", SetBuddyMushroom::new);
    public static final Supplier<SetGrubBug> SET_GRUB_BUG = ACTIONS.register("set_grub_bug", SetGrubBug::new);
}
