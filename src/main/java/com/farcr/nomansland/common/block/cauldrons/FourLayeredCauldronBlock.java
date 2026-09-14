package com.farcr.nomansland.common.block.cauldrons;

import com.farcr.nomansland.common.recipe.CauldronInteractionInput;
import com.farcr.nomansland.common.recipe.CauldronInteractionRecipe;
import com.farcr.nomansland.common.registry.NMLRecipeSerializers;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class FourLayeredCauldronBlock extends AbstractCauldronBlock {

    public static final IntegerProperty LEVEL = IntegerProperty.create("level", 1, 4);
    public final @Nullable Holder<ParticleType<?>> particleType;

    public FourLayeredCauldronBlock(@Nullable Holder<ParticleType<?>> particleType) {
        super(Properties.ofFullCopy(Blocks.CAULDRON), CauldronInteraction.EMPTY);
        registerDefaultState(this.stateDefinition.any().setValue(LEVEL, 1));
        this.particleType = particleType;
    }

    public static final MapCodec<FourLayeredCauldronBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BuiltInRegistries.PARTICLE_TYPE.holderByNameCodec().fieldOf("particle_type").forGetter(f -> f.particleType)
    ).apply(instance, FourLayeredCauldronBlock::new));

    @Override
    protected MapCodec<? extends FourLayeredCauldronBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LEVEL);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        boolean interacted = false;
        BlockState newState = null;
        RecipeManager recipes = level.getRecipeManager();

        CauldronInteractionInput input = new CauldronInteractionInput(state, stack);
        Optional<RecipeHolder<CauldronInteractionRecipe>> optional = recipes.getRecipeFor(
                NMLRecipeSerializers.CAULDRON_INTERACTION_RECIPE.get(),
                input,
                level
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
            interacted = true;
            if (!level.isClientSide) {
                level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS);
                int i = state.getValue(LEVEL) - cost;
                newState = i == 0 ? Blocks.CAULDRON.defaultBlockState() : state.setValue(LEVEL, i);
                level.setBlockAndUpdate(pos, newState);
                player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, result));
            }
        }

        if (interacted) {
            if (!level.isClientSide) {
                player.awardStat(Stats.USE_CAULDRON);
                player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
                level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(newState));
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        } else return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public boolean isFull(BlockState blockState) {
        return blockState.getValue(LEVEL) == 4;
    }

    public void raiseFillLevel(BlockState state, Level level, BlockPos pos) {
        BlockState newState = state.setValue(LEVEL, state.getValue(LEVEL) + 1);
        level.setBlockAndUpdate(pos, newState);
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(newState));
    }

    public static void lowerFillLevel(BlockState state, Level level, BlockPos pos) {
        int i = state.getValue(LEVEL) - 1;
        BlockState newState = i == 0 ? Blocks.CAULDRON.defaultBlockState() : state.setValue(LEVEL, i);
        level.setBlockAndUpdate(pos, newState);
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(newState));
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (isEntityInsideContent(state, pos, entity) && entity instanceof ItemEntity itemEntity) {
            RecipeManager recipes = level.getRecipeManager();

            CauldronInteractionInput input = new CauldronInteractionInput(state, itemEntity.getItem());
            Optional<RecipeHolder<CauldronInteractionRecipe>> optional = recipes.getRecipeFor(
                    NMLRecipeSerializers.CAULDRON_INTERACTION_RECIPE.get(),
                    input,
                    level
            );

            Optional<CauldronInteractionRecipe> recipe = optional
                    .map(RecipeHolder::value);

            ItemStack result = recipe
                    .map(r -> r.assemble(input, level.registryAccess()))
                    .orElse(ItemStack.EMPTY);

            int cost = recipe
                    .map(CauldronInteractionRecipe::levelCost)
                    .orElse(1);

            if (!result.isEmpty() && !level.isClientSide()) {
                itemEntity.discard();
                int i = state.getValue(LEVEL) - cost;
                BlockState blockstate = i == 0 ? Blocks.CAULDRON.defaultBlockState() : state.setValue(LEVEL, i);
                level.setBlockAndUpdate(pos, blockstate);

                {
                    ItemEntity newItemEntity = new ItemEntity(level,
                            pos.getX() + .5,
                            pos.getY() + .5,
                            pos.getZ() + .5,
                            result);

                    level.addFreshEntity(newItemEntity);
                }
            }
        }
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        return Blocks.CAULDRON.getCloneItemStack(state, target, level, pos, player);
    }

    @Override
    protected double getContentHeight(BlockState state) {
        return (double) (4 + state.getValue(LEVEL) * 2.75) / 16;
    }
}
