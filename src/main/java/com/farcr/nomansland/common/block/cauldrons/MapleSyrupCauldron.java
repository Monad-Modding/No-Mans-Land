package com.farcr.nomansland.common.block.cauldrons;

import com.farcr.nomansland.common.registry.NMLParticleTypes;
import com.farcr.nomansland.common.registry.NMLSounds;
import com.farcr.nomansland.common.registry.items.NMLItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class MapleSyrupCauldron extends FourLayeredCauldronBlock {
    public MapleSyrupCauldron() {
        super(NMLParticleTypes.MAPLE_SYRUP_DROPLET);
    }

    @Override
    protected MapCodec<MapleSyrupCauldron> codec() {
        return MapCodec.unit(MapleSyrupCauldron::new);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        boolean interacted = false;

        ItemStack bottleStack = new ItemStack(NMLItems.MAPLE_SYRUP_BOTTLE.get());
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
