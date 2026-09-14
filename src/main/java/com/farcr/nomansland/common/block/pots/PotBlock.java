package com.farcr.nomansland.common.block.pots;

import com.farcr.nomansland.common.blockentity.PotBlockEntity;
import com.farcr.nomansland.common.entity.FallingPotEntity;
import com.farcr.nomansland.common.extension.LivingEntityExtension;
import com.farcr.nomansland.common.item.AncientPotDebugItem;
import com.farcr.nomansland.common.item.AncientPotItem;
import com.farcr.nomansland.common.registry.NMLRegistries;
import com.farcr.nomansland.common.registry.items.NMLItems;
import com.farcr.nomansland.common.world.saved_data.RegeneratingPotsData;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.FastColor;
import net.minecraft.util.RandomSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.ThrowablePotionItem;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.event.EventHooks;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

public class PotBlock extends BaseEntityBlock implements SimpleWaterloggedBlock, Fallable {

    private static final DirectionProperty HORIZONTAL_FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private static final BooleanProperty BRITTLE = BooleanProperty.create("brittle");
    private static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    protected final PotSize size;

    public PotBlock(PotSize size, Properties properties) {
        super(properties);
        this.size = size;
        registerDefaultState(stateDefinition.any().setValue(HORIZONTAL_FACING, Direction.NORTH).setValue(WATERLOGGED, false).setValue(BRITTLE, false).setValue(POWERED, false));
    }

    public static final MapCodec<PotBlock> CODEC = RecordCodecBuilder.mapCodec(
            (instance) -> instance.group(
                    PotSize.CODEC.fieldOf("size").forGetter(p -> p.size),
                    propertiesCodec()
            ).apply(instance, PotBlock::new)
    );

    public MapCodec<? extends PotBlock> codec() {
        return CODEC;
    }

    public PotSize getSize() {
        return size;
    }

    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        if (!state.getValue(POWERED)) {
            level.scheduleTick(pos, this, this.getDelayAfterPlace());
        }

        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluidstate = context.getLevel().getFluidState(context.getClickedPos());
        return defaultBlockState().setValue(HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite()).setValue(WATERLOGGED, fluidstate.getType() == Fluids.WATER);
    }

    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(POWERED)) {
            level.setBlock(pos, state.setValue(POWERED, false), 2);
            level.updateNeighborsAt(pos, this);
        }

        if (isFree(level.getBlockState(pos.below())) && pos.getY() >= level.getMinBuildHeight()) {
            if (level.getBlockEntity(pos) instanceof PotBlockEntity pot && pot.isLiving()) {
                pot.wakeUpSilent();
                return;
            }
            CompoundTag beData = null;
            ResourceLocation variantId = null;
            if (level.getBlockEntity(pos) instanceof PotBlockEntity pot) {
                beData = pot.saveCustomOnly(level.registryAccess());
                if (pot.variant != null) {
                    variantId = level.registryAccess().registryOrThrow(NMLRegistries.POT_VARIANT_KEY).getKey(pot.variant);
                }
                pot.skipBreakEffects = true;
            }
            FallingPotEntity falling = FallingPotEntity.fall(level, pos, state);
            if (beData != null) {
                falling.blockData = beData;
            }
            if (variantId != null) {
                falling.setVariantId(variantId);
            }
        }
    }

    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    protected int getDirectSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
        return blockState.getSignal(blockAccess, pos, side);
    }

    protected int getSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
        return blockState.getValue(POWERED) ? 15 : 0;
    }

    private void startSignal(Level level, BlockPos pos) {
        if (!level.isClientSide()) {
            BlockState state = level.getBlockState(pos);
            if (!state.getValue(POWERED)) {
                level.setBlock(pos, state.setValue(POWERED, true), 2);
                level.updateNeighborsAt(pos, this);
                level.scheduleTick(pos, this, 4);
            }
        }
        spawnRedstoneParticles(level, pos);
    }

    protected void spawnRedstoneParticles(Level level, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel) {
            double height = size == PotSize.LARGE ? 1.5 : 0.5;
            for (int i = 0; i < (size == PotSize.LARGE ? 10 : 6); i++) {
                double x = pos.getX() + 0.25 + level.getRandom().nextDouble() * 0.5;
                double y = pos.getY() + level.getRandom().nextDouble() * height;
                double z = pos.getZ() + 0.25 + level.getRandom().nextDouble() * 0.5;
                serverLevel.sendParticles(DustParticleOptions.REDSTONE, x, y, z, 1, 0, 0, 0, 0);
            }
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level instanceof ServerLevel serverLevel)) return ItemInteractionResult.CONSUME;

        if (!(level.getBlockEntity(pos) instanceof PotBlockEntity pot) || pot.variant == null) {
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }

        if (stack.is(NMLItems.POTMASTER_DEBUG_STICK.get())) {
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }

        if (stack.is(Items.BRUSH)) {
            return tryInsert(stack, serverLevel, pos, player, pot, ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION);
        }

        if (pot.hasModifier(PotModifier.TRAPPED)) {
            startSignal(level, pos);
        }

        if (pot.isLiving()) {
            pot.wakeUp(null);
            return ItemInteractionResult.SUCCESS;
        }

        if (pot.hasModifier(PotModifier.INFESTED)) {
            int amount = level.getRandom().nextInt(1, 4);
            double spawnY = pos.getY() + (size == PotSize.LARGE ? 2 : 1);
            DifficultyInstance difficulty = serverLevel.getCurrentDifficultyAt(pos);
            for (int i = 0; i < amount; i++) {
                spawnPotSilverfish(serverLevel, pos.getX() + 0.5, spawnY, pos.getZ() + 0.5, 0, difficulty);
            }
            pot.removeModifier(PotModifier.INFESTED);
        }

        if (pot.hasModifier(PotModifier.OOZING)) {
            int amount = level.getRandom().nextInt(1, 4);
            double spawnY = pos.getY() + (size == PotSize.LARGE ? 2 : 1);
            DifficultyInstance difficulty = serverLevel.getCurrentDifficultyAt(pos);
            for (int i = 0; i < amount; i++) {
                spawnPotSlime(serverLevel, pos.getX() + 0.5, spawnY, pos.getZ() + 0.5, 0, 1, difficulty);
            }
            pot.removeModifier(PotModifier.OOZING);
        }

        if (stack.isEmpty() || stack.getItem() instanceof AncientPotItem) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (stack.is(Items.HONEYCOMB) && !pot.hasModifier(PotModifier.WAXED) && player.isShiftKeyDown()) {
            pot.addModifier(PotModifier.WAXED);
            pot.wobble(DecoratedPotBlockEntity.WobbleStyle.POSITIVE);
            player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
            stack.shrink(1);
            level.playSound(null, pos, SoundEvents.HONEYCOMB_WAX_ON, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.levelEvent(3003, pos, 0);
            pot.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
            level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
            return ItemInteractionResult.SUCCESS;
        }

        if (stack.getItem() instanceof PotionItem && !(stack.getItem() instanceof ThrowablePotionItem) && pot.getStoredPotion().equals(PotionContents.EMPTY) && player.isShiftKeyDown()) {
            PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
            pot.setStoredPotion(contents);
            pot.wobble(DecoratedPotBlockEntity.WobbleStyle.POSITIVE);
            player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
            stack.shrink(1);
            if (!player.getAbilities().instabuild) {
                player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE));
            }
            level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
            int color = contents.getColor();
            ParticleOptions particle = ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, FastColor.ARGB32.color(255, color));
            serverLevel.sendParticles(particle, pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5, 10, 0.2, 0.1, 0.2, 0);
            pot.setChanged();
            level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
            return ItemInteractionResult.SUCCESS;
        }

        return tryInsert(stack, serverLevel, pos, player, pot, ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION);
    }

    private ItemInteractionResult tryInsert(ItemStack stack, ServerLevel level, BlockPos pos, Player player, PotBlockEntity pot, ItemInteractionResult onFailure) {
        if (!pot.insert(stack.copyWithCount(1))) {
            return onFailure;
        }
        pot.wobble(DecoratedPotBlockEntity.WobbleStyle.POSITIVE);
        player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
        stack.shrink(1);
        level.playSound(null, pos, SoundEvents.DECORATED_POT_INSERT, SoundSource.BLOCKS, 1, 0.7F + 0.5F * pot.getFullness());
        level.sendParticles(ParticleTypes.DUST_PLUME, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, 7, 0, 0, 0, 0);
        pot.setChanged();
        level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof PotBlockEntity pot)) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        level.playSound(null, pos, SoundEvents.DECORATED_POT_INSERT_FAIL, SoundSource.BLOCKS, 1, 1);
        pot.wobble(DecoratedPotBlockEntity.WobbleStyle.NEGATIVE);

        if (pot.isLiving()) {
            pot.wakeUp(null);
        }

        return InteractionResult.SUCCESS;
    }

    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }

    @Override
    protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1;
    }

    private static final VoxelShape SMALL_FALLBACK = Shapes.box(2.0 / 16, 0, 2.0 / 16, 14.0 / 16, 1, 14.0 / 16);
    private static final double COLLISION_INSET = 0.02;
    private static final Map<VoxelShape, VoxelShape> COLLISION_SHAPE_CACHE = new ConcurrentHashMap<>();

    public static VoxelShape variantShapeOf(@Nullable PotBlockEntity pot, VoxelShape fallback) {
        if (pot == null || pot.variant == null) return fallback;
        VoxelShape variantShape = pot.variant.shape();
        return variantShape != null && !variantShape.isEmpty() ? variantShape : fallback;
    }

    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return variantShapeOf(level.getBlockEntity(pos) instanceof PotBlockEntity pot ? pot : null, SMALL_FALLBACK);
    }

    public static VoxelShape collisionShapeOf(VoxelShape shape) {
        if (shape.isEmpty()) return shape;
        return COLLISION_SHAPE_CACHE.computeIfAbsent(shape, PotBlock::insetFromBlockBounds);
    }

    private static VoxelShape insetFromBlockBounds(VoxelShape shape) {
        VoxelShape[] holder = { Shapes.empty() };
        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> holder[0] = Shapes.or(holder[0], Shapes.box(
                minX <= 0 ? minX + COLLISION_INSET : minX,
                minY <= 0 ? minY + COLLISION_INSET : minY,
                minZ <= 0 ? minZ + COLLISION_INSET : minZ,
                maxX >= 1 ? maxX - COLLISION_INSET : maxX,
                maxY >= 1 ? maxY - COLLISION_INSET : maxY,
                maxZ >= 1 ? maxZ - COLLISION_INSET : maxZ)));
        return holder[0].isEmpty() ? shape : holder[0];
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return collisionShapeOf(getShape(state, level, pos, context));
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HORIZONTAL_FACING, BRITTLE, POWERED, WATERLOGGED);
    }

    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PotBlockEntity(pos, state);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (level.getBlockEntity(pos) instanceof PotBlockEntity pot) {
            if (pot.variant == null) {
                pot.ensureVariant(size);
            } else if (state.getValue(BRITTLE) != pot.variant.traits().contains(PotTrait.BRITTLE)) state.setValue(BRITTLE, pot.variant.traits().contains(PotTrait.BRITTLE));
        }

        level.scheduleTick(pos, this, this.getDelayAfterPlace());
    }

    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof PotBlockEntity pot && pot.variant != null) {
            if (pot.shouldDropItems) {
                Containers.dropContents(level, pos, pot);
            }

            if (pot.skipBreakEffects) {
                if (pot.hasModifier(PotModifier.WAXED)) {
                    pot.removeModifier(PotModifier.WAXED);
                    pot.setChanged();
                    Block.popResource(level, pos, pot.getPotAsItem());
                    Block.popResource(level, pos, new ItemStack(Items.HONEYCOMB));
                }
            } else {
                if (!level.isClientSide) {
                    applyBreakEffects((ServerLevel) level, pos.getCenter(),
                            state, pot.variant, pot.getModifiers(),
                            pot.getStoredPotion(), size == PotSize.LARGE, !pot.preventRegen);
                }
                level.updateNeighbourForOutputSignal(pos, state.getBlock());
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    public static void applyBreakEffects(
            ServerLevel level, Vec3 pos,
            BlockState state, PotVariant variant,
            Set<PotModifier> modifiers, PotionContents storedPotion,
            boolean isLarge, boolean canRegenerate
    ) {
        RandomSource random = level.getRandom();
        BlockPos blockPos = BlockPos.containing(pos);
        DifficultyInstance difficulty = level.getCurrentDifficultyAt(blockPos);
        double x = pos.x;
        double y = pos.y;
        double z = pos.z;

        if (modifiers.contains(PotModifier.INFESTED)) {
            int count = random.nextInt(2, 4);
            for (int i = 0; i < count; i++) {
                spawnPotSilverfish(level, x + (random.nextDouble() - 0.5) * 0.5, y, z + (random.nextDouble() - 0.5) * 0.5, random.nextFloat() * 360, difficulty);
            }
            modifiers.remove(PotModifier.INFESTED);
        }

        if (modifiers.contains(PotModifier.OOZING)) {
            int count = random.nextInt(2, 4);
            for (int i = 0; i < count; i++) {
                spawnPotSlime(level, x + (random.nextDouble() - 0.5) * 0.5, y, z + (random.nextDouble() - 0.5) * 0.5, random.nextFloat() * 360, random.nextInt(1, 3), difficulty);
            }
            modifiers.remove(PotModifier.OOZING);
        }

        modifiers.remove(PotModifier.TRAPPED);

        if (!storedPotion.equals(PotionContents.EMPTY)) {
            spawnPotionCloud(level, blockPos, storedPotion);
        }

        if (canRegenerate && variant != null && variant.traits().contains(PotTrait.REGENERATES)) {
            ResourceLocation variantKey = level.registryAccess().registryOrThrow(NMLRegistries.POT_VARIANT_KEY).getKey(variant);
            if (variantKey != null) {
                int delay = random.nextInt(20, 40) * 20;
                RegeneratingPotsData.getOrDefault(level).addPot(blockPos, new PotData(state, variantKey, modifiers), delay);
                level.sendParticles(
                        new PotShatterParticleOption(variant.model(), delay, blockPos),
                        x, y + (isLarge ? 0.8 : 0.5), z,
                        isLarge ? 135 : 67, isLarge ? 0.4 : 0.25, 0.3, isLarge ? 0.4 : 0.25, 0.1);
            }
        }
    }

    private static void spawnPotSilverfish(ServerLevel level, double x, double y, double z, float yRot, DifficultyInstance difficulty) {
        Silverfish silverfish = EntityType.SILVERFISH.create(level);
        if (silverfish != null) {
            silverfish.moveTo(x, y, z, yRot, 0);
            EventHooks.finalizeMobSpawn(silverfish, level, difficulty, MobSpawnType.TRIGGERED, null);
            silverfish.skipDropExperience();
            ((LivingEntityExtension) silverfish).nml$skipDroppingDeathLoot();
            level.addFreshEntity(silverfish);
            silverfish.spawnAnim();
        }
    }

    private static void spawnPotSlime(ServerLevel level, double x, double y, double z, float yRot, int slimeSize, DifficultyInstance difficulty) {
        Slime slime = EntityType.SLIME.create(level);
        if (slime != null) {
            slime.moveTo(x, y, z, yRot, 0);
            EventHooks.finalizeMobSpawn(slime, level, difficulty, MobSpawnType.TRIGGERED, null);
            slime.setSize(slimeSize, true);
            slime.skipDropExperience();
            ((LivingEntityExtension) slime).nml$skipDroppingDeathLoot();
            level.addFreshEntity(slime);
        }
    }

    public static void spawnPotionCloud(ServerLevel level, BlockPos pos, PotionContents contents) {
        AreaEffectCloud cloud = new AreaEffectCloud(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        cloud.setPotionContents(contents);
        cloud.setRadius(3.0F);
        cloud.setRadiusOnUse(-0.5F);
        cloud.setWaitTime(10);
        cloud.setRadiusPerTick(-cloud.getRadius() / (float) cloud.getDuration());
        level.addFreshEntity(cloud);
    }

    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    protected SoundType getSoundType(BlockState state) {
        return state.getValue(BRITTLE) ? SoundType.DECORATED_POT_CRACKED : SoundType.DECORATED_POT;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof PotBlockEntity pot && pot.variant != null) {
            pot.shouldDropItems = true;
            boolean willDropSelf = dropsItself(player.getMainHandItem(), level);
            if (pot.hasModifier(PotModifier.WAXED) || willDropSelf) {
                pot.skipBreakEffects = true;
                pot.setChanged();
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

//    @Override
//    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
//        List<ItemStack> drops = super.getDrops(state, params);
//        BlockEntity be = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
//        if (be instanceof PotBlockEntity pot) {
//            for (ItemStack drop : drops) {
//                if (drop.is(this.asItem())) {
//                    pot.droppedSelf = true;
//                    break;
//                }
//            }
//        }
//        return drops;
//    }

    @Override
    protected float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof PotBlockEntity pot && pot.variant != null && pot.variant.traits().contains(PotTrait.BRITTLE)) {
            return 1;
        }
        float base = super.getDestroyProgress(state, player, level, pos);
        return player.getMainHandItem().canPerformAction(ItemAbilities.PICKAXE_DIG) ? base * 3.0F : base;
    }

    @Override
    public void attack(BlockState state, Level level, BlockPos pos, Player player) {
        if (!level.isClientSide) {
            ItemStack held = player.getMainHandItem();
            if (held.is(NMLItems.POTMASTER_DEBUG_STICK.get()) && held.getItem() instanceof AncientPotDebugItem debug) {
                debug.handleLeftClick(level, pos, player);
                return;
            }
            if (level.getBlockEntity(pos) instanceof PotBlockEntity pot && pot.isLiving()
                    && !hasSilkTouch(held, level) && !held.is(Items.BRUSH)) {
                pot.wakeUp(player);
            }
        }
    }

    private static boolean hasSilkTouch(Player player, Level level) {
        return hasSilkTouch(player.getMainHandItem(), level);
    }

    private static boolean hasSilkTouch(ItemStack tool, Level level) {
        return tool.getEnchantmentLevel(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.SILK_TOUCH)) > 0;
    }

    private static boolean dropsItself(ItemStack tool, Level level) {
        return tool.isEmpty() || tool.is(Items.BRUSH) || hasSilkTouch(tool, level);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof PotBlockEntity pot && pot.isLiving()) {
            pot.wakeUp(null);
        }
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof PotBlockEntity pot && pot.isLiving()) {
            pot.wakeUp(null);
        }
        super.stepOn(level, pos, state, entity);
    }

    @Override
    protected void onExplosionHit(BlockState state, Level level, BlockPos pos, Explosion explosion, BiConsumer<ItemStack, BlockPos> dropConsumer) {
        if (explosion.getBlockInteraction() != Explosion.BlockInteraction.TRIGGER_BLOCK) {
            if (!level.isClientSide && level.getBlockEntity(pos) instanceof PotBlockEntity pot && pot.variant != null) {
                pot.shouldDropItems = true;
                pot.preventRegen = true;
            }
            state.onBlockExploded(level, pos, explosion);
        }
    }

    @Override
    public void onCaughtFire(BlockState state, Level level, BlockPos pos, @Nullable Direction direction, @Nullable LivingEntity igniter) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof PotBlockEntity pot && pot.variant != null) {
            pot.preventRegen = true;
        }
        super.onCaughtFire(state, level, pos, direction, igniter);
    }

    protected void onProjectileHit(Level level, BlockState state, BlockHitResult hit, Projectile projectile) {
        BlockPos blockpos = hit.getBlockPos();
        if (!level.isClientSide && projectile.mayInteract(level, blockpos) && projectile.mayBreak(level)) {
            if (level.getBlockEntity(blockpos) instanceof PotBlockEntity pot && pot.isLiving()) {
                pot.wakeUp(projectile.getOwner() instanceof LivingEntity le ? le : null);
            } else {
                if (level.getBlockEntity(blockpos) instanceof PotBlockEntity pot) {
                    pot.shouldDropItems = true;
                }
                level.destroyBlock(blockpos, false, projectile);
            }
        }
    }

    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof PotBlockEntity pot ? pot.getPotAsItem() : super.getCloneItemStack(level, pos, state);
    }

    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(pos));
    }

    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(HORIZONTAL_FACING, rotation.rotate(state.getValue(HORIZONTAL_FACING)));
    }

    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(HORIZONTAL_FACING)));
    }

    public void onBrokenAfterFall(Level level, BlockPos pos, FallingBlockEntity fallingBlock) {
        if (fallingBlock instanceof FallingPotEntity fpe) {
            fpe.spawnShatterParticles();
        }
    }

    public void onLand(Level level, BlockPos pos, BlockState state, BlockState replaceableState, FallingBlockEntity fallingBlock) {
        double fallen = fallingBlock instanceof FallingPotEntity fpe ? fpe.getFallDistance() : fallingBlock.fallDistance;
        if (fallingBlock.getBlockState().getValue(BRITTLE) || fallen > 4) {
            if (fallingBlock instanceof FallingPotEntity fpe) {
                fpe.spawnShatterParticles();
            }
            if (level.getBlockEntity(pos) instanceof PotBlockEntity pot) {
                pot.variant = null;
            }
            level.destroyBlock(pos, true);
            return;
        }
        if (fallingBlock.blockData != null && level.getBlockEntity(pos) instanceof PotBlockEntity pot) {
            pot.restoreFromFallingBlock(fallingBlock.blockData, level.registryAccess());
            pot.setChanged();
        }
    }

    public int getExpDrop(BlockState state, LevelAccessor level, BlockPos pos, BlockEntity blockEntity, Entity breaker, ItemStack tool) {
        if (blockEntity instanceof PotBlockEntity pot && pot.variant != null && pot.variant.traits().contains(PotTrait.DROPS_EXPERIENCE)) {
            boolean dropsItself = tool.isEmpty()
                    || tool.is(Items.BRUSH)
                    || tool.getEnchantmentLevel(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                        .getOrThrow(Enchantments.SILK_TOUCH)) > 0;
            if (!dropsItself) {
                return level.getRandom().nextInt(2, 6);
            }
        }
        return 0;
    }

    @Override
    public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return level.getBlockEntity(pos) instanceof PotBlockEntity pot && pot.variant != null && pot.variant.traits().contains(PotTrait.FLAMMABLE);
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 5;
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 5;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (level.getBlockEntity(pos) instanceof PotBlockEntity pot) {
            PotionContents potionContents = pot.getStoredPotion();
            if (!potionContents.equals(PotionContents.EMPTY) && random.nextFloat() < 0.15) {
                int i = potionContents.getColor();
                ParticleOptions particle = ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, FastColor.ARGB32.color(80, i));

                double d0 = pos.getX() + 0.5 + random.nextInt(-40, 40) * 0.01;
                double d1 = pos.getY() + random.nextInt(-10, 40) * 0.001 + (size == PotSize.LARGE ? 1.8 : 0.8);
                double d2 = pos.getZ() + 0.5 + random.nextInt(-40, 40) * 0.01;

                level.addAlwaysVisibleParticle(particle, d0, d1, d2, 0, 0, 0);
            }
        }
    }

    protected int getDelayAfterPlace() {
        return 2;
    }

    public static boolean isFree(BlockState state) {
        return state.isAir() || state.is(BlockTags.FIRE) || state.liquid() || state.canBeReplaced();
    }
}
