package com.farcr.nomansland.common.blockentity;

import com.farcr.nomansland.common.block.PhysicsBarrelBlock;
import com.farcr.nomansland.common.integration.Mods;
import com.farcr.nomansland.common.integration.sable.BarrelPhysics;
import com.farcr.nomansland.common.registry.NMLBlockEntities;
import dev.ryanhcode.sable.companion.SableCompanion;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Position;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class PhysicsBarrelBlockEntity extends BlockEntity {
    private static final double FOOT_OFFSET = 0.4;

    private int fuse;
    private long fuseEnd;
    private boolean physical;

    public PhysicsBarrelBlockEntity(BlockPos pos, BlockState state) {
        super(NMLBlockEntities.PHYSICS_BARREL.get(), pos, state);
    }

    public int getFuse() {
        return this.fuse;
    }

    public void setFuse(int fuse) {
        this.fuse = fuse;
        if (this.level != null) {
            this.fuseEnd = this.level.getGameTime() + fuse;
            if (!this.level.isClientSide()) {
                this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
            }
        }
        this.setChanged();
    }

    public long getFuseEnd() {
        return this.fuseEnd;
    }

    public boolean isPhysical() {
        return this.physical;
    }

    public void setPhysical(boolean physical) {
        this.physical = physical;
        this.setChanged();
    }

    public static Vec3 worldCentre(Level level, BlockPos pos) {
        return SableCompanion.INSTANCE.projectOutOfSubLevel(level, (Position) Vec3.atCenterOf(pos));
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PhysicsBarrelBlockEntity barrel) {
        if (!(level instanceof ServerLevel server)) return;
        if (barrel.physical && Mods.SABLE.isLoaded()) {
            BarrelPhysics.pushByBodies(server, pos);
        }
        if (!state.getValue(PhysicsBarrelBlock.LIT)) return;
        if (--barrel.fuse <= 0 || inFireOrLava(server, worldCentre(server, pos))) {
            PhysicsBarrelBlock.detonate(server, pos);
        }
    }

    private static boolean inFireOrLava(ServerLevel level, Vec3 centre) {
        BlockPos middle = BlockPos.containing(centre);
        BlockPos foot = BlockPos.containing(centre.x, centre.y - FOOT_OFFSET, centre.z);
        return burns(level, middle) || burns(level, foot);
    }

    private static boolean burns(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.getBlock() instanceof BaseFireBlock || state.getFluidState().is(FluidTags.LAVA);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.fuse = tag.getInt("fuse");
        this.fuseEnd = tag.getLong("fuse_end");
        this.physical = tag.getBoolean("physical");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("fuse", this.fuse);
        tag.putLong("fuse_end", this.fuseEnd);
        tag.putBoolean("physical", this.physical);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveCustomOnly(registries);
    }
}
