package com.farcr.nomansland.common.blockentity;

import com.farcr.nomansland.NMLConfig;
import com.farcr.nomansland.common.block.cauldrons.FourLayeredCauldronBlock;
import com.farcr.nomansland.common.block.tap.TapBlock;
import com.farcr.nomansland.common.block.tap.TapInteraction;
import com.farcr.nomansland.common.block.tap.TapTrees;
import com.farcr.nomansland.common.registry.NMLBlockEntities;
import com.farcr.nomansland.common.registry.NMLRegistries;
import com.farcr.nomansland.common.registry.NMLTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.CauldronFluidContent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

public class TapBlockEntity extends BlockEntity {

    private static final int REFRESH_TICKS = 100;
    private static final int TREE_RECHECK_TICKS = 1200;
    private static final int RETRY_TICKS = 20;
    private static final int TARGET_REACH = 3;
    private static final int GENERIC_UNIT = 250;
    private static final int GENERIC_INTERVAL = 80;
    private static final String TIMER_KEY = "timer";
    private static final String PARTICLE_KEY = "particle";
    private static final String DRIP_CHANCE_KEY = "drip_chance";

    @Nullable
    private Flow flow;
    @Nullable
    private ParticleOptions particle;
    private float dripChance = TapInteraction.DEFAULT_DRIP_CHANCE;
    @Nullable
    private Holder<TapInteraction> treeChecked;
    private boolean treeValid;
    private long treeCheckedAt;
    private long nextRefresh;
    private boolean dirty = true;
    private int timer = -1;

    public TapBlockEntity(BlockPos pos, BlockState state) {
        super(NMLBlockEntities.TAP.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TapBlockEntity tap) {
        tap.tickServer((ServerLevel) level, pos, state);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, TapBlockEntity tap) {
        if (tap.particle == null || state.getValue(TapBlock.CLOSED) || level.random.nextFloat() >= tap.dripChance) return;
        TapBlock.spawnDrippingParticles(level, pos, state, tap.particle);
    }

    public void markChanged(boolean source) {
        this.dirty = true;
        if (source) {
            this.treeChecked = null;
        }
    }

    private void tickServer(ServerLevel level, BlockPos pos, BlockState state) {
        if (state.getValue(TapBlock.CLOSED)) {
            this.flow = null;
            this.setParticle(level, pos, state, null, TapInteraction.DEFAULT_DRIP_CHANCE);
            this.dirty = true;
            return;
        }
        if (this.dirty || level.getGameTime() >= this.nextRefresh) {
            this.refresh(level, pos, state);
        }
        if (this.flow == null || this.flow.target() == null || this.flow.kind() == Kind.TAPPED && this.flow.interaction().value().output().isEmpty()) return;
        if (this.timer < 0) {
            this.timer = this.interval(level);
        }
        if (this.timer > 0) {
            this.timer--;
            return;
        }
        boolean flowed = this.perform(level, pos, state, this.flow);
        this.timer = flowed ? this.interval(level) : RETRY_TICKS;
        this.setChanged();
    }

    private void refresh(ServerLevel level, BlockPos pos, BlockState state) {
        this.dirty = false;
        this.nextRefresh = level.getGameTime() + REFRESH_TICKS;
        BlockPos sourcePos = TapBlock.getSourcePos(pos, state);
        BlockState source = level.getBlockState(sourcePos);
        BlockPos target = findTarget(level, pos);
        Flow next = this.tapped(level, pos, sourcePos, source, target);
        if (next == null) {
            next = generic(level, state, sourcePos, source, target);
        }
        if (!Objects.equals(next == null ? null : next.key(), this.flow == null ? null : this.flow.key())) {
            this.timer = -1;
        }
        this.flow = next;
        this.setParticle(level, pos, state, next == null ? null : next.particle(), next == null ? TapInteraction.DEFAULT_DRIP_CHANCE : next.dripChance());
    }

    @Nullable
    private Flow tapped(ServerLevel level, BlockPos pos, BlockPos sourcePos, BlockState source, @Nullable BlockPos target) {
        Registry<TapInteraction> registry = level.registryAccess().registryOrThrow(NMLRegistries.TAP_INTERACTION_KEY);
        for (Holder.Reference<TapInteraction> holder : registry.holders().toList()) {
            TapInteraction interaction = holder.value();
            if (!interaction.source().matches(source) || !interaction.conditions().test(level, pos)) continue;
            if (interaction.source().tree().isPresent() && !this.isTree(level, sourcePos, source, holder)) continue;
            return new Flow(Kind.TAPPED, holder, target, null, interaction.particle().orElse(null), interaction.dripChance());
        }
        return null;
    }

    private boolean isTree(ServerLevel level, BlockPos sourcePos, BlockState source, Holder<TapInteraction> holder) {
        long now = level.getGameTime();
        if (holder.equals(this.treeChecked) && now - this.treeCheckedAt < TREE_RECHECK_TICKS) return this.treeValid;
        this.treeChecked = holder;
        this.treeCheckedAt = now;
        this.treeValid = TapTrees.isTree(level, sourcePos, source, holder.value().source().tree().get());
        return this.treeValid;
    }

    @Nullable
    private static Flow generic(ServerLevel level, BlockState state, BlockPos sourcePos, BlockState source, @Nullable BlockPos target) {
        if (source.hasProperty(BlockStateProperties.WATERLOGGED) && source.getValue(BlockStateProperties.WATERLOGGED)) return new Flow(Kind.WATERLOGGED, null, target, Fluids.WATER, ParticleTypes.FALLING_WATER, TapInteraction.DEFAULT_DRIP_CHANCE);
        if (source.getBlock() instanceof AbstractCauldronBlock && levelProperty(source) != null) {
            CauldronFluidContent content = CauldronFluidContent.getForBlock(source.getBlock());
            Fluid fluid = content == null || content.fluid == Fluids.EMPTY ? null : content.fluid;
            return new Flow(Kind.CAULDRON, null, target, fluid, cauldronParticle(source.getBlock(), fluid), TapInteraction.DEFAULT_DRIP_CHANCE);
        }
        IFluidHandler handler = Capabilities.FluidHandler.BLOCK.getCapability(level, sourcePos, source, null, state.getValue(TapBlock.FACING));
        if (handler == null) return null;
        FluidStack available = handler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE);
        if (available.isEmpty()) return null;
        return new Flow(Kind.FLUID, null, target, available.getFluid(), dripParticle(available.getFluid()), TapInteraction.DEFAULT_DRIP_CHANCE);
    }

    private int interval(ServerLevel level) {
        if (this.flow != null && this.flow.kind() == Kind.TAPPED && this.flow.interaction() != null) {
            double speed = NMLConfig.FILLING_SPEED_MULTIPLIER.get();
            if (speed <= 0.0) return Integer.MAX_VALUE;
            return Math.max(1, (int) (this.flow.interaction().value().interval().sample(level.random) / speed));
        }
        return GENERIC_INTERVAL;
    }

    private boolean perform(ServerLevel level, BlockPos pos, BlockState state, Flow flow) {
        BlockPos target = flow.target();
        BlockPos sourcePos = TapBlock.getSourcePos(pos, state);
        BlockState source = level.getBlockState(sourcePos);
        BlockState targetState = level.getBlockState(target);
        switch (flow.kind()) {
            case TAPPED -> {
                TapInteraction interaction = flow.interaction().value();
                if (!interaction.source().matches(source)) {
                    this.dirty = true;
                    return false;
                }
                if (interaction.output().isEmpty()) return false;
                TapInteraction.Output output = interaction.output().get();
                boolean filled = output.cauldron().isPresent() ? raiseCauldron(level, target, targetState, output.cauldron().get()) : putFluid(level, target, targetState, output.fluid().get().copy());
                if (filled && !interaction.consume().isEmpty()) {
                    consume(level, sourcePos, source, interaction.consume());
                }
                return filled;
            }
            case WATERLOGGED -> {
                return putFluid(level, target, targetState, new FluidStack(Fluids.WATER, unit(Fluids.WATER)));
            }
            case CAULDRON -> {
                if (!(source.getBlock() instanceof AbstractCauldronBlock) || levelProperty(source) == null) {
                    this.dirty = true;
                    return false;
                }
                boolean filled = flow.fluid() != null ? putFluid(level, target, targetState, new FluidStack(flow.fluid(), unit(flow.fluid()))) : raiseCauldron(level, target, targetState, source.getBlock());
                if (filled) {
                    lowerCauldron(level, sourcePos, source);
                }
                return filled;
            }
            case FLUID -> {
                IFluidHandler handler = Capabilities.FluidHandler.BLOCK.getCapability(level, sourcePos, source, null, state.getValue(TapBlock.FACING));
                if (handler == null || flow.fluid() == null) {
                    this.dirty = true;
                    return false;
                }
                int amount = unit(flow.fluid());
                FluidStack drained = handler.drain(new FluidStack(flow.fluid(), amount), IFluidHandler.FluidAction.SIMULATE);
                if (drained.getAmount() < amount || !putFluid(level, target, targetState, drained.copy())) return false;
                handler.drain(new FluidStack(flow.fluid(), amount), IFluidHandler.FluidAction.EXECUTE);
                return true;
            }
        }
        return false;
    }

    @Nullable
    public static BlockPos findTarget(Level level, BlockPos tap) {
        BlockPos.MutableBlockPos cursor = tap.mutable();
        for (int depth = 1; depth <= TARGET_REACH; depth++) {
            cursor.move(Direction.DOWN);
            BlockState state = level.getBlockState(cursor);
            if (state.getBlock() instanceof AbstractCauldronBlock || state.is(NMLTags.CATCHES_DRIPS)) return cursor.immutable();
            if (state.isCollisionShapeFullBlock(level, cursor)) return null;
        }
        return null;
    }

    private static boolean putFluid(Level level, BlockPos pos, BlockState state, FluidStack stack) {
        if (state.getBlock() instanceof AbstractCauldronBlock) {
            CauldronFluidContent content = CauldronFluidContent.getForFluid(stack.getFluid());
            return content != null && raiseCauldron(level, pos, state, content.block);
        }
        IFluidHandler handler = Capabilities.FluidHandler.BLOCK.getCapability(level, pos, state, null, Direction.UP);
        if (handler == null || handler.fill(stack, IFluidHandler.FluidAction.SIMULATE) != stack.getAmount()) return false;
        handler.fill(stack, IFluidHandler.FluidAction.EXECUTE);
        return true;
    }

    private static boolean raiseCauldron(Level level, BlockPos pos, BlockState state, Block cauldron) {
        if (state.is(Blocks.CAULDRON)) {
            BlockState filled = cauldron.defaultBlockState();
            level.setBlockAndUpdate(pos, filled);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(filled));
            return true;
        }
        IntegerProperty property = levelProperty(state);
        if (!state.is(cauldron) || property == null || ((AbstractCauldronBlock) state.getBlock()).isFull(state)) return false;
        BlockState raised = state.setValue(property, state.getValue(property) + 1);
        level.setBlockAndUpdate(pos, raised);
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(raised));
        return true;
    }

    private static void lowerCauldron(Level level, BlockPos pos, BlockState state) {
        IntegerProperty property = levelProperty(state);
        int remaining = property == null ? 0 : state.getValue(property) - 1;
        BlockState lowered = remaining <= 0 ? Blocks.CAULDRON.defaultBlockState() : state.setValue(property, remaining);
        level.setBlockAndUpdate(pos, lowered);
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(lowered));
    }

    @Nullable
    private static IntegerProperty levelProperty(BlockState state) {
        if (state.hasProperty(FourLayeredCauldronBlock.LEVEL)) return FourLayeredCauldronBlock.LEVEL;
        if (state.hasProperty(LayeredCauldronBlock.LEVEL)) return LayeredCauldronBlock.LEVEL;
        CauldronFluidContent content = CauldronFluidContent.getForBlock(state.getBlock());
        return content == null ? null : content.levelProperty;
    }

    private static int unit(Fluid fluid) {
        CauldronFluidContent content = CauldronFluidContent.getForFluid(fluid);
        return content == null ? GENERIC_UNIT : Math.max(1, content.totalAmount / Math.max(1, content.maxLevel));
    }

    private static void consume(Level level, BlockPos pos, BlockState state, Map<String, String> changes) {
        BlockState changed = state;
        for (Map.Entry<String, String> change : changes.entrySet()) {
            Property<?> property = changed.getBlock().getStateDefinition().getProperty(change.getKey());
            if (property != null) {
                changed = withValue(changed, property, change.getValue());
            }
        }
        if (changed == state) return;
        level.setBlockAndUpdate(pos, changed);
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(changed));
    }

    private static <T extends Comparable<T>> BlockState withValue(BlockState state, Property<T> property, String value) {
        return property.getValue(value).map(parsed -> state.setValue(property, parsed)).orElse(state);
    }

    @Nullable
    private static ParticleOptions cauldronParticle(Block cauldron, @Nullable Fluid fluid) {
        if (cauldron instanceof FourLayeredCauldronBlock layered && layered.particleType != null && layered.particleType.value() instanceof ParticleOptions options) return options;
        return fluid == null ? null : dripParticle(fluid);
    }

    @Nullable
    private static ParticleOptions dripParticle(Fluid fluid) {
        if (fluid.isSame(Fluids.WATER)) return ParticleTypes.FALLING_WATER;
        if (fluid.isSame(Fluids.LAVA)) return ParticleTypes.FALLING_LAVA;
        CauldronFluidContent content = CauldronFluidContent.getForFluid(fluid);
        return content == null ? null : cauldronParticle(content.block, null);
    }

    private void setParticle(ServerLevel level, BlockPos pos, BlockState state, @Nullable ParticleOptions particle, float dripChance) {
        if (Objects.equals(this.particle, particle) && this.dripChance == dripChance) return;
        this.particle = particle;
        this.dripChance = dripChance;
        this.setChanged();
        level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt(TIMER_KEY, this.timer);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains(TIMER_KEY)) {
            this.timer = tag.getInt(TIMER_KEY);
        }
        this.dripChance = tag.contains(DRIP_CHANCE_KEY) ? tag.getFloat(DRIP_CHANCE_KEY) : TapInteraction.DEFAULT_DRIP_CHANCE;
        this.particle = tag.contains(PARTICLE_KEY) ? ParticleTypes.CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), tag.get(PARTICLE_KEY)).result().orElse(null) : null;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        if (this.particle != null) {
            ParticleTypes.CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), this.particle).result().ifPresent(encoded -> tag.put(PARTICLE_KEY, encoded));
            tag.putFloat(DRIP_CHANCE_KEY, this.dripChance);
        }
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private enum Kind {
        TAPPED,
        WATERLOGGED,
        CAULDRON,
        FLUID
    }

    private record Flow(Kind kind, @Nullable Holder<TapInteraction> interaction, @Nullable BlockPos target, @Nullable Fluid fluid, @Nullable ParticleOptions particle, float dripChance) {

        private Object key() {
            return this.interaction != null ? this.interaction : this.kind;
        }
    }
}
