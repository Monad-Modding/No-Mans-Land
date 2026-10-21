package com.farcr.nomansland.common.entity.cave_carp.ai;

import com.farcr.nomansland.common.entity.cave_carp.CaveCarp;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class CaveCarpSwimGoal extends RandomSwimmingGoal {

    private final CaveCarp carp;

    public CaveCarpSwimGoal(CaveCarp carp) {
        super(carp, 1.0, 40);
        this.carp = carp;
    }

    @Override
    @Nullable
    protected Vec3 getPosition() {
        for (int i = 0; i < 8; i++) {
            Vec3 pos = super.getPosition();
            if (pos != null && this.carp.isStillWater(BlockPos.containing(pos))) {
                return pos;
            }
        }
        return null;
    }
}
