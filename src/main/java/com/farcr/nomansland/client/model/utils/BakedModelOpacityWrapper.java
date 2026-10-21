package com.farcr.nomansland.client.model.utils;

import com.farcr.nomansland.NoMansLand;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.FastColor;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BakedModelOpacityWrapper extends BakedModelWrapper<BakedModel> {
    private float bakedAlpha;
    public void setAlphaValue(float bakedAlpha) {
        this.bakedAlpha = bakedAlpha;
    }

    public BakedModelOpacityWrapper(BakedModel originalModel) {
        super(originalModel);
    }

    BakedQuad modifyQuads(BakedQuad bakedQuad) {
        int[] vertices = bakedQuad.getVertices().clone();
        int step = vertices.length / 4;
        for (int i = 0; i < 4; i++) {
            vertices[i * step + 3] = FastColor.ARGB32.colorFromFloat(
                1f, bakedAlpha, bakedAlpha, bakedAlpha
            );
        }
        return new BakedQuad(
            vertices,
            bakedQuad.getTintIndex(),
            bakedQuad.getDirection(),
            bakedQuad.getSprite(),
            bakedQuad.isTinted(),
            bakedQuad.hasAmbientOcclusion()
        );
    }

    /*
    * "Deprecated" by NF but still safer to use because
    * the internal pipeline uses this. i think. or sodium, one of the two
    */
    @Override public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, RandomSource source) {
        return super.getQuads(state, direction, source)
            .stream().map(this::modifyQuads).toList();
    }
}
