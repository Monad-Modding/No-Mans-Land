package com.farcr.nomansland.common.integration.sable;

import com.farcr.nomansland.common.block.PhysicsBarrelBlock;
import com.farcr.nomansland.common.block.PhysicsBarrelCarriers;
import com.farcr.nomansland.common.blockentity.PhysicsBarrelBlockEntity;
import com.farcr.nomansland.common.networking.barrel.ClientboundBarrelAssembledPacket;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.SubLevelAssemblyHelper;
import dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.BoundingBox3d;
import dev.ryanhcode.sable.companion.math.BoundingBox3i;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.HangingEntityItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.util.ArrayList;
import java.util.List;

public final class BarrelPhysics {
    private static final double STEER_GAIN = 8.0;
    private static final double STEER_MAX_SPEED = 10.0;
    private static final double STEER_SPIN_DAMPING = 0.3;
    private static final double ORIENT_GAIN = 1.0;
    private static final double BODY_HALF_WIDTH = 0.5;
    private static final double PUSH_GAIN = 20.0;
    private static final double PUSH_MIN = 0.01;
    private static final double PUSH_FOOT_CLEARANCE = 0.4;
    private static final double PUNCH_SPEED = 6.0;
    private static final double PUNCH_LIFT = 2.0;
    private static final double PUNCH_SPIN = 3.0;
    private static final double PUNCH_REACH = 6.0;

    private BarrelPhysics() {
    }

    @Nullable
    public static BlockPos toPhysics(ServerLevel level, BlockPos pos) {
        if (isOwnSubLevel(level, pos)) return pos;
        if (!(level.getBlockEntity(pos) instanceof PhysicsBarrelBlockEntity barrel)) return null;
        barrel.setPhysical(true);
        ServerSubLevel subLevel = SubLevelAssemblyHelper.assembleBlocks(level, pos, List.of(pos), new BoundingBox3i(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1));
        if (subLevel == null || subLevel.isRemoved()) {
            barrel.setPhysical(false);
            return null;
        }
        BlockPos plot = subLevel.getPlot().getCenterBlock();
        long fuseEnd = level.getBlockEntity(plot) instanceof PhysicsBarrelBlockEntity moved ? moved.getFuseEnd() : 0L;
        PacketDistributor.sendToPlayersTrackingChunk(level, new ChunkPos(pos), new ClientboundBarrelAssembledPacket(pos.immutable(), plot, level.getBlockState(plot), fuseEnd));
        return plot;
    }

    public static boolean isOwnSubLevel(Level level, BlockPos pos) {
        SubLevel subLevel = Sable.HELPER.getContaining(level, (Vec3i) pos);
        return subLevel != null && !subLevel.isRemoved() && subLevel.getPlot() != null && subLevel.getPlot().getCenterBlock().equals(pos) && level.getBlockEntity(pos) instanceof PhysicsBarrelBlockEntity barrel && barrel.isPhysical();
    }

    public static boolean isBarrelSubLevel(Level level, BlockPos pos) {
        SubLevel subLevel = Sable.HELPER.getContaining(level, (Vec3i) pos);
        return subLevel != null && subLevel.getPlot() != null && isOwnSubLevel(level, subLevel.getPlot().getCenterBlock());
    }

    public static void discard(ServerLevel level, BlockPos pos) {
        SubLevel subLevel = Sable.HELPER.getContaining(level, (Vec3i) pos);
        if (subLevel == null || subLevel.isRemoved()) return;
        SubLevelContainer.getContainer(level).removeSubLevel(subLevel, SubLevelRemovalReason.REMOVED);
    }

    public static List<BlockPos> barrelsNear(ServerLevel level, Vec3 centre, double radius) {
        List<BlockPos> barrels = new ArrayList<>();
        BoundingBox3d area = new BoundingBox3d(centre.x - radius, centre.y - radius, centre.z - radius, centre.x + radius, centre.y + radius, centre.z + radius);
        int reach = (int) Math.ceil(radius);
        for (SubLevel subLevel : SubLevelContainer.getContainer(level).queryIntersecting(area)) {
            if (subLevel.isRemoved() || subLevel.getPlot() == null) continue;
            Vec3 local = subLevel.logicalPose().transformPositionInverse(centre);
            BlockPos origin = BlockPos.containing(local);
            for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-reach, -reach, -reach), origin.offset(reach, reach, reach))) {
                if (Vec3.atCenterOf(pos).distanceToSqr(local) <= radius * radius && level.getBlockState(pos).getBlock() instanceof PhysicsBarrelBlock) {
                    barrels.add(pos.immutable());
                }
            }
        }
        return barrels;
    }

    @Nullable
    private static RigidBodyHandle handleOf(ServerLevel level, BlockPos pos) {
        if (!(Sable.HELPER.getContaining(level, (Vec3i) pos) instanceof ServerSubLevel subLevel) || subLevel.isRemoved()) return null;
        RigidBodyHandle handle = RigidBodyHandle.of(subLevel);
        return handle != null && handle.isValid() ? handle : null;
    }

    public static void steer(ServerLevel level, BlockPos pos, Player holder) {
        RigidBodyHandle handle = handleOf(level, pos);
        if (handle == null || !(Sable.HELPER.getContaining(level, (Vec3i) pos) instanceof ServerSubLevel subLevel)) return;
        Vec3 centre = PhysicsBarrelBlockEntity.worldCentre(level, pos);
        Vec3 goal = holder.getEyePosition().add(holder.getLookAngle().scale(PhysicsBarrelCarriers.HOLD_DISTANCE));
        Vec3 desired = goal.subtract(centre).scale(STEER_GAIN);
        if (desired.length() > STEER_MAX_SPEED) {
            desired = desired.normalize().scale(STEER_MAX_SPEED);
        }
        Vector3dc velocity = handle.getLinearVelocity();
        Vector3dc spin = handle.getAngularVelocity();
        Vector3d orient = orientSpin(subLevel, centre, holder);
        Vector3d spinChange = orient.sub(spin).mul(STEER_SPIN_DAMPING);
        handle.addLinearAndAngularVelocity(new Vector3d(desired.x - velocity.x(), desired.y - velocity.y(), desired.z - velocity.z()), spinChange);
    }

    private static Vector3d orientSpin(ServerSubLevel subLevel, Vec3 centre, Player holder) {
        Quaterniondc current = subLevel.logicalPose().orientation();
        double towardX = holder.getX() - centre.x;
        double towardZ = holder.getZ() - centre.z;
        Quaterniond target;
        if (towardX * towardX + towardZ * towardZ < 1.0E-4) {
            Vector3d front = current.transform(new Vector3d(0.0, 0.0, -1.0));
            target = new Quaterniond().rotationY(Math.atan2(-front.x, -front.z));
        } else {
            target = new Quaterniond().rotationY(Math.atan2(-towardX, -towardZ));
        }
        Quaterniond error = target.mul(current.conjugate(new Quaterniond()));
        if (error.w < 0.0) {
            error.set(-error.x, -error.y, -error.z, -error.w);
        }
        double halfSine = Math.sqrt(error.x * error.x + error.y * error.y + error.z * error.z);
        if (halfSine < 1.0E-6) return new Vector3d();
        double angle = 2.0 * Math.atan2(halfSine, error.w);
        return new Vector3d(error.x, error.y, error.z).div(halfSine).mul(angle * ORIENT_GAIN);
    }

    public static void pushByBodies(ServerLevel level, BlockPos pos) {
        RigidBodyHandle handle = null;
        Vec3 centre = PhysicsBarrelBlockEntity.worldCentre(level, pos);
        AABB body = new AABB(centre, centre).inflate(BODY_HALF_WIDTH);
        Player holder = PhysicsBarrelCarriers.holderOf(level, pos);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, body)) {
            if (entity.isSpectator() || entity == holder || entity.getY() > centre.y + PUSH_FOOT_CLEARANCE) continue;
            Vec3 toward = new Vec3(centre.x - entity.getX(), 0.0, centre.z - entity.getZ());
            if (toward.lengthSqr() < 1.0E-6) continue;
            toward = toward.normalize();
            double along = (entity.getX() - entity.xo) * toward.x + (entity.getZ() - entity.zo) * toward.z;
            if (along < PUSH_MIN) continue;
            if (handle == null) {
                handle = handleOf(level, pos);
                if (handle == null) return;
            }
            Vec3 push = toward.scale(along * PUSH_GAIN);
            handle.addLinearAndAngularVelocity(new Vector3d(push.x, 0.0, push.z), new Vector3d());
        }
    }

    public static void punch(ServerPlayer player, BlockPos pos) {
        ServerLevel level = player.serverLevel();
        if (!isBarrelSubLevel(level, pos) || player.getEyePosition().distanceToSqr(PhysicsBarrelBlockEntity.worldCentre(level, pos)) > PUNCH_REACH * PUNCH_REACH) return;
        RigidBodyHandle handle = handleOf(level, pos);
        if (handle == null) return;
        float strength = player.getAttackStrengthScale(0.5F);
        player.resetAttackStrengthTicker();
        Vec3 push = player.getLookAngle().scale(PUNCH_SPEED * strength).add(0.0, PUNCH_LIFT * strength, 0.0);
        Vector3d spin = new Vector3d(level.getRandom().nextGaussian(), level.getRandom().nextGaussian(), level.getRandom().nextGaussian()).mul(PUNCH_SPIN * strength);
        handle.addLinearAndAngularVelocity(new Vector3d(push.x, push.y, push.z), spin);
    }

    @SubscribeEvent
    public static void onUseOn(PlayerInteractEvent.RightClickBlock event) {
        Item held = event.getItemStack().getItem();
        if (!(held instanceof BlockItem) && !(held instanceof HangingEntityItem) && !(held instanceof BucketItem)) return;
        if (!isBarrelSubLevel(event.getLevel(), event.getPos())) return;
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.FAIL);
    }

    @SubscribeEvent
    public static void onPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel() instanceof Level level && !(event.getPlacedBlock().getBlock() instanceof PhysicsBarrelBlock) && isBarrelSubLevel(level, event.getPos())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onHang(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof HangingEntity hanging && !event.loadedFromDisk() && isBarrelSubLevel(event.getLevel(), hanging.blockPosition())) {
            event.setCanceled(true);
        }
    }
}
