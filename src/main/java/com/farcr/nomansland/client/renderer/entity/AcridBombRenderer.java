package com.farcr.nomansland.client.renderer.entity;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.entity.bombs.AcridBomb;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.resources.model.ModelResourceLocation;

public class AcridBombRenderer extends ThrowableBombRenderer<AcridBomb> {

    private static final ModelResourceLocation MODEL = ModelResourceLocation.standalone(NoMansLand.location("entity/acrid_bomb"));

    public AcridBombRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ModelResourceLocation getModelLocation(AcridBomb entity) {
        return MODEL;
    }
}
