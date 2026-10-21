package com.farcr.nomansland.common.mixin;

import com.farcr.nomansland.common.registry.NMLTags;
import com.farcr.nomansland.common.world.saved_data.WardedSpacesData;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WaterAvoidingRandomStrollGoal.class)
public class WaterAvoidingRandomStrollGoalMixin extends RandomStrollGoalMixin {

    @Inject(method = "getPosition", at = @At("HEAD"), cancellable = true)
    private void getPosition(final CallbackInfoReturnable<Vec3> cir) {
        if (this.mob instanceof Enemy && !this.mob.getType().is(NMLTags.WARD_REPELLED_BLACKLIST)
                && this.mob.level() instanceof final ServerLevel serverLevel) {
            final WardedSpacesData wardedSpacesData = WardedSpacesData.get(serverLevel);

            if (wardedSpacesData.isWarded(this.mob.level(), this.mob.blockPosition())) {
                final Pair<BlockPos, Integer> pair = wardedSpacesData.getAffectingEffigyAt(this.mob.level(), this.mob.blockPosition());

                if (pair != null) {
                    final int effigyRange = pair.right();
                    final BlockPos effigyPos = pair.left();

                    final Vec3 newPosition = LandRandomPos.getPosAway(this.mob, this.mob.getRandom().nextInt(effigyRange / 4, effigyRange + effigyRange / 4), 10, Vec3.atCenterOf(effigyPos));
                    if (newPosition != null) cir.setReturnValue(newPosition);
                }
            }
        }
    }
}
