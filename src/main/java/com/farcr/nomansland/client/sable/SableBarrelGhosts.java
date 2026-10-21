package com.farcr.nomansland.client.sable;

import com.farcr.nomansland.common.block.PhysicsBarrelBlock;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4d;
import org.joml.Matrix4f;

public final class SableBarrelGhosts {

    private SableBarrelGhosts() {
    }

    public static boolean isShown(ClientLevel level, BlockPos plot) {
        if (!(Sable.HELPER.getContaining(level, (Vec3i) plot) instanceof ClientSubLevel subLevel) || subLevel.isRemoved() || !subLevel.isFinalized()) return false;
        return level.getBlockState(plot).getBlock() instanceof PhysicsBarrelBlock;
    }

    @Nullable
    public static Matrix4f transform(ClientLevel level, BlockPos plot, Vec3 camera, float partialTick) {
        if (!(Sable.HELPER.getContaining(level, (Vec3i) plot) instanceof ClientSubLevel subLevel) || subLevel.isRemoved()) return null;
        Matrix4d plotToWorld = subLevel.renderPose(partialTick).bakeIntoMatrix(new Matrix4d());
        Matrix4d matrix = new Matrix4d().translation(-camera.x, -camera.y, -camera.z).mul(plotToWorld).translate(plot.getX(), plot.getY(), plot.getZ());
        return new Matrix4f(matrix);
    }
}
