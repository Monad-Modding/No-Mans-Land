package com.farcr.nomansland.common.event;

import net.minecraft.world.level.gameevent.GameEvent;
import com.farcr.nomansland.NMLConfig;
import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.registry.NMLTags;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.List;
import java.util.Map;

import static net.minecraft.world.level.block.SnowyDirtBlock.SNOWY;

@EventBusSubscriber(modid = NoMansLand.MODID)
public class PathMakingEvents {

    /**
     * Verifies that the Path/Farmland Tweak can proceed. In order to proceed:
     *  - The player may not be a spectator
     *  - The stack needs to be nomansland:makes_paths or nomansland:makes_farmland
     *  - The item needs to have the till or flatten ability
     * @param event The original RightClickBlock event
     * @return True/False based on the above
     */
    private static boolean canContinue(PlayerInteractEvent.RightClickBlock event) {
        ItemStack stack = event.getItemStack();
        Item item = stack.getItem();

        if (event.getEntity().isSpectator()) return false;

        if (!(stack.is(NMLTags.MAKES_PATHS) || stack.is(NMLTags.MAKES_FARMLAND))) return false;

        return item.canPerformAction(stack, ItemAbilities.HOE_TILL) || item.canPerformAction(stack, ItemAbilities.SHOVEL_FLATTEN);
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!NMLConfig.PATH_TWEAKS.get() || !canContinue(event)) return;

        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();

        if (state.canBeReplaced()) {
            pos = pos.below();
            state = level.getBlockState(pos);
        }

        List<Block> pathableBlocks = List.of(
                Blocks.GRAVEL,
                Blocks.SAND,
                Blocks.RED_SAND,
                Blocks.MYCELIUM,
                Blocks.PODZOL,
                Blocks.DIRT,
                Blocks.COARSE_DIRT,
                Blocks.ROOTED_DIRT,
                Blocks.SNOW_BLOCK,
                Blocks.GRASS_BLOCK,
                NMLBlocks.SILT.get()
        );

        //Paths
        if ((pathableBlocks.contains(state.getBlock()) || (state.is(Blocks.GRASS_BLOCK) && state.getValue(SNOWY))) && event.getFace() != Direction.DOWN && stack.getItem().canPerformAction(stack, ItemAbilities.SHOVEL_FLATTEN) && !player.isSpectator() && (level.isEmptyBlock(pos.above()) || level.getBlockState(pos.above()).canBeReplaced())) {
            if (state.is(BlockTags.SAND))
                level.playSound(player, pos, SoundEvents.SAND_FALL, SoundSource.BLOCKS, 1, 1);
            else if (state.is(Blocks.GRAVEL))
                level.playSound(player, pos, SoundEvents.GRAVEL_FALL, SoundSource.BLOCKS, 1, 1);
            else if (((state.is(Blocks.DIRT) || state.is(Blocks.COARSE_DIRT) || state.is(Blocks.ROOTED_DIRT) || state.is(Blocks.GRASS_BLOCK)) && level.getBlockState(pos.above()).is(Blocks.SNOW)) || state.is(Blocks.SNOW))
                level.playSound(player, pos, SoundEvents.SNOW_BREAK, SoundSource.BLOCKS, 1, 1);
            else
                level.playSound(player, pos, SoundEvents.SHOVEL_FLATTEN, SoundSource.BLOCKS, 1, 1);

            if (!level.isClientSide) {
                stack.hurtAndBreak(1, player, stack.getEquipmentSlot());

                BlockState pathState = ImmutableMap.ofEntries(
                        Map.entry(Blocks.GRAVEL, NMLBlocks.GRAVEL_PATH),
                        Map.entry(Blocks.SAND, NMLBlocks.SAND_PATH),
                        Map.entry(Blocks.RED_SAND, NMLBlocks.RED_SAND_PATH),
                        Map.entry(Blocks.GRASS_BLOCK, Blocks.DIRT_PATH.defaultBlockState().getBlockHolder()),
                        Map.entry(Blocks.MYCELIUM, NMLBlocks.MYCELIUM_PATH),
                        Map.entry(Blocks.PODZOL, NMLBlocks.PODZOL_PATH),
                        Map.entry(Blocks.DIRT, NMLBlocks.DIRT_PATH),
                        Map.entry(Blocks.COARSE_DIRT, NMLBlocks.DIRT_PATH),
                        Map.entry(Blocks.ROOTED_DIRT, NMLBlocks.DIRT_PATH),
                        Map.entry(Blocks.SNOW_BLOCK, NMLBlocks.SNOW_PATH),
                        Map.entry(NMLBlocks.SILT.get(), NMLBlocks.SILT_PATH)
                ).get(state.getBlock()).value().defaultBlockState();

                BlockState flattened = (state.is(Blocks.DIRT) || state.is(Blocks.COARSE_DIRT) || state.is(Blocks.ROOTED_DIRT) || state.is(Blocks.GRASS_BLOCK)) && level.getBlockState(pos.above()).is(Blocks.SNOW)
                        ? NMLBlocks.SNOWY_GRASS_PATH.get().defaultBlockState() : pathState;
                level.setBlockAndUpdate(pos, flattened);
                level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, flattened));
            }

            event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide()));
            event.setCanceled(true);
        }

        //Dirt Path into Farmland
        if (stack.getItem().canPerformAction(stack, ItemAbilities.HOE_TILL) && (state.is(NMLBlocks.DIRT_PATH.get()) || state.is(Blocks.PODZOL)) && !player.isSpectator() && level.isEmptyBlock(pos.above())) {
            level.playSound(player, pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);

            if (!level.isClientSide()) {
                stack.hurtAndBreak(1, player, stack.getEquipmentSlot());

                level.setBlockAndUpdate(pos, state.is(Blocks.PODZOL) ? Blocks.DIRT.defaultBlockState() : Blocks.FARMLAND.defaultBlockState());
            }

            event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide()));
            event.setCanceled(true);
        }

        // Farmland
        if ((state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT)) && stack.getItem().canPerformAction(stack, ItemAbilities.HOE_TILL)) {
            level.playSound(player, pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1, 1);

            if (!level.isClientSide()) {
                stack.hurtAndBreak(1, player, stack.getEquipmentSlot());

                level.setBlockAndUpdate(pos, Blocks.FARMLAND.defaultBlockState());
                if (level.getBlockState(pos.above()).canBeReplaced()) level.destroyBlock(pos.above(), false);
            }

            event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide()));
            event.setCanceled(true);
        }

        //Farmland untilling
        if (event.getFace() != Direction.DOWN && stack.getItem().canPerformAction(stack, ItemAbilities.SHOVEL_FLATTEN) && state.is(Blocks.FARMLAND) && !player.isSpectator() && level.isEmptyBlock(pos.above())) {
            level.playSound(player, pos, SoundEvents.SHOVEL_FLATTEN, SoundSource.BLOCKS, 1.0F, 1.0F);

            if (!level.isClientSide()) {
                stack.hurtAndBreak(1, player, stack.getEquipmentSlot());

                level.setBlockAndUpdate(pos, Blocks.DIRT.defaultBlockState());
            }

            event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide()));
            event.setCanceled(true);

        }
    }
}

