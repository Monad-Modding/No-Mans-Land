package com.farcr.nomansland.common.entity.tortoise.ai;

import com.farcr.nomansland.common.entity.tortoise.Tortoise;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;
import java.util.UUID;

public class TortoiseSearchForDangerGoal extends Goal {
    private final Tortoise tortoise;

    public TortoiseSearchForDangerGoal(Tortoise tortoise) {
        this.tortoise = tortoise;
        this.setFlags(EnumSet.of(Flag.LOOK, Flag.MOVE, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        return this.tortoise.inShell() && this.tortoise.getLastHurtByUUID() != null && this.tortoise.level().getGameTime() - this.tortoise.getHurtWhen() > 700L;
    }

    @Override
    public void start() {
        super.start();
        UUID hurtBy = this.tortoise.getLastHurtByUUID();
        Entity attacker = hurtBy != null && this.tortoise.level() instanceof ServerLevel serverLevel ? serverLevel.getEntity(hurtBy) : null;
        if (attacker != null && attacker.isAlive() && attacker.distanceToSqr(this.tortoise) < 121.0D) {
            this.tortoise.setHurtWhen(this.tortoise.level().getGameTime());
            this.tortoise.retreatShell(true);
            this.tortoise.setSearching(false);
        } else {
            this.tortoise.retreatShell(false);
            this.tortoise.setSearching(false);
            this.tortoise.setLastHurtByUUID(null);
        }
    }
}
