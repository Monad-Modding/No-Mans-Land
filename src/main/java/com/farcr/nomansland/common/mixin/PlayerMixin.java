package com.farcr.nomansland.common.mixin;

import com.farcr.nomansland.common.dreams.DreamManager;
import com.farcr.nomansland.common.dreams.DreamType;
import com.farcr.nomansland.common.dreams.dreamtypes.MoonlightDreamType;
import com.farcr.nomansland.common.extension.PlayerExtension;
import com.farcr.nomansland.common.registry.NMLDamageTypes;
import com.farcr.nomansland.common.registry.NMLSounds;
import com.farcr.nomansland.common.registry.entities.NMLEffects;
import com.mojang.datafixers.util.Either;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Unit;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin implements PlayerExtension {

    @Inject(method = "getHurtSound", at = @At("HEAD"), cancellable = true)
    private void nml$getHurtSound(DamageSource source, CallbackInfoReturnable<SoundEvent> info) {
        if (source.is(NMLDamageTypes.SPIKE_POKE)) info.setReturnValue(NMLSounds.PLAYER_HURT_SPIKE_TRAP.get());
    }

    @Unique private void nml$setLastSleepDimension(ResourceKey<Level> dimension) {
        this.nml$lastSleepDimension = dimension;
    }
    @Unique private ResourceKey<Level> nml$lastSleepDimension;
    @Override public ResourceKey<Level> nml$getLastSleepDimension() {
        return nml$lastSleepDimension;
    }

    @Unique private void nml$setLastSleepPosition(BlockPos pos) {
        this.nml$lastSleepPosition = pos;
    }
    @Unique private BlockPos nml$lastSleepPosition;
    @Override public BlockPos nml$getLastSleepPosition() {
        return nml$lastSleepPosition;
    }

    @Inject(
        method = "startSleepInBed",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;startSleeping(Lnet/minecraft/core/BlockPos;)V"
        )
    )
    private void nml$startSleepInBed(
        BlockPos bedPos, CallbackInfoReturnable<Either<Player.BedSleepingProblem, Unit>> cir
    ) {
        nml$lastSleepPosition = bedPos;
        nml$lastSleepDimension = ((Player) (Object) this).level().dimension();
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void nml$addAdditionalSaveData(CompoundTag compound, CallbackInfo ci) {
        if (nml$getLastSleepDimension() != null) {
            ResourceKey.codec(Registries.DIMENSION)
                .encodeStart(NbtOps.INSTANCE, nml$getLastSleepDimension()).result()
                .ifPresent((result) -> compound.put("LastSleepingDimension", result));
        }
        if (nml$getLastSleepPosition() != null)
            compound.put("LastSleepingPosition", NbtUtils.writeBlockPos(nml$getLastSleepPosition()));
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void nml$readAdditionalSaveData(CompoundTag compound, CallbackInfo ci) {
        ResourceKey.codec(Registries.DIMENSION)
            .parse(NbtOps.INSTANCE, compound.get("LastSleepingDimension"))
            .result().ifPresent(this::nml$setLastSleepDimension);
        NbtUtils.readBlockPos(compound, "LastSleepingPosition").ifPresent(this::nml$setLastSleepPosition);
    }

    @Unique private Player nml$Self = ((Player) (Object) this);

    @Inject(method = "stopSleepInBed", at = @At("HEAD"), cancellable = true)
    private void nml$stopSleeping(CallbackInfo ci) {
        if (DreamManager.getPlayerShouldDream(nml$Self))
            ci.cancel();
    }

    @Inject(method = "isImmobile", at = @At("RETURN"), cancellable = true)
    private void nml$playerImmobile(CallbackInfoReturnable<Boolean> cir) {
        DreamType.DreamTypeInstance dreamTypeInstance = DreamManager.getAmbiguousDreamTypeInstance(nml$Self);
        if (dreamTypeInstance instanceof MoonlightDreamType.MoonlightDreamTypeInstance moonlightDreamType
        && moonlightDreamType.moonPresenceTime > 0) cir.setReturnValue(true);
    }

    @Inject(method = "mayUseItemAt", at = @At("RETURN"), cancellable = true)
    private void nml$cancelInteraction(BlockPos pos, Direction facing, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (DreamManager.getAmbiguousDreamType(nml$Self) != null) cir.setReturnValue(false);
        if (this.nml$Self.hasEffect(NMLEffects.STASIS)) cir.setReturnValue(false);
    }

    @Inject(method = "blockActionRestricted", at = @At("RETURN"), cancellable = true)
    private void nml$cancelAction(Level level, BlockPos pos, GameType gameMode, CallbackInfoReturnable<Boolean> cir) {
        if (DreamManager.getAmbiguousDreamType(nml$Self) != null) cir.setReturnValue(true);
        if (this.nml$Self.hasEffect(NMLEffects.STASIS)) cir.setReturnValue(true);
    }
}