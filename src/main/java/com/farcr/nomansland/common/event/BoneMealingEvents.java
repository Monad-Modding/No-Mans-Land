package com.farcr.nomansland.common.event;

import net.minecraft.world.level.gameevent.GameEvent;
import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SpreadingSnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = NoMansLand.MODID)
public class BoneMealingEvents {

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();

        //Sugarcane Cutting
        if (event.getFace() != Direction.DOWN && stack.getItem().canPerformAction(stack, ItemAbilities.SHEARS_TRIM) && state.is(Blocks.SUGAR_CANE) && !player.isSpectator()) {
            level.playSound(player, pos, SoundEvents.SHEEP_SHEAR, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (!level.isClientSide()) {
                stack.hurtAndBreak(1, player, stack.getEquipmentSlot());

                level.setBlockAndUpdate(pos, NMLBlocks.CUT_SUGAR_CANE.get().defaultBlockState());
                level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, NMLBlocks.CUT_SUGAR_CANE.get().defaultBlockState()));
            }
            event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide()));
            event.setCanceled(true);

        }

        //Vine Cutting
        if (stack.getItem().canPerformAction(stack, ItemAbilities.SHEARS_TRIM) && state.is(Blocks.VINE) && !player.isSpectator()) {
            level.playSound(player, pos, SoundEvents.SHEEP_SHEAR, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (!level.isClientSide()) {
                stack.hurtAndBreak(1, player, stack.getEquipmentSlot());

                BlockState cut = NMLBlocks.CUT_VINE.get().withPropertiesOf(state);
                level.setBlockAndUpdate(pos, cut);
                level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, cut));
            }
            event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide()));
            event.setCanceled(true);

        }

        //Bone-Mealing
        if (stack.is(Items.BONE_MEAL) && !player.isSpectator()) {
            // Bonemealing dirt
            if (state.is(Blocks.DIRT) && !level.getBlockState(pos.above()).isSolid()) {
                // Ensure the dirt that is being right-clicked has a suitable block such as grass nearby
                for (Direction d : Direction.values()) {
                    for (Direction d1 : Direction.values()) {
                        if (d == d1) break;
                        BlockPos newBlockPos = pos.relative(d);
                        if (level.getBlockState(newBlockPos).getBlock() instanceof SpreadingSnowyDirtBlock) {
                            if (!player.isCreative()) stack.shrink(1);
                            bonemealDirt(level, pos, level.getBlockState(newBlockPos));
                            level.playSound(player, pos, SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS, 1F, 1F);
                            if (!level.isClientSide()) player.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
                            event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide()));
                            event.setCanceled(true);
                            return;
                        } else if (level.getBlockState(newBlockPos.relative(d1)).getBlock() instanceof SpreadingSnowyDirtBlock) {
                            if (!player.isCreative()) stack.shrink(1);
                            bonemealDirt(level, pos, level.getBlockState(newBlockPos.relative(d1)));
                            level.playSound(player, pos, SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS, 1F, 1F);
                            if (!level.isClientSide()) player.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
                            event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide()));
                            event.setCanceled(true);
                            return;
                        }
                    }
                }
            }
        }
    }

    public static void bonemealDirt(Level level, BlockPos pos, BlockState state) {
        if (!level.isClientSide) level.setBlockAndUpdate(pos, state);

        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        // Iterate through a cube and find suitable dirt blocks that can be turned into the new block
        for (BlockPos bp : BlockPos.betweenClosed(x - 3, y - 1, z - 3, x + 3, y + 2, z + 3)) {
            BlockState block = level.getBlockState(bp);
            for (Direction d : Direction.values()) {
                Block newBlock = level.getBlockState(bp.relative(d)).getBlock();
                if (level.random.nextFloat() < .3F && block == Blocks.DIRT.defaultBlockState() && newBlock instanceof SpreadingSnowyDirtBlock && !level.getBlockState(bp.above()).isSolid()) {
                    BlockPos particlePosition = bp.above();
                    if (!level.isClientSide) level.setBlockAndUpdate(bp, state);
                    spawnParticles(level, particlePosition);
                }
            }
        }
    }

    public static void spawnParticles(Level level, BlockPos pos) {
        for (int i = 0; i <= 3; i++) {
            level.addParticle(ParticleTypes.COMPOSTER,
                    pos.getX() + level.random.nextFloat() - level.random.nextFloat(),
                    pos.getY() + 0.2 + level.random.nextFloat() - level.random.nextFloat(),
                    pos.getZ() + level.random.nextFloat() - level.random.nextFloat(),
                    0, 0, 0);
        }
    }
}
