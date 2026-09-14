package com.farcr.nomansland.common.block.cauldrons;

import com.farcr.nomansland.common.integration.Mods;
import com.farcr.nomansland.common.registry.NMLParticleTypes;
import com.farcr.nomansland.common.registry.NMLSounds;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.EffectCures;
import net.neoforged.neoforge.event.EventHooks;

import java.util.ArrayList;
import java.util.Iterator;

public class MilkCauldron extends FourLayeredCauldronBlock {
    public MilkCauldron() {
        super(NMLParticleTypes.MILK_DROPLET);
    }

    @Override
    protected MapCodec<MilkCauldron> codec() {
        return MapCodec.unit(MilkCauldron::new);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        Iterator<MobEffectInstance> itr = player.getActiveEffects().iterator();
        ArrayList<Holder<MobEffect>> compatibleEffects = new ArrayList<>();

        MobEffectInstance selectedEffect;
        while(itr.hasNext()) {
            selectedEffect = itr.next();
            if (selectedEffect.getCures().contains(EffectCures.MILK)) {
                compatibleEffects.add(selectedEffect.getEffect());
            }
        }

        if (!level.isClientSide) {
            if (!compatibleEffects.isEmpty()) {
                selectedEffect = player.getEffect(compatibleEffects.get(level.random.nextInt(compatibleEffects.size())));
                if (selectedEffect != null && !EventHooks.onEffectRemoved(player, selectedEffect, EffectCures.MILK)) {
                    player.removeEffect(selectedEffect.getEffect());
                }
            }

            level.playSound(player, pos, NMLSounds.PLAYER_DRINK_MILK.get(), SoundSource.PLAYERS, 0.5F, 1);
            lowerFillLevel(state, level, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        boolean interacted = false;

        if (player.isHolding(Items.BUCKET) && isFull(state)) {
            interacted = true;
            if (!level.isClientSide) {
                player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, Items.MILK_BUCKET.getDefaultInstance()));
                BlockState newState = Blocks.CAULDRON.defaultBlockState();
                level.setBlockAndUpdate(pos, newState);
                level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(newState));
                level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS);
            }
        } else if (player.isHolding(Items.MILK_BUCKET) && !isFull(state)) {
            interacted = true;
            if (!level.isClientSide) {
                player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, Items.BUCKET.getDefaultInstance()));
                BlockState newState = state.setValue(LEVEL, 3);
                level.setBlockAndUpdate(pos, newState);
                level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(newState));
                level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS);
            }
        } else if (Mods.FARMERSDELIGHT.isLoaded()) {
            if (stack.is(Items.GLASS_BOTTLE)) {
                interacted = true;
                if (!level.isClientSide) {
                    player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, Mods.FARMERSDELIGHT.getItem("milk_bottle").getDefaultInstance()));
                    lowerFillLevel(state, level, pos);
                    level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS);
                }
            } else if (stack.is(Mods.FARMERSDELIGHT.getItem("milk_bottle")) && !isFull(state)) {
                interacted = true;
                if (!level.isClientSide) {
                    player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, Items.GLASS_BOTTLE.getDefaultInstance()));
                    raiseFillLevel(state, level, pos);
                    level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS);
                }
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

        if (!level.isClientSide && entity.isOnFire() && isEntityInsideContent(state, pos, entity)) {
            entity.clearFire();
            level.playSound(entity, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1, 1);
        }
    }
}
