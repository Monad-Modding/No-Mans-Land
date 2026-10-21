package com.farcr.nomansland.common.dreams.dreamtypes;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.renderer.dreams.MoonlightDreamRenderer;
import com.farcr.nomansland.common.dreams.DreamType;
import com.farcr.nomansland.common.dreams.dreamlevel.DreamServerLevel;
import com.farcr.nomansland.common.friend.FriendMoon;
import com.farcr.nomansland.common.friend.FriendMoonUpdate;
import com.farcr.nomansland.common.networking.friend.FriendMoonUpdatePacket;
import com.farcr.nomansland.common.registry.NMLAttachmentTypes;
import com.farcr.nomansland.common.registry.NMLCriteriaTriggers;
import com.mojang.math.Axis;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.attachment.AttachmentType;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

public class MoonlightDreamType extends DreamType {
    public MoonlightDreamType() {
        this.setCondition(this::timeCondition)
            .setCanSprint(false).setHUDHidden(true)
            .setRenderer(() -> MoonlightDreamRenderer::new)
            .setChunkGenerator(this::moonlightChunkGenerator)
            .setSpawnPoint(new Vec3(0.5, MONOLITH_HEIGHT + 2, -19.5))
            .setInstanceSupplier(() -> new MoonlightDreamTypeInstance(this));
    }

    @Override
    public boolean timeCondition(ServerPlayer player, ServerLevel level) {
        return super.timeCondition(player, level);
    }

    @Override
    public void onDreamEnd(ServerPlayer player, boolean success) {
        super.onDreamEnd(player, success);
        if (success) {
            NMLCriteriaTriggers.DREAM_FRIEND_MOON.get().trigger(player);

            FriendMoon.getOrDefault(player.getServer().overworld())
                .updatePlayerFriendShadow(player);

            AttachmentType<Long> type = NMLAttachmentTypes.LAST_MOON_CARVING_INTERACTION.get();
            if(player.hasData(type) && player.getData(type) != -1L) {
                player.setData(type, -1L);
            }
        }
    }

    public static final int MAX_MOON_GAZE_TIME = 150;
    public static final Quaternionf SKY_ROTATION = Axis.XP.rotationDegrees(55f);

    public static class MoonlightDreamTypeInstance extends DreamTypeInstance {
        private boolean hasSeenMoon = false;

        public MoonlightDreamTypeInstance(DreamType dreamType) {
            super(dreamType);
        }

        public void hasSeenMoon(ServerPlayer player) {
            if (!hasSeenMoon) {
                FriendMoonUpdatePacket.toClient(player, FriendMoonUpdate.ToClient.DREAM_WAKE_UP_MOON);
                hasSeenMoon = true;
            }
        }

        private float moonGazeTime = 0f;
        public float moonPresenceTime;

        public static AABB DREAM_BOUNDING_BOX = new AABB(
            new BlockPos(0, 0, 0)
        ).inflate(36, 100, 200);

        @Override
        public void tick(Level level) {
            super.tick(level);

            int size = 4;
            AABB boundingBox = new AABB(BASIN_POSITION.subtract(new Vec3i(size, 0, size)))
                .inflate(size, level.getMaxBuildHeight(), size);

            List<? extends Player> playerList = level.players();
            if (!playerList.isEmpty()) {
                Player player = playerList.getFirst();
                if (!boundingBox.contains(player.position())) {
                    moonPresenceTime = 0f;
                    moonGazeTime = 0f;
                    // force player exit if out of bounds
                    if (level instanceof DreamServerLevel dreamLevel) {
                        if (!DREAM_BOUNDING_BOX.contains(player.position()))
                            dreamLevel.endDream(false);
                    }
                    return;
                }
                moonPresenceTime++;
                Vector3f targetPosition = player.getEyePosition().toVector3f()
                    .add(new Vector3f(0, 100, 0).rotate(MoonlightDreamType.SKY_ROTATION)).normalize();
                Vector3f facingDirection = player.getEyePosition().add(player.getViewVector(1.0f)).toVector3f().normalize();

                if (facingDirection.normalize().distance(targetPosition.normalize()) <= 0.185
                && player instanceof ServerPlayer serverPlayer) hasSeenMoon(serverPlayer);

                if (level instanceof DreamServerLevel serverLevel && hasSeenMoon) {
                    if (moonGazeTime >= MAX_MOON_GAZE_TIME + 50) serverLevel.endDream(true);
                    moonGazeTime++;
                }
            }
        }
    }

    private static final int CHUNK_SIZE = 16;
    public static final int MONOLITH_HEIGHT = 18;
    public static final BlockPos BASIN_POSITION = new BlockPos(4, 0, 116);

    @Override
    public void createStructures(
        ChunkGenerator generator,
        RegistryAccess registryAccess,
        ChunkGeneratorStructureState structureState,
        StructureManager structureManager, ChunkAccess chunk,
        StructureTemplateManager structureTemplateManager
    ) {
        ChunkPos chunkPos = chunk.getPos();
        if (new ChunkPos(BASIN_POSITION).equals(chunkPos)) {
            Structure structure = registryAccess.registryOrThrow(Registries.STRUCTURE).getOrThrow(
                ResourceKey.create(Registries.STRUCTURE, NoMansLand.location("dream_meeting_point"))
            );
            StructureStart structurestart = structure.generate(
                registryAccess, generator, generator.getBiomeSource(),
                structureState.randomState(), structureTemplateManager,
                structureState.getLevelSeed(), chunk.getPos(), 0, chunk, ((uh) -> true)
            );
            structureManager.setStartForStructure(SectionPos.of(new BlockPos(0, MONOLITH_HEIGHT, 0)), structure, structurestart, chunk);
        }
    }

    public void moonlightChunkGenerator(ChunkAccess chunk, StructureManager manager, WorldGenRegion level) {
        ChunkPos chunkPos = chunk.getPos();
        int startingX = chunkPos.x * CHUNK_SIZE;
        int startingZ = chunkPos.z * CHUNK_SIZE;

        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                BlockPos blockPos = new BlockPos(startingX + x, 0, startingZ + z);
                chunk.setBlockState(blockPos.above(MONOLITH_HEIGHT + 1), Blocks.BARRIER.defaultBlockState(), false);

                int approachMax = 108;
                if (Math.abs(blockPos.getX()) <= 1 && blockPos.getZ() < approachMax) {
                    int height = (blockPos.getZ() - approachMax) + (MONOLITH_HEIGHT + 4);
                    if (height > 0) {
                        for (int i = 0; i < height; i++)
                            chunk.setBlockState(blockPos.above(i), Blocks.STONE.defaultBlockState(), false);
                        chunk.setBlockState(blockPos.above(height), Blocks.STONE_STAIRS.defaultBlockState()
                            .rotate(chunk.getLevel(), blockPos.above(height), Rotation.CLOCKWISE_180), false);
                    }
                }

                if (startingZ > 2) {
                    int checkerboardX = (Math.abs(startingX + x) + 2) / 5;
                    if (checkerboardX > 0) {
                        int checkerboardZ = (Math.abs(startingZ + z) - 3) / 5;
                        if (checkerboardX < 2 && checkerboardZ > 20 && checkerboardZ < 24)
                                continue;

                        int monolithIndex = (Math.abs(startingX + x) + 1) % 5;
                        if (monolithIndex < 3 && ((Math.abs(startingZ + z) - 3) % 5) < 1) {
                            for (int i = 1; i < MONOLITH_HEIGHT; i++)
                                chunk.setBlockState(blockPos.above(i), Blocks.STONE.defaultBlockState(), false);
                            BlockPos monolithStairPos = blockPos.above(MONOLITH_HEIGHT + 1);
                            if (monolithIndex == 1) {
                                chunk.setBlockState(blockPos.above(MONOLITH_HEIGHT), Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, 10), false);
                                chunk.setBlockState(monolithStairPos, Blocks.STONE.defaultBlockState(), false);
                            } else {
                                boolean flippedStairs = monolithIndex == 0;
                                if (startingX < 0)
                                    flippedStairs = !flippedStairs;
                                chunk.setBlockState(blockPos.above(MONOLITH_HEIGHT), Blocks.STONE.defaultBlockState(), false);
                                chunk.setBlockState(monolithStairPos,
                                    Blocks.STONE_STAIRS.defaultBlockState().rotate(chunk.getLevel(), monolithStairPos,
                                        flippedStairs ? Rotation.CLOCKWISE_90 : Rotation.COUNTERCLOCKWISE_90),
                                    false
                                );
                            }
                        }
                    }
                }
            }

        }

    }
}
