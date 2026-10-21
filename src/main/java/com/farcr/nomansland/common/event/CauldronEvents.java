package com.farcr.nomansland.common.event;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.integration.Mods;
import com.farcr.nomansland.common.recipe.CauldronInteractionInput;
import com.farcr.nomansland.common.recipe.CauldronInteractionRecipe;
import com.farcr.nomansland.common.registry.NMLRecipeSerializers;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import com.farcr.nomansland.common.registry.items.NMLDataComponents;
import com.farcr.nomansland.common.registry.items.NMLItems;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CauldronBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;

import java.util.Optional;

import static com.farcr.nomansland.common.block.cauldrons.FourLayeredCauldronBlock.LEVEL;

@EventBusSubscriber(modid = NoMansLand.MODID)
public class CauldronEvents {

    @SubscribeEvent
    public static void onRightClickBlock(final UseItemOnBlockEvent event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        Player player = event.getPlayer();
        ItemStack stack = event.getItemStack();
        InteractionHand hand = event.getHand();

        if (event.getUsePhase() == UseItemOnBlockEvent.UsePhase.BLOCK && player != null) {
            boolean interacted = false;
            BlockState newState = null;

            if (state.getBlock() instanceof CauldronBlock) {
                if (stack.is(Items.MILK_BUCKET)) {
                    player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, Items.BUCKET.getDefaultInstance()));
                    interacted = true;
                    newState = NMLBlocks.MILK_CAULDRON.get().defaultBlockState().setValue(LEVEL, 4);
                    level.setBlockAndUpdate(pos, newState);
                    level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS);
                } else if (stack.is(Items.HONEY_BOTTLE)) {
                    player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, Items.GLASS_BOTTLE.getDefaultInstance()));
                    interacted = true;
                    newState = NMLBlocks.HONEY_CAULDRON.get().defaultBlockState();
                    level.setBlockAndUpdate(pos, newState);
                    level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS);
                } else if (stack.is(NMLItems.MAPLE_SYRUP_BOTTLE)) {
                    player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, Items.GLASS_BOTTLE.getDefaultInstance()));
                    interacted = true;
                    newState = NMLBlocks.MAPLE_SYRUP_CAULDRON.get().defaultBlockState();
                    level.setBlockAndUpdate(pos, newState);
                    level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS);
                } else if (stack.is(NMLItems.RESIN_OIL_BOTTLE)) {
                    player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, Items.GLASS_BOTTLE.getDefaultInstance()));
                    interacted = true;
                    newState = NMLBlocks.RESIN_OIL_CAULDRON.get().defaultBlockState();
                    level.setBlockAndUpdate(pos, newState);
                    level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS);
                } else if (stack.is(NMLItems.RESIN) && (player.hasInfiniteMaterials() || stack.getCount() >= 2)) {
                    player.getItemInHand(hand).consume(2, player);
                    interacted = true;
                    newState = NMLBlocks.RESIN_CAULDRON.get().defaultBlockState();
                    level.setBlockAndUpdate(pos, newState);
                    level.playSound(null, pos, SoundEvents.HONEY_BLOCK_PLACE, SoundSource.BLOCKS);
                } else if (Mods.FARMERSDELIGHT.isLoaded() && stack.is(Mods.FARMERSDELIGHT.getItem("milk_bottle"))) {
                    player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, Items.GLASS_BOTTLE.getDefaultInstance()));
                    interacted = true;
                    newState = NMLBlocks.MILK_CAULDRON.get().defaultBlockState();
                    level.setBlockAndUpdate(pos, newState);
                    level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS);
                }
            } else if (state.getBlock() instanceof LayeredCauldronBlock) {
                if (state.is(Blocks.WATER_CAULDRON) && stack.getCount() == 1 && stack.has(NMLDataComponents.TRANSLUCENT.get())) {
                    ItemStack washed = stack.copy();
                    washed.remove(NMLDataComponents.TRANSLUCENT.get());
                    player.setItemInHand(hand, washed);
                    int i = state.getValue(LayeredCauldronBlock.LEVEL) - 1;
                    newState = i == 0 ? Blocks.CAULDRON.defaultBlockState() : state.setValue(LayeredCauldronBlock.LEVEL, i);
                    level.setBlockAndUpdate(pos, newState);
                    level.playSound(null, pos, SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 1.0F, 1.0F);
                    player.awardStat(Stats.USE_CAULDRON);
                    player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
                    level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(newState));
                    event.cancelWithResult(ItemInteractionResult.sidedSuccess(level.isClientSide));
                    return;
                }

                RecipeManager recipes = level.getRecipeManager();

                CauldronInteractionInput input = new CauldronInteractionInput(state, stack);
                Optional<RecipeHolder<CauldronInteractionRecipe>> optional = recipes.getRecipeFor(
                        NMLRecipeSerializers.CAULDRON_INTERACTION_RECIPE.get(), input, level
                );

                Optional<CauldronInteractionRecipe> recipe = optional
                        .map(RecipeHolder::value);

                ItemStack result = recipe
                        .map(r -> r.assemble(input, level.registryAccess()))
                        .orElse(ItemStack.EMPTY);

                int cost = recipe
                        .map(CauldronInteractionRecipe::levelCost)
                        .orElse(1);

                if (!result.isEmpty()) {
                    level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS);
                    int i = state.getValue(LayeredCauldronBlock.LEVEL) - cost;
                    newState = i == 0 ? Blocks.CAULDRON.defaultBlockState() : state.setValue(LayeredCauldronBlock.LEVEL, i);
                    level.setBlockAndUpdate(pos, newState);

                    player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, result));

                    interacted = true;
                }
            }

            if (interacted) {
                player.awardStat(Stats.USE_CAULDRON);
                player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
                level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(newState));
                event.cancelWithResult(ItemInteractionResult.sidedSuccess(level.isClientSide));
            }
        }
    }
}
