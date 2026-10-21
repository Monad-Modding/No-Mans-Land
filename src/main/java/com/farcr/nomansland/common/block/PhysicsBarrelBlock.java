package com.farcr.nomansland.common.block;

import com.farcr.nomansland.common.blockentity.PhysicsBarrelBlockEntity;
import com.farcr.nomansland.common.entity.barrel.PhysicsBarrel;
import com.farcr.nomansland.common.integration.Mods;
import com.farcr.nomansland.common.integration.sable.BarrelPhysics;
import com.farcr.nomansland.common.registry.NMLBlockEntities;
import com.farcr.nomansland.common.registry.NMLParticleTypes;
import com.farcr.nomansland.common.registry.NMLSounds;
import com.farcr.nomansland.common.registry.NMLTags;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.ItemAbilities;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class PhysicsBarrelBlock extends BaseEntityBlock {
    public static final MapCodec<PhysicsBarrelBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Detonation.CODEC.fieldOf("detonation").forGetter(PhysicsBarrelBlock::getDetonation),
            propertiesCodec()
    ).apply(instance, PhysicsBarrelBlock::new));
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final int MIN_FUSE = 60;
    public static final int FUSE_SPREAD = 41;
    public static final int CHAIN_FUSE = 1;
    public static final double CHAIN_RADIUS = 4.0;
    private static final VoxelShape SHAPE = Shapes.or(Block.box(2, 0, 2, 14, 16, 14), Block.box(1, 2, 1, 15, 14, 15));
    private static final float IGNITE_ON_BREAK_CHANCE = 0.2F;
    private static final float EXPLOSIVE_POWER = 4.0F;
    private static final double BLAST_RADIUS = 4.0;
    private static final float FLAMMABLE_DAMAGE = 10.0F;
    private static final int FLAMMABLE_BURN_SECONDS = 8;
    private static final float TOXIC_DAMAGE = 5.0F;
    private static final int TOXIC_GAS_RADIUS = 3;

    private final Detonation detonation;

    public PhysicsBarrelBlock(Detonation detonation, Properties properties) {
        super(properties);
        this.detonation = detonation;
        this.registerDefaultState(this.stateDefinition.any().setValue(LIT, false));
    }

    public Detonation getDetonation() {
        return this.detonation;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return state.getValue(LIT) ? RenderShape.ENTITYBLOCK_ANIMATED : RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PhysicsBarrelBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return createTickerHelper(type, NMLBlockEntities.PHYSICS_BARREL.get(), PhysicsBarrelBlockEntity::serverTick);
    }

    public static boolean isFirestarter(ItemStack stack) {
        return stack.is(NMLTags.FIRESTARTERS) || stack.getItem().canPerformAction(stack, ItemAbilities.FIRESTARTER_LIGHT);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (state.getValue(LIT) || !isFirestarter(stack)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        level.playSound(player, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);
        if (level instanceof ServerLevel server) {
            ignite(server, pos, MIN_FUSE + server.getRandom().nextInt(FUSE_SPREAD), true);
            if (!player.getAbilities().instabuild) {
                if (stack.isDamageableItem()) {
                    stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
                } else {
                    stack.shrink(1);
                }
            }
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!player.getMainHandItem().isEmpty()) return InteractionResult.PASS;
        if (level instanceof ServerLevel server) {
            if (Mods.SABLE.isLoaded()) {
                BlockPos grabbed = BarrelPhysics.toPhysics(server, pos);
                if (grabbed != null) {
                    PhysicsBarrelCarriers.grab(player, grabbed);
                }
            } else {
                PhysicsBarrelCarriers.grab(player, toEntity(server, pos, state, 0));
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        if (level instanceof ServerLevel server && !player.getAbilities().instabuild) {
            if (state.getValue(LIT)) {
                detonate(server, pos);
                return false;
            }
            boolean silk = EnchantmentHelper.getItemEnchantmentLevel(server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH), player.getMainHandItem()) > 0;
            if (!silk && server.getRandom().nextFloat() < IGNITE_ON_BREAK_CHANCE) {
                ignite(server, pos, MIN_FUSE + server.getRandom().nextInt(FUSE_SPREAD), true);
                return false;
            }
        }
        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }

    @Override
    protected void onExplosionHit(BlockState state, Level level, BlockPos pos, Explosion explosion, BiConsumer<ItemStack, BlockPos> dropConsumer) {
        if (level instanceof ServerLevel server && explosion.getBlockInteraction() != Explosion.BlockInteraction.KEEP) {
            ignite(server, pos, CHAIN_FUSE, false);
            return;
        }
        super.onExplosionHit(state, level, pos, explosion, dropConsumer);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)) return;
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 1.05;
        double z = pos.getZ() + 0.5;
        level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0, 0.05, 0.0);
        if (random.nextInt(3) == 0) {
            level.addParticle(ParticleTypes.FLAME, x, y, z, 0.0, 0.02, 0.0);
        }
    }

    public static void ignite(ServerLevel level, BlockPos pos, int fuse, boolean physics) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof PhysicsBarrelBlock) || !(level.getBlockEntity(pos) instanceof PhysicsBarrelBlockEntity barrel)) return;
        if (state.getValue(LIT) && barrel.getFuse() <= fuse) return;
        if (!state.getValue(LIT)) {
            level.setBlock(pos, state.setValue(LIT, true), Block.UPDATE_ALL);
            level.playSound(null, pos, NMLSounds.BOMB_FUSED.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        barrel.setFuse(fuse);
        if (!physics) return;
        if (Mods.SABLE.isLoaded()) {
            BarrelPhysics.toPhysics(level, pos);
        } else {
            toEntity(level, pos, level.getBlockState(pos), fuse);
        }
    }

    private static PhysicsBarrel toEntity(ServerLevel level, BlockPos pos, BlockState state, int fuse) {
        PhysicsBarrel barrel = PhysicsBarrel.fromBlock(level, pos, state, fuse);
        level.removeBlock(pos, false);
        level.addFreshEntity(barrel);
        return barrel;
    }

    public static void detonate(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof PhysicsBarrelBlock barrel)) return;
        Vec3 centre = PhysicsBarrelBlockEntity.worldCentre(level, pos);
        boolean ownSubLevel = Mods.SABLE.isLoaded() && BarrelPhysics.isOwnSubLevel(level, pos);
        level.removeBlock(pos, false);
        if (ownSubLevel) {
            BarrelPhysics.discard(level, pos);
        }
        barrel.getDetonation().blow(level, centre);
        chain(level, centre);
    }

    public static void chain(ServerLevel level, Vec3 centre) {
        BlockPos origin = BlockPos.containing(centre);
        int reach = (int) Math.ceil(CHAIN_RADIUS);
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-reach, -reach, -reach), origin.offset(reach, reach, reach))) {
            if (Vec3.atCenterOf(pos).distanceToSqr(centre) <= CHAIN_RADIUS * CHAIN_RADIUS && level.getBlockState(pos).getBlock() instanceof PhysicsBarrelBlock) {
                ignite(level, pos.immutable(), CHAIN_FUSE, false);
            }
        }
        for (PhysicsBarrel barrel : level.getEntitiesOfClass(PhysicsBarrel.class, new AABB(centre, centre).inflate(CHAIN_RADIUS))) {
            if (barrel.position().distanceToSqr(centre) <= CHAIN_RADIUS * CHAIN_RADIUS) {
                barrel.ignite(CHAIN_FUSE);
            }
        }
        if (Mods.SABLE.isLoaded()) {
            for (BlockPos pos : BarrelPhysics.barrelsNear(level, centre, CHAIN_RADIUS)) {
                ignite(level, pos, CHAIN_FUSE, false);
            }
        }
    }

    private static void hurtAround(ServerLevel level, Vec3 centre, float damage, int burnSeconds) {
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, new AABB(centre, centre).inflate(BLAST_RADIUS))) {
            if (entity.position().distanceToSqr(centre) > BLAST_RADIUS * BLAST_RADIUS) continue;
            entity.hurt(level.damageSources().inFire(), damage);
            if (burnSeconds > 0) {
                entity.igniteForSeconds(burnSeconds);
            }
        }
    }

    private static void coatInFire(ServerLevel level, Vec3 centre) {
        BlockPos origin = BlockPos.containing(centre);
        int reach = (int) Math.ceil(BLAST_RADIUS);
        List<BlockPos> open = new ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-reach, -reach, -reach), origin.offset(reach, reach, reach))) {
            if (Vec3.atCenterOf(pos).distanceToSqr(centre) > BLAST_RADIUS * BLAST_RADIUS) continue;
            BlockState state = level.getBlockState(pos);
            if (state.isAir()) {
                open.add(pos.immutable());
            } else if (state.canBeReplaced() && state.getFluidState().isEmpty() && !(state.getBlock() instanceof BaseFireBlock)) {
                BlockState fire = BaseFireBlock.getState(level, pos);
                if (fire.canSurvive(level, pos)) {
                    level.setBlockAndUpdate(pos, fire);
                }
            }
        }
        for (BlockPos pos : open) {
            BlockState below = level.getBlockState(pos.below());
            if (below.getBlock() instanceof BaseFireBlock || below.canBeReplaced()) continue;
            if (BaseFireBlock.canBePlacedAt(level, pos, Direction.UP)) {
                level.setBlockAndUpdate(pos, BaseFireBlock.getState(level, pos));
            }
        }
    }

    private static void fillWithGas(ServerLevel level, Vec3 centre) {
        BlockPos origin = BlockPos.containing(centre);
        int radiusSq = TOXIC_GAS_RADIUS * TOXIC_GAS_RADIUS;
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-TOXIC_GAS_RADIUS, -TOXIC_GAS_RADIUS, -TOXIC_GAS_RADIUS), origin.offset(TOXIC_GAS_RADIUS, TOXIC_GAS_RADIUS, TOXIC_GAS_RADIUS))) {
            if (pos.distSqr(origin) <= radiusSq && level.getBlockState(pos).isAir()) {
                ToxicGasBlock.place(level, pos.immutable());
            }
        }
    }

    public enum Detonation implements StringRepresentable {
        EXPLOSIVE("explosive"),
        FLAMMABLE("flammable"),
        TOXIC("toxic");

        public static final Codec<Detonation> CODEC = StringRepresentable.fromEnum(Detonation::values);

        private final String name;

        Detonation(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public void blow(ServerLevel level, Vec3 centre) {
            switch (this) {
                case EXPLOSIVE -> level.explode(null, centre.x, centre.y, centre.z, EXPLOSIVE_POWER, Level.ExplosionInteraction.TNT);
                case FLAMMABLE -> {
                    level.explode(null, centre.x, centre.y, centre.z, 0.0F, Level.ExplosionInteraction.NONE);
                    level.sendParticles(ParticleTypes.FLAME, centre.x, centre.y, centre.z, 120, 1.5, 1.0, 1.5, 0.08);
                    hurtAround(level, centre, FLAMMABLE_DAMAGE, FLAMMABLE_BURN_SECONDS);
                    coatInFire(level, centre);
                }
                case TOXIC -> {
                    level.explode(null, centre.x, centre.y, centre.z, 0.0F, Level.ExplosionInteraction.NONE);
                    level.sendParticles(NMLParticleTypes.TOXIC_GAS.get(), centre.x, centre.y, centre.z, 120, 1.5, 1.0, 1.5, 0.05);
                    hurtAround(level, centre, TOXIC_DAMAGE, 0);
                    fillWithGas(level, centre);
                }
            }
        }
    }
}
