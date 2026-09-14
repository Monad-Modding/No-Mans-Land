package com.farcr.nomansland.common.block.torches;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.MapCodec;

import com.farcr.nomansland.common.registry.NMLRegistries;
import com.farcr.nomansland.common.registry.NMLSounds;
import com.farcr.nomansland.common.registry.NMLTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class ExtinguishedWallTorchBlock extends WallTorchBlock {

    private Block litBlock;

    public ExtinguishedWallTorchBlock(SimpleParticleType flameParticle, Properties properties) {
        super(flameParticle, properties);
    }

    public static final MapCodec<WallTorchBlock> CODEC = RecordCodecBuilder.<ExtinguishedWallTorchBlock>mapCodec(instance -> instance.group(
            PARTICLE_OPTIONS_FIELD.forGetter(block -> block.flameParticle),
            propertiesCodec()
    ).apply(instance, ExtinguishedWallTorchBlock::new)).xmap(block -> (WallTorchBlock) block, block -> (ExtinguishedWallTorchBlock) block);

    @Override
    public MapCodec<WallTorchBlock> codec() {
        return CODEC;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
    }

    @Override
    public ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.is(NMLTags.FIRESTARTERS)) {
            level.playSound(player,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    stack.is(Items.FLINT_AND_STEEL) ? NMLSounds.TORCH_LIGHT_BY_FLINT_AND_STEEL : NMLSounds.TORCH_LIGHT,
                    SoundSource.BLOCKS,
                    1.0F,
                    1.0F);
            level.setBlock(pos, getLitBlock().withPropertiesOf(state), 3);
            if (!level.isClientSide) {
                Direction direction = state.getValue(FACING).getOpposite();
                ServerLevel serverLevel = (ServerLevel) level;
                serverLevel.sendParticles(flameParticle, pos.getX() + 0.5 + 0.2 * direction.getStepX(), pos.getY() + 0.92, pos.getZ() + 0.5 + 0.2 * direction.getStepZ(), 5, 0, 0, 0, 0);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected void spawnAfterBreak(BlockState state, ServerLevel level, BlockPos pos, ItemStack stack, boolean dropExperience) {
        Direction direction = state.getValue(FACING).getOpposite();
        double dx = pos.getX() + 0.5;
        double dy = pos.getY() + 0.7;
        double dz = pos.getZ() + 0.5;
        level.sendParticles(ParticleTypes.SMOKE, dx + 0.2 * direction.getStepX(), dy + 0.22, dz + 0.2 * direction.getStepZ(), level.random.nextInt(2, 7), 0, 0, 0, 0);
    }

    @Override
    protected void onProjectileHit(Level level, BlockState state, BlockHitResult hit, Projectile projectile) {
        if (!level.isClientSide && projectile.isOnFire()) {
            level.setBlock(hit.getBlockPos(), this.getLitBlock().withPropertiesOf(state), 11);
        }
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        return getLitBlock().asItem().getDefaultInstance();
    }

    @Override
    public String getDescriptionId() {
        return getLitBlock().getDescriptionId();
    }

    /**
     * Lazily initializes the lit version of this block. <p>
     * Initializing {@link ExtinguishedWallTorchBlock#litBlock} in the constructor is too soon.
     *
     * @return The lit version of this block
     */
    public Block getLitBlock() {
        if (this.litBlock == null) {
            Block litBlock = null;
            for (final ExtinguishableBlockPairing block : NMLRegistries.EXTINGUISHABLE_BLOCKS) {
                if (this == block.extinguishedBlock()) {
                    litBlock = block.litBlock();
                    break;
                }
            }

            this.litBlock = litBlock;
        }

        //lit block should not be null after this. if it is, then we missed something.
        assert this.litBlock != null;

        return this.litBlock;
    }
}
