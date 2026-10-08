package com.farcr.nomansland.common.entity.ai;

import net.minecraft.world.level.gameevent.GameEvent;
import com.farcr.nomansland.common.block.cauldrons.WitchStewCauldron;
import com.farcr.nomansland.common.integration.FDIntegration;
import com.farcr.nomansland.common.mixin.IWitchAccessor;
import com.farcr.nomansland.common.registry.NMLSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.NotNull;

import static com.farcr.nomansland.common.block.cauldrons.FourLayeredCauldronBlock.LEVEL;

public class WitchBowlStewGoal extends MoveToBlockGoal {
    protected int ticksWaited;

    public WitchBowlStewGoal(LivingEntity living, double speedIn) {
        super((PathfinderMob) living, speedIn, 16);
    }

    @Override
    protected boolean isValidTarget(LevelReader level, BlockPos pos) {
        BlockState blockstate = level.getBlockState(pos.above());
        Block block = blockstate.getBlock();
        return block instanceof WitchStewCauldron && blockstate.getValue(LEVEL) > 0;
    }

    public void tick() {
        if (this.mob.getMainHandItem().isEmpty() && this.mob.blockPosition().closerThan(this.blockPos, 2.75)) {
            this.mob.setItemSlot(EquipmentSlot.MAINHAND, Items.BOWL.getDefaultInstance());
        }
        if (this.isReachedTarget()) {
            if (this.ticksWaited >= 25) {
                this.onReachedTarget();
            } else {
                if (ticksWaited > 10) {
                    this.mob.getLookControl().setLookAt(this.blockPos.getX() + 0.5, this.blockPos.getY() + 1.2, this.blockPos.getZ() + 0.5);
                }
                ++this.ticksWaited;
            }
        }
        if (this.mob instanceof Witch witch) {
            witch.setUsingItem(false);
        }
        super.tick();
    }

    protected void onReachedTarget() {
        Level level = this.mob.level();
        this.mob.getLookControl().setLookAt(this.blockPos.getX() + 0.5, this.blockPos.getY() + 1.2, this.blockPos.getZ() + 0.5);
        if (this.mob.tickCount % 30 == 0) {
            if (EventHooks.canEntityGrief(level, this.mob) && !level.isClientSide) {
                BlockState state = level.getBlockState(this.blockPos.above());
                if (state.getBlock() instanceof WitchStewCauldron) {
                    if (state.getValue(LEVEL) > 1) {
                        level.setBlock(this.blockPos.above(), (BlockState) state.setValue(LEVEL, state.getValue(LEVEL) - 1), 3);
                    } else {
                        level.setBlock(this.blockPos.above(), FDIntegration.EMPTY_WITCH_STEW.block().defaultBlockState(), 3);
                    }
                    level.playSound(null, this.blockPos.above(), NMLSounds.WITCH_STEW_CAULDRON_EMPTY.value(), this.mob.getSoundSource());
                    level.gameEvent(null, GameEvent.FLUID_PICKUP, this.blockPos.above());
                    this.mob.swing(InteractionHand.MAIN_HAND);
                    this.mob.setItemSlot(EquipmentSlot.MAINHAND, FDIntegration.WITCH_STEW_ITEM.stack());
                    if (this.mob instanceof Witch witch) {
                        witch.setUsingItem(true);
                        ((IWitchAccessor) witch).setUsingTime(this.mob.getMainHandItem().getUseDuration(this.mob) + 6);
                    }
                }
            }
            this.stop();
        }
    }

    public boolean canUse() {
        return super.canUse() && (this.mob.getMainHandItem().isEmpty() || this.mob.getMainHandItem().is(Items.BOWL));
    }

    public boolean canContinueToUse() {
        if (!super.canContinueToUse() && this.mob.getMainHandItem().is(Items.BOWL)) {
            this.mob.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            if (this.mob.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.ANGRY_VILLAGER, this.mob.getX(),
                        this.mob.getY() + 0.5, this.mob.getZ(), 5, 0.5, 0.5, 0.5, 0);
            }
            // the imaginary subtitle of "Witch chuckles darkly" is so amusing to me
            this.mob.playSound(SoundEvents.WITCH_AMBIENT, 0.8F, 0.7F);
        }
        return super.canContinueToUse() && !this.mob.getMainHandItem().is(FDIntegration.WITCH_STEW_ITEM)
                && !this.mob.hasEffect(MobEffects.REGENERATION);
    }

    public void start() {
        super.start();
        this.ticksWaited = 0;
    }

    protected int nextStartTick(@NotNull PathfinderMob creature) {
        return 200;
    }

    public double acceptedDistance() {
        return 2.0D;
    }
}
