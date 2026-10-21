package com.farcr.nomansland.common.mixin;

import com.farcr.nomansland.common.entity.cervidae.moose.Moose;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import static com.farcr.nomansland.common.entity.cervidae.moose.Moose.modifyMooseMovedWronglyThreshold;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {

    @Shadow public ServerPlayer player;

    /**
     * I don't understand why this mixin is needed...
     * When a moose is being ridden and one attempts to step up a ledge, depending on the angle the moose and the player will not be allowed to proceed.
     * This isn't tied to the actual change in height, which is ignored by the method if above half a block.
     * It is tied to the horizontal movement ??? the comparison between the previous XY coordinates and the ones post-movement is greater than 0.0625, which then has the moose teleported back.
     * And like, don't horse move faster than that anyway???? I feel like it's something else that's actually the root issue, and I'm just unaware of it.
     * When riding a moose, we quadruple the threshold for what is considered to be a "moved wrongly" movement.
     * I think like every other modpack has this behavior completely disabled anyway, so I'd say it's fine.
     * If the actual root issue of the Moose-Step-Up issue is identified, this method should be removed/adjusted.
     *
     * @author SammySemicolon
     */
    @ModifyExpressionValue(method = "handleMoveVehicle", at = @At(value = "CONSTANT", args = "doubleValue=0.0625"), require = 0)
    public double nml$makeMooseNormal(double original) {
        if (this.player.getRootVehicle() instanceof Moose) {
            return modifyMooseMovedWronglyThreshold();
        }
        return original;
    }
}
