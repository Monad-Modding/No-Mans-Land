package com.farcr.nomansland.common.world.structure.poolelements;

import com.farcr.nomansland.common.world.structure.StructureErosion;
import com.farcr.nomansland.common.world.structure.StructurePlaceSettingsErosionHolder;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.Optional;

public class ErodedSinglePoolElement extends SinglePoolElement {
    final StructureErosion.Type erosionType;

    protected ErodedSinglePoolElement(Either<ResourceLocation, StructureTemplate> template,
                                      Holder<StructureProcessorList> processors,
                                      StructureTemplatePool.Projection projection,
                                      Optional<LiquidSettings> overrideLiquidSettings,
                                      StructureErosion.Type type) {
        super(template, processors, projection, overrideLiquidSettings);
        this.erosionType = type;
    }

    @Override
    protected StructurePlaceSettings getSettings(Rotation rotation, BoundingBox boundingBox, LiquidSettings liquidSettings, boolean offset) {
        return ((StructurePlaceSettingsErosionHolder) super.getSettings(rotation, boundingBox, liquidSettings, offset))
                .nml$setErosionType(erosionType);
    }

    public static class Type implements StructurePoolElementType<ErodedSinglePoolElement> {
        public static final MapCodec<ErodedSinglePoolElement> CODEC = RecordCodecBuilder.mapCodec(
                instance -> instance.group(
                                templateCodec(),
                                processorsCodec(),
                                projectionCodec(),
                                overrideLiquidSettingsCodec(),
                                StructureErosion.Type.CODEC.optionalFieldOf("erosion_type", StructureErosion.Type.ALL).forGetter(element -> element.erosionType)
                        ).apply(instance, ErodedSinglePoolElement::new)
        );

        @Override
        public MapCodec<ErodedSinglePoolElement> codec() { return CODEC; }
    }
}
