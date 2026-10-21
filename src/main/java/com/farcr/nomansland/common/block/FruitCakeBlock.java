package com.farcr.nomansland.common.block;

import com.mojang.serialization.MapCodec;
import com.farcr.nomansland.common.integration.FDIntegration;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import vectorwing.farmersdelight.common.registry.ModSounds;
import vectorwing.farmersdelight.common.tag.ModTags;
import vectorwing.farmersdelight.common.utility.ItemUtils;

public class FruitCakeBlock extends CakeBlock {
    public FruitCakeBlock(Properties properties) {
        super(properties);
    }
    public static final MapCodec<CakeBlock> CODEC = simpleCodec(FruitCakeBlock::new)
            .xmap(block -> (CakeBlock) block, block -> (FruitCakeBlock) block);

    @Override
    public MapCodec<CakeBlock> codec() {
        return CODEC;
    }


    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        Item item = stack.getItem();
        int bites = state.getValue(BITES);
        if (stack.is(ItemTags.CANDLES) && bites == 0) {
            Block var10 = Block.byItem(item);
            if (var10 instanceof CandleBlock candleBlock) {
                stack.consume(1, player);
                level.playSound(null, pos, SoundEvents.CAKE_ADD_CANDLE, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.setBlockAndUpdate(pos, CandleFruitCakeBlock.byCandle(candleBlock));
                level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                player.awardStat(Stats.ITEM_USED.get(item));
                return ItemInteractionResult.SUCCESS;
            }
        }

        if (stack.is(ModTags.Items.KNIVES)) {
            if (bites < 6) {
                level.setBlockAndUpdate(pos, state.setValue(CakeBlock.BITES, bites + 1));
            } else level.removeBlock(pos, false);

            ItemUtils.spawnItemEntity(level, new ItemStack(FDIntegration.FRUIT_CAKE_SLICE.get()),
                    pos.getX() + (bites * 0.1), pos.getY() + 0.2, pos.getZ() + 0.5,
                    -0.05, 0, 0);
            level.playSound(null, pos, ModSounds.BLOCK_FOOD_SLICE.get(), SoundSource.PLAYERS, 0.8F, 0.8F);

            return ItemInteractionResult.SUCCESS;
        }

        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }


}
