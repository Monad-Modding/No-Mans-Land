package com.farcr.nomansland.client;

import com.farcr.nomansland.client.model.armor.AncientBronzeMaskModel;
import com.farcr.nomansland.client.model.armor.GasMaskModel;
import com.farcr.nomansland.client.model.tortoise.TortoiseShellModel;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public class NMLArmorModels {

    public static AncientBronzeMaskModel ANCIENT_BRONZE_MASK;
    public static TortoiseShellModel TORTOISE_SHELL;
    public static GasMaskModel GAS_MASK;

    /**
     * This event is oddly named, we use it to bake some of the layers and pair them up with our model objects.
     */
    public static void addLayers(EntityRenderersEvent.AddLayers event) {
        ANCIENT_BRONZE_MASK = new AncientBronzeMaskModel(event.getEntityModels().bakeLayer(NMLModelLayers.ANCIENT_BRONZE_MASK_LAYER));
        TORTOISE_SHELL = new TortoiseShellModel(event.getEntityModels().bakeLayer(NMLModelLayers.TORTOISE_SHELL_LAYER));
        GAS_MASK = new GasMaskModel(event.getEntityModels().bakeLayer(NMLModelLayers.GAS_MASK_LAYER));
    }
}