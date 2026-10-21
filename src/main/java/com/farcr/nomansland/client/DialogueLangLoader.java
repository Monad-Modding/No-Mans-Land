package com.farcr.nomansland.client;

import com.farcr.nomansland.common.friend.condition.DialogueConditionCompiler;
import com.farcr.nomansland.common.friend.dialogue.DialoguePool;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.locale.Language;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class DialogueLangLoader extends SimplePreparableReloadListener<Map<String, Map<String, String>>> {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final DialogueLangLoader INSTANCE = new DialogueLangLoader();

    private Map<String, Map<String, String>> cache = new HashMap<>();

    @Override
    protected Map<String, Map<String, String>> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        String langCode = Minecraft.getInstance().getLanguageManager().getSelected().toLowerCase(Locale.ROOT);
        Map<String, Map<String, String>> result = new HashMap<>();

        for (ResourceKey<Registry<DialoguePool>> registryKey : DialogueConditionCompiler.REGISTRIES) {
            String registryPath = registryKey.location().getPath();
            String category = registryPath.substring(registryPath.lastIndexOf('/') + 1);

            Map<String, String> categoryMap = result.computeIfAbsent(category, k -> new HashMap<>());

            // Load en_us as fallback first
            loadLangFile(resourceManager, category, "en_us", categoryMap);
            // Then overlay the selected language on top
            if (!"en_us".equals(langCode)) {
                loadLangFile(resourceManager, category, langCode, categoryMap);
            }
        }

        return result;
    }

    private void loadLangFile(ResourceManager resourceManager, String category, String langCode, Map<String, String> categoryMap) {
        String path = "lang/nomansland/dialogue_pools/" + category + "/" + langCode + ".json";

        for (String namespace : resourceManager.getNamespaces()) {
            ResourceLocation resourceLocation = ResourceLocation.fromNamespaceAndPath(namespace, path);
            List<Resource> resources = resourceManager.getResourceStack(resourceLocation);
            for (Resource resource : resources) {
                try (InputStream inputStream = resource.open()) {
                    Language.loadFromJson(inputStream, categoryMap::put);
                } catch (IOException e) {
                    LOGGER.warn("Failed to load dialogue translations for {} from pack {}", langCode, resource.sourcePackId(), e);
                }
            }
        }
    }

    @Override
    protected void apply(Map<String, Map<String, String>> prepared, ResourceManager resourceManager, ProfilerFiller profiler) {
        cache = prepared;
    }

    public Optional<String> getString(String category, String key) {
        Map<String, String> categoryMap = cache.get(category);
        if (categoryMap == null) return Optional.empty();
        return Optional.ofNullable(categoryMap.get(key));
    }
}
