package com.farcr.nomansland.common.block.cauldrons;

import com.farcr.nomansland.common.integration.FDIntegration;
import com.farcr.nomansland.common.registry.NMLSounds;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class WitchStewCauldron extends FourLayeredCauldronBlock {
    public WitchStewCauldron() {
        super(null);
    }

    @Override
    protected MapCodec<WitchStewCauldron> codec() {
        return MapCodec.unit(WitchStewCauldron::new);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        double d0 = pos.getX() + 0.5 + random.nextInt(-40, 40) * 0.01;
        double d1 = pos.getY() + random.nextInt(-10, 40) * 0.001 + getContentHeight(state);
        double d2 = pos.getZ() + 0.5 + random.nextInt(-40, 40) * 0.01;

        Particle particle = Minecraft.getInstance().levelRenderer.addParticleInternal(
                ParticleTypes.EFFECT, ParticleTypes.EFFECT.getOverrideLimiter(), d0, d1, d2, 0D, 0D, 0D
        );

        if (particle != null) {
            float f1 = 0.75F + random.nextFloat() * 0.25F;
            particle.setColor(0.5294118F * f1, 0.6392157F * f1, 0.3882353F * f1);
            particle.setPower(random.nextFloat() * 4);
        }

        if (random.nextInt(15) == 0) {
            level.playLocalSound(d0, d1, d2, NMLSounds.WITCH_STEW_CAULDRON_AMBIENT.get(), SoundSource.BLOCKS, 0.5F, random.nextFloat() * 0.2F + 0.9F, false);
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.is(Items.BOWL)) {
            if (!level.isClientSide) {
                stack.consume(1, player);

                if (!(player.isCreative() && player.getInventory().hasAnyMatching(s -> s.getItem() == FDIntegration.WITCH_STEW_ITEM.item()))) {
                    ItemStack item = new ItemStack(FDIntegration.WITCH_STEW_ITEM.item());
                    if (!player.addItem(item) ) {
                        player.drop(item, false);
                    }
                }

                level.playSound(null, pos, NMLSounds.WITCH_STEW_CAULDRON_EMPTY.value(), SoundSource.BLOCKS);

                if (state.getValue(LEVEL) > 1) {
                    lowerFillLevel(state, level, pos);
                } else {
                    level.setBlockAndUpdate(pos, FDIntegration.EMPTY_WITCH_STEW.block().defaultBlockState());
                }

                player.awardStat(Stats.USE_CAULDRON);
                player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);

        if (isEntityInsideContent(state, pos, entity)) {
            if (entity instanceof LivingEntity living && state.getValue(LEVEL) > 3 && !level.isClientSide) {
                living.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100));
            }

            if (!level.isClientSide && entity.isOnFire()) {
                entity.clearFire();
                level.playSound(entity, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1, 1);
            }
        }
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        return isFull(state) ? FDIntegration.WITCH_STEW_BLOCK_ITEM.stack() : super.getCloneItemStack(state, target, level, pos, player);
    }
}
