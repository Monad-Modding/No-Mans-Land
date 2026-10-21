package com.farcr.nomansland.common.entity.centipede;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.entity.PartEntity;
import org.jetbrains.annotations.Nullable;

public class CentipedePart extends PartEntity<Centipede> {
    public final Centipede parent;
    private final EntityDimensions partDimensions;

    public CentipedePart(Centipede parent, float width, float height) {
        super(parent);
        this.partDimensions = EntityDimensions.scalable(width, height);
        this.refreshDimensions();
        this.parent = parent;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Nullable
    @Override
    public ItemStack getPickResult() {
        return this.parent.getPickResult();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return !this.isInvulnerableTo(source) && this.parent.hurtPart(this, source, amount);
    }

    @Override
    public boolean is(Entity entity) {
        return this == entity || this.parent == entity;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity serverEntity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return this.partDimensions;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }
}
