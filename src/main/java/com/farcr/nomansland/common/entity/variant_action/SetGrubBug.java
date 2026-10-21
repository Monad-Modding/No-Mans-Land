package com.farcr.nomansland.common.entity.variant_action;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.tazer.mixed_litter.actions.VariantActionType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

public class SetGrubBug implements VariantActionType {
    private boolean resolved;
    private EntityType<?> bug;

    public EntityType<?> getBug() {
        return resolved ? bug : null;
    }

    @Override
    public VariantActionType resolve(JsonObject actionsArgs, JsonObject variantArgs, JsonObject defaultArgs) {
        SetGrubBug result = new SetGrubBug();
        JsonObject arguments = VariantActionType.resolveArguments(actionsArgs, variantArgs, defaultArgs);
        if (arguments != null && arguments.has("entity")) {
            JsonElement element = arguments.get("entity");
            if (element.isJsonPrimitive()) {
                String id = element.getAsString();
                if (!id.isEmpty()) {
                    ResourceLocation location = ResourceLocation.tryParse(id);
                    if (location != null && BuiltInRegistries.ENTITY_TYPE.containsKey(location)) {
                        result.bug = BuiltInRegistries.ENTITY_TYPE.get(location);
                    }
                }
            }
        }
        result.resolved = true;
        return result;
    }
}
