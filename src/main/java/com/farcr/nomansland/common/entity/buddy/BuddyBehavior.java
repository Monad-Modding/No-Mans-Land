package com.farcr.nomansland.common.entity.buddy;

import com.farcr.nomansland.common.networking.buddy.ClientboundBuddyCrouchPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

public class BuddyBehavior extends Behavior<Buddy> {
    public BuddyBehavior() {
        super(Map.of());
    }

    // Enderman Code
    private static final double STARE_PRECISION = 0.5; // 0.025 for endermen
    public boolean staringAt(LivingEntity player, Buddy buddy) {
        Vec3 vec3 = player.getViewVector(1.0F).normalize();
        Vec3 vec31 = new Vec3(buddy.getX() - player.getX(), buddy.getEyeY() - player.getEyeY(), buddy.getZ() - player.getZ());
        double d0 = vec31.length();
        vec31 = vec31.normalize();
        double d1 = vec3.dot(vec31);
        boolean lookingAt = (d1 > 1.0 - STARE_PRECISION / d0 && player.hasLineOfSight(buddy));
//        if (!lookingAt)
//            forgetStaring(player);
        return lookingAt;
    }

    public void forgetStaring(LivingEntity entity) {
        stareTime.remove(entity);
        greetTime.remove(entity);
    }

    private final Map<LivingEntity, Integer> stareTime = new HashMap<>();
    public boolean boredOfStaring(LivingEntity player) {
        stareTime.put(player, stareTime.getOrDefault(player, 0) + 1);
        return (stareTime.get(player) < 30) && (greetTime.getOrDefault(player, 0) <= 0);
    }

    private final Map<LivingEntity, Integer> greetTime = new HashMap<>();
    private int crouchTimer = 0;
    public void mimicPlayerGreeting(LivingEntity player, Buddy buddy) {
        int curGreetTime = greetTime.getOrDefault(player, 0);
        if (player.isCrouching())
            greetTime.put(player, greetTime.getOrDefault(player, 0) + 1);
        else if (curGreetTime > 0) {
            greetTime.put(player, greetTime.get(player) + 1);
            if (curGreetTime >= 3) {
                crouchTimer++;
                if (crouchTimer % 8 == 2) {
                    PacketDistributor.sendToPlayersTrackingEntity(
                        buddy, new ClientboundBuddyCrouchPacket(buddy.getId()));
                }
                if (crouchTimer > 20)
                    forgetStaring(player);
            }
        }
    }

    private static long moonGazeUntil = -1;
    private static long moonGazeNextRoll = 0;

    @Override
    protected void start(ServerLevel level, Buddy buddy, long gameTime) {
        var brain = buddy.getBrain();

        Optional<List<Player>> playerList = brain.getMemory(MemoryModuleType.NEAREST_PLAYERS);
        if (playerList.isPresent()) {
            Optional<Player> playerFocus = playerList.get().stream().filter(
                player -> staringAt(player, buddy)
            ).max(Comparator.comparingInt((player) -> greetTime.getOrDefault(player, 0)));
            if (playerFocus.isPresent()) {
                mimicPlayerGreeting(playerFocus.get(), buddy);
                buddy.getLookControl().setLookAt(playerFocus.get().getEyePosition(gameTime));
                return;
            }
        }
        crouchTimer = 0;

        if (buddy.followTarget != null && buddy.followTimer > 0) {
            buddy.followTimer--;
            if (buddy.followTarget.isAlive() && buddy.distanceTo(buddy.followTarget) < 20) {
                if (brain.getMemory(MemoryModuleType.WALK_TARGET).isEmpty() && buddy.distanceTo(buddy.followTarget) > 5) {
                    BehaviorUtils.setWalkAndLookTargetMemories(buddy, buddy.followTarget.blockPosition(),
                        0.2F, 4);
                }
                buddy.getLookControl().setLookAt(buddy.followTarget.getEyePosition(gameTime));
            } else {
                buddy.followTarget = null;
                buddy.followTimer = 0;
            }
            return;
        }

        NearestVisibleLivingEntities entities = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
            .orElse(NearestVisibleLivingEntities.empty());

        Optional<LivingEntity> staringPlayer = entities.findClosest(player -> staringAt(player, buddy) && !boredOfStaring(player));
        if (staringPlayer.isPresent()) {
            buddy.getLookControl().setLookAt(staringPlayer.get().getEyePosition(gameTime));
            return;
        }

        for (var entry : stareTime.entrySet()) {
            if (entry.getValue() >= 30 && entry.getKey().isAlive() && buddy.getRandom().nextInt(3) == 0) {
                buddy.followTarget = entry.getKey();
                buddy.followTimer = 100 + buddy.getRandom().nextInt(100);
                break;
            }
        }
        stareTime.clear();

        long dayTime = level.getDayTime() % 24000L;
        boolean isNight = dayTime >= 13000 && dayTime <= 23000;
        if (isNight && brain.getMemory(MemoryModuleType.WALK_TARGET).isEmpty()) {
            if (gameTime > moonGazeUntil && gameTime >= moonGazeNextRoll) {
                moonGazeNextRoll = gameTime + 250;
                if (buddy.getRandom().nextInt(250) == 0)
                    moonGazeUntil = gameTime + 60 + buddy.getRandom().nextInt(60);
            }
            if (gameTime <= moonGazeUntil) {
                float timeOfDay = dayTime / 24000.0f;
                float moonAngle = (float) (timeOfDay * Math.PI * 2 + Math.PI / 2.0);
                buddy.getLookControl().setLookAt(
                    buddy.getX() + Math.cos(moonAngle) * 100,
                    buddy.getY() + Math.sin(moonAngle) * 100,
                    buddy.getZ()
                );
                return;
            }
        }
    }
}
