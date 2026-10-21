package com.farcr.nomansland.common.blockentity.anchor;

import com.farcr.nomansland.common.registry.NMLCriteriaTriggers;
import com.farcr.nomansland.common.registry.NMLParticleTypes;
import com.farcr.nomansland.common.registry.NMLTags;
import dev.ryanhcode.sable.companion.SableCompanion;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AnchorListener implements GameEventListener {

    private final BlockState state;
    private final PositionSource positionSource;

    public AnchorListener(BlockState state, PositionSource positionSource) {
        this.state = state;
        this.positionSource = positionSource;
    }

    public static List<Vec3> surroundBoundingBox(AABB boundingBox, double step) {
        double minX = boundingBox.minX;
        double minY = boundingBox.minY;
        double minZ = boundingBox.minZ;
        double maxX = boundingBox.maxX;
        double maxY = boundingBox.maxY;
        double maxZ = boundingBox.maxZ;

        List<Vec3> pointList = new ArrayList<>();

        for (double x = minX; x <= maxX; x += step) {
            for (double y = minY; y <= maxY; y += step) {
                for (double z = minZ; z <= maxZ; z += step) {
                    pointList.add(new Vec3(x, y, z));
                }
            }
        }

        return pointList;
    }

    public PositionSource getListenerSource() {
        return positionSource;
    }

    public int getListenerRadius() {
        return 16;
    }

    @Override
    public boolean handleGameEvent(ServerLevel level, Holder<GameEvent> gameEvent, GameEvent.Context context, Vec3 pos) {
        if (context.sourceEntity() instanceof LivingEntity livingEntity && context.sourceEntity() instanceof Enemy && !(livingEntity.getType().getTags().toList().contains(NMLTags.ANCHOR_BLACKLIST)) && GameEvent.ENTITY_DIE.is(gameEvent)) {
            Vec3 sourcePos = positionSource.getPosition(level).orElseThrow();
            MonsterAnchorBlockEntity monsterAnchorBlockEntity = (MonsterAnchorBlockEntity) Optional.ofNullable(level.getBlockEntity(BlockPos.containing(sourcePos))).orElseThrow();

            if (SableCompanion.INSTANCE.distanceSquaredWithSubLevels(level, pos, sourcePos) > Mth.square(monsterAnchorBlockEntity.range)) return false;

            if (livingEntity instanceof Slime slime && slime.getSize() > 1) return false;

            if (!livingEntity.wasExperienceConsumed()) {

                // Add the entity to the dead entity list
                CompoundTag tag = new CompoundTag();
                if (livingEntity.save(tag)) monsterAnchorBlockEntity.entityQueue.add(tag);

                // Stop the mob from dropping experience and loot
                livingEntity.skipDropExperience();
                livingEntity.nml$skipDroppingDeathLoot();

                // Surround the bounding box of the monster with embers
                AABB boundingBox = livingEntity.getBoundingBox();
                surroundBoundingBox(boundingBox, 0.2).forEach(point ->
                        level.sendParticles(NMLParticleTypes.MALEVOLENT_EMBERS.get(), point.x, point.y, point.z, 1, 0, 0, 0, 0));

                tryAwardAdvancement(level, livingEntity);
            }
            return true;
        }

        return false;
    }

    public DeliveryMode getDeliveryMode() {
        return DeliveryMode.BY_DISTANCE;
    }

    private static void tryAwardAdvancement(Level level, LivingEntity livingEntity) {
        if (livingEntity.getLastHurtByMob() instanceof ServerPlayer serverplayer) {
            DamageSource damagesource = livingEntity.getLastDamageSource() == null
                    ? level.damageSources().playerAttack(serverplayer) : livingEntity.getLastDamageSource();
            NMLCriteriaTriggers.KILL_MOB_NEAR_MONSTER_ANCHOR.get().trigger(serverplayer, livingEntity, damagesource);
        }
    }
}
