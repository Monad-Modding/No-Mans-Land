package com.farcr.nomansland.common.handler;

import com.farcr.nomansland.common.blockentity.InvertedBellControllerBlockEntity;
import com.farcr.nomansland.common.extension.LivingEntityExtension;
import com.farcr.nomansland.common.networking.ClientboundInvertedBellPacket;
import com.farcr.nomansland.common.registry.NMLSounds;
import com.farcr.nomansland.common.registry.NMLTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.level.BlockCollisions;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipBlockStateContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Runs serverside logic of inverted bell teleports, gathering entities, sending packets to relevant clients, chunkloading the destination, and teleporting targets
 */
public class InvertedBellServerHandler extends SavedData {
    public static final int TELEPORT_ENTITY_TIME = 60;
    public static final int FAILURE_NAUSEA_DURATION = 200;
    public static final double RANGE_SQUARED = 16*16;

    private final List<ActiveTeleport> teleports = new ArrayList<>();

    public void beginTeleport(ServerLevel level, BlockPos fromPos, Direction fromDir, @Nullable BlockPos toPos, @Nullable Direction toDir, @Nullable ResourceKey<Level> toDimension) {
        this.teleports.add(new ActiveTeleport(level, fromPos, fromDir, toPos, toDir, toDimension));
    }

    public static boolean canTeleport(Entity entity, Vec3 from) {
        return entity instanceof LivingEntity &&
                !entity.getType().is(NMLTags.INVERTED_BELL_UNAFFECTED) &&
                entity.distanceToSqr(from) < InvertedBellServerHandler.RANGE_SQUARED &&
                entity.level().isBlockInLine(
                        new ClipBlockStateContext(entity.getEyePosition(), from, state -> state.is(BlockTags.OCCLUDES_VIBRATION_SIGNALS))
                ).getType() != HitResult.Type.BLOCK;
    }

    public void tick(ServerLevel level) {
        Iterator<ActiveTeleport> it = this.teleports.iterator();
        while (it.hasNext()) {
            ActiveTeleport entry = it.next();
            if (entry.tick(level)) {
                it.remove();
            }
            this.setDirty();
        }
    }

    public static InvertedBellServerHandler get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(new Factory<>(
                InvertedBellServerHandler::new, InvertedBellServerHandler::load
        ), "inverted_bell");
    }

    public static InvertedBellServerHandler load(CompoundTag compoundTag, HolderLookup.Provider registries) {
        return new InvertedBellServerHandler();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        return new CompoundTag();
    }

    public static class ActiveTeleport {
        private int timer = 0;
        private final List<Entity> teleportingEntities;
        private final List<ServerPlayer> teleportingPlayers;
        private final BlockPos fromPos;
        private final Direction fromDir;
        private final @Nullable BlockPos toPos;
        private final @Nullable Direction toDir;
        private final ServerLevel toLevel;

        private ActiveTeleport(ServerLevel level, BlockPos fromPos, Direction fromDir, @Nullable BlockPos toPos, @Nullable Direction toDir, @Nullable ResourceKey<Level> toDimension) {
            ServerLevel toLevel = (toDimension == null || toDimension == level.dimension()) ? level : level.getServer().getLevel(toDimension);
            if (toPos != null && (toLevel == null || !toLevel.getWorldBorder().isWithinBounds(toPos))) {
                toPos = null;
                toDir = null;
            }
            this.toLevel = toLevel != null ? toLevel : level;
            this.teleportingEntities = level.getEntities(null, new AABB(fromPos).inflate(16)).stream()
                    .filter(e -> InvertedBellServerHandler.canTeleport(e, fromPos.getCenter())).collect(Collectors.toList());
            this.teleportingPlayers = new ArrayList<>();
            Iterator<Entity> it = this.teleportingEntities.iterator();
            while (it.hasNext()) {
                if (it.next() instanceof ServerPlayer serverPlayer) {
                    this.teleportingPlayers.add(serverPlayer);
                    it.remove();
                }
            }

            this.fromPos = fromPos;
            this.fromDir = fromDir;
            this.toPos = toPos;
            this.toDir = toDir;

            if (toPos != null) {
                ChunkPos fromChunk = new ChunkPos(toPos);
                this.toLevel.getChunkSource().addRegionTicket(InvertedBellControllerBlockEntity.BELL_TICKET, fromChunk, 0, fromChunk);
                ChunkPos toChunk = new ChunkPos(toPos);
                this.toLevel.getChunkSource().addRegionTicket(InvertedBellControllerBlockEntity.BELL_TICKET, toChunk, 0, toChunk);
            }

            this.teleportingEntities.forEach(e -> {
                if (e instanceof LivingEntityExtension extension) {
                    extension.nml$beginBellParalysis();
                }
            });
            this.teleportingPlayers.forEach(e -> {
                PacketDistributor.sendToPlayer(e, ClientboundInvertedBellPacket.FADE_IN);
                e.connection.send(new ClientboundSoundPacket(
                        NMLSounds.INVERTED_BELL_RING, SoundSource.BLOCKS,
                        fromPos.getX() + 0.5, fromPos.getY() + 0.5, fromPos.getZ() + 0.5,
                        0.8f, 1.0f, level.getRandom().nextLong()
                ));
            });
        }

        public boolean tick(ServerLevel level) {
            this.timer++;
            if (this.timer == TELEPORT_ENTITY_TIME) {
                this.teleportEntities(level);
                this.teleportPlayers(level);
            }
            return this.timer > TELEPORT_ENTITY_TIME;
        }

        public void teleportEntities(ServerLevel level) {
            for (Entity entity : this.teleportingEntities) {
                if (entity.distanceToSqr(this.fromPos.getCenter()) < InvertedBellServerHandler.RANGE_SQUARED) {
                    if (entity.getType().is(NMLTags.INVERTED_BELL_REPULSED)) {
                        if (entity instanceof LivingEntityExtension extension) {
                            if (entity instanceof Mob mob) mob.skipDropExperience();
                            extension.nml$skipDroppingDeathLoot();
                        }
                        entity.kill();
                    } else {
                        this.doTeleportEntity(entity, level);
                        if (this.toPos != null &&
                                level.getBlockEntity(this.fromPos) instanceof InvertedBellControllerBlockEntity fromIbbe &&
                                this.toLevel.getBlockEntity(this.toPos) instanceof InvertedBellControllerBlockEntity toIbbe) {
                            toIbbe.ringCooldown = fromIbbe.ringCooldown;
                        }
                    }
                }
            }
        }

        public void teleportPlayers(ServerLevel level) {
            for (ServerPlayer serverPlayer : this.teleportingPlayers) {
                if (!this.doTeleportEntity(serverPlayer, level)) {
                    PacketDistributor.sendToPlayer(serverPlayer, ClientboundInvertedBellPacket.FADE_OUT_PAINFUL);
                } else {
                    PacketDistributor.sendToPlayer(serverPlayer, ClientboundInvertedBellPacket.FADE_OUT);
                }
            }
        }

        /**
         * Teleports an entity based on the relative positions and directions</br>
         * If target is obstructed, deals damage and applies nausea instead
         * @return false if teleport target was obstructed
         */
        private boolean doTeleportEntity(Entity entity, ServerLevel level) {
            Vec3 diff = entity.position().subtract(this.fromPos.getCenter());
            if (diff.lengthSqr() > RANGE_SQUARED) {
                return true;
            }

            if (this.toPos == null || this.toDir == null) {
                applyFailedTeleport(entity, level);
                return false;
            }

            float dYRot = this.fromDir.toYRot() - this.toDir.toYRot();
            diff = diff.yRot((float)(dYRot / 180 * Math.PI));
            Vec3 newPos = this.toPos.getCenter().add(diff);

            if (entityAtPositionIsColliding(entity, newPos, this.toLevel)) {
                applyFailedTeleport(entity, level);
                return false;
            } else {
                if (!entity.isPassenger()) {
                    entity.teleportTo(this.toLevel, newPos.x, newPos.y, newPos.z,
                            EnumSet.noneOf(RelativeMovement.class),
                            entity.getYRot() - dYRot, entity.getXRot());
                }
                return true;
            }
        }

        private static void applyFailedTeleport(Entity entity, ServerLevel level) {
            if (entity instanceof LivingEntity livingEntity) {
                float damage = Math.max(0.0F, livingEntity.getHealth() - 1.0F);
                if (damage > 0.0F) {
                    livingEntity.hurt(level.damageSources().cramming(), damage);
                }
                if (livingEntity.getHealth() > 1.0F) {
                    livingEntity.setHealth(1.0F);
                }
                livingEntity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, FAILURE_NAUSEA_DURATION));
            } else {
                entity.hurt(level.damageSources().cramming(), 10);
            }
        }

        public static boolean entityAtPositionIsColliding(Entity entity, Vec3 pos, ServerLevel level) {
            AABB bb = entity.getBoundingBox();
            bb = bb.move(pos.subtract(bb.getBottomCenter())).deflate(1E-2);
            BlockCollisions<VoxelShape> collisions = new BlockCollisions<>(level, entity, bb, true, (mutPos, shape) -> shape);
            return collisions.hasNext();
        }
    }
}
