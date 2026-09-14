package com.farcr.nomansland;

import com.farcr.nomansland.common.event.CreativeModeTabHandler;
import com.farcr.nomansland.common.integration.BBIntegration;
import com.farcr.nomansland.common.integration.FDIntegration;
import com.farcr.nomansland.common.integration.Mods;
import com.farcr.nomansland.common.integration.boatload.BoatloadIntegration;
import com.farcr.nomansland.common.integration.everycompat.EveryCompatIntegration;
import com.farcr.nomansland.common.integration.nirvana.NirvanaIntegration;
import com.farcr.nomansland.common.registry.*;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import com.farcr.nomansland.common.registry.blocks.NMLExtinguishables;
import com.farcr.nomansland.common.registry.entities.*;
import com.farcr.nomansland.common.registry.items.*;
import com.farcr.nomansland.common.registry.worldgen.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(NoMansLand.MODID)
public class NoMansLand {

    public static final String MODID = "nomansland";
    public static final Logger LOGGER = LogManager.getLogger("No Man's Land");

    public NoMansLand(IEventBus bus, ModContainer container) {
        NMLItems.ITEMS.register(bus);
        NMLBlocks.BLOCKS.addAlias(NoMansLand.location("apple_fruit"), NoMansLand.location("apple"));
        NMLBlocks.BLOCKS.addAlias(NoMansLand.location("pear_fruit"), NoMansLand.location("pear"));
        NMLBlocks.BLOCKS.register(bus);
        NMLExtinguishables.EXTINGUISHABLES.register(bus);
        NMLEntityDataSerializers.ENTITY_DATA_SERIALIZERS.register(bus);
        NMLEntityDataAttachments.DATA_ATTACHMENTS.register(bus);
        NMLEntities.ENTITIES.register(bus);
        NMLSensors.SENSORS.register(bus);
        NMLMemoryModules.MEMORY_MODULES.register(bus);
        NMLFeatures.FEATURES.register(bus);
        NMLFoliagePlacerTypes.FOLIAGE_PLACER_TYPES.register(bus);
        NMLTrunkPlacerTypes.TRUNK_PLACER_TYPES.register(bus);
        NMLSounds.SOUND_EVENTS.register(bus);
        NMLPotions.POTION_REGISTRY.register(bus);
        NMLCreativeTabs.CREATIVE_TABS.register(bus);
        NMLParticleTypes.PARTICLE_TYPES.register(bus);
        NMLBlockEntities.BLOCK_ENTITIES.register(bus);
        NMLLootModifiers.LOOT_MODIFIERS.register(bus);
        NMLTreeDecoratorTypes.TREE_DECORATOR_TYPES.register(bus);
        NMLPondDecoratorTypes.POND_DECORATOR_TYPES.register(bus);
        NMLBoulderDecoratorTypes.BOULDER_DECORATOR_TYPES.register(bus);
        NMLFallenTreeDecoratorTypes.FALLEN_TREE_DECORATOR_TYPES.register(bus);
        NMLDialogueConditions.DIALOGUE_CONDITION_REGISTRY.register(bus);
        NMLContextualMusic.CONTEXTUAL_MUSIC_REGISTRY.register(bus);
        NMLDreamTypes.DREAM_TYPES_REGISTRY.register(bus);
        NMLFogModifiers.FOG_MODIFIERS.register(bus);
        NMLCarvingTypes.CARVING_TYPES.register(bus);
        NMLMobVariants.FROG_VARIANTS.register(bus);
        NMLEffects.MOB_EFFECTS.register(bus);
        NMLStructureProcessorTypes.STRUCTURE_PROCESSOR_TYPES.register(bus);
        NMLCriteriaTriggers.TRIGGERS.register(bus);
        NMLRecipeSerializers.RECIPE_SERIALIZERS.register(bus);
        NMLRecipeSerializers.RECIPE_TYPES.register(bus);
        NMLConditions.CONDITION_CODECS.register(bus);
        NMLFluids.FLUID_TYPES.register(bus);
        NMLFluids.FLUIDS.register(bus);
        NMLBiomeModifiers.BIOME_MODIFIERS.register(bus);
        NMLPlacementModifiers.PLACEMENT_MODIFIER_TYPES.register(bus);
        NMLDensityFunctions.DENSITY_FUNCTIONS.register(bus);
        NMLMaterialConditions.MATERIAL_CONDITIONS.register(bus);
        NMLMaterialRules.MATERIAL_RULES.register(bus);
        NMLArmorMaterials.ARMOR_MATERIALS.register(bus);
        NMLDataComponents.DATA_COMPONENTS.register(bus);
        NMLVariantActions.ACTIONS.register(bus);
        NMLStructureTypes.STRUCTURE_TYPES.register(bus);
        NMLStructurePlacements.STRUCTURE_PLACEMENTS.register(bus);
        NMLBlockStateProviderTypes.BLOCKSTATE_PROVIDER_TYPES.register(bus);
        NMLStructureElementTypes.STRUCTURE_ELEMENTS.register(bus);
        NMLMapDecorationTypes.MAP_DECORATION_TYPES.register(bus);
        NMLAttachmentTypes.ATTACHMENT_TYPES.register(bus);

        if (Mods.FARMERSDELIGHT.isLoaded()) {
            FDIntegration.register();
            bus.addListener(FDIntegration::addBlockEntities);
        }

        if (Mods.NIRVANA.isLoaded()) NirvanaIntegration.register();
        if (Mods.BLOCKBOX.isLoaded()) BBIntegration.register();
        if (Mods.BOATLOAD.isLoaded()) BoatloadIntegration.register();
        if (Mods.EVERYCOMP.isLoaded()) EveryCompatIntegration.register();

        bus.register(new CreativeModeTabHandler());
        bus.addListener(NMLBlockEntities::addBlockEntities);

        container.registerConfig(ModConfig.Type.COMMON, NMLConfig.COMMON_CONFIG);
        container.registerConfig(ModConfig.Type.CLIENT, NMLConfig.CLIENT_CONFIG);
        container.registerConfig(ModConfig.Type.STARTUP, NMLConfig.STARTUP_CONFIG);
    }

    public static ResourceLocation location(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
