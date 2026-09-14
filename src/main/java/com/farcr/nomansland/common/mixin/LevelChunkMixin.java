package com.farcr.nomansland.common.mixin;

import com.farcr.nomansland.common.extension.LevelChunkExtension;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LevelChunk.class)
public class LevelChunkMixin implements LevelChunkExtension {
    @Unique public boolean nml$ignoreBuddyAnchor = false;

    public boolean nml$shouldIgnoreBuddyAnchor() {
        return nml$ignoreBuddyAnchor;
    }

    public void nml$setIgnoreBuddyAnchor() {
        this.nml$ignoreBuddyAnchor = true;
    }

    public void nml$clearIgnoreBuddyAnchor() {
        this.nml$ignoreBuddyAnchor = false;
    }
}
