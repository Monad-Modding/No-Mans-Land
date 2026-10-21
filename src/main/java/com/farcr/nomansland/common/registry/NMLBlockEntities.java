package com.farcr.nomansland.common.registry;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.blockentity.*;
import com.farcr.nomansland.common.blockentity.anchor.MonsterAnchorBlockEntity;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class NMLBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, NoMansLand.MODID);

    public static final Supplier<BlockEntityType<TapBlockEntity>> TAP =
            BLOCK_ENTITIES.register("tap", () ->
                    BlockEntityType.Builder.of(TapBlockEntity::new, NMLBlocks.TAP.get()).build(null));

    public static final Supplier<BlockEntityType<MonsterAnchorBlockEntity>> MONSTER_ANCHOR =
            BLOCK_ENTITIES.register("monster_anchor", () ->
                    BlockEntityType.Builder.of(MonsterAnchorBlockEntity::new, NMLBlocks.MONSTER_ANCHOR.get()).build(null));

    public static final Supplier<BlockEntityType<WardingEffigyBlockEntity>> WARDING_EFFIGY =
            BLOCK_ENTITIES.register("warding_effigy", () ->
                    BlockEntityType.Builder.of(WardingEffigyBlockEntity::new, NMLBlocks.WARDING_EFFIGY.get()).build(null));

    public static final Supplier<BlockEntityType<RemainsBlockEntity>> REMAINS =
            BLOCK_ENTITIES.register("remains", () ->
                    BlockEntityType.Builder.of(RemainsBlockEntity::new, NMLBlocks.REMAINS.get()).build(null));

    public static final Supplier<BlockEntityType<PotBlockEntity>> POT =
            BLOCK_ENTITIES.register("pot", () ->
                    BlockEntityType.Builder.of(PotBlockEntity::new, NMLBlocks.ANCIENT_POT.get(), NMLBlocks.LARGE_ANCIENT_POT.get()).build(null));

    public static final Supplier<BlockEntityType<MoonlightBasinBlockEntity>> MOONLIGHT_BASIN =
        BLOCK_ENTITIES.register("moonlight_basin", () ->
            BlockEntityType.Builder.of(MoonlightBasinBlockEntity::new, NMLBlocks.MOONLIGHT_BASIN.get()).build(null));

    public static final Supplier<BlockEntityType<InvertedBellControllerBlockEntity>> INVERTED_BELL =
        BLOCK_ENTITIES.register("inverted_bell", () ->
                BlockEntityType.Builder.of(InvertedBellControllerBlockEntity::new, NMLBlocks.INVERTED_BELL.get()).build(null));

    public static final Supplier<BlockEntityType<AncientBronzeBellBlockEntity>> ANCIENT_BRONZE_BELL =
            BLOCK_ENTITIES.register("ancient_bronze_bell", () ->
                    BlockEntityType.Builder.of(AncientBronzeBellBlockEntity::new, NMLBlocks.ANCIENT_BRONZE_BELL.get()).build(null));

    public static final Supplier<BlockEntityType<MoonCarvingBlockEntity>> MOON_CARVING =
        BLOCK_ENTITIES.register("moon_carving", () ->
            BlockEntityType.Builder.of(MoonCarvingBlockEntity::new, NMLBlocks.MOON_CARVING.get()).build(null));

    public static void addBlockEntities(final BlockEntityTypeAddBlocksEvent event) {
        event.modify(
                BlockEntityType.SIGN,
                NMLBlocks.MAPLE.sign().get(), NMLBlocks.MAPLE.wallSign().get(),
                NMLBlocks.PINE.sign().get(), NMLBlocks.PINE.wallSign().get(),
                NMLBlocks.WALNUT.sign().get(), NMLBlocks.WALNUT.wallSign().get(),
                NMLBlocks.WILLOW.sign().get(), NMLBlocks.WILLOW.wallSign().get()
        );

        event.modify(
                BlockEntityType.HANGING_SIGN,
                NMLBlocks.MAPLE.hangingSign().get(), NMLBlocks.MAPLE.hangingWallSign().get(),
                NMLBlocks.PINE.hangingSign().get(), NMLBlocks.PINE.hangingWallSign().get(),
                NMLBlocks.WALNUT.hangingSign().get(), NMLBlocks.WALNUT.hangingWallSign().get(),
                NMLBlocks.WILLOW.hangingSign().get(), NMLBlocks.WILLOW.hangingWallSign().get()
        );
    }
}
