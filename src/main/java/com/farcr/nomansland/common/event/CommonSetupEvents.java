package com.farcr.nomansland.common.event;

import com.farcr.nomansland.NMLConfig;
import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.blockentity.BombDispenseBehavior;
import com.farcr.nomansland.common.definitions.BlockDefinition;
import com.farcr.nomansland.common.definitions.ItemDefinition;
import com.farcr.nomansland.common.entity.billhook_bass.BillhookBass;
import com.farcr.nomansland.common.entity.buddy.Buddy;
import com.farcr.nomansland.common.entity.cervidae.deer.Deer;
import com.farcr.nomansland.common.entity.frienderman.Frienderman;
import com.farcr.nomansland.common.entity.goose.Goose;
import com.farcr.nomansland.common.entity.living_pot.LivingPot;
import com.farcr.nomansland.common.entity.tortoise.Tortoise;
import com.farcr.nomansland.common.integration.FDIntegration;
import com.farcr.nomansland.common.integration.Mods;
import com.farcr.nomansland.common.integration.create.CreateIntegration;
import com.farcr.nomansland.common.item.ThrowableBombItem;
import com.farcr.nomansland.common.mixin.BlockBehaviourAccessModifier;
import com.farcr.nomansland.common.networking.*;
import com.farcr.nomansland.common.networking.alchemist_tools.*;
import com.farcr.nomansland.common.registry.NMLFluids;
import com.farcr.nomansland.common.registry.NMLSounds;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import com.farcr.nomansland.common.registry.blocks.NMLFlammables;
import com.farcr.nomansland.common.registry.entities.NMLEntities;
import com.farcr.nomansland.common.registry.items.NMLItems;
import com.farcr.nomansland.common.world.generation.NMLBiomePlacements;
import com.farcr.nomansland.common.world.generation.NMLDensityModifications;
import com.farcr.nomansland.common.world.generation.NMLSurfaceRules;
import net.minecraft.core.dispenser.BoatDispenseItemBehavior;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.fluids.RegisterCauldronFluidContentEvent;

import static com.farcr.nomansland.common.block.cauldrons.FourLayeredCauldronBlock.LEVEL;

@EventBusSubscriber(modid = NoMansLand.MODID)
public class CommonSetupEvents {
    @SubscribeEvent
    public static void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            if (NMLConfig.BIOMES.get()) NMLBiomePlacements.register();
            NMLDensityModifications.register();
            NMLSurfaceRules.register();
            NMLFlammables.register();
            if (Mods.CREATE.isLoaded()) CreateIntegration.registerOpenPipeEffects();

            overrideMushroomSounds();

            for (BlockDefinition<?> definition : NMLBlocks.BLOCK_DEFINITIONS) {
                if (definition.get() instanceof FlowerPotBlock flowerPotBlock) {
                    flowerPotBlock.getEmptyPot().addPlant(BuiltInRegistries.BLOCK.getKey(flowerPotBlock.getPotted()), () -> flowerPotBlock);
                }
            }

            for (ItemDefinition<?> definition : NMLItems.ITEM_DEFINITIONS) {
                Item item = definition.item();
                if (item instanceof ThrowableBombItem) DispenserBlock.registerBehavior(item, new BombDispenseBehavior(item));
                else if (item instanceof ProjectileItem) DispenserBlock.registerProjectileBehavior(item);

                if (item instanceof BoatItem boat) DispenserBlock.registerBehavior(item, new BoatDispenseItemBehavior(boat.type, boat.hasChest));
            }
        });
    }

    private static void overrideMushroomSounds() {
        setSoundType(Blocks.RED_MUSHROOM, NMLSounds.MUSHROOM_CAP);
        setSoundType(Blocks.BROWN_MUSHROOM, NMLSounds.MUSHROOM_CAP);
        setSoundType(Blocks.RED_MUSHROOM_BLOCK, NMLSounds.MUSHROOM_CAP);
        setSoundType(Blocks.BROWN_MUSHROOM_BLOCK, NMLSounds.MUSHROOM_CAP);
        setSoundType(Blocks.MUSHROOM_STEM, NMLSounds.MUSHROOM_CAP);

        if (Mods.FARMERSDELIGHT.isLoaded()) FDIntegration.overrideMushroomColonySounds();
    }

    public static void setSoundType(Block block, SoundType soundType) {
        ((BlockBehaviourAccessModifier) block).nml$setSoundType(soundType);
    }

    @SubscribeEvent
    public static void createEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(NMLEntities.BILLHOOK_BASS.get(), BillhookBass.createAttributes().build());
        event.put(NMLEntities.DEER.get(), Deer.createAttributes().build());
        // TODO: moose
//        event.put(NMLEntities.MOOSE.get(), Moose.createAttributes().build());
        event.put(NMLEntities.GOOSE.get(), Goose.createAttributes().build());
        event.put(NMLEntities.TORTOISE.get(), Tortoise.createAttributes().build());
        event.put(NMLEntities.LIVING_POT.get(), LivingPot.createAttributes().build());
        event.put(NMLEntities.BUDDY.get(), Buddy.createAttributes().build());
        event.put(NMLEntities.FRIENDERMAN.get(), Frienderman.createAttributes().build());
        event.put(NMLEntities.DREAMING_PLAYER.get(), Mob.createMobAttributes().build());
    }

    @SubscribeEvent
    public static void registerSpawnPlacements(final RegisterSpawnPlacementsEvent event) {
        event.register(NMLEntities.BILLHOOK_BASS.get(), SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, BillhookBass::checkSurfaceWaterAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(NMLEntities.DEER.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Deer::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        // TODO: moose
//        event.register(NMLEntities.MOOSE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Moose::checkMooseSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(EntityType.CAMEL, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Camel::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(EntityType.HUSK, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(NMLEntities.TORTOISE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Tortoise::checkTortoiseSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(NMLEntities.GOOSE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.OCEAN_FLOOR, Goose::checkGooseSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    @SubscribeEvent
    public static void registerCauldronFluidContent(final RegisterCauldronFluidContentEvent event) {
        event.register(NMLBlocks.RESIN_OIL_CAULDRON.get(), NMLFluids.RESIN_OIL.get(), 1000, LEVEL);
        if (NeoForgeMod.MILK.isBound())
            event.register(NMLBlocks.MILK_CAULDRON.get(), NeoForgeMod.MILK.get(), 1000, LEVEL);
        if (Mods.CREATE.isLoaded())
            event.register(NMLBlocks.HONEY_CAULDRON.get(), Mods.CREATE.getFluid("honey"), 1000, LEVEL);
    }
}
