package com.farcr.nomansland.common.registry.blocks;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.block.*;
import com.farcr.nomansland.common.block.cauldrons.*;
import com.farcr.nomansland.common.block.fruit_trees.FruitBlock;
import com.farcr.nomansland.common.block.fruit_trees.FruitLeavesBlock;
import com.farcr.nomansland.common.block.fruit_trees.FruitType;
import com.farcr.nomansland.common.block.moonlight.MoonlightBasinBlock;
import com.farcr.nomansland.common.block.moonlight.MoonlightCandleBlock;
import com.farcr.nomansland.common.block.pots.LargePotBlock;
import com.farcr.nomansland.common.block.pots.PotBlock;
import com.farcr.nomansland.common.block.pots.PotSize;
import com.farcr.nomansland.common.block.tap.TapBlock;
import com.farcr.nomansland.common.block.torches.*;
import com.farcr.nomansland.common.definitions.BlockDefinition;
import com.farcr.nomansland.common.definitions.BlockProperties;
import com.farcr.nomansland.common.registry.NMLSounds;
import com.farcr.nomansland.common.registry.items.NMLItems;
import com.farcr.nomansland.common.registry.worldgen.NMLFeatures;
import com.farcr.nomansland.common.registry.worldgen.NMLTreeGrowers;
import com.farcr.nomansland.datagen.loot.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockBehaviour.OffsetType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static net.minecraft.world.level.block.state.BlockBehaviour.Properties.of;
import static net.minecraft.world.level.block.state.BlockBehaviour.Properties.ofFullCopy;

@SuppressWarnings("unused")
public class NMLBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(NoMansLand.MODID);
    public static List<BlockDefinition<?>> BLOCK_DEFINITIONS = new ArrayList<>();
    public static List<Woodset> WOODSETS = new ArrayList<>();

    public static final BlockDefinition<VineBlock> CUT_VINE = registerNoItem("cut_vine",
            () -> new VineBlock(of().mapColor(MapColor.PLANT).replaceable().noCollission().strength(0.2F).sound(SoundType.VINE).ignitedByLava().pushReaction(PushReaction.DESTROY)), new BlockProperties(new OtherShearsBlockLootType(() -> Blocks.VINE), false));
    public static final BlockDefinition<CutSugarCaneBlock> CUT_SUGAR_CANE = registerNoItem("cut_sugar_cane",
            () -> new CutSugarCaneBlock(of().mapColor(MapColor.PLANT).noCollission().instabreak().sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY)), new BlockProperties(new OtherBlockLootType(() -> Blocks.SUGAR_CANE), false));

    //Decorations
    public static final BlockDefinition<SconceTorchBlock> SCONCE_TORCH = registerNoItem("sconce_torch",
            () -> new SconceTorchBlock(ParticleTypes.FLAME, ofFullCopy(Blocks.TORCH).sound(NMLSounds.SCONCE_TORCH)), new BlockProperties(new SelfBlockLootType(), false));
    public static final BlockDefinition<ExtinguishedSconceTorchBlock> EXTINGUISHED_SCONCE_TORCH = registerNoItem("extinguished_sconce_torch",
            () -> new ExtinguishedSconceTorchBlock(ParticleTypes.FLAME, of().noCollission().instabreak().sound(NMLSounds.SCONCE_TORCH).pushReaction(PushReaction.DESTROY).noLootTable()), BlockProperties.custom(true));
    public static final BlockDefinition<SconceWallTorchBlock> SCONCE_WALL_TORCH = registerNoItem("sconce_wall_torch",
            () -> new SconceWallTorchBlock(ParticleTypes.FLAME, ofFullCopy(Blocks.WALL_TORCH).sound(NMLSounds.SCONCE_TORCH).lootFrom(SCONCE_TORCH)), BlockProperties.custom(true));
    public static final BlockDefinition<ExtinguishedSconceWallTorchBlock> EXTINGUISHED_SCONCE_WALL_TORCH = registerNoItem("extinguished_sconce_wall_torch",
            () -> new ExtinguishedSconceWallTorchBlock(ParticleTypes.FLAME, of().noCollission().instabreak().sound(NMLSounds.SCONCE_TORCH).pushReaction(PushReaction.DESTROY).noLootTable()), BlockProperties.custom(true));
    public static final BlockDefinition<SconceTorchBlock> SCONCE_SOUL_TORCH = registerNoItem("sconce_soul_torch",
            () -> new SconceTorchBlock(ParticleTypes.SOUL_FIRE_FLAME, ofFullCopy(Blocks.SOUL_TORCH).sound(NMLSounds.SCONCE_TORCH)), new BlockProperties(new SelfBlockLootType(), false));
    public static final BlockDefinition<ExtinguishedSconceTorchBlock> EXTINGUISHED_SCONCE_SOUL_TORCH = registerNoItem("extinguished_sconce_soul_torch",
            () -> new ExtinguishedSconceTorchBlock(ParticleTypes.SOUL_FIRE_FLAME, of().noCollission().instabreak().sound(NMLSounds.SCONCE_TORCH).pushReaction(PushReaction.DESTROY).noLootTable()), BlockProperties.custom(true));
    public static final BlockDefinition<SconceWallTorchBlock> SCONCE_SOUL_WALL_TORCH = registerNoItem("sconce_soul_wall_torch",
            () -> new SconceWallTorchBlock(ParticleTypes.SOUL_FIRE_FLAME, ofFullCopy(Blocks.SOUL_WALL_TORCH).sound(NMLSounds.SCONCE_TORCH).lootFrom(SCONCE_SOUL_TORCH)), BlockProperties.custom(true));
    public static final BlockDefinition<ExtinguishedSconceWallTorchBlock> EXTINGUISHED_SCONCE_SOUL_WALL_TORCH = registerNoItem("extinguished_sconce_soul_wall_torch",
            () -> new ExtinguishedSconceWallTorchBlock(ParticleTypes.SOUL_FIRE_FLAME, of().noCollission().instabreak().sound(NMLSounds.SCONCE_TORCH).pushReaction(PushReaction.DESTROY).noLootTable()), BlockProperties.custom(true));
    public static final BlockDefinition<ExtinguishedTorchBlock> EXTINGUISHED_TORCH = registerNoItem("extinguished_torch",
            () -> new ExtinguishedTorchBlock(ParticleTypes.FLAME, of().noCollission().instabreak().sound(SoundType.WOOD).pushReaction(PushReaction.DESTROY).noLootTable()), BlockProperties.custom(true));
    public static final BlockDefinition<ExtinguishedWallTorchBlock> EXTINGUISHED_WALL_TORCH = registerNoItem("extinguished_wall_torch",
            () -> new ExtinguishedWallTorchBlock(ParticleTypes.FLAME, of().noCollission().instabreak().sound(SoundType.WOOD).pushReaction(PushReaction.DESTROY).noLootTable()), BlockProperties.custom(true));
    public static final BlockDefinition<ExtinguishedTorchBlock> EXTINGUISHED_SOUL_TORCH = registerNoItem("extinguished_soul_torch",
            () -> new ExtinguishedTorchBlock(ParticleTypes.SOUL_FIRE_FLAME, of().noCollission().instabreak().sound(SoundType.WOOD).pushReaction(PushReaction.DESTROY).noLootTable()), BlockProperties.custom(true));
    public static final BlockDefinition<ExtinguishedWallTorchBlock> EXTINGUISHED_SOUL_WALL_TORCH = registerNoItem("extinguished_soul_wall_torch",
            () -> new ExtinguishedWallTorchBlock(ParticleTypes.SOUL_FIRE_FLAME, of().noCollission().instabreak().sound(SoundType.WOOD).pushReaction(PushReaction.DESTROY).noLootTable()), BlockProperties.custom(true));

    public static final BlockDefinition<WoodenScaffoldingBlock> WOODEN_SCAFFOLDING = registerNoItem("wooden_scaffolding",
            () -> new WoodenScaffoldingBlock(Block.Properties.ofFullCopy(Blocks.SCAFFOLDING).noCollission().sound(NMLSounds.WOODEN_SCAFFOLDING)), new BlockProperties(new SelfBlockLootType(), false));

    public static final BlockDefinition<PlatformBlock> WOODEN_PLATFORM = register("wooden_platform",
            () -> new PlatformBlock(Block.Properties.ofFullCopy(Blocks.SPRUCE_PLANKS).sound(SoundType.CHERRY_WOOD)), new BlockProperties(new SelfBlockLootType(), false));
    public static final BlockDefinition<PlatformStairsBlock> WOODEN_PLATFORM_STAIRS = register("wooden_platform_stairs",
            () -> new PlatformStairsBlock(Block.Properties.ofFullCopy(NMLBlocks.WOODEN_PLATFORM.get())), new BlockProperties(new SelfBlockLootType(), false));
    public static final BlockDefinition<CrudeDoorBlock> CRUDE_DOOR = register("crude_door",
            () -> new CrudeDoorBlock(crudeBlockSetType(), BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).noOcclusion().pushReaction(PushReaction.DESTROY)), new BlockProperties(new DoorBlockLootType(), false));
    public static final BlockDefinition<CrudeTrapDoorBlock> CRUDE_TRAPDOOR = register("crude_trapdoor",
            () -> new CrudeTrapDoorBlock(crudeBlockSetType(), BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)), new BlockProperties(new SelfBlockLootType(), false));

    public static final BlockDefinition<ResinCauldron> RESIN_CAULDRON = registerNoItem("resin_cauldron", ResinCauldron::new, BlockProperties.custom(true));

    public static final BlockDefinition<ResinOilCauldron> RESIN_OIL_CAULDRON = registerNoItem("resin_oil_cauldron", ResinOilCauldron::new, BlockProperties.cauldron());

    public static final BlockDefinition<HoneyCauldron> HONEY_CAULDRON = registerNoItem("honey_cauldron", HoneyCauldron::new, BlockProperties.cauldron());

    public static final BlockDefinition<MilkCauldron> MILK_CAULDRON = registerNoItem("milk_cauldron", MilkCauldron::new, BlockProperties.cauldron());

    public static final BlockDefinition<MapleSyrupCauldron> MAPLE_SYRUP_CAULDRON = registerNoItem("maple_syrup_cauldron", MapleSyrupCauldron::new, BlockProperties.cauldron());


    //Plants and Other Natural Decorations
    public static final BlockDefinition<GrassSproutsBlock> GRASS_SPROUTS = register("grass_sprouts",
            () -> new GrassSproutsBlock(Block.Properties.ofFullCopy(Blocks.FERN).offsetType(OffsetType.XZ)));
    public static final BlockDefinition<SimpleFoliageBlock> OAT_GRASS = register("oat_grass",
            () -> new SimpleFoliageBlock(Block.Properties.ofFullCopy(Blocks.FERN).offsetType(OffsetType.XYZ)));
    public static final BlockDefinition<DesertFoliageBlock> SHORT_BEACHGRASS = register("short_beachgrass",
            () -> new DesertFoliageBlock(Block.Properties.ofFullCopy(Blocks.DEAD_BUSH).mapColor(MapColor.SAND).offsetType(OffsetType.XYZ)), new BlockProperties(new ShearsBlockLootType(), false));
    public static final BlockDefinition<DesertFoliageBlock> TALL_BEACHGRASS = register("tall_beachgrass",
            () -> new DesertFoliageBlock(Block.Properties.ofFullCopy(Blocks.DEAD_BUSH).mapColor(MapColor.SAND).offsetType(OffsetType.XYZ)), new BlockProperties(new ShearsBlockLootType(), false));
    public static final BlockDefinition<DesertFoliageBlock> DRIED_GRASS = register("dried_grass",
            () -> new DesertFoliageBlock(Block.Properties.ofFullCopy(Blocks.DEAD_BUSH).offsetType(OffsetType.XZ)), new BlockProperties(new ShearsBlockLootType(), false));
    public static final BlockDefinition<FrostedGrassBlock> FROSTED_GRASS = registerNoItem("frosted_grass",
            () -> new FrostedGrassBlock(of().mapColor(MapColor.SNOW).replaceable().noCollission().instabreak().sound(SoundType.GRASS).offsetType(OffsetType.XYZ).ignitedByLava().pushReaction(PushReaction.DESTROY)));
    public static final BlockDefinition<SimpleFoliageBlock> FIDDLEHEAD = register("fiddlehead",
            () -> new SimpleFoliageBlock(Block.Properties.ofFullCopy(Blocks.FERN).offsetType(OffsetType.XYZ)));
    public static final BlockDefinition<SimpleFoliageBlock> MYCELIUM_SPROUTS = register("mycelium_sprouts",
            () -> new SimpleFoliageBlock(Block.Properties.ofFullCopy(Blocks.SHORT_GRASS).mapColor(MapColor.COLOR_PURPLE).offsetType(OffsetType.XYZ)), new BlockProperties(new ShearsBlockLootType(), false));
    public static final BlockDefinition<CaveFoliageBlock> CAVE_WEEDS = register("cave_weeds",
            () -> new CaveFoliageBlock(Block.Properties.ofFullCopy(Blocks.SHORT_GRASS).mapColor(MapColor.TERRACOTTA_GRAY).offsetType(OffsetType.XYZ)));
    public static final BlockDefinition<SimpleFoliageBlock> MYCELIUM_GROWTHS = register("mycelium_growths",
            () -> new SimpleFoliageBlock(Block.Properties.ofFullCopy(Blocks.SHORT_GRASS).mapColor(MapColor.COLOR_PURPLE).offsetType(OffsetType.XYZ)), new BlockProperties(new ShearsBlockLootType(), false));
    public static final BlockDefinition<WaterPlantBlock> CATTAIL = register("cattail",
            () -> new WaterPlantBlock(Block.Properties.ofFullCopy(Blocks.TALL_GRASS).offsetType(OffsetType.XYZ)));
    public static final BlockDefinition<WaterPlantBlock> REEDS = register("reeds",
            () -> new WaterPlantBlock(Block.Properties.ofFullCopy(Blocks.TALL_GRASS).offsetType(OffsetType.XYZ)));
    public static final BlockDefinition<WaterSurfacePlant> DUCKWEED = registerNoItem("duckweed",
            () -> new WaterSurfacePlant(Block.Properties.ofFullCopy(Blocks.LILY_PAD).noCollission().offsetType(OffsetType.XYZ)), new BlockProperties(new ShearsBlockLootType(), false));
    public static final BlockDefinition<WaterSurfacePlant> WATER_MOSAIC = registerNoItem("water_mosaic",
            () -> new WaterSurfacePlant(Block.Properties.ofFullCopy(Blocks.LILY_PAD).noCollission().offsetType(OffsetType.XYZ)), new BlockProperties(new ShearsBlockLootType(), false));
    public static final BlockDefinition<BeardMossBlock> BEARD_MOSS = register("beard_moss",
            () -> new BeardMossBlock(Block.Properties.of().mapColor(MapColor.PLANT).instabreak().sound(SoundType.MOSS).noOcclusion().noCollission().offsetType(OffsetType.XZ)), new BlockProperties(new ShearsBlockLootType(), false));
    public static final BlockDefinition<LeavesBlock> YELLOW_BIRCH_LEAVES = register("yellow_birch_leaves",
            () -> new LeavesBlock(ofFullCopy(Blocks.BIRCH_LEAVES).isViewBlocking((s, g, p) -> false).isSuffocating(((s, g, p) -> false))));
    public static final BlockDefinition<MultifacePlantBlock> ROSE_VINES = register("rose_vines",
            () -> new MultifacePlantBlock(Block.Properties.of().mapColor(MapColor.PLANT).instabreak().sound(SoundType.VINE).noOcclusion().noCollission()), new BlockProperties(new ShearsBlockLootType(), false));
    public static final BlockDefinition<SaplingBlock> YELLOW_BIRCH_SAPLING = register("yellow_birch_sapling",
            () -> new SaplingBlock(NMLTreeGrowers.YELLOW_BIRCH, ofFullCopy(Blocks.BIRCH_SAPLING)), BlockProperties.sapling());
    public static final BlockDefinition<FlowerPotBlock> POTTED_YELLOW_BIRCH_SAPLING = registerNoItem("potted_yellow_birch_sapling",
            () -> new FlowerPotBlock(() -> ((FlowerPotBlock) Blocks.FLOWER_POT), NMLBlocks.YELLOW_BIRCH_SAPLING,
                    ofFullCopy(Blocks.POTTED_BIRCH_SAPLING).noOcclusion()), BlockProperties.flowerPot(YELLOW_BIRCH_SAPLING));
    public static final BlockDefinition<LeavesBlock> AUTUMNAL_OAK_LEAVES = register("autumnal_oak_leaves",
            () -> new LeavesBlock(ofFullCopy(Blocks.OAK_LEAVES).isViewBlocking((s, g, p) -> false).isSuffocating(((s, g, p) -> false))));
    public static final BlockDefinition<SaplingBlock> AUTUMNAL_OAK_SAPLING = register("autumnal_oak_sapling",
            () -> (new SaplingBlock(NMLTreeGrowers.AUTUMNAL_OAK, ofFullCopy(Blocks.OAK_SAPLING))), BlockProperties.sapling());
    public static final BlockDefinition<FlowerPotBlock> POTTED_AUTUMNAL_OAK_SAPLING = registerNoItem("potted_autumnal_oak_sapling",
            () -> new FlowerPotBlock(() -> ((FlowerPotBlock) Blocks.FLOWER_POT), NMLBlocks.AUTUMNAL_OAK_SAPLING,
                    ofFullCopy(Blocks.POTTED_OAK_SAPLING).noOcclusion()), BlockProperties.flowerPot(AUTUMNAL_OAK_SAPLING));
    public static final BlockDefinition<PaleCherryLeavesBlock> PALE_CHERRY_LEAVES = register("pale_cherry_leaves",
            () -> new PaleCherryLeavesBlock(ofFullCopy(Blocks.CHERRY_LEAVES).isViewBlocking((s, g, p) -> false).isSuffocating(((s, g, p) -> false))));
    public static final BlockDefinition<SaplingBlock> PALE_CHERRY_SAPLING = register("pale_cherry_sapling",
            () -> new SaplingBlock(NMLTreeGrowers.PALE_CHERRY, ofFullCopy(Blocks.CHERRY_SAPLING)), BlockProperties.sapling());
    public static final BlockDefinition<FlowerPotBlock> POTTED_PALE_CHERRY_SAPLING = registerNoItem("potted_pale_cherry_sapling",
            () -> new FlowerPotBlock(() -> ((FlowerPotBlock) Blocks.FLOWER_POT), NMLBlocks.PALE_CHERRY_SAPLING,
                    ofFullCopy(Blocks.POTTED_CHERRY_SAPLING).noOcclusion()), BlockProperties.flowerPot(PALE_CHERRY_SAPLING));
    public static final BlockDefinition<LeavesBlock> FROSTED_LEAVES = register("frosted_leaves",
            () -> new LeavesBlock(ofFullCopy(Blocks.OAK_LEAVES).isViewBlocking((s, g, p) -> false).isSuffocating(((s, g, p) -> false))));
    public static final BlockDefinition<FlowerBlock> ACONITE = register("aconite",
            () -> new FlowerBlock(MobEffects.MOVEMENT_SLOWDOWN, 20, ofFullCopy(Blocks.POPPY)), BlockProperties.smallFlower());
    public static final BlockDefinition<FlowerPotBlock> POTTED_ACONITE = registerNoItem("potted_aconite",
            () -> new FlowerPotBlock(() -> ((FlowerPotBlock) Blocks.FLOWER_POT), NMLBlocks.ACONITE,
                    ofFullCopy(Blocks.POTTED_POPPY).noOcclusion()), BlockProperties.flowerPot(ACONITE));
    public static final BlockDefinition<FlowerBlock> STARFLOWER = register("starflower",
            () -> new FlowerBlock(MobEffects.MOVEMENT_SLOWDOWN, 40, ofFullCopy(Blocks.POPPY)), BlockProperties.smallFlower());
    public static final BlockDefinition<FlowerPotBlock> POTTED_STARFLOWER = registerNoItem("potted_starflower",
            () -> new FlowerPotBlock(() -> ((FlowerPotBlock) Blocks.FLOWER_POT), NMLBlocks.STARFLOWER,
                    ofFullCopy(Blocks.POTTED_POPPY).noOcclusion()), BlockProperties.flowerPot(STARFLOWER));
    public static final BlockDefinition<FlowerBlock> THISTLE = register("thistle",
            () -> new FlowerBlock(MobEffects.SATURATION, 3, ofFullCopy(Blocks.POPPY)), BlockProperties.smallFlower());
    public static final BlockDefinition<FlowerPotBlock> POTTED_THISTLE = registerNoItem("potted_thistle",
            () -> new FlowerPotBlock(() -> ((FlowerPotBlock) Blocks.FLOWER_POT), NMLBlocks.THISTLE,
                    ofFullCopy(Blocks.POTTED_POPPY).noOcclusion()), BlockProperties.flowerPot(THISTLE));
    public static final BlockDefinition<FlowerBlock> BLUE_LUPINE = register("blue_lupine",
            () -> new FlowerBlock(MobEffects.ABSORPTION, 5, ofFullCopy(Blocks.POPPY)), BlockProperties.smallFlower());
    public static final BlockDefinition<FlowerPotBlock> POTTED_BLUE_LUPINE = registerNoItem("potted_blue_lupine",
            () -> new FlowerPotBlock(() -> ((FlowerPotBlock) Blocks.FLOWER_POT), NMLBlocks.BLUE_LUPINE,
                    ofFullCopy(Blocks.POTTED_POPPY).noOcclusion()), BlockProperties.flowerPot(BLUE_LUPINE));
    public static final BlockDefinition<FlowerBlock> RED_LUPINE = register("red_lupine",
            () -> new FlowerBlock(MobEffects.ABSORPTION, 5, ofFullCopy(Blocks.POPPY)), BlockProperties.smallFlower());
    public static final BlockDefinition<FlowerPotBlock> POTTED_RED_LUPINE = registerNoItem("potted_red_lupine",
            () -> new FlowerPotBlock(() -> ((FlowerPotBlock) Blocks.FLOWER_POT), NMLBlocks.RED_LUPINE,
                    ofFullCopy(Blocks.POTTED_POPPY).noOcclusion()), BlockProperties.flowerPot(RED_LUPINE));
    public static final BlockDefinition<FlowerBlock> YELLOW_LUPINE = register("yellow_lupine",
            () -> new FlowerBlock(MobEffects.ABSORPTION, 5, ofFullCopy(Blocks.POPPY)), BlockProperties.smallFlower());
    public static final BlockDefinition<FlowerPotBlock> POTTED_YELLOW_LUPINE = registerNoItem("potted_yellow_lupine",
            () -> new FlowerPotBlock(() -> ((FlowerPotBlock) Blocks.FLOWER_POT), NMLBlocks.YELLOW_LUPINE,
                    ofFullCopy(Blocks.POTTED_POPPY).noOcclusion()), BlockProperties.flowerPot(YELLOW_LUPINE));
    public static final BlockDefinition<FlowerBlock> PINK_LUPINE = register("pink_lupine",
            () -> new FlowerBlock(MobEffects.ABSORPTION, 5, ofFullCopy(Blocks.POPPY)), BlockProperties.smallFlower());
    public static final BlockDefinition<FlowerPotBlock> POTTED_PINK_LUPINE = registerNoItem("potted_pink_lupine",
            () -> new FlowerPotBlock(() -> ((FlowerPotBlock) Blocks.FLOWER_POT), NMLBlocks.PINK_LUPINE,
                    ofFullCopy(Blocks.POTTED_POPPY).noOcclusion()), BlockProperties.flowerPot(PINK_LUPINE));
    public static final BlockDefinition<FlowerBlock> AUTUMN_CROCUS = register("autumn_crocus",
            () -> new FlowerBlock(MobEffects.BLINDNESS, 10, ofFullCopy(Blocks.POPPY)), BlockProperties.smallFlower());
    public static final BlockDefinition<FlowerPotBlock> POTTED_AUTUMN_CROCUS = registerNoItem("potted_autumn_crocus",
            () -> new FlowerPotBlock(() -> ((FlowerPotBlock) Blocks.FLOWER_POT), NMLBlocks.AUTUMN_CROCUS,
                    ofFullCopy(Blocks.POTTED_POPPY).noOcclusion()), BlockProperties.flowerPot(AUTUMN_CROCUS));
    public static final BlockDefinition<FlowerBlock> WILD_MINT = register("wild_mint",
            () -> new FlowerBlock(MobEffects.SATURATION, 1, ofFullCopy(Blocks.POPPY)), BlockProperties.smallFlower());
    public static final BlockDefinition<FlowerPotBlock> POTTED_WILD_MINT = registerNoItem("potted_wild_mint",
            () -> new FlowerPotBlock(() -> ((FlowerPotBlock) Blocks.FLOWER_POT), NMLBlocks.WILD_MINT,
                    ofFullCopy(Blocks.POTTED_POPPY).noOcclusion()), BlockProperties.flowerPot(WILD_MINT));
    public static final BlockDefinition<FlowerBlock> PICKLEWEED = register("pickleweed",
            () -> new FlowerBlock(MobEffects.SATURATION, 1, ofFullCopy(Blocks.POPPY)), BlockProperties.smallFlower());
    public static final BlockDefinition<FlowerPotBlock> POTTED_PICKLEWEED = registerNoItem("potted_pickleweed",
            () -> new FlowerPotBlock(() -> ((FlowerPotBlock) Blocks.FLOWER_POT), NMLBlocks.PICKLEWEED,
                    ofFullCopy(Blocks.POTTED_POPPY).noOcclusion()), BlockProperties.flowerPot(PICKLEWEED));
    public static final BlockDefinition<FlatFlowerBlock> RAFFLESIA = register("rafflesia",
            () -> new FlatFlowerBlock(MobEffects.HUNGER, 60, ofFullCopy(Blocks.POPPY)), BlockProperties.smallFlower());
    public static final BlockDefinition<DesertFoliageBlock> BARREL_CACTUS = register("barrel_cactus",
            () -> new DesertFoliageBlock(of().mapColor(MapColor.PLANT).noCollission().instabreak().sound(SoundType.BIG_DRIPLEAF).offsetType(OffsetType.XZ).pushReaction(PushReaction.DESTROY)), new BlockProperties(new SelfBlockLootType(), false));
    public static final BlockDefinition<FlowerPotBlock> POTTED_BARREL_CACTUS = registerNoItem("potted_barrel_cactus",
            () -> new FlowerPotBlock(() -> ((FlowerPotBlock) Blocks.FLOWER_POT), NMLBlocks.BARREL_CACTUS,
                    ofFullCopy(Blocks.POTTED_POPPY).noOcclusion()), BlockProperties.flowerPot(BARREL_CACTUS));
    public static final BlockDefinition<SimpleFoliageBlock> LAVENDER_BUSH = register("lavender_bush",
            () -> new SimpleFoliageBlock(ofFullCopy(Blocks.SHORT_GRASS).offsetType(OffsetType.XYZ)), BlockProperties.flowerbed());
    public static final BlockDefinition<DesertFoliageBlock> SUCCULENT = register("succulent",
            () -> new DesertFoliageBlock(of().mapColor(MapColor.PLANT).noCollission().instabreak().sound(SoundType.FLOWERING_AZALEA).offsetType(OffsetType.XZ).pushReaction(PushReaction.DESTROY)), new BlockProperties(new SelfBlockLootType(), false));
    public static final BlockDefinition<FlowerPotBlock> POTTED_SUCCULENT = registerNoItem("potted_succulent",
            () -> new FlowerPotBlock(() -> ((FlowerPotBlock) Blocks.FLOWER_POT), NMLBlocks.SUCCULENT,
                    ofFullCopy(Blocks.POTTED_POPPY).noOcclusion()), BlockProperties.flowerPot(SUCCULENT));
    public static final BlockDefinition<FlowerbedBlock> CLOVER_PATCH = register("clover_patch",
            () -> new FlowerbedBlock(MobEffects.LUCK, 5, Block.Properties.of().mapColor(MapColor.PLANT).replaceable().noCollission().instabreak().sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY)), BlockProperties.flowerbed());
    public static final BlockDefinition<FlowerbedBlock> GROUND_IVY = register("ground_ivy",
            () -> new FlowerbedBlock(MobEffects.POISON, 10, Block.Properties.of().mapColor(MapColor.PLANT).replaceable().noCollission().instabreak().sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY)), BlockProperties.flowerbed());
    public static final BlockDefinition<FlowerbedBlock> WHITE_FLOWERBED = register("white_flowerbed",
            () -> new FlowerbedBlock(MobEffects.MOVEMENT_SLOWDOWN, 10, Block.Properties.of().mapColor(MapColor.PLANT).replaceable().noCollission().instabreak().sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY)), BlockProperties.flowerbed());
    public static final BlockDefinition<FlowerbedBlock> YELLOW_FLOWERBED = register("yellow_flowerbed",
            () -> new FlowerbedBlock(MobEffects.MOVEMENT_SLOWDOWN, 5, Block.Properties.of().mapColor(MapColor.PLANT).replaceable().noCollission().instabreak().sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY)), BlockProperties.flowerbed());
    public static final BlockDefinition<FlowerbedBlock> RED_FLOWERBED = register("red_flowerbed",
            () -> new FlowerbedBlock(MobEffects.HEAL, 1, Block.Properties.of().mapColor(MapColor.PLANT).replaceable().noCollission().instabreak().sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY)), BlockProperties.flowerbed());
    public static final BlockDefinition<FlowerbedBlock> BLUE_FLOWERBED = register("blue_flowerbed",
            () -> new FlowerbedBlock(MobEffects.DAMAGE_RESISTANCE, 10, Block.Properties.of().mapColor(MapColor.PLANT).replaceable().noCollission().instabreak().sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY)), BlockProperties.flowerbed());
    public static final BlockDefinition<FlowerbedBlock> VIOLET_FLOWERBED = register("violet_flowerbed",
            () -> new FlowerbedBlock(MobEffects.DAMAGE_RESISTANCE, 5, Block.Properties.of().mapColor(MapColor.PLANT).replaceable().noCollission().instabreak().sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY)), BlockProperties.flowerbed());

    public static final BlockDefinition<GroundPickupBlock> PEBBLES = register("pebbles",
            () -> new GroundPickupBlock(of().mapColor(MapColor.STONE).noCollission().instabreak().sound(SoundType.STONE).pushReaction(PushReaction.DESTROY)));
    public static final BlockDefinition<GroundPickupBlock> SEASHELLS = register("seashells",
            () -> new GroundPickupBlock(of().mapColor(MapColor.NONE).noCollission().instabreak().sound(NMLSounds.SEASHELLS).pushReaction(PushReaction.DESTROY).offsetType(OffsetType.XZ)), new BlockProperties(new SelfBlockLootType(), false));

    public static final BlockDefinition<IciclesBlock> ICICLES = register("icicles",
            () -> new IciclesBlock(of().mapColor(MapColor.ICE).sound(SoundType.GLASS).pushReaction(PushReaction.DESTROY).offsetType(OffsetType.XZ).dynamicShape()), BlockProperties.icicles());

    //Underground
    public static final BlockDefinition<AmethystBlock> QUARTZITE = register("quartzite",
            () -> new AmethystBlock(of().mapColor(MapColor.TERRACOTTA_WHITE).strength(1.3F).sound(NMLSounds.QUARTZITE).requiresCorrectToolForDrops()));
    public static final BlockDefinition<BuddingQuartziteBlock> BUDDING_QUARTZITE = register("budding_quartzite",
            () -> new BuddingQuartziteBlock(of().mapColor(MapColor.TERRACOTTA_WHITE).randomTicks().strength(1.3F).sound(NMLSounds.QUARTZITE).pushReaction(PushReaction.DESTROY).noLootTable()));
    public static final BlockDefinition<AmethystClusterBlock> QUARTZITE_CLUSTER = register("quartzite_cluster",
            () -> new AmethystClusterBlock(7, 3, of().mapColor(MapColor.TERRACOTTA_WHITE).forceSolidOn().noOcclusion().randomTicks().sound(NMLSounds.QUARTZITE_CLUSTER).strength(1.3F).lightLevel((state) -> 5).pushReaction(PushReaction.DESTROY)));
    public static final BlockDefinition<AmethystClusterBlock> SMALL_QUARTZITE_BUD = register("small_quartzite_bud",
            () -> new AmethystClusterBlock(3, 4, ofFullCopy(NMLBlocks.QUARTZITE_CLUSTER.get()).sound(NMLSounds.QUARTZITE_CLUSTER).forceSolidOn().lightLevel((state) -> 1).pushReaction(PushReaction.DESTROY)));
    public static final BlockDefinition<AmethystClusterBlock> MEDIUM_QUARTZITE_BUD = register("medium_quartzite_bud",
            () -> new AmethystClusterBlock(4, 3, ofFullCopy(NMLBlocks.QUARTZITE_CLUSTER.get()).sound(NMLSounds.QUARTZITE_CLUSTER).forceSolidOn().lightLevel((state) -> 2).pushReaction(PushReaction.DESTROY)));
    public static final BlockDefinition<AmethystClusterBlock> LARGE_QUARTZITE_BUD = register("large_quartzite_bud",
            () -> new AmethystClusterBlock(5, 3, ofFullCopy(NMLBlocks.QUARTZITE_CLUSTER.get()).sound(NMLSounds.QUARTZITE_CLUSTER).forceSolidOn().lightLevel((state) -> 4).pushReaction(PushReaction.DESTROY)));
    public static final BlockDefinition<Block> SILT = register("silt",
            () -> new Block(of().mapColor(MapColor.TERRACOTTA_GRAY).sound(SoundType.ROOTED_DIRT).strength(0.6F)), BlockProperties.dirtLike());
    public static final BlockDefinition<PathBlock> SILT_PATH = register("silt_path",
            () -> new PathBlock(ofFullCopy(NMLBlocks.SILT.get()), NMLBlocks.SILT.get(), false), BlockProperties.simplePath(SILT));
    public static final BlockDefinition<Block> COARSE_SILT = register("coarse_silt",
            () -> new Block(ofFullCopy(NMLBlocks.SILT.get())), BlockProperties.dirtLike());
    //Paths
    public static final BlockDefinition<PathBlock> DIRT_PATH = register("dirt_path",
            () -> new PathBlock(ofFullCopy(Blocks.DIRT), Blocks.DIRT, false), BlockProperties.simplePath(Blocks.DIRT));
    public static final BlockDefinition<PathBlock> MYCELIUM_PATH = register("mycelium_path",
            () -> new PathBlock(of().mapColor(MapColor.COLOR_PURPLE).strength(0.5F).sound(SoundType.GRASS), Blocks.DIRT, false), BlockProperties.simplePath(Blocks.DIRT));
    public static final BlockDefinition<PathBlock> PODZOL_PATH = register("podzol_path",
            () -> new PathBlock(ofFullCopy(Blocks.PODZOL), Blocks.PODZOL, false), BlockProperties.simplePath(Blocks.DIRT));
    public static final BlockDefinition<PathBlock> SNOWY_GRASS_PATH = register("snowy_grass_path",
            () -> new PathBlock(of().mapColor(MapColor.SNOW).strength(0.5F).sound(SoundType.GRASS), Blocks.DIRT, false), BlockProperties.simplePath(Blocks.DIRT));
    public static final BlockDefinition<PathBlock> SNOW_PATH = register("snow_path",
            () -> new PathBlock(ofFullCopy(Blocks.SNOW_BLOCK), Blocks.SNOW_BLOCK, false), BlockProperties.path());
    public static final BlockDefinition<PathBlock> GRAVEL_PATH = register("gravel_path",
            () -> new PathBlock(ofFullCopy(Blocks.GRAVEL), Blocks.GRAVEL, true), BlockProperties.path());
    public static final BlockDefinition<PathBlock> SAND_PATH = register("sand_path",
            () -> new PathBlock(ofFullCopy(Blocks.SAND), Blocks.SAND, true), BlockProperties.simplePath(Blocks.SAND));
    public static final BlockDefinition<PathBlock> RED_SAND_PATH = register("red_sand_path",
            () -> new PathBlock(ofFullCopy(Blocks.RED_SAND), Blocks.RED_SAND, true), BlockProperties.simplePath(Blocks.RED_SAND));
    //Dungeon
    public static final BlockDefinition<RemainsBlock> REMAINS = registerNoItem("remains",
            () -> new RemainsBlock(Blocks.COARSE_DIRT, SoundEvents.BRUSH_SAND, SoundEvents.BRUSH_SAND_COMPLETED, of().mapColor(MapColor.DIRT).strength(0.25F).sound(SoundType.SUSPICIOUS_SAND).pushReaction(PushReaction.DESTROY)), BlockProperties.custom(false));
    public static final BlockDefinition<MonsterAnchorBlock> MONSTER_ANCHOR = register("monster_anchor",
            () -> new MonsterAnchorBlock(ofFullCopy(Blocks.SPAWNER).strength(7, 7).sound(NMLSounds.MONSTER_ANCHOR).noOcclusion()));
    public static final BlockDefinition<WardingEffigyBlock> WARDING_EFFIGY = registerNoItem("warding_effigy",
            () -> new WardingEffigyBlock(of()
                    .strength(1.5F)
                    .noOcclusion()
                    .pushReaction(PushReaction.DESTROY)
                    .sound(SoundType.CHERRY_WOOD)
            ));
    public static final BlockDefinition<AncestralEffigyBlock> ANCESTRAL_EFFIGY = register("ancestral_effigy",
          () -> new AncestralEffigyBlock(ofFullCopy(Blocks.STONE).offsetType(OffsetType.XYZ).dynamicShape().noOcclusion()));
    public static final BlockDefinition<AncestralCarvingBlock> ANCESTRAL_CARVING = register("ancestral_carving",
          () -> new AncestralCarvingBlock(ofFullCopy(Blocks.STONE)));

    public static final BlockDefinition<PotBlock> ANCIENT_POT = registerNoItem("ancient_pot", () -> new PotBlock(PotSize.SMALL, of().mapColor(MapColor.TERRACOTTA_BROWN).noOcclusion().dynamicShape().strength(1, 3.5F).sound(SoundType.DECORATED_POT)));
    public static final BlockDefinition<LargePotBlock> LARGE_ANCIENT_POT = registerNoItem("large_ancient_pot", () -> new LargePotBlock(of().mapColor(MapColor.TERRACOTTA_BROWN).noOcclusion().dynamicShape().strength(1, 3.5F).sound(SoundType.DECORATED_POT).hasPostProcess((state, getter, pos) -> true)));

    // Moonlight
    /* Farcr doesn't want these to show up in the Creative Tabs, so I'm just manually registering the items. */
    public static final BlockDefinition<MoonlightBasinBlock> MOONLIGHT_BASIN = registerNoItem("moonlight_basin",
        () -> new MoonlightBasinBlock(ofFullCopy(Blocks.BEDROCK).dynamicShape().lightLevel(MoonlightCandleBlock.LIGHT_EMISSION).sound(SoundType.COPPER)));
    public static final BlockDefinition<MoonlightCandleBlock> MOONLIGHT_CANDLE = registerNoItem("moonlight_candle",
        () -> new MoonlightCandleBlock(ofFullCopy(Blocks.BEDROCK).noOcclusion().lightLevel(MoonlightCandleBlock.LIGHT_EMISSION).sound(SoundType.CANDLE).offsetType(OffsetType.XZ).dynamicShape()));

    public static final BlockDefinition<MoonCarvingBlock> MOON_CARVING = registerNoItem("moon_carving",
            () -> new MoonCarvingBlock(ofFullCopy(Blocks.STONE)));

    public static final BlockDefinition<AncientBronzeBellBlock> ANCIENT_BRONZE_BELL = register("ancient_bronze_bell",
            () -> new AncientBronzeBellBlock(ofFullCopy(Blocks.BELL)), BlockProperties.stoneLike());

    public static final BlockDefinition<InvertedBellBlock> INVERTED_BELL = registerNoItem("inverted_bell",
            () -> new InvertedBellBlock(ofFullCopy(Blocks.BEDROCK).sound(SoundType.COPPER).noOcclusion().forceSolidOn()));
    //Tiles
    public static final BlockDefinition<Block> MUNDANE_TILES = register("mundane_tiles",
            () -> new Block(ofFullCopy(Blocks.STONE_BRICKS)), BlockProperties.stoneLike());
    public static final BlockDefinition<StairBlock> MUNDANE_TILE_STAIRS = register("mundane_tile_stairs",
            () -> new StairBlock(MUNDANE_TILES.get().defaultBlockState(), ofFullCopy(NMLBlocks.MUNDANE_TILES.get())), BlockProperties.stoneLikeStairs());
    public static final BlockDefinition<SlabBlock> MUNDANE_TILE_SLAB = register("mundane_tile_slab",
            () -> new SlabBlock(ofFullCopy(NMLBlocks.MUNDANE_TILES.get())), BlockProperties.stoneLikeSlab());
    public static final BlockDefinition<Block> EARTHEN_TILES = register("earthen_tiles",
            () -> new Block(ofFullCopy(Blocks.PACKED_MUD).sound(NMLSounds.EARTHEN_TILES)), BlockProperties.stoneLike());
    public static final BlockDefinition<StairBlock> EARTHEN_TILE_STAIRS = register("earthen_tile_stairs",
            () -> new StairBlock(EARTHEN_TILES.get().defaultBlockState(), ofFullCopy(NMLBlocks.EARTHEN_TILES.get())), BlockProperties.stoneLikeStairs());
    public static final BlockDefinition<SlabBlock> EARTHEN_TILE_SLAB = register("earthen_tile_slab",
            () -> new SlabBlock(ofFullCopy(NMLBlocks.EARTHEN_TILES.get())), BlockProperties.stoneLikeSlab());
    public static final BlockDefinition<Block> DROSS_TILES = register("dross_tiles",
            () -> new Block(ofFullCopy(Blocks.PACKED_MUD)), BlockProperties.stoneLike());
    public static final BlockDefinition<StairBlock> DROSS_TILE_STAIRS = register("dross_tile_stairs",
            () -> new StairBlock(DROSS_TILES.get().defaultBlockState(), ofFullCopy(NMLBlocks.DROSS_TILES.get())), BlockProperties.stoneLikeStairs());
    public static final BlockDefinition<SlabBlock> DROSS_TILE_SLAB = register("dross_tile_slab",
            () -> new SlabBlock(ofFullCopy(NMLBlocks.DROSS_TILES.get())), BlockProperties.stoneLikeSlab());

    public static final BlockDefinition<Block> LARGE_STELLAR_TILE = register("large_stellar_tile",
            () -> new Block(ofFullCopy(Blocks.DEEPSLATE_BRICKS)), BlockProperties.stoneLike());
    public static final BlockDefinition<Block> STELLAR_TILES = register("stellar_tiles",
            () -> new Block(ofFullCopy(Blocks.DEEPSLATE_TILES)), BlockProperties.stoneLike());
//    public static final BlockDefinition<StairBlock> STELLAR_TILE_STAIRS = register("stellar_tile_stairs",
//            () -> new StairBlock(STELLAR_TILES.get().defaultBlockState(), ofFullCopy(NMLBlocks.LARGE_STELLAR_TILE.get())), BlockProperties.stoneLikeStairs());
//    public static final BlockDefinition<SlabBlock> STELLAR_TILE_SLAB = register("stellar_tile_slab",
//            () -> new SlabBlock(ofFullCopy(NMLBlocks.STELLAR_TILES.get())), BlockProperties.stoneLikeSlab());

    public static final BlockDefinition<Block> LARGE_SOLAR_TILE = register("large_solar_tile",
            () -> new Block(ofFullCopy(Blocks.DEEPSLATE_BRICKS)), BlockProperties.stoneLike());
    public static final BlockDefinition<Block> SOLAR_TILES = register("solar_tiles",
            () -> new Block(ofFullCopy(Blocks.DEEPSLATE_TILES)), BlockProperties.stoneLike());
//    public static final BlockDefinition<StairBlock> SOLAR_TILE_STAIRS = register("solar_tile_stairs",
//            () -> new StairBlock(SOLAR_TILES.get().defaultBlockState(), ofFullCopy(NMLBlocks.LARGE_SOLAR_TILE.get())), BlockProperties.stoneLikeStairs());
//    public static final BlockDefinition<SlabBlock> SOLAR_TILE_SLAB = register("solar_tile_slab",
//            () -> new SlabBlock(ofFullCopy(NMLBlocks.SOLAR_TILES.get())), BlockProperties.stoneLikeSlab());

    public static final BlockDefinition<Block> LARGE_LUNAR_TILE = register("large_lunar_tile",
            () -> new Block(ofFullCopy(Blocks.DEEPSLATE_BRICKS)), BlockProperties.stoneLike());
    public static final BlockDefinition<Block> LUNAR_TILES = register("lunar_tiles",
            () -> new Block(ofFullCopy(Blocks.DEEPSLATE_TILES)), BlockProperties.stoneLike());
//    public static final BlockDefinition<StairBlock> LUNAR_TILE_STAIRS = register("lunar_tile_stairs",
//            () -> new StairBlock(LUNAR_TILES.get().defaultBlockState(), ofFullCopy(NMLBlocks.LARGE_LUNAR_TILE.get())), BlockProperties.stoneLikeStairs());
//    public static final BlockDefinition<SlabBlock> LUNAR_TILE_SLAB = register("lunar_tile_slab",
//            () -> new SlabBlock(ofFullCopy(NMLBlocks.LUNAR_TILES.get())), BlockProperties.stoneLikeSlab());

    public static final BlockDefinition<Block> SILT_BRICKS = register("silt_bricks",
            () -> new Block(ofFullCopy(Blocks.PACKED_MUD)), BlockProperties.stoneLike());
    public static final BlockDefinition<StairBlock> SILT_BRICK_STAIRS = register("silt_brick_stairs",
            () -> new StairBlock(SILT_BRICKS.get().defaultBlockState(), ofFullCopy(NMLBlocks.SILT_BRICKS.get())), BlockProperties.stoneLikeStairs());
    public static final BlockDefinition<SlabBlock> SILT_BRICK_SLAB = register("silt_brick_slab",
            () -> new SlabBlock(ofFullCopy(NMLBlocks.SILT_BRICKS.get())), BlockProperties.stoneLikeSlab());
    //Stone
    public static final BlockDefinition<Block> FADED_STONE_BRICKS = register("faded_stone_bricks",
            () -> new Block(ofFullCopy(Blocks.STONE_BRICKS)), BlockProperties.stoneLike());
    public static final BlockDefinition<Block> POLISHED_STONE = register("polished_stone",
            () -> new Block(ofFullCopy(Blocks.STONE_BRICKS)), BlockProperties.stoneLike());
    public static final BlockDefinition<StairBlock> POLISHED_STONE_STAIRS = register("polished_stone_stairs",
            () -> new StairBlock(POLISHED_STONE.get().defaultBlockState(), ofFullCopy(NMLBlocks.POLISHED_STONE.get())), BlockProperties.stoneLikeStairs());
    public static final BlockDefinition<SlabBlock> POLISHED_STONE_SLAB = register("polished_stone_slab",
            () -> new SlabBlock(ofFullCopy(NMLBlocks.POLISHED_STONE.get())), BlockProperties.stoneLikeSlab());
    public static final BlockDefinition<Block> COBBLESTONE_BRICKS = register("cobblestone_bricks",
            () -> new Block(ofFullCopy(Blocks.COBBLESTONE)), BlockProperties.stoneLike());
    public static final BlockDefinition<StairBlock> COBBLESTONE_BRICK_STAIRS = register("cobblestone_brick_stairs",
            () -> new StairBlock(COBBLESTONE_BRICKS.get().defaultBlockState(), ofFullCopy(NMLBlocks.COBBLESTONE_BRICKS.get())), BlockProperties.stoneLikeStairs());
    public static final BlockDefinition<SlabBlock> COBBLESTONE_BRICK_SLAB = register("cobblestone_brick_slab",
            () -> new SlabBlock(ofFullCopy(NMLBlocks.COBBLESTONE_BRICKS.get())), BlockProperties.stoneLikeSlab());
    public static final BlockDefinition<WallBlock> COBBLESTONE_BRICK_WALL = register("cobblestone_brick_wall",
            () -> new WallBlock(ofFullCopy(NMLBlocks.COBBLESTONE_BRICKS.get())), BlockProperties.stoneLikeWall());
    public static final BlockDefinition<Block> CRACKED_COBBLESTONE_BRICKS = register("cracked_cobblestone_bricks",
            () -> new Block(ofFullCopy(NMLBlocks.COBBLESTONE_BRICKS.get())), BlockProperties.stoneLike());
    public static final BlockDefinition<Block> MOSSY_COBBLESTONE_BRICKS = register("mossy_cobblestone_bricks",
            () -> new Block(ofFullCopy(Blocks.MOSSY_COBBLESTONE)), BlockProperties.stoneLike());
    public static final BlockDefinition<StairBlock> MOSSY_COBBLESTONE_BRICK_STAIRS = register("mossy_cobblestone_brick_stairs",
            () -> new StairBlock(MOSSY_COBBLESTONE_BRICKS.get().defaultBlockState(), ofFullCopy(NMLBlocks.MOSSY_COBBLESTONE_BRICKS.get())), BlockProperties.stoneLikeStairs());
    public static final BlockDefinition<SlabBlock> MOSSY_COBBLESTONE_BRICK_SLAB = register("mossy_cobblestone_brick_slab",
            () -> new SlabBlock(ofFullCopy(NMLBlocks.MOSSY_COBBLESTONE_BRICKS.get())), BlockProperties.stoneLikeSlab());
    public static final BlockDefinition<WallBlock> MOSSY_COBBLESTONE_BRICK_WALL = register("mossy_cobblestone_brick_wall",
            () -> new WallBlock(ofFullCopy(NMLBlocks.MOSSY_COBBLESTONE_BRICKS.get())), BlockProperties.stoneLikeWall());


    public static final BlockDefinition<Block> SILTSTONE = register("siltstone",
            () -> new Block(ofFullCopy(Blocks.STONE).sound(NMLSounds.SILTSTONE)), BlockProperties.stoneLike());
    public static final BlockDefinition<StairBlock> SILTSTONE_STAIRS = register("siltstone_stairs",
            () -> new StairBlock(SILTSTONE.get().defaultBlockState(), ofFullCopy(NMLBlocks.SILTSTONE.get())), BlockProperties.stoneLikeStairs());
    public static final BlockDefinition<SlabBlock> SILTSTONE_SLAB = register("siltstone_slab",
            () -> new SlabBlock(ofFullCopy(NMLBlocks.SILTSTONE.get())), BlockProperties.stoneLikeSlab());
    public static final BlockDefinition<WallBlock> SILTSTONE_WALL = register("siltstone_wall",
            () -> new WallBlock(ofFullCopy(NMLBlocks.SILTSTONE.get())), BlockProperties.stoneLikeWall());
    public static final BlockDefinition<Block> POLISHED_SILTSTONE = register("polished_siltstone",
            () -> new Block(ofFullCopy(NMLBlocks.SILTSTONE.get())), BlockProperties.stoneLike());
    public static final BlockDefinition<StairBlock> POLISHED_SILTSTONE_STAIRS = register("polished_siltstone_stairs",
            () -> new StairBlock(POLISHED_SILTSTONE.get().defaultBlockState(), ofFullCopy(NMLBlocks.POLISHED_SILTSTONE.get())), BlockProperties.stoneLikeStairs());
    public static final BlockDefinition<SlabBlock> POLISHED_SILTSTONE_SLAB = register("polished_siltstone_slab",
            () -> new SlabBlock(ofFullCopy(NMLBlocks.POLISHED_SILTSTONE.get())), BlockProperties.stoneLikeSlab());
    public static final BlockDefinition<Block> SILTSTONE_BRICKS = register("siltstone_bricks",
            () -> new Block(ofFullCopy(Blocks.STONE_BRICKS)), BlockProperties.stoneLike());
    public static final BlockDefinition<StairBlock> SILTSTONE_BRICK_STAIRS = register("siltstone_brick_stairs",
            () -> new StairBlock(SILTSTONE_BRICKS.get().defaultBlockState(), ofFullCopy(NMLBlocks.SILTSTONE_BRICKS.get())), BlockProperties.stoneLikeStairs());
    public static final BlockDefinition<SlabBlock> SILTSTONE_BRICK_SLAB = register("siltstone_brick_slab",
            () -> new SlabBlock(ofFullCopy(NMLBlocks.SILTSTONE_BRICKS.get())), BlockProperties.stoneLikeSlab());
    public static final BlockDefinition<WallBlock> SILTSTONE_BRICK_WALL = register("siltstone_brick_wall",
            () -> new WallBlock(ofFullCopy(Blocks.STONE_BRICK_WALL)), BlockProperties.stoneLikeWall());
    public static final BlockDefinition<Block> CHISELED_SILTSTONE = register("chiseled_siltstone",
            () -> new Block(ofFullCopy(NMLBlocks.SILTSTONE.get())), BlockProperties.stoneLike());
    //Deepslate
    public static final BlockDefinition<Block> COBBLED_DEEPSLATE_BRICKS = register("cobbled_deepslate_bricks",
            () -> new Block(ofFullCopy(Blocks.COBBLED_DEEPSLATE)), BlockProperties.stoneLike());
    public static final BlockDefinition<StairBlock> COBBLED_DEEPSLATE_BRICK_STAIRS = register("cobbled_deepslate_brick_stairs",
            () -> new StairBlock(COBBLED_DEEPSLATE_BRICKS.get().defaultBlockState(), ofFullCopy(Blocks.COBBLED_DEEPSLATE_STAIRS)), BlockProperties.stoneLikeStairs());
    public static final BlockDefinition<SlabBlock> COBBLED_DEEPSLATE_BRICK_SLAB = register("cobbled_deepslate_brick_slab",
            () -> new SlabBlock(ofFullCopy(Blocks.COBBLED_DEEPSLATE_SLAB)), BlockProperties.stoneLikeSlab());
    public static final BlockDefinition<WallBlock> COBBLED_DEEPSLATE_BRICK_WALL = register("cobbled_deepslate_brick_wall",
            () -> new WallBlock(ofFullCopy(Blocks.COBBLED_DEEPSLATE_WALL)), BlockProperties.stoneLikeWall());
    //Bricks
    public static final BlockDefinition<Block> COARSE_BRICKS = register("coarse_bricks",
            () -> new Block(ofFullCopy(Blocks.BRICKS)), BlockProperties.stoneLike());
    public static final BlockDefinition<StairBlock> COARSE_BRICK_STAIRS = register("coarse_brick_stairs",
            () -> new StairBlock(COARSE_BRICKS.get().defaultBlockState(), ofFullCopy(NMLBlocks.COARSE_BRICKS.get())), BlockProperties.stoneLikeStairs());
    public static final BlockDefinition<SlabBlock> COARSE_BRICK_SLAB = register("coarse_brick_slab",
            () -> new SlabBlock(ofFullCopy(NMLBlocks.COARSE_BRICKS.get())), BlockProperties.stoneLikeSlab());
    public static final BlockDefinition<Block> COARSE_BRICK_WALL = register("coarse_brick_wall",
            () -> new WallBlock(ofFullCopy(NMLBlocks.COARSE_BRICKS.get())), BlockProperties.stoneLikeWall());
    public static final BlockDefinition<Block> MOSSY_COARSE_BRICKS = register("mossy_coarse_bricks",
            () -> new Block(ofFullCopy(NMLBlocks.COARSE_BRICKS.get())), BlockProperties.stoneLike());
    public static final BlockDefinition<Block> MOSSY_COARSE_BRICK_STAIRS = register("mossy_coarse_brick_stairs",
            () -> new StairBlock(COARSE_BRICKS.get().defaultBlockState(), ofFullCopy(NMLBlocks.COARSE_BRICKS.get())), BlockProperties.stoneLikeStairs());
    public static final BlockDefinition<Block> MOSSY_COARSE_BRICK_SLAB = register("mossy_coarse_brick_slab",
            () -> new SlabBlock(ofFullCopy(NMLBlocks.COARSE_BRICKS.get())), BlockProperties.stoneLikeSlab());
    public static final BlockDefinition<Block> MOSSY_COARSE_BRICK_WALL = register("mossy_coarse_brick_wall",
            () -> new WallBlock(ofFullCopy(NMLBlocks.COARSE_BRICKS.get())), BlockProperties.stoneLikeWall());
    //Trimmed Planks and Bookshelves
    public static final BlockDefinition<Block> TRIMMED_OAK_PLANKS = register("trimmed_oak_planks",
            () -> new TrimmedPlankBlock(ofFullCopy(Blocks.OAK_PLANKS)), BlockProperties.trimmedPlanks(Blocks.OAK_PLANKS));
    public static final BlockDefinition<Block> SPRUCE_BOOKSHELF = register("spruce_bookshelf",
            () -> new Block(ofFullCopy(Blocks.BOOKSHELF)), BlockProperties.bookshelf(Blocks.SPRUCE_PLANKS));
    public static final BlockDefinition<Block> TRIMMED_SPRUCE_PLANKS = register("trimmed_spruce_planks",
            () -> new TrimmedPlankBlock(ofFullCopy(Blocks.SPRUCE_PLANKS)), BlockProperties.trimmedPlanks(Blocks.SPRUCE_PLANKS));
    public static final BlockDefinition<Block> BIRCH_BOOKSHELF = register("birch_bookshelf",
            () -> new Block(ofFullCopy(Blocks.BOOKSHELF)), BlockProperties.bookshelf(Blocks.BIRCH_PLANKS));
    public static final BlockDefinition<Block> TRIMMED_BIRCH_PLANKS = register("trimmed_birch_planks",
            () -> new TrimmedPlankBlock(ofFullCopy(Blocks.BIRCH_PLANKS)), BlockProperties.trimmedPlanks(Blocks.BIRCH_PLANKS));
    public static final BlockDefinition<Block> JUNGLE_BOOKSHELF = register("jungle_bookshelf",
            () -> new Block(ofFullCopy(Blocks.BOOKSHELF)), BlockProperties.bookshelf(Blocks.JUNGLE_PLANKS));
    public static final BlockDefinition<Block> TRIMMED_JUNGLE_PLANKS = register("trimmed_jungle_planks",
            () -> new TrimmedPlankBlock(ofFullCopy(Blocks.JUNGLE_PLANKS)), BlockProperties.trimmedPlanks(Blocks.JUNGLE_PLANKS));
    public static final BlockDefinition<Block> DARK_OAK_BOOKSHELF = register("dark_oak_bookshelf",
            () -> new Block(ofFullCopy(Blocks.BOOKSHELF)), BlockProperties.bookshelf(Blocks.DARK_OAK_PLANKS));
    public static final BlockDefinition<Block> TRIMMED_DARK_OAK_PLANKS = register("trimmed_dark_oak_planks",
            () -> new TrimmedPlankBlock(ofFullCopy(Blocks.DARK_OAK_PLANKS)), BlockProperties.trimmedPlanks(Blocks.DARK_OAK_PLANKS));
    public static final BlockDefinition<Block> ACACIA_BOOKSHELF = register("acacia_bookshelf",
            () -> new Block(ofFullCopy(Blocks.BOOKSHELF)), BlockProperties.bookshelf(Blocks.ACACIA_PLANKS));
    public static final BlockDefinition<Block> TRIMMED_ACACIA_PLANKS = register("trimmed_acacia_planks",
            () -> new TrimmedPlankBlock(ofFullCopy(Blocks.ACACIA_PLANKS)), BlockProperties.trimmedPlanks(Blocks.ACACIA_PLANKS));
    public static final BlockDefinition<Block> MANGROVE_BOOKSHELF = register("mangrove_bookshelf",
            () -> new Block(ofFullCopy(Blocks.BOOKSHELF)), BlockProperties.bookshelf(Blocks.MANGROVE_PLANKS));
    public static final BlockDefinition<Block> TRIMMED_MANGROVE_PLANKS = register("trimmed_mangrove_planks",
            () -> new TrimmedPlankBlock(ofFullCopy(Blocks.MANGROVE_PLANKS)), BlockProperties.trimmedPlanks(Blocks.MANGROVE_PLANKS));
    public static final BlockDefinition<Block> CHERRY_BOOKSHELF = register("cherry_bookshelf",
            () -> new Block(ofFullCopy(Blocks.BOOKSHELF)), BlockProperties.bookshelf(Blocks.CHERRY_PLANKS));
    public static final BlockDefinition<Block> TRIMMED_CHERRY_PLANKS = register("trimmed_cherry_planks",
            () -> new TrimmedPlankBlock(ofFullCopy(Blocks.CHERRY_PLANKS)), BlockProperties.trimmedPlanks(Blocks.CHERRY_PLANKS));
    public static final BlockDefinition<Block> WARPED_BOOKSHELF = register("warped_bookshelf",
            () -> new Block(ofFullCopy(Blocks.BOOKSHELF)), BlockProperties.bookshelf(Blocks.WARPED_PLANKS));
    public static final BlockDefinition<Block> TRIMMED_WARPED_PLANKS = register("trimmed_warped_planks",
            () -> new TrimmedPlankBlock(ofFullCopy(Blocks.WARPED_PLANKS)), BlockProperties.trimmedPlanks(Blocks.WARPED_PLANKS));
    public static final BlockDefinition<Block> CRIMSON_BOOKSHELF = register("crimson_bookshelf",
            () -> new Block(ofFullCopy(Blocks.BOOKSHELF)), BlockProperties.bookshelf(Blocks.CRIMSON_PLANKS));
    public static final BlockDefinition<Block> TRIMMED_CRIMSON_PLANKS = register("trimmed_crimson_planks",
            () -> new TrimmedPlankBlock(ofFullCopy(Blocks.CRIMSON_PLANKS)), BlockProperties.trimmedPlanks(Blocks.CRIMSON_PLANKS));
    public static final BlockDefinition<Block> BAMBOO_BOOKSHELF = register("bamboo_bookshelf",
            () -> new Block(ofFullCopy(Blocks.BOOKSHELF)), BlockProperties.bookshelf(Blocks.BAMBOO_PLANKS));
    public static final BlockDefinition<Block> TRIMMED_BAMBOO_PLANKS = register("trimmed_bamboo_planks",
            () -> new TrimmedPlankBlock(ofFullCopy(Blocks.BAMBOO_PLANKS)), BlockProperties.trimmedPlanks(Blocks.BAMBOO_PLANKS));

    //Pine
    public static final Woodset PINE = new Woodset("pine");
    public static final BlockDefinition<Block> PINE_LEAVES = register("pine_leaves",
            () -> new LeavesBlock(ofFullCopy(Blocks.OAK_LEAVES).isViewBlocking((s, g, p) -> false).isSuffocating(((s, g, p) -> false))));
    public static final BlockDefinition<Block> PINE_SAPLING = register("pine_sapling",
            () -> new SaplingBlock(NMLTreeGrowers.PINE, ofFullCopy(Blocks.OAK_SAPLING)), BlockProperties.sapling());
    public static final BlockDefinition<FlowerPotBlock> POTTED_PINE_SAPLING = registerNoItem("potted_pine_sapling",
            () -> new FlowerPotBlock(() -> ((FlowerPotBlock) Blocks.FLOWER_POT), NMLBlocks.PINE_SAPLING,
                    ofFullCopy(Blocks.POTTED_OAK_SAPLING).noOcclusion()), BlockProperties.flowerPot(PINE_SAPLING));

    //Maple
    public static final Woodset MAPLE = new Woodset("maple");
    public static final BlockDefinition<Block> MAPLE_LEAVES = register("maple_leaves",
            () -> new LeavesBlock(ofFullCopy(Blocks.OAK_LEAVES).isViewBlocking((s, g, p) -> false).isSuffocating(((s, g, p) -> false))));
    public static final BlockDefinition<Block> MAPLE_SAPLING = register("maple_sapling",
            () -> new SaplingBlock(NMLTreeGrowers.MAPLE, ofFullCopy(Blocks.OAK_SAPLING)), BlockProperties.sapling());
    public static final BlockDefinition<FlowerPotBlock> POTTED_MAPLE_SAPLING = registerNoItem("potted_maple_sapling",
            () -> new FlowerPotBlock(() -> ((FlowerPotBlock) Blocks.FLOWER_POT), NMLBlocks.MAPLE_SAPLING,
                    ofFullCopy(Blocks.POTTED_OAK_SAPLING).noOcclusion()), BlockProperties.flowerPot(MAPLE_SAPLING));
    public static final BlockDefinition<Block> RED_MAPLE_LEAVES = register("red_maple_leaves",
            () -> new LeavesBlock(ofFullCopy(Blocks.OAK_LEAVES).isViewBlocking((s, g, p) -> false).isSuffocating(((s, g, p) -> false))));
    public static final BlockDefinition<Block> RED_MAPLE_SAPLING = register("red_maple_sapling",
            () -> new SaplingBlock(NMLTreeGrowers.RED_MAPLE, ofFullCopy(Blocks.OAK_SAPLING)), BlockProperties.sapling());
    public static final BlockDefinition<FlowerPotBlock> POTTED_RED_MAPLE_SAPLING = registerNoItem("potted_red_maple_sapling",
            () -> new FlowerPotBlock(() -> ((FlowerPotBlock) Blocks.FLOWER_POT), NMLBlocks.RED_MAPLE_SAPLING,
                    ofFullCopy(Blocks.POTTED_OAK_SAPLING).noOcclusion()), BlockProperties.flowerPot(RED_MAPLE_SAPLING));

    //Walnut
    public static final Woodset WALNUT = new Woodset("walnut");
    public static final BlockDefinition<Block> WALNUT_LEAVES = register("walnut_leaves",
            () -> new LeavesBlock(ofFullCopy(Blocks.OAK_LEAVES).isViewBlocking((s, g, p) -> false).isSuffocating(((s, g, p) -> false))));
    public static final BlockDefinition<Block> WALNUT_SAPLING = register("walnut_sapling",
            () -> new SaplingBlock(NMLTreeGrowers.WALNUT, ofFullCopy(Blocks.OAK_SAPLING)), BlockProperties.sapling());
    public static final BlockDefinition<FlowerPotBlock> POTTED_WALNUT_SAPLING = registerNoItem("potted_walnut_sapling",
            () -> new FlowerPotBlock(() -> ((FlowerPotBlock) Blocks.FLOWER_POT), NMLBlocks.WALNUT_SAPLING,
                    ofFullCopy(Blocks.POTTED_OAK_SAPLING).noOcclusion()), BlockProperties.flowerPot(WALNUT_SAPLING));

//Willow
    public static final Woodset WILLOW = new Woodset("willow");
    public static final BlockDefinition<LeavesBlock> WILLOW_LEAVES = register("willow_leaves",
            () -> new LeavesBlock(ofFullCopy(Blocks.OAK_LEAVES).isViewBlocking((s, g, p) -> false).isSuffocating(((s, g, p) -> false))));
    public static final BlockDefinition<SaplingBlock> WILLOW_SAPLING = register("willow_sapling",
            () -> new SaplingBlock(NMLTreeGrowers.WILLOW, ofFullCopy(Blocks.OAK_SAPLING)), BlockProperties.sapling());
    public static final BlockDefinition<FlowerPotBlock> POTTED_WILLOW_SAPLING = registerNoItem("potted_willow_sapling",
            () -> new FlowerPotBlock(() -> ((FlowerPotBlock) Blocks.FLOWER_POT), NMLBlocks.WILLOW_SAPLING,
                    ofFullCopy(Blocks.POTTED_OAK_SAPLING).noOcclusion()), BlockProperties.flowerPot(WILLOW_SAPLING));

    public static final BlockDefinition<TapBlock> TAP = register("tap",
            () -> new TapBlock(of().mapColor(MapColor.STONE).requiresCorrectToolForDrops().noOcclusion().strength(2.0F).randomTicks().pushReaction(PushReaction.DESTROY)), new BlockProperties(new SelfBlockLootType(), false));
    public static final BlockDefinition<SpikeTrapBlock> SPIKE_TRAP = register("spike_trap",
            () -> new SpikeTrapBlock(of().mapColor(MapColor.METAL).strength(1.5F, 6.0F).requiresCorrectToolForDrops().noOcclusion()), new BlockProperties(new SelfBlockLootType(), false));

    //Storage
    public static final BlockDefinition<Block> COD_BARREL = register("cod_barrel",
            () -> new Block(ofFullCopy(Blocks.BARREL)), BlockProperties.fishBarrel());
    public static final BlockDefinition<Block> SALMON_BARREL = register("salmon_barrel",
            () -> new Block(ofFullCopy(NMLBlocks.COD_BARREL.get())), BlockProperties.fishBarrel());
    public static final BlockDefinition<Block> BILLHOOK_BASS_BARREL = register("billhook_bass_barrel",
            () -> new Block(ofFullCopy(NMLBlocks.COD_BARREL.get())), BlockProperties.fishBarrel());
    public static final BlockDefinition<Block> PUFFERFISH_BARREL = register("pufferfish_barrel",
            () -> new Block(ofFullCopy(NMLBlocks.COD_BARREL.get())), BlockProperties.fishBarrel());
    public static final BlockDefinition<Block> TROPICAL_FISH_BARREL = register("tropical_fish_barrel",
            () -> new Block(ofFullCopy(NMLBlocks.COD_BARREL.get())), BlockProperties.fishBarrel());
//    public static final BlockDefinition<Block> CAVE_CARP_BARREL = registerBlock("cave_carp_barrel",
//            () -> new Block(ofFullCopy(NMLBlocks.COD_BARREL.get())));
    public static final BlockDefinition<Block> APPLE_CRATE = register("apple_crate",
            () -> new Block(ofFullCopy(Blocks.BARREL)), new BlockProperties(new SelfBlockLootType(), false));
    public static final BlockDefinition<Block> PEAR_CRATE = register("pear_crate",
            () -> new Block(ofFullCopy(Blocks.BARREL)), new BlockProperties(new SelfBlockLootType(), false));
    //Thatch
    public static final BlockDefinition<Block> THATCH = register("thatch",
            () -> new Block(ofFullCopy(Blocks.HAY_BLOCK).sound(NMLSounds.THATCH)), new BlockProperties(new SelfBlockLootType(), false));
    public static final BlockDefinition<StairBlock> THATCH_STAIRS = register("thatch_stairs",
            () -> new StairBlock(THATCH.get().defaultBlockState(), ofFullCopy(NMLBlocks.THATCH.get())), new BlockProperties(new SelfBlockLootType(), false));
    public static final BlockDefinition<SlabBlock> THATCH_SLAB = register("thatch_slab",
            () -> new SlabBlock(ofFullCopy(NMLBlocks.THATCH.get())), new BlockProperties(new SelfBlockLootType(), false));
    public static final BlockDefinition<StrawBedBlock> STRAW_BED = register("straw_bed",
            () -> new StrawBedBlock(ofFullCopy(NMLBlocks.THATCH.get())), new BlockProperties(new BedBlockLootType(), false));
    //Mushrooms
    public static final BlockDefinition<SurfaceMushroomBlock> FIELD_MUSHROOM = registerNoItem("field_mushroom",
            () -> new SurfaceMushroomBlock((NMLFeatures.HUGE_FIELD_MUSHROOM), (ofFullCopy(Blocks.RED_MUSHROOM).sound(NMLSounds.MUSHROOM_CAP).mapColor(MapColor.TERRACOTTA_WHITE))), new BlockProperties(new SelfBlockLootType(), false));
    public static final BlockDefinition<FlowerPotBlock> POTTED_FIELD_MUSHROOM = registerNoItem("potted_field_mushroom",
            () -> new FlowerPotBlock(() -> ((FlowerPotBlock) Blocks.FLOWER_POT), NMLBlocks.FIELD_MUSHROOM,
                    ofFullCopy(Blocks.POTTED_RED_MUSHROOM).noOcclusion()), BlockProperties.flowerPot(FIELD_MUSHROOM));
    public static final BlockDefinition<HugeMushroomBlock> FIELD_MUSHROOM_BLOCK = register("field_mushroom_block",
            () -> new HugeMushroomBlock((ofFullCopy(Blocks.RED_MUSHROOM_BLOCK).sound(NMLSounds.MUSHROOM_CAP).mapColor(MapColor.TERRACOTTA_WHITE))));

    public static final BlockDefinition<ShelfMushroomBlock> SHELF_MUSHROOM = register("shelf_mushroom",
            () -> new ShelfMushroomBlock((ofFullCopy(Blocks.BROWN_MUSHROOM).sound(NMLSounds.MUSHROOM_CAP))));
    public static final BlockDefinition<SlabBlock> SHELF_MUSHROOM_BLOCK = register("shelf_mushroom_block",
            () -> new SlabBlock((ofFullCopy(Blocks.BROWN_MUSHROOM_BLOCK).sound(NMLSounds.MUSHROOM_CAP))));

    public static final BlockDefinition<RotatedPillarBlock> VERMILION_FROGLIGHT = register("vermilion_froglight",
            () -> new RotatedPillarBlock(ofFullCopy(Blocks.OCHRE_FROGLIGHT).mapColor(MapColor.COLOR_PINK)), BlockProperties.froglight());

    // Fruity Stuff
    public static final BlockDefinition<Block> APPLE_FRUIT = registerNoItem("apple",
            () -> new FruitBlock(of()
                    .mapColor(MapColor.PLANT)
                    .randomTicks()
                    .instabreak()
                    .sound(SoundType.AZALEA)
                    .pushReaction(PushReaction.DESTROY)
                    .offsetType(OffsetType.XYZ)
                    .dynamicShape()
                    ,FruitType.APPLE_OAK), BlockProperties.custom(false));

    public static final BlockDefinition<Block> APPLE_FRUIT_LEAVES = registerNoItem("apple_fruit_leaves",
            () -> new FruitLeavesBlock(ofFullCopy(Blocks.OAK_LEAVES).isViewBlocking((s, g, p) -> false).isSuffocating(((s, g, p) -> false)), FruitType.APPLE_OAK));

    public static final BlockDefinition<Block> PEAR_FRUIT = registerNoItem("pear",
            () -> new FruitBlock(of()
                    .mapColor(MapColor.PLANT)
                    .randomTicks()
                    .instabreak()
                    .sound(SoundType.AZALEA)
                    .pushReaction(PushReaction.DESTROY)
                    .offsetType(OffsetType.XYZ)
                    .dynamicShape()
                    ,FruitType.PEAR_AUTUMNAL_OAK), BlockProperties.custom(false));

    public static final BlockDefinition<Block> PEAR_FRUIT_LEAVES = registerNoItem("pear_fruit_leaves",
            () -> new FruitLeavesBlock(ofFullCopy(NMLBlocks.AUTUMNAL_OAK_LEAVES.get()).isViewBlocking((s, g, p) -> false).isSuffocating(((s, g, p) -> false)), FruitType.PEAR_AUTUMNAL_OAK));

    //Eggs
    public static final BlockDefinition<TortoiseEggBlock> TORTOISE_EGGS = register("tortoise_egg",
            () -> new TortoiseEggBlock((ofFullCopy(Blocks.TURTLE_EGG).randomTicks())));

    public static <T extends Block> BlockDefinition<T> registerNoItem(String name, Supplier<T> block, BlockProperties properties) {
        DeferredBlock<T> deferred = BLOCKS.register(name, block);
        BlockDefinition<T> definition = BlockDefinition.fromHolder(deferred, properties);
        BLOCK_DEFINITIONS.add(definition);
        return definition;
    }

    public static <T extends Block> BlockDefinition<T> registerNoItem(String name, Supplier<T> block) {
        return registerNoItem(name, block, BlockProperties.custom(false));
    }

    public static <T extends Block> BlockDefinition<T> register(String name, Supplier<T> block, BlockProperties properties) {
        BlockDefinition<T> definition = registerNoItem(name, block, properties);
        NMLItems.register(name, () -> new BlockItem(definition.get(), new Item.Properties()));
        return definition;
    }

    public static <T extends Block> BlockDefinition<T> register(String name, Supplier<T> block) {
        return register(name, block, BlockProperties.custom(false));
    }

    private static BlockSetType crudeBlockSetType;

    private static BlockSetType crudeBlockSetType() {
        if (crudeBlockSetType == null) {
            crudeBlockSetType = BlockSetType.register(new BlockSetType(
                    NoMansLand.MODID + ":crude",
                    true, true, true,
                    BlockSetType.PressurePlateSensitivity.EVERYTHING,
                    SoundType.WOOD,
                    NMLSounds.CRUDE_DOOR_CLOSE.get(),
                    NMLSounds.CRUDE_DOOR_OPEN.get(),
                    NMLSounds.CRUDE_TRAPDOOR_CLOSE.get(),
                    NMLSounds.CRUDE_TRAPDOOR_OPEN.get(),
                    SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_OFF,
                    SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_ON,
                    SoundEvents.WOODEN_BUTTON_CLICK_OFF,
                    SoundEvents.WOODEN_BUTTON_CLICK_ON
            ));
        }
        return crudeBlockSetType;
    }

    public static class Woodset {
        private final BlockDefinition<Block> PLANKS;
        private final BlockDefinition<Block> STAIRS;
        private final BlockDefinition<Block> SLAB;
        private final BlockDefinition<Block> TRIMMED_PLANKS;
        private final BlockDefinition<Block> LOG;
        private final BlockDefinition<Block> WOOD;
        private final BlockDefinition<Block> STRIPPED_LOG;
        private final BlockDefinition<Block> STRIPPED_WOOD;
        private final BlockDefinition<Block> FENCE;
        private final BlockDefinition<Block> FENCE_GATE;
        private final BlockDefinition<Block> BUTTON;
        private final BlockDefinition<Block> PRESSURE_PLATE;
        private final BlockDefinition<Block> DOOR;
        private final BlockDefinition<Block> TRAPDOOR;
        private final BlockDefinition<Block> BOOKSHELF;
        private final BlockDefinition<StandingSignBlock> SIGN;
        private final BlockDefinition<WallSignBlock> WALL_SIGN;
        private final BlockDefinition<CeilingHangingSignBlock> HANGING_SIGN;
        private final BlockDefinition<WallHangingSignBlock> HANGING_WALL_SIGN;

        public Woodset(String name) {
            BlockSetType blockSetType = BlockSetType.register(new BlockSetType(NoMansLand.MODID + ":" + name));
            WoodType woodType = WoodType.register(new WoodType(NoMansLand.MODID + ":" + name, blockSetType));
            WOODSETS.add(this);
            PLANKS = register(name + "_planks",
                    () -> new Block(ofFullCopy(Blocks.OAK_PLANKS)), BlockProperties.planks());
            STAIRS = register(name + "_stairs",
                    () -> new StairBlock(PLANKS.get().defaultBlockState(), ofFullCopy(PLANKS.get())), BlockProperties.woodenStairs(PLANKS));
            SLAB = register(name + "_slab",
                    () -> new SlabBlock(ofFullCopy(PLANKS.get())), BlockProperties.woodenSlab(PLANKS));
            TRIMMED_PLANKS = register("trimmed_" + name + "_planks",
                    () -> new TrimmedPlankBlock(ofFullCopy(PLANKS.get())), BlockProperties.trimmedPlanks(PLANKS));
            LOG = register(name + "_log",
                    () -> new LogBlock(ofFullCopy(Blocks.OAK_LOG)), BlockProperties.log());
            WOOD = register(name + "_wood",
                    () -> new LogBlock(ofFullCopy(Blocks.OAK_WOOD)), BlockProperties.log());
            STRIPPED_LOG = register("stripped_" + name + "_log",
                    () -> new LogBlock(ofFullCopy(Blocks.STRIPPED_OAK_LOG)), BlockProperties.log());
            STRIPPED_WOOD = register("stripped_" + name + "_wood",
                    () -> new LogBlock(ofFullCopy(Blocks.STRIPPED_OAK_WOOD)), BlockProperties.log());
            FENCE = register(name + "_fence",
                    () -> new FenceBlock(ofFullCopy(Blocks.OAK_FENCE)), BlockProperties.woodenFence(PLANKS));
            FENCE_GATE = register(name + "_fence_gate",
                    () -> new FenceGateBlock(woodType, ofFullCopy(Blocks.OAK_FENCE_GATE)), BlockProperties.fenceGate(PLANKS));
            BUTTON = register(name + "_button",
                    () -> new ButtonBlock(blockSetType, 15, ofFullCopy(Blocks.OAK_BUTTON)), BlockProperties.woodenButton(PLANKS));
            PRESSURE_PLATE = register(name + "_pressure_plate",
                    () -> new PressurePlateBlock(blockSetType,
                            ofFullCopy(Blocks.OAK_PRESSURE_PLATE)), BlockProperties.woodenPressurePlate(PLANKS));
            DOOR = register(name + "_door",
                    () -> new DoorBlock(blockSetType, ofFullCopy(Blocks.OAK_DOOR)), BlockProperties.woodenDoor(PLANKS));
            TRAPDOOR = register(name + "_trapdoor",
                    () -> new TrapDoorBlock(blockSetType, ofFullCopy(Blocks.OAK_TRAPDOOR)), BlockProperties.woodenTrapdoor(PLANKS));
            BOOKSHELF = register(name + "_bookshelf",
                    () -> new Block(ofFullCopy(Blocks.BOOKSHELF)), BlockProperties.bookshelf(PLANKS));
            SIGN = registerNoItem(name + "_sign",
                    () -> new StandingSignBlock(woodType, ofFullCopy(Blocks.OAK_SIGN)), BlockProperties.sign());
            WALL_SIGN = registerNoItem(name + "_wall_sign",
                    () -> {
                        Block signBlock = SIGN.get();
                        return new WallSignBlock(woodType, ofFullCopy(Blocks.OAK_WALL_SIGN).lootFrom(() -> signBlock));
                    }, BlockProperties.custom(true));
            HANGING_SIGN = registerNoItem(name + "_hanging_sign",
                    () -> new CeilingHangingSignBlock(woodType, ofFullCopy(Blocks.OAK_HANGING_SIGN)), BlockProperties.hangingSign());
            HANGING_WALL_SIGN = registerNoItem(name + "_wall_hanging_sign",
                    () -> {
                        Block hangingSignBlock = HANGING_SIGN.get();
                        return new WallHangingSignBlock(woodType, ofFullCopy(Blocks.OAK_WALL_HANGING_SIGN).lootFrom(() -> hangingSignBlock));
                    }, BlockProperties.custom(true));
        }

        public BlockDefinition<Block> planks() { return PLANKS; }
        public BlockDefinition<Block> stairs() { return STAIRS; }
        public BlockDefinition<Block> slab() { return SLAB; }
        public BlockDefinition<Block> trimmedPlanks() { return TRIMMED_PLANKS; }
        public BlockDefinition<Block> log() { return LOG; }
        public BlockDefinition<Block> wood() { return WOOD; }
        public BlockDefinition<Block> strippedLog() { return STRIPPED_LOG; }
        public BlockDefinition<Block> strippedWood() { return STRIPPED_WOOD; }
        public BlockDefinition<Block> fence() { return FENCE; }
        public BlockDefinition<Block> fenceGate() { return FENCE_GATE; }
        public BlockDefinition<Block> button() { return BUTTON; }
        public BlockDefinition<Block> pressurePlate() { return PRESSURE_PLATE; }
        public BlockDefinition<Block> door() { return DOOR; }
        public BlockDefinition<Block> trapdoor() { return TRAPDOOR; }
        public BlockDefinition<Block> bookshelf() { return BOOKSHELF; }
        public BlockDefinition<StandingSignBlock> sign() { return SIGN; }
        public BlockDefinition<WallSignBlock> wallSign() { return WALL_SIGN; }
        public BlockDefinition<CeilingHangingSignBlock> hangingSign() { return HANGING_SIGN; }
        public BlockDefinition<WallHangingSignBlock> hangingWallSign() { return HANGING_WALL_SIGN; }

        public void setFlammables() {
            FireBlock fireBlock = (FireBlock) Blocks.FIRE;
            fireBlock.setFlammable(PLANKS.block(), 5, 20);
            fireBlock.setFlammable(STAIRS.block(), 5, 20);
            fireBlock.setFlammable(SLAB.block(), 5, 20);
            fireBlock.setFlammable(FENCE.block(), 5, 20);
            fireBlock.setFlammable(BOOKSHELF.block(), 30, 20);
            fireBlock.setFlammable(TRIMMED_PLANKS.block(), 5, 20);
        }

        public @Nullable BlockState checkLogStripping(BlockState state) {
            if (state.is(LOG.block())) return STRIPPED_LOG.block().defaultBlockState().setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS));
            if (state.is(WOOD.block())) return STRIPPED_WOOD.block().defaultBlockState().setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS));
            return null;
        }
    }
}