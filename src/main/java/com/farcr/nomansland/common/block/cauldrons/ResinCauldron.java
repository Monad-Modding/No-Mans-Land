package com.farcr.nomansland.common.block.cauldrons;

import com.farcr.nomansland.common.registry.NMLParticleTypes;
import com.farcr.nomansland.common.registry.NMLSounds;
import com.farcr.nomansland.common.registry.NMLTags;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import com.farcr.nomansland.common.registry.items.NMLItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class ResinCauldron extends FourLayeredCauldronBlock {
    public ResinCauldron() {
        super(NMLParticleTypes.RESIN_DROPLET);
    }

    @Override
    protected MapCodec<ResinCauldron> codec() {
        return MapCodec.unit(ResinCauldron::new);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            ItemStack containedStack = new ItemStack(NMLItems.RESIN.get(), 2);
            if (!player.getInventory().add(containedStack))
                player.drop(containedStack, false);
            lowerFillLevel(state, level, pos);
            level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS);
            player.awardStat(Stats.USE_CAULDRON);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        boolean interacted = false;

        if (stack.is(NMLItems.RESIN) && (player.hasInfiniteMaterials() || stack.getCount() >= 2) && !isFull(state)) {
            interacted = true;
            if (!level.isClientSide) {
                stack.consume(2, player);
                raiseFillLevel(state, level, pos);
                level.playSound(null, pos, NMLSounds.RESIN_CONSUMED.get(), SoundSource.BLOCKS);
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

            if (entity instanceof ItemEntity itemEntity && !level.isClientSide) {
                if (itemEntity.getItem().is(NMLTags.MAKES_RESIN_OIL) && level.getBlockState(pos.below()).is(NMLTags.HEAT_SOURCES)) {
                    entity.remove(Entity.RemovalReason.DISCARDED);
                    level.playSound(null, pos, NMLSounds.HONEYCOMB_CONSUMED.get(), SoundSource.BLOCKS, 1, 0.5F);
                    level.setBlockAndUpdate(pos, NMLBlocks.RESIN_OIL_CAULDRON.get().withPropertiesOf(state));
                }
            }
        }
    }
}
