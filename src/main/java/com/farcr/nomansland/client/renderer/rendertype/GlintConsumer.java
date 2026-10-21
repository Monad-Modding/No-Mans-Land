package com.farcr.nomansland.client.renderer.rendertype;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/*
* Eventually streamline this for fun :P
* Don't really see a need right now, though
*
* Idea being register a glint rendertype with a condition & it should handle the context automatically or so
* but that can come later I'm just trying to get this done atm
*/
public class GlintConsumer {

    final static List<BiFunction<MultiBufferSource, VertexConsumer, VertexConsumer>> multiConsumer = List.of(
        MoonlightGlowRenderType::getConsumer
        // handling this one separately
        // AncestralGlintRenderType::getConsumer
    );

    public static VertexConsumer consume(MultiBufferSource bufferSource, VertexConsumer originalConsumer) {
        VertexConsumer finalConsumer = originalConsumer;
        for (var consumer : multiConsumer) finalConsumer = consumer.apply(bufferSource, finalConsumer);
        return finalConsumer;
    }
}
