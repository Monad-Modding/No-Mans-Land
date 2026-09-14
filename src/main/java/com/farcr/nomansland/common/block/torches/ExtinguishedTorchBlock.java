package com.farcr.nomansland.common.block.torches;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.MapCodec;

import com.farcr.nomansland.common.registry.NMLRegistries;
import com.farcr.nomansland.common.registry.NMLSounds;
import com.farcr.nomansland.common.registry.NMLTags;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class ExtinguishedTorchBlock extends TorchBlock {

    private Block litBlock;

    public ExtinguishedTorchBlock(final SimpleParticleType flameParticle, final Properties properties) {
        super(flameParticle, properties);
    }

    public static final MapCodec<ExtinguishedTorchBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            PARTICLE_OPTIONS_FIELD.forGetter(block -> block.flameParticle),
            propertiesCodec()
    ).apply(instance, ExtinguishedTorchBlock::new));

    @Override
    public MapCodec<? extends ExtinguishedTorchBlock> codec() {
        return CODEC;
    }

    @Override
    public void animateTick(final BlockState state, final Level level, final BlockPos pos, final RandomSource random) {
    }

    @Override
    protected ItemInteractionResult useItemOn(final ItemStack stack, final BlockState state, final Level level, final BlockPos pos, final Player player, final InteractionHand hand, final BlockHitResult hitResult) {
        if (player.getItemInHand(hand).is(NMLTags.FIRESTARTERS)) {
            level.playSound(player,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    stack.is(Items.FLINT_AND_STEEL) ? NMLSounds.TORCH_LIGHT_BY_FLINT_AND_STEEL : NMLSounds.TORCH_LIGHT,
                    SoundSource.BLOCKS,
                    1.0F,
                    1.0F);
            level.setBlock(pos, this.getLitBlock().defaultBlockState(), 3);
            if (!level.isClientSide) {
                final ServerLevel serverLevel = (ServerLevel) level;
                serverLevel.sendParticles(this.flameParticle, pos.getX() + 0.5, pos.getY() + 0.7, pos.getZ() + 0.5, 5, 0, 0, 0, 0);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected void spawnAfterBreak(final BlockState state, final ServerLevel level, final BlockPos pos, final ItemStack stack, final boolean dropExperience) {
        level.sendParticles(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 0.7, pos.getZ() + 0.5, level.random.nextInt(2, 7), 0, 0, 0, 0.05);
    }

    @Override
    protected void onProjectileHit(final Level level, final BlockState state, final BlockHitResult hit, final Projectile projectile) {
        if (!level.isClientSide && projectile.isOnFire()) {
            level.setBlock(hit.getBlockPos(), this.getLitBlock().withPropertiesOf(state), 11);
        }
    }

    @Override
    public ItemStack getCloneItemStack(final BlockState state, final HitResult target, final LevelReader level, final BlockPos pos, final Player player) {
        return this.getLitBlock().getCloneItemStack(state, target, level, pos, player);
    }

    @Override
    public String getDescriptionId() {
        return this.getLitBlock().getDescriptionId();
    }

    /**
     * Lazily initializes the lit version of this block. <p>
     * Initializing {@link ExtinguishedTorchBlock#litBlock} in the constructor is too soon.
     *
     * @return The lit version of this block
     */
    public Block getLitBlock() {
        if (this.litBlock == null) {
            for (final ExtinguishableBlockPairing pair : NMLRegistries.EXTINGUISHABLE_BLOCKS) {
                if (pair.isExtinguishedVersion(this)) {
                    this.litBlock = pair.litBlock();
                    break;
                }
            }
        }

        //lit block should not be null after this. if it is, then we missed something.
        assert this.litBlock != null;
        return this.litBlock;
    }
}
