package com.farcr.nomansland.common.block.cauldrons;

import com.farcr.nomansland.common.registry.NMLParticleTypes;
import com.farcr.nomansland.common.registry.NMLTags;
import com.farcr.nomansland.common.registry.entities.NMLEffects;
import com.farcr.nomansland.common.registry.items.NMLItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class ResinOilCauldron extends FourLayeredCauldronBlock {
    public ResinOilCauldron() {
        super(NMLParticleTypes.OIL);
    }

    @Override
    protected MapCodec<ResinOilCauldron> codec() {
        return MapCodec.unit(ResinOilCauldron::new);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (level.getBlockState(pos.below()).is(NMLTags.HEAT_SOURCES)) {
            double d0 = pos.getX() + 0.5 + random.nextInt(-40, 40)*0.01;
            double d1 = pos.getY() + random.nextInt(-10, 40)*0.001 + getContentHeight(state);
            double d2 = pos.getZ() + 0.5 + random.nextInt(-40, 40)*0.01;
            level.addParticle(NMLParticleTypes.RESIN_OIL_BUBBLE.get(), d0, d1, d2, 0.0, 0.0, 0.0);
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        boolean interacted = false;

        ItemStack bottleStack = new ItemStack(NMLItems.RESIN_OIL_BOTTLE.get());
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

        if (isEntityInsideContent(state, pos, entity) && level instanceof ServerLevel serverLevel && entity instanceof LivingEntity livingEntity) {
            livingEntity.addEffect(new MobEffectInstance(NMLEffects.FLAMMABLE, 100));

            if (entity.isOnFire() && this.isEntityInsideContent(state, pos, entity) && entity.getY() < pos.getY() + 0.6) {
                entity.setRemainingFireTicks(entity.getRemainingFireTicks() + 50 * state.getValue(LEVEL));
                level.setBlockAndUpdate(pos, Blocks.CAULDRON.defaultBlockState());
                for (int i = 0; i <= state.getValue(LEVEL) * 25; i++) {
                serverLevel.sendParticles(ParticleTypes.FLAME,
                        entity.getX() + level.random.nextInt(-10, 10) * 0.005,
                        entity.getY() + 0.7 + level.random.nextInt(-10, 10) * 0.05 - i * 0.2,
                        entity.getZ() + level.random.nextInt(-10, 10) * 0.01,
                        state.getValue(LEVEL) * 2,
                        level.random.nextInt(-10, 10) * 0.005, state.getValue(LEVEL) * 0.2,
                        level.random.nextInt(-10, 10) * 0.005,
                        0.05);
                serverLevel.sendParticles(ParticleTypes.SMALL_FLAME,
                        entity.getX() + level.random.nextInt(-10, 10) * 0.005,
                        entity.getY() + 0.7 + level.random.nextInt(-10, 10) * 0.05 - i * 0.2,
                        entity.getZ() + level.random.nextInt(-10, 10) * 0.01,
                        state.getValue(LEVEL) * 2,
                        level.random.nextInt(-10, 10) * 0.005, state.getValue(LEVEL) * 0.2,
                        level.random.nextInt(-10, 10) * 0.005,
                        0.05);
                    serverLevel.sendParticles(ParticleTypes.SMOKE,
                            entity.getX() + level.random.nextInt(-10, 10) * 0.005,
                            entity.getY() + 0.7 + level.random.nextInt(-10, 10) * 0.05 - i * 0.2,
                            entity.getZ() + level.random.nextInt(-10, 10) * 0.01,
                            state.getValue(LEVEL) * 2,
                            level.random.nextInt(-10, 10) * 0.005, state.getValue(LEVEL) * 0.2,
                            level.random.nextInt(-10, 10) * 0.005,
                            0.05);
                    serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,
                            entity.getX() + level.random.nextInt(-10, 10) * 0.005,
                            entity.getY() + 0.7 + level.random.nextInt(-10, 10) * 0.05 - i * 0.2,
                            entity.getZ() + level.random.nextInt(-10, 10) * 0.01,
                            state.getValue(LEVEL) * 2,
                            level.random.nextInt(-10, 10) * 0.005, state.getValue(LEVEL) * 0.2,
                            level.random.nextInt(-10, 10) * 0.005,
                            0.05);
                }

                entity.hurt(entity.damageSources().inFire(), state.getValue(LEVEL) * 3);
                serverLevel.playLocalSound(pos, SoundEvents.GENERIC_BURN, SoundSource.BLOCKS, state.getValue(LEVEL) * 0.5F, 0.5F, false);
                serverLevel.playLocalSound(pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, state.getValue(LEVEL) * 0.5F, 0.5F, false);
            }
        }
    }
}
