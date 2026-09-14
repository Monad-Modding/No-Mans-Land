package com.farcr.nomansland.common.block.cauldrons;

import com.farcr.nomansland.common.registry.NMLSounds;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class HoneyCauldron extends FourLayeredCauldronBlock {
    public HoneyCauldron() {
        super(Holder.direct(ParticleTypes.FALLING_HONEY));
    }

    @Override
    protected MapCodec<HoneyCauldron> codec() {
        return MapCodec.unit(HoneyCauldron::new);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        boolean interacted = false;

        if (isFull(state)) {
            interacted = true;
            if (!level.isClientSide) {
                ItemStack containedStack = new ItemStack(Items.HONEY_BLOCK);
                if (!player.getInventory().add(containedStack))
                    player.drop(containedStack, false);
                BlockState newState = Blocks.CAULDRON.defaultBlockState();
                level.setBlockAndUpdate(pos, newState);
                level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(newState));
                level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS);
            }
        }

        if (interacted) {
            if (!level.isClientSide) player.awardStat(Stats.USE_CAULDRON);
            return InteractionResult.sidedSuccess(level.isClientSide);
        } else return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        boolean interacted = false;

        ItemStack bottleStack = new ItemStack(Items.HONEY_BOTTLE);
        if (stack.is(Items.GLASS_BOTTLE)) {
            interacted = true;
            if (!level.isClientSide) {
                player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, bottleStack));
                lowerFillLevel(state, level, pos);
                level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS);
            }
        } else if (stack.is(bottleStack.getItem()) && !isFull(state)) {
            interacted = true;
            if (!level.isClientSide) {
                player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, Items.GLASS_BOTTLE.getDefaultInstance()));
                raiseFillLevel(state, level, pos);
                level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS);
            }
        }

        if (interacted) {
            if (!level.isClientSide) {
                player.awardStat(Stats.USE_CAULDRON);
                player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        } else return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);

        if (isEntityInsideContent(state, pos, entity)) {
            if (entity instanceof LivingEntity && state.getValue(LEVEL) > 1) {
                if (!level.isClientSide) level.playSound(null, pos, NMLSounds.STICKY_CAULDRON_SLIDE.get(), SoundSource.BLOCKS, 1, 1);
                entity.makeStuckInBlock(state, new Vec3(.9, .9, .9));
            }

            if (!level.isClientSide && entity.isOnFire()) {
                entity.clearFire();
                level.playSound(entity, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1, 1);
            }
        }
    }
}
