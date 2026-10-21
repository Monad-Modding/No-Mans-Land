package com.farcr.nomansland.client.renderer.entity;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.NMLModelLayers;
import com.farcr.nomansland.client.model.BuriedModel;
import com.farcr.nomansland.client.renderer.entity.layers.BuriedArmorLayer;
import com.farcr.nomansland.common.entity.Buried;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class BuriedRenderer extends MobRenderer<Buried, BuriedModel> {

    public BuriedRenderer(EntityRendererProvider.Context context) {
        super(context, new BuriedModel(context.bakeLayer(NMLModelLayers.BURIED_LAYER)), 0.5F);
        this.addLayer(new BuriedArmorLayer(this,
                context.bakeLayer(NMLModelLayers.BURIED_INNER_ARMOR),
                context.bakeLayer(NMLModelLayers.BURIED_OUTER_ARMOR)));
    }

    @Override
    public ResourceLocation getTextureLocation(Buried entity) {
        return NoMansLand.location("textures/entity/buried/buried_0.png");
    }
}
