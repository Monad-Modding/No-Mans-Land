package com.farcr.nomansland.datagen;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.datagen.sound.NMLSoundProvider;
import com.farcr.nomansland.datagen.tags.NMLBiomeTagsProvider;
import com.farcr.nomansland.datagen.tags.NMLBlockTagsProvider;
import com.farcr.nomansland.datagen.tags.NMLItemTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = NoMansLand.MODID)
public class DataGenEvents {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();

        boolean server = event.includeServer();
        boolean client = event.includeClient();

        // Loot tables
        generator.addProvider(
                server,
                new LootTableProvider(
                        generator.getPackOutput(),
                        Set.of(),
                        List.of(new LootTableProvider.SubProviderEntry(NMLBlockLootSubProvider::new, LootContextParamSets.BLOCK)),
                        lookupProvider
                )
        );

        //Sounds
        generator.addProvider(server, new NMLSoundProvider(packOutput, existingFileHelper));

        // Tags
        generator.addProvider(server, new NMLBiomeTagsProvider(packOutput, lookupProvider, existingFileHelper));

        BlockTagsProvider blockTagsProvider = generator.addProvider(server, new NMLBlockTagsProvider(packOutput, lookupProvider, existingFileHelper));
        generator.addProvider(server, new NMLItemTagsProvider(packOutput, lookupProvider, blockTagsProvider.contentsGetter(), existingFileHelper));

        generator.addProvider(server, new NMLDatapackEntriesProvider(packOutput, lookupProvider));

        // Lang
        Path existingDataRoot = packOutput.getOutputFolder().getParent().getParent().resolve("main/resources");
        generator.addProvider(client, new NMLLanguageProvider(packOutput));
        generator.addProvider(client, new NMLDialogueLanguageProvider(packOutput, existingDataRoot, lookupProvider));

        // these don't really work with the existing file structure...
        // made mostly for my sake. use further if you'd like :P
        //    ~ cappin
        //generator.addProvider(true, new NMLRecipeProvider(packOutput, lookupProvider));
        //generator.addProvider(client, new NMLBlockStateProvider(packOutput, existingFileHelper));
    }
}
