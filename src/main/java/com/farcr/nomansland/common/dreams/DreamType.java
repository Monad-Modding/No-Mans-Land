package com.farcr.nomansland.common.dreams;

import com.farcr.nomansland.client.renderer.dreams.IDreamRenderer;
import com.farcr.nomansland.common.registry.NMLRegistries;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.util.TriConsumer;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/*
* Class that stores information about dream types`
*/
public class DreamType {
    public static final Codec<DreamType> CODEC = NMLRegistries.DREAM_TYPE.byNameCodec();

    public @Nullable BiFunction<ServerPlayer, ServerLevel, Boolean> biconsumer;
    public DreamType setCondition(@Nullable BiFunction<ServerPlayer, ServerLevel, Boolean> condition) {
        this.biconsumer = condition;
        return this;
    }

    public boolean canSprint = false;
    public DreamType setCanSprint(boolean canSprint) {
        this.canSprint = canSprint;
        return this;
    }

    public boolean timeCondition(ServerPlayer player, ServerLevel level) {
        DreamStorage storage = DreamManager.getOrDefault(
            Objects.requireNonNull(player.getServer())).getPlayerStorage(player);
        return (storage.getTimeRemainingForDream(this) > 0);
    }

    protected void defaultChunkGenerator(ChunkAccess chunk, StructureManager manager, WorldGenRegion level) {
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                BlockPos blockPos = new BlockPos(i, 0, j);
                chunk.setBlockState(
                    blockPos,
                    Blocks.BEDROCK.defaultBlockState(),
                    false
                );
                if (i % 8 == 0 && j % 8 == 0) {
                    chunk.setBlockState(
                        blockPos.above(),
                        Blocks.GLOWSTONE.defaultBlockState(),
                        false
                    );
                }
            }
        }
    }

    public Supplier<DreamTypeInstance> instanceSupplier = () -> new DreamTypeInstance(this);
    public void setInstanceSupplier(Supplier<DreamTypeInstance> instanceSupplier) {
        this.instanceSupplier = instanceSupplier;
    }

    public static class DreamTypeInstance {
        private final DreamType dreamType;
        public DreamType getDreamType() {
            return dreamType;
        }
        public DreamTypeInstance(DreamType dreamType) {
            this.dreamType = dreamType;
        }
        public void tick(Level level) {}
    }

    public void onDreamEnd(ServerPlayer player, boolean success) {}

    public void createStructures(
        ChunkGenerator generator,
        RegistryAccess registryAccess,
        ChunkGeneratorStructureState structureState,
        StructureManager structureManager, ChunkAccess chunk,
        StructureTemplateManager structureTemplateManager
    ) {}

    public Supplier<IDreamRenderer> dreamRenderer;
    public DreamType setRenderer(Supplier<Supplier<IDreamRenderer>> dreamRenderer) {
        if (FMLEnvironment.dist.isClient()) this.dreamRenderer = dreamRenderer.get();
        return this;
    }

    public TriConsumer<ChunkAccess, StructureManager, WorldGenRegion> chunkGenerator = this::defaultChunkGenerator;
    public DreamType setChunkGenerator(TriConsumer<ChunkAccess, StructureManager, WorldGenRegion> chunkGenerator) {
        this.chunkGenerator = chunkGenerator;
        return this;
    }

    public Vec3 spawnPoint = new Vec3(0, 0, 0);
    public DreamType setSpawnPoint(Vec3 spawnPoint) {
        this.spawnPoint = spawnPoint;
        return this;
    }

    public double worldBorder = 0.0d;
    public DreamType setWorldBorder(double worldBorder) {
        this.worldBorder = worldBorder;
        return this;
    }

    private boolean hideHUD = true;
    public boolean hideHUD() {
        return hideHUD;
    }

    public DreamType setHUDHidden(boolean hudHidden) {
        this.hideHUD = hudHidden;
        return this;
    }
}
