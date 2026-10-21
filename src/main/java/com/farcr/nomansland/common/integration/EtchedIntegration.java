package com.farcr.nomansland.common.integration;

import gg.moonflower.etched.common.blockentity.AlbumJukeboxBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class EtchedIntegration {

    public static boolean ejectPlayingDisc(Level level, BlockPos pos, BlockEntity blockEntity) {
        if (!(blockEntity instanceof AlbumJukeboxBlockEntity album)) return false;

        int size = album.getContainerSize();
        int index = album.getPlayingIndex();
        if (index < 0 || index >= size || album.getItem(index).isEmpty()) {
            index = -1;
            for (int slot = 0; slot < size; slot++) {
                if (!album.getItem(slot).isEmpty()) {
                    index = slot;
                    break;
                }
            }
        }
        if (index >= 0) {
            ItemStack disc = album.removeItemNoUpdate(index);
            album.setChanged();
            if (!disc.isEmpty())
                Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, disc);
        }

        album.stopPlaying();
        return true;
    }
}
