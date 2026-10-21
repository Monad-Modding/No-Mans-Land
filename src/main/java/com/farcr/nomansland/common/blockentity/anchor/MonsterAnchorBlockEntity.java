package com.farcr.nomansland.common.blockentity.anchor;

import com.farcr.nomansland.NMLConfig;
import com.farcr.nomansland.common.block.MonsterAnchorBlock;
import com.farcr.nomansland.common.registry.NMLBlockEntities;
import com.farcr.nomansland.common.registry.NMLParticleTypes;
import com.farcr.nomansland.common.registry.NMLSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;

import static com.farcr.nomansland.common.blockentity.anchor.AnchorListener.surroundBoundingBox;

public class MonsterAnchorBlockEntity extends BlockEntity implements GameEventListener.Provider<AnchorListener> {

    public final ArrayList<CompoundTag> entityQueue;
    private final AnchorListener anchorListener;
    public int timeResurrecting;
    public int timeIdle;
    public int range;

    public MonsterAnchorBlockEntity(BlockPos pos, BlockState state) {
        super(NMLBlockEntities.MONSTER_ANCHOR.get(), pos, state);
        this.anchorListener = new AnchorListener(state, new BlockPositionSource(pos));
        this.entityQueue = new ArrayList<>();
        this.timeResurrecting = 0;
        this.timeIdle = 0;
        this.range = 7;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, MonsterAnchorBlockEntity monsterAnchor) {
        ServerLevel serverLevel = (ServerLevel) level;
        ArrayList<CompoundTag> entityQueue = monsterAnchor.entityQueue;
        boolean empty = entityQueue.isEmpty();
        RandomSource random = level.random;
        int timeBetweenResurrections = NMLConfig.TICKS_BETWEEN_RESURRECTIONS.get();

        if (empty) {
            // Start counting ticks
            monsterAnchor.timeResurrecting = 0;
            monsterAnchor.timeIdle++;

            // Deactivate the spawners after 10 seconds of inactivity
            if (state.getValue(MonsterAnchorBlock.ACTIVE)) {
                level.playSound(null, pos, NMLSounds.MONSTER_ANCHOR_DEACTIVATE.get(), SoundSource.BLOCKS, 1, 0.75F);
                level.setBlockAndUpdate(pos, state.setValue(MonsterAnchorBlock.ACTIVE, false));
                level.gameEvent(GameEvent.BLOCK_DEACTIVATE, pos, GameEvent.Context.of(state));
            } else if (monsterAnchor.timeIdle % 5 == 0) {
                serverLevel.sendParticles(ParticleTypes.SMOKE, pos.getX() + 0.5 + random.nextInt(0, 3) * 0.01 - random.nextInt(0, 3) * 0.01, pos.getY() + 0.2, pos.getZ() + 0.5 + random.nextInt(0, 3) * 0.01 - random.nextInt(0, 3) * 0.01, 3, 0, 0, 0, 0);
                monsterAnchor.timeIdle = 0;
            }
            return;
        }

        // Code only continues of there is an entity in the queue

        // Start counting ticks
        monsterAnchor.timeIdle = 0;
        monsterAnchor.timeResurrecting++;

        // While it is resurrecting, these particles will always spawn with a particle count proportional to the progress of the current resurrection, thus playing in a loop
        serverLevel.sendParticles((ParticleOptions) NMLParticleTypes.MALEVOLENT_FLAME.get(),
                pos.getX() + random.nextFloat(),
                pos.getY() + random.nextFloat(),
                pos.getZ() + random.nextFloat(),
                monsterAnchor.timeResurrecting, 0, 0, 0, 0.0);

        // Loop through all the entities in the queue
        for (int i = 0; i < entityQueue.size(); i++) {
            CompoundTag deadEntity = entityQueue.get(i);
            Entity loadedEntity = EntityType.loadEntityRecursive(deadEntity, level, e -> e);
            if (loadedEntity instanceof LivingEntity resurrectedEntity) {
                Vec3 spawningPosition = resurrectedEntity.position();

                if (!resurrectedEntity.isSupportedBy(BlockPos.containing(spawningPosition).below())) {
                    for (int y = 1; y < 5; y++) {
                        if (level.getBlockState(BlockPos.containing(spawningPosition).below(y)).isSolid()) {
                            spawningPosition = spawningPosition.subtract(0, y-1, 0);
                            resurrectedEntity.moveTo(spawningPosition);
                            break;
                        }
                    }
                }

                    if (level.random.nextFloat() <= 0.1F)
                        serverLevel.sendParticles((ParticleOptions) NMLParticleTypes.MALEVOLENT_FLAME.get(),
                            spawningPosition.x + random.nextFloat() - random.nextFloat(),
                            spawningPosition.y + random.nextFloat() - random.nextFloat(),
                            spawningPosition.z + random.nextFloat() - random.nextFloat(),
                            3, 0, 0, 0, 0);

                if (monsterAnchor.timeResurrecting % timeBetweenResurrections == 0 && i != 0) {
                    double y = spawningPosition.y + 0.1;
                    surroundBoundingBox(resurrectedEntity.getBoundingBox(), 0.2)
                            .forEach(point -> serverLevel.sendParticles((ParticleOptions) NMLParticleTypes.MALEVOLENT_EMBERS.get(), point.x, y, point.z, 1, 0, 0, 0, 0));
                }


                // Select the first entity on the list
                if (i == 0) {
                    // This sound plays a bit late, so it is played before the mob is resurrected to time it perfectly
                    if (monsterAnchor.timeResurrecting == timeBetweenResurrections - 78) {
                        double y = spawningPosition.y + 0.1;
                        surroundBoundingBox(resurrectedEntity.getBoundingBox(), 0.2)
                                .forEach(point -> serverLevel.sendParticles((ParticleOptions) NMLParticleTypes.MALEVOLENT_EMBERS.get(), point.x, y, point.z, 1, 0, 0, 0, 0));
                        // Turn the block active on mob resurrection
                        if (!state.getValue(MonsterAnchorBlock.ACTIVE)) {
                            level.playSound(null, pos, NMLSounds.MONSTER_ANCHOR_ACTIVATE.get(), SoundSource.BLOCKS, 1, 1F);
                            level.setBlockAndUpdate(pos, state.setValue(MonsterAnchorBlock.ACTIVE, true));
                            level.gameEvent(GameEvent.BLOCK_ACTIVATE, pos, GameEvent.Context.of(state));
                        }
                        level.playSound(null, pos, NMLSounds.MONSTER_ANCHOR_RESURRECTION.get(), SoundSource.BLOCKS, 1, 1F);

                    }
                    if (monsterAnchor.timeResurrecting == timeBetweenResurrections) {
                        monsterAnchor.timeResurrecting = 0;

                        // Resurrect the entity
                        resurrectedEntity.setHealth(resurrectedEntity.getMaxHealth());
                        resurrectedEntity.setDeltaMovement(Vec3.ZERO);
                        resurrectedEntity.nml$setBeingResurrected();

                        level.addFreshEntity(resurrectedEntity);
                        resurrectedEntity.playSound(NMLSounds.MONSTER_ANCHOR_SPAWN.get(), 1, 1F);
                        double y = spawningPosition.y + 0.5;
                        surroundBoundingBox(resurrectedEntity.getBoundingBox(), 0.4)
                                .forEach(point -> serverLevel.sendParticles((ParticleOptions) NMLParticleTypes.MALEVOLENT_FLAME.get(), point.x, y, point.z, 1, 0, 0, 0, 0.1));
                    }
                } else resurrectedEntity.remove(Entity.RemovalReason.DISCARDED);
            }
        }

        if (monsterAnchor.timeResurrecting % timeBetweenResurrections == 0) entityQueue.removeFirst();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("TimeIdle", timeIdle);
        tag.putInt("TimeResurrecting", timeResurrecting);
        tag.putInt("Range", range);
        entityQueue.forEach(entityTag -> tag.put("Entity" + entityQueue.indexOf(entityTag), entityTag));

        super.saveAdditional(tag, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        timeIdle = tag.getInt("TimeIdle");
        timeResurrecting = tag.getInt("TimeResurrecting");
        range = tag.contains("Range") ? Math.min(tag.getInt("Range"), 16) : 7;
        int i = 0;
        Tag foundTag = tag.get("Entity0");
        while(foundTag instanceof CompoundTag entityTag) {
            entityQueue.add(entityTag);
            i++;
            foundTag = tag.get("Entity"+i);
        }

        super.loadAdditional(tag, registries);
    }

    public AnchorListener getListener() {
        return this.anchorListener;
    }
}