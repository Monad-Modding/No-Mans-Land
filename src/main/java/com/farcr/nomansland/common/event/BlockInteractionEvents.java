package com.farcr.nomansland.common.event;

import com.farcr.nomansland.NMLConfig;
import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.block.torches.ExtinguishableBlockPairing;
import com.farcr.nomansland.common.integration.Mods;
import com.farcr.nomansland.common.registry.*;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import static com.farcr.nomansland.common.block.FrostedGrassBlock.SNOWLOGGED;
import static net.minecraft.world.level.block.SnowyDirtBlock.SNOWY;

@EventBusSubscriber(modid = NoMansLand.MODID)
public class BlockInteractionEvents {
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();

        lightAndExtinguishTorches(event, level, pos, state, player, stack);
        frostGrass(event, level, pos, state, player, stack);
        placeLadderColumn(event, level, pos, state, player, stack);
        placeRailLine(event, level, pos, state, player, stack);
    }

    private static void lightAndExtinguishTorches(PlayerInteractEvent.RightClickBlock event, Level level, BlockPos pos, BlockState state, Player player, ItemStack stack) {
        boolean isExtinguishing = stack.getItem().canPerformAction(stack, ItemAbilities.SHOVEL_DOUSE) && NMLConfig.TORCH_EXTINGUISHING.get();
        boolean isLighting = stack.is(NMLTags.FIRESTARTERS) || stack.getItem().canPerformAction(stack, ItemAbilities.FIRESTARTER_LIGHT);
        if (!player.isSpectator() && (isExtinguishing || isLighting)) {
            for (ExtinguishableBlockPairing pair : NMLRegistries.EXTINGUISHABLE_BLOCKS) {
                if (isExtinguishing) { //extinguishing block
                    if (pair.isLitVersion(state)) {
                        level.playSound(player, pos, NMLSounds.TORCH_EXTINGUISH.get(), SoundSource.BLOCKS, 0.4F, 1.0F);
                        level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                        level.setBlockAndUpdate(pos, pair.extinguishedBlock().withPropertiesOf(state));
                        event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide()));
                        event.setCanceled(true);
                        break;
                    }
                } else { //lighting block
                    if (pair.isExtinguishedVersion(state)) {
                        level.playSound(player, pos, stack.is(Items.FLINT_AND_STEEL) ? NMLSounds.TORCH_LIGHT_BY_FLINT_AND_STEEL.get() : NMLSounds.TORCH_LIGHT.get(), SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);
                        level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                        level.setBlockAndUpdate(pos, pair.litBlock().withPropertiesOf(state));
                        event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide()));
                        event.setCanceled(true);
                        break;
                    }
                }
            }

            // Lighting vanilla candles, candle cakes, and campfires with firestarters
            if (isLighting && !event.isCanceled()
                    && state.hasProperty(BlockStateProperties.LIT)
                    && !state.getValue(BlockStateProperties.LIT)
                    && (state.is(BlockTags.CANDLES) || state.is(BlockTags.CANDLE_CAKES) || state.is(BlockTags.CAMPFIRES))) {
                level.playSound(player, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);
                level.setBlock(pos, state.setValue(BlockStateProperties.LIT, Boolean.TRUE), 11);
                level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide()));
                event.setCanceled(true);
            }
        }
    }

    private static void frostGrass(PlayerInteractEvent.RightClickBlock event, Level level, BlockPos pos, BlockState state, Player player, ItemStack stack) {
        // Grass Frosting
        if (stack.is(Blocks.SNOW.asItem()) && !player.isSpectator() && state.is(Blocks.SHORT_GRASS) && !Mods.SNOWREALMAGIC.isLoaded()) {
            level.setBlockAndUpdate(pos, NMLBlocks.FROSTED_GRASS.get().defaultBlockState().setValue(SNOWLOGGED, true));
            stack.consume(1, player);
            level.playSound(player, pos, SoundEvents.SNOW_PLACE, SoundSource.PLAYERS, 1, (level.random.nextFloat() - level.random.nextFloat()) * 0.6F + 1.2F);
            BlockPos posUnder = pos.below();
            BlockState stateUnder = level.getBlockState(posUnder);
            if (stateUnder.getBlock() instanceof SnowyDirtBlock)
                level.setBlockAndUpdate(posUnder, stateUnder.setValue(SNOWY, true));
            event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide()));
            event.setCanceled(true);
        }
    }

    private static void placeLadderColumn(PlayerInteractEvent.RightClickBlock event, Level level, BlockPos pos, BlockState state, Player player, ItemStack stack) {
        // Ladder Placement
        if (stack.is(Items.LADDER) && state.is(Blocks.LADDER) && !player.isSpectator() && !player.isCrouching() && !player.isFakePlayer()) {
            Direction ladderFacing = state.getValue(LadderBlock.FACING);
            if (ladderFacing == event.getFace()) {
                BlockPos.MutableBlockPos mutable = pos.below().mutable();
                for (int i = 0; i < NMLConfig.MAX_LADDER_PLACEMENT_LENGTH.get(); i++) {
                    BlockState state2 = level.getBlockState(mutable);
                    if (state2.is(BlockTags.REPLACEABLE)) {
                        if (state.canSurvive(level, mutable)) {
                            SoundType soundtype = state.getSoundType(level, pos, player);
                            level.playSound(player, mutable, soundtype.getPlaceSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                            stack.consume(1, player);
                            BlockState ladderState = Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, ladderFacing);
                            level.setBlockAndUpdate(mutable, ladderState);
                            level.gameEvent(GameEvent.BLOCK_PLACE, pos, GameEvent.Context.of(player, ladderState));
                            event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide()));
                            event.setCanceled(true);
                            break;
                        }
                    } else if (!state2.is(Blocks.LADDER)) {
                        break;
                    }
                    mutable.move(Direction.DOWN);
                }
            }
        }
    }

    private static void placeRailLine(PlayerInteractEvent.RightClickBlock event, Level level, BlockPos pos, BlockState state, Player player, ItemStack stack) {
        // Rail Placement
        if (stack.is(ItemTags.RAILS) && state.is(BlockTags.RAILS) && !player.isSpectator() && !player.isCrouching() && !player.isFakePlayer()) {
            Direction playerDir = player.getDirection();
            RailShape railShape = null;
            if (state.getBlock() instanceof BaseRailBlock) {
                railShape = state.getValue(((BaseRailBlock) state.getBlock()).getShapeProperty());
            }
            if (railShape != null) {
                int railCount = 0;
                for (Direction direction : Direction.Plane.HORIZONTAL) {
                    if (level.getBlockState(pos.relative(direction)).is(BlockTags.RAILS))
                        railCount++;
                }
                RailShape placedShape;
                BlockPos.MutableBlockPos mutable = pos.mutable();
                // Iterate through the rails to find the end of a connected rail segment
                for (int i = 0; i <= NMLConfig.MAX_RAIL_PLACMENT_LENGTH.get(); i++) {
                    // A load of blockpos + blockstates used lower down
                    BlockPos m = mutable.immutable();
                    BlockPos mBelow = m.below();
                    BlockPos mAbove = m.above();
                    BlockPos mBelow2 = mBelow.below();
                    BlockPos mSlopeBase = mBelow.relative(playerDir.getOpposite());
                    BlockState stateBase = level.getBlockState(m);
                    BlockState stateBelow = level.getBlockState(mBelow);
                    BlockState stateAbove = level.getBlockState(mAbove);
                    BlockState stateBelow2 = level.getBlockState(mBelow2);
                    BlockState stateSlopeBase = level.getBlockState(mSlopeBase);
                    if (stateBase.is(BlockTags.RAILS)) {
                        // Continue along the chain normally
                        RailShape offsetShape = null;
                        if (stateBase.getBlock() instanceof BaseRailBlock) {
                            offsetShape = stateBase.getValue(((BaseRailBlock) stateBase.getBlock()).getShapeProperty());
                        }

                        if (offsetShape == null)
                            break;

                        // The big if chain
                        // Straights are gone because of woke
                        // Curves
                        if (offsetShape == RailShape.NORTH_EAST) {
                            if (playerDir == Direction.SOUTH) playerDir = Direction.EAST;
                            else if (playerDir == Direction.WEST) playerDir = Direction.NORTH;
                        } else if (offsetShape == RailShape.NORTH_WEST) {
                            if (playerDir == Direction.SOUTH) playerDir = Direction.WEST;
                            else if (playerDir == Direction.EAST) playerDir = Direction.NORTH;
                        } else if (offsetShape == RailShape.SOUTH_EAST) {
                            if (playerDir == Direction.NORTH) playerDir = Direction.EAST;
                            else if (playerDir == Direction.WEST) playerDir = Direction.SOUTH;
                        } else if (offsetShape == RailShape.SOUTH_WEST) {
                            if (playerDir == Direction.NORTH) playerDir = Direction.WEST;
                            else if (playerDir == Direction.EAST) playerDir = Direction.SOUTH;
                        }
                        // Ramps - this doesn't handle ramps down since they're at a different y level
                        else if (offsetShape == RailShape.ASCENDING_NORTH) {
                            if (playerDir == Direction.NORTH) mutable.move(Direction.UP);
                            else if (playerDir != Direction.SOUTH) break;
                        } else if (offsetShape == RailShape.ASCENDING_SOUTH) {
                            if (playerDir == Direction.SOUTH) mutable.move(Direction.UP);
                            else if (playerDir != Direction.NORTH) break;
                        } else if (offsetShape == RailShape.ASCENDING_EAST) {
                            if (playerDir == Direction.EAST) mutable.move(Direction.UP);
                            else if (playerDir != Direction.WEST) break;
                        } else if (offsetShape == RailShape.ASCENDING_WEST) {
                            if (playerDir == Direction.WEST) mutable.move(Direction.UP);
                            else if (playerDir != Direction.EAST) break;
                        }
                        // Edge case
                        else if (offsetShape != RailShape.EAST_WEST && offsetShape != RailShape.NORTH_SOUTH) {
                            break;
                        }

                        mutable.move(playerDir);
                    } else if (stateBelow.is(BlockTags.RAILS)) {
                        // If we've got rails below, we've likely got a slope and should continue on the chain there
                        // If it's not connected via a slope there'll be special handling to allow us to chain rails down slopes
                        boolean canGoDown = false;
                        RailShape offsetShape = null;
                        if (stateBelow.getBlock() instanceof BaseRailBlock) {
                            offsetShape = stateBelow.getValue(((BaseRailBlock) stateBelow.getBlock()).getShapeProperty());
                        }

                        if (offsetShape == null) break;

                        if (offsetShape == RailShape.ASCENDING_NORTH) {
                            if (playerDir == Direction.SOUTH) {
                                mutable.move(Direction.DOWN);
                                canGoDown = true;
                            } else if (playerDir != Direction.NORTH) break;
                        } else if (offsetShape == RailShape.ASCENDING_SOUTH) {
                            if (playerDir == Direction.NORTH) {
                                mutable.move(Direction.DOWN);
                                canGoDown = true;
                            } else if (playerDir != Direction.SOUTH) break;
                        } else if (offsetShape == RailShape.ASCENDING_EAST) {
                            if (playerDir == Direction.WEST) {
                                mutable.move(Direction.DOWN);
                                canGoDown = true;
                            } else if (playerDir != Direction.EAST) break;
                        } else if (offsetShape == RailShape.ASCENDING_WEST) {
                            if (playerDir == Direction.EAST) {
                                mutable.move(Direction.DOWN);
                                canGoDown = true;
                            } else if (playerDir != Direction.WEST) break;
                        }

                        if (!canGoDown) {
                            // If we don't have a rail below us that we can follow, we just place a rail straight ahead if possible
                            // This works, somehow
                            // Mostly just copied and simplified from the logic further down the main if chain
                            if (stateBase.is(BlockTags.REPLACEABLE)) {
                                if (placeRail(mutable.immutable(), stack, playerDir, level, player)) {
                                    event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide()));
                                    event.setCanceled(true);
                                    break;
                                }
                            } else {
                                // If we can't go down a block and can't place a new rail, give up
                                break;
                            }
                        }
                    } else if (stateBase.isFaceSturdy(level, mutable, Direction.UP, SupportType.RIGID) && stateAbove.is(BlockTags.REPLACEABLE)) {
                        // If we've got a block in front of us with air above, go up the slope
                        if (placeRail(mutable.immutable().above(), stack, playerDir, level, player)) {
                            event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide()));
                            event.setCanceled(true);
                            break;
                        }
                    } else if (stateBelow2.isFaceSturdy(level, mutable.immutable().below(2), Direction.UP, SupportType.RIGID) && stateSlopeBase.isFaceSturdy(level, mutable.immutable().below().relative(playerDir.getOpposite()), playerDir, SupportType.RIGID) && stateBelow.is(BlockTags.REPLACEABLE)) {
                        // If we have support below us for a slope down, and support below where the slope would go, place it
                        if (placeRail(mutable.immutable().below(), stack, playerDir, level, player)) {
                            event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide()));
                            event.setCanceled(true);
                            break;
                        }
                    } else if (stateBase.is(BlockTags.REPLACEABLE)) {
                        // If we're at an empty space and nothing else fits, just plop down a rail
                        if (placeRail(mutable.immutable(), stack, playerDir, level, player)) {
                            event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide()));
                            event.setCanceled(true);
                            break;
                        }
                    } else {
                        // No way to place a rail, give up
                        break;
                    }
                }
            }
        }
    }

    private static boolean placeRail(BlockPos position, ItemStack stack, Direction playerDir, Level level, Player player) {
        BlockState state = ((BlockItem) stack.getItem()).getBlock().defaultBlockState();
        RailShape placedShape = switch (playerDir) {
            case Direction.NORTH, Direction.SOUTH -> RailShape.NORTH_SOUTH;
            case Direction.EAST, Direction.WEST -> RailShape.EAST_WEST;
            default -> null;
        };
        if (state.getBlock() instanceof BaseRailBlock) {
            state = state.setValue(((BaseRailBlock) state.getBlock()).getShapeProperty(), placedShape);
        }
        if (state.canSurvive(level, position)) {
            SoundType soundtype = state.getSoundType(level, position, player);
            level.playSound(player, position, soundtype.getPlaceSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
            stack.consume(1, player);
            level.setBlockAndUpdate(position, state);
            return true;
        }
        return false;
    }
}
