package com.farcr.nomansland.common.blockentity;

import com.farcr.nomansland.NMLConfig;
import com.farcr.nomansland.common.block.cauldrons.FourLayeredCauldronBlock;
import com.farcr.nomansland.common.registry.NMLBlockEntities;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.CauldronFluidContent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;

import static com.farcr.nomansland.common.block.tap.TapBlock.*;
import static net.minecraft.world.level.block.BeehiveBlock.HONEY_LEVEL;
import static net.minecraft.world.level.block.StairBlock.WATERLOGGED;

public class TapBlockEntity extends BlockEntity {
    public int timeEmptying;

    public TapBlockEntity(BlockPos pos, BlockState state) {
        super(NMLBlockEntities.TAP.get(), pos, state);
        this.timeEmptying = 0;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TapBlockEntity tap) {
        if(state.getValue(CLOSED)) return;

        // Ensure there is a cauldron within 3 blocks under the tap
        BlockPos cauldronPos = getCauldronPos(level, pos);
        boolean cauldronFound = cauldronPos != null;
        cauldronPos = cauldronFound ? cauldronPos : pos.below();
        BlockState cauldronState = level.getBlockState(cauldronPos);
        BlockState stateBehind = getBlockStateBehind(level, pos, state);
        BlockPos posBehind = pos.relative(state.getValue(FACING).getOpposite());
        Block cauldronBlock = cauldronState.getBlock();

        boolean waterloggedSource = stateBehind.hasProperty(WATERLOGGED) && stateBehind.getValue(WATERLOGGED);

        if (!cauldronFound) {
            ParticleType<?> leaking = sourceDripParticle(level, state, posBehind, stateBehind, waterloggedSource);
            if (leaking != null) spawnDrippingParticles(level, pos, state, leaking);
            return;
        }

        if (waterloggedSource) {
            fillFromWaterloggedSource(level, pos, state, tap, cauldronPos, cauldronState);
            return;
        }

        if (stateBehind.hasProperty(HONEY_LEVEL) && stateBehind.getValue(HONEY_LEVEL) == 5 && !((AbstractCauldronBlock) cauldronBlock).isFull(cauldronState) && (cauldronBlock instanceof CauldronBlock || cauldronState.is(NMLBlocks.HONEY_CAULDRON.block()))) {
            tap.timeEmptying++;
            if (tap.timeEmptying < NMLConfig.TICKS_TO_FILL_CAULDRON.get()) {
                ParticleType<?> honeyParticle = cauldronParticle(NMLBlocks.HONEY_CAULDRON.get());
                if (honeyParticle != null) spawnDrippingParticles(level, pos, state, honeyParticle);
            }
            else {
                tap.timeEmptying = 0;
                if (cauldronState.hasProperty(FourLayeredCauldronBlock.LEVEL))
                    level.setBlockAndUpdate(cauldronPos, cauldronState.setValue(FourLayeredCauldronBlock.LEVEL, cauldronState.getValue(FourLayeredCauldronBlock.LEVEL) + 1));
                else level.setBlockAndUpdate(cauldronPos, NMLBlocks.HONEY_CAULDRON.get().defaultBlockState());
                level.setBlockAndUpdate(posBehind, stateBehind.setValue(HONEY_LEVEL, 0));
                level.gameEvent(GameEvent.BLOCK_CHANGE, posBehind, GameEvent.Context.of(stateBehind));
                level.gameEvent(GameEvent.BLOCK_CHANGE, cauldronPos, GameEvent.Context.of(cauldronState));
            }
            return;
        }

        if (drainFromUnregisteredCauldron(level, pos, state, tap, posBehind, stateBehind, cauldronPos, cauldronState)) return;
        drainIntoCauldron(level, pos, state, tap, posBehind, stateBehind, cauldronPos, cauldronState);
    }

    private static void fillFromWaterloggedSource(Level level, BlockPos pos, BlockState state, TapBlockEntity tap, BlockPos cauldronPos, BlockState cauldronState) {
        spawnDrippingParticles(level, pos, state, ParticleTypes.FALLING_WATER);

        Block cauldronBlock = cauldronState.getBlock();
        if (((AbstractCauldronBlock) cauldronBlock).isFull(cauldronState)) return;
        if (!(cauldronBlock instanceof CauldronBlock) && !cauldronState.is(Blocks.WATER_CAULDRON)) return;

        tap.timeEmptying++;
        if (tap.timeEmptying <= NMLConfig.TICKS_TO_FILL_CAULDRON.get()) return;

        tap.timeEmptying = 0;
        if (cauldronState.hasProperty(LayeredCauldronBlock.LEVEL))
            level.setBlockAndUpdate(cauldronPos, cauldronState.setValue(LayeredCauldronBlock.LEVEL, cauldronState.getValue(LayeredCauldronBlock.LEVEL) + 1));
        else level.setBlockAndUpdate(cauldronPos, Blocks.WATER_CAULDRON.defaultBlockState());

        level.gameEvent(GameEvent.BLOCK_CHANGE, cauldronPos, GameEvent.Context.of(level.getBlockState(cauldronPos)));
    }

    private static boolean drainFromUnregisteredCauldron(Level level, BlockPos pos, BlockState state, TapBlockEntity tap, BlockPos posBehind, BlockState stateBehind, BlockPos cauldronPos, BlockState cauldronState) {
        if (!(stateBehind.getBlock() instanceof FourLayeredCauldronBlock sourceCauldron)) return false;
        if (CauldronFluidContent.getForBlock(sourceCauldron) != null) return false;
        if (((AbstractCauldronBlock) cauldronState.getBlock()).isFull(cauldronState)) {
            if (sourceCauldron.particleType != null) spawnDrippingParticles(level, pos, state, sourceCauldron.particleType.value());
            return true;
        }

        tap.timeEmptying++;
        if (tap.timeEmptying < NMLConfig.TICKS_TO_FILL_CAULDRON.get()) {
            if (sourceCauldron.particleType != null) spawnDrippingParticles(level, pos, state, sourceCauldron.particleType.value());
            return true;
        }

        tap.timeEmptying = 0;
        FourLayeredCauldronBlock.lowerFillLevel(stateBehind, level, posBehind);
        if (cauldronState.hasProperty(FourLayeredCauldronBlock.LEVEL))
            level.setBlockAndUpdate(cauldronPos, stateBehind.setValue(FourLayeredCauldronBlock.LEVEL, cauldronState.getValue(FourLayeredCauldronBlock.LEVEL) + 1));
        else level.setBlockAndUpdate(cauldronPos, stateBehind.getBlock().defaultBlockState());

        level.gameEvent(GameEvent.BLOCK_CHANGE, cauldronPos, GameEvent.Context.of(cauldronState));
        level.gameEvent(GameEvent.BLOCK_CHANGE, posBehind, GameEvent.Context.of(stateBehind));
        return true;
    }

    private static boolean drainIntoCauldron(Level level, BlockPos pos, BlockState state, TapBlockEntity tap, BlockPos posBehind, BlockState stateBehind, BlockPos cauldronPos, BlockState cauldronState) {
        IFluidHandler source = Capabilities.FluidHandler.BLOCK.getCapability(level, posBehind, stateBehind, null, state.getValue(FACING));
        if (source == null) return false;

        IFluidHandler target = Capabilities.FluidHandler.BLOCK.getCapability(level, cauldronPos, cauldronState, null, Direction.UP);
        if (target == null) return false;

        FluidStack available = source.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE);
        if (available.isEmpty()) return false;

        CauldronFluidContent content = CauldronFluidContent.getForFluid(available.getFluid());
        if (content == null) return false;

        ParticleType<?> particleType = dripParticleFor(available.getFluid());
        int amountPerLevel = Math.max(1, content.totalAmount / Math.max(1, content.maxLevel));

        if (FluidUtil.tryFluidTransfer(target, source, amountPerLevel, false).isEmpty()) {
            if (particleType != null) spawnDrippingParticles(level, pos, state, particleType);
            return true;
        }

        tap.timeEmptying++;
        if (tap.timeEmptying < NMLConfig.TICKS_TO_FILL_CAULDRON.get()) {
            if (particleType != null) spawnDrippingParticles(level, pos, state, particleType);
            return true;
        }

        tap.timeEmptying = 0;
        FluidUtil.tryFluidTransfer(target, source, amountPerLevel, true);

        level.gameEvent(GameEvent.BLOCK_CHANGE, cauldronPos, GameEvent.Context.of(level.getBlockState(cauldronPos)));
        level.gameEvent(GameEvent.BLOCK_CHANGE, posBehind, GameEvent.Context.of(level.getBlockState(posBehind)));
        return true;
    }

    @Nullable
    private static ParticleType<?> sourceDripParticle(Level level, BlockState state, BlockPos posBehind, BlockState stateBehind, boolean waterloggedSource) {
        IFluidHandler source = Capabilities.FluidHandler.BLOCK.getCapability(level, posBehind, stateBehind, null, state.getValue(FACING));
        if (source != null) {
            FluidStack available = source.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE);
            if (!available.isEmpty()) {
                ParticleType<?> particleType = dripParticleFor(available.getFluid());
                if (particleType != null) return particleType;
            }
        }
        return waterloggedSource ? ParticleTypes.FALLING_WATER : null;
    }

    @Nullable
    private static ParticleType<?> dripParticleFor(Fluid fluid) {
        if (fluid == Fluids.WATER) return ParticleTypes.FALLING_WATER;
        if (fluid == Fluids.LAVA) return ParticleTypes.FALLING_LAVA;

        CauldronFluidContent content = CauldronFluidContent.getForFluid(fluid);
        return content == null ? null : cauldronParticle(content.block);
    }

    @Nullable
    private static ParticleType<?> cauldronParticle(Block cauldron) {
        if (cauldron instanceof FourLayeredCauldronBlock fourLayeredCauldron && fourLayeredCauldron.particleType != null)
            return fourLayeredCauldron.particleType.value();
        return null;
    }
}
