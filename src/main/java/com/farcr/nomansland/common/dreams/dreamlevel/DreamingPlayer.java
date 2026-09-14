package com.farcr.nomansland.common.dreams.dreamlevel;

import com.farcr.nomansland.common.registry.NMLParticleTypes;
import com.farcr.nomansland.common.registry.entities.NMLEntityDataSerializers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class DreamingPlayer extends Mob {
    // tethered player for client rendering / tricking clients into thinking player is x
    public DreamingPlayer(EntityType<? extends DreamingPlayer> entityType, Level level) {
        super(entityType, level);
    }

    public void setTetheredPlayer(ServerPlayer serverPlayer) {
        entityData.set(DREAM_PLAYER_SNAPSHOT, new DreamPlayerSnapshot(
            serverPlayer.getUUID(), serverPlayer.saveWithoutId(new CompoundTag()),
            serverPlayer.getEntityData().get(Player.DATA_PLAYER_MODE_CUSTOMISATION)
        ));
        this.setXRot(serverPlayer.getXRot());
        this.setYRot(serverPlayer.getYRot());
        this.setYBodyRot(serverPlayer.yBodyRot);
        this.setYHeadRot(serverPlayer.yHeadRot);
        this.setPos(serverPlayer.position());
        serverPlayer.getSleepingPos().ifPresentOrElse(this::startSleeping,
            () -> this.startSleeping(serverPlayer.blockPosition()));
    }

    public DreamPlayerSnapshot getClientSnapshot() {
        UUID uuid = entityData.get(DREAM_PLAYER_SNAPSHOT).uuid();
        if (uuid == this.getUUID()) return null;
        return entityData.get(DREAM_PLAYER_SNAPSHOT);
    }

    public ServerPlayer getTetheredPlayer() {
        UUID uuid = entityData.get(DREAM_PLAYER_SNAPSHOT).uuid();
        if (uuid == this.getUUID()) return null;
        if (this.getServer() != null) return this.getServer().getPlayerList().getPlayer(uuid);
        return null;
    }

    private static final EntityDataAccessor<DreamPlayerSnapshot> DREAM_PLAYER_SNAPSHOT =
        SynchedEntityData.defineId(DreamingPlayer.class, NMLEntityDataSerializers.DREAM_PLAYER_SNAPSHOT.get());

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DREAM_PLAYER_SNAPSHOT,
            new DreamPlayerSnapshot(this.getUUID(), new CompoundTag(), (byte) 0)
        );
        super.defineSynchedData(builder);
    }

    @Override
    protected EntityDimensions getDefaultDimensions(Pose pose) {
        return SLEEPING_DIMENSIONS;
    }

    public Optional<Player> discardTether() {
        return discardTether(true);
    }

    public Optional<Player> discardTether(boolean returnPlayerToBody) {
        if (this.level() instanceof ServerLevel level) {
            if (getTetheredPlayer() == null)
                return Optional.empty();
            Player player = getTetheredPlayer();
            this.remove(RemovalReason.DISCARDED);
            if (returnPlayerToBody)
                player.teleportTo(level, this.getX(), this.getY(), this.getZ(), Set.of(), player.getXRot(), player.getYRot());
            this.getSleepingPos().ifPresent(player::setSleepingPos);
            player.stopSleeping();
            // Recalculate player since old player doesn't exist anymore
            return Optional.ofNullable(getTetheredPlayer());
        }
        return Optional.empty();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (discardTether().isPresent())
            return discardTether().get().hurt(source, amount);
        return super.hurt(source, amount);
    }

    @Override
    public UUID getUUID() {
        return super.getUUID();
    }

    public Player clientOnlyRemotePlayer;
    public boolean debug = false;

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);

        entityData.set(DREAM_PLAYER_SNAPSHOT, new DreamPlayerSnapshot(
            compound.getUUID("PlayerTether"),
            compound.getCompound("PlayerData"),
            compound.getByte("PlayerCustomization")
        ));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);

        DreamPlayerSnapshot snapshot = getClientSnapshot();
        compound.putUUID("PlayerTether", snapshot.uuid());
        compound.put("PlayerData", snapshot.compoundTag());
        compound.putByte("PlayerCustomization", snapshot.playerModelCustomization());
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType reason) {
        return reason != MobSpawnType.COMMAND;
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        return InteractionResult.FAIL;
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        if (level() instanceof ServerLevel level) {
            this.getSleepingPos().ifPresent(
                (sleepingPos) -> {
                    if (level.getBlockState(sleepingPos).getOptionalValue(BlockStateProperties.OCCUPIED).isPresent())
                        level.getBlockState(sleepingPos).setBedOccupied(level, sleepingPos, this, false);
                }
            );
        }
        super.remove(reason);
    }

    public void addChunkTicket(ServerLevel serverLevel) {
        serverLevel.getChunkSource().addRegionTicket(TicketType.PORTAL, new ChunkPos(this.blockPosition()), 3, this.blockPosition());
    }

    private void endDreamForTether() {
        ServerPlayer tetheredPlayer = getTetheredPlayer();
        if (tetheredPlayer == null) {
            discardTether();
            return;
        }
        if (tetheredPlayer.serverLevel() instanceof DreamServerLevel dreamLevel)
            dreamLevel.endDream(false);
        else {
            debug = false;
            discardTether();
        }
    }

    float tickCooldown = 0;
    float maxParticleTick = 15;
    @Override
    public void aiStep() {
        Level level = this.level();
        if (level.isClientSide) {
            tickCooldown++;
            if (tickCooldown < maxParticleTick)
                return;
            tickCooldown = 0;
            level.addParticle(
                NMLParticleTypes.DEEP_SLEEP.get(),
                getX(), getY(), getZ(),
                0, 0.01, 0
            );
        }
    }

    @Override
    public void tick() {
        if (!level().isClientSide && getTetheredPlayer() == null)
            this.remove(RemovalReason.DISCARDED);
        else if (getTetheredPlayer() != null) {
            if (getTetheredPlayer().level().dimension().equals(this.level().dimension()))
                this.remove(RemovalReason.DISCARDED); // originally i would discard tether but in odd cases I feel the player might be teleported unintentionally
//                discardTether();
        }

        if (level() instanceof ServerLevel serverLevel) {
            addChunkTicket(serverLevel);
            if (!debug && getTetheredPlayer() != null) {
                Optional<BlockPos> sleepingPos = this.getSleepingPos();
                if (sleepingPos.isPresent() && !serverLevel.getBlockState(sleepingPos.get())
                    .isBed(serverLevel, sleepingPos.get(), this))
                    endDreamForTether();
            }
        }

        this.setDeltaMovement(Vec3.ZERO);
        if (!isSleeping() && !debug) discardTether();

        // hackily obtained from the renderer only on the client as to not . crash the server
        // can you really call yourself a programmer if you havent said "i hate programming" once in your life
        if (clientOnlyRemotePlayer != null) {
            clientOnlyRemotePlayer.setDeltaMovement(this.getDeltaMovement());
            clientOnlyRemotePlayer.walkAnimation.setSpeed(0f);
            clientOnlyRemotePlayer.setXRot(getXRot());
            clientOnlyRemotePlayer.setYRot(getYRot());
            clientOnlyRemotePlayer.setYBodyRot(yBodyRot);
            clientOnlyRemotePlayer.setYHeadRot(yHeadRot);
            clientOnlyRemotePlayer.tick();
        }
        super.tick();
    }
}
