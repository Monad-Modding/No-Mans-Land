package com.farcr.nomansland.client.renderer.entity;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.NMLModelLayers;
import com.farcr.nomansland.client.model.moose.MooseAntlersLayer;
import com.farcr.nomansland.client.model.moose.MooseModel;
import com.farcr.nomansland.client.model.moose.MooseSaddleLayer;
import com.farcr.nomansland.common.entity.cervidae.moose.Moose;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class MooseRenderer extends MobRenderer<Moose, MooseModel<Moose>> {

    public MooseRenderer(EntityRendererProvider.Context context) {
        super(context, new MooseModel<>(context.bakeLayer(NMLModelLayers.MOOSE_LAYER)), 1f);
        this.addLayer(new MooseAntlersLayer(this));
        this.addLayer(new MooseSaddleLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(Moose pEntity) {
        return NoMansLand.location("textures/entity/moose/moose_brown.png");
    }
}