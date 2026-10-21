package com.farcr.nomansland.common.block.tap;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record TapInteraction(Source source, Optional<Output> output, IntProvider interval, Optional<ParticleOptions> particle, float dripChance, Map<String, String> consume, Conditions conditions) {

    public static final float DEFAULT_DRIP_CHANCE = 0.25F;

    private static final float RANDOM_TICK_INTERVAL = 4096.0F / 3.0F;
    private static final float LEGACY_TAP_CHANCE = 0.05F;

    private static final Codec<TapInteraction> CURRENT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Source.CODEC.fieldOf("source").forGetter(TapInteraction::source),
            Output.CODEC.optionalFieldOf("output").forGetter(TapInteraction::output),
            IntProvider.codec(1, 1728000).optionalFieldOf("interval", ConstantInt.of(1)).forGetter(TapInteraction::interval),
            ParticleTypes.CODEC.optionalFieldOf("particle").forGetter(TapInteraction::particle),
            Codec.floatRange(0.0F, 1.0F).optionalFieldOf("drip_chance", DEFAULT_DRIP_CHANCE).forGetter(TapInteraction::dripChance),
            Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("consume", Map.of()).forGetter(TapInteraction::consume),
            Conditions.CODEC.optionalFieldOf("conditions", Conditions.ANY).forGetter(TapInteraction::conditions)
    ).apply(instance, TapInteraction::new));

    private static final Codec<TapInteraction> LEGACY_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockStateProvider.CODEC.listOf().fieldOf("sources").forGetter(interaction -> List.of()),
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("cauldron").forGetter(interaction -> Blocks.CAULDRON),
            Codec.floatRange(0.0F, 10.0F).fieldOf("rate").forGetter(interaction -> 0.0F),
            BuiltInRegistries.PARTICLE_TYPE.byNameCodec().optionalFieldOf("particle").forGetter(interaction -> Optional.empty())
    ).apply(instance, TapInteraction::fromLegacy));

    public static final Codec<TapInteraction> CODEC = Codec.withAlternative(CURRENT_CODEC, LEGACY_CODEC);

    private static TapInteraction fromLegacy(List<BlockStateProvider> providers, Block cauldron, float rate, Optional<ParticleType<?>> particleType) {
        RandomSource random = RandomSource.create(0L);
        List<BlockState> states = new ArrayList<>();
        List<Holder<Block>> blocks = new ArrayList<>();
        boolean logs = !providers.isEmpty();
        for (BlockStateProvider provider : providers) {
            BlockState state = provider.getState(random, BlockPos.ZERO);
            states.add(state);
            if (!blocks.contains(state.getBlockHolder())) {
                blocks.add(state.getBlockHolder());
            }
            logs &= state.getBlock() instanceof RotatedPillarBlock;
        }
        Optional<TreeRequirement> tree = logs ? Optional.of(TreeRequirement.DEFAULT) : Optional.empty();
        Source source = new Source(HolderSet.direct(blocks), Optional.empty(), tree, states);
        Optional<Output> output = rate > 0.0F ? Optional.of(new Output(Optional.of(cauldron), Optional.empty())) : Optional.empty();
        int average = rate > 0.0F ? Math.max(1, Math.round(RANDOM_TICK_INTERVAL / (LEGACY_TAP_CHANCE * rate))) : 1;
        IntProvider interval = average > 1 ? UniformInt.of(Math.max(1, average / 2), average * 3 / 2) : ConstantInt.of(average);
        Optional<ParticleOptions> particle = particleType.filter(SimpleParticleType.class::isInstance).map(SimpleParticleType.class::cast);
        return new TapInteraction(source, output, interval, particle, DEFAULT_DRIP_CHANCE, Map.of(), Conditions.ANY);
    }

    public record Source(HolderSet<Block> blocks, Optional<StatePropertiesPredicate> properties, Optional<TreeRequirement> tree, List<BlockState> states) {

        public static final Codec<Source> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("block").forGetter(Source::blocks),
                StatePropertiesPredicate.CODEC.optionalFieldOf("properties").forGetter(Source::properties),
                TreeRequirement.CODEC.optionalFieldOf("tree").forGetter(Source::tree),
                BlockState.CODEC.listOf().optionalFieldOf("states", List.of()).forGetter(Source::states)
        ).apply(instance, Source::new));

        public boolean matches(BlockState state) {
            if (!state.is(this.blocks) || !this.states.isEmpty() && !this.states.contains(state)) return false;
            return this.properties.isEmpty() || this.properties.get().matches(state);
        }
    }

    public record TreeRequirement(Optional<HolderSet<Block>> trunk, Optional<HolderSet<Block>> leaves, Optional<HolderSet<Block>> ground, int minHeight) {

        public static final TreeRequirement DEFAULT = new TreeRequirement(Optional.empty(), Optional.empty(), Optional.empty(), 3);

        public static final Codec<TreeRequirement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                RegistryCodecs.homogeneousList(Registries.BLOCK).optionalFieldOf("trunk").forGetter(TreeRequirement::trunk),
                RegistryCodecs.homogeneousList(Registries.BLOCK).optionalFieldOf("leaves").forGetter(TreeRequirement::leaves),
                RegistryCodecs.homogeneousList(Registries.BLOCK).optionalFieldOf("ground").forGetter(TreeRequirement::ground),
                Codec.intRange(1, TapTrees.MAX_TRUNK).optionalFieldOf("min_height", 3).forGetter(TreeRequirement::minHeight)
        ).apply(instance, TreeRequirement::new));
    }

    public record Output(Optional<Block> cauldron, Optional<FluidStack> fluid) {

        public static final Codec<Output> CODEC = RecordCodecBuilder.<Output>create(instance -> instance.group(
                BuiltInRegistries.BLOCK.byNameCodec().optionalFieldOf("cauldron").forGetter(Output::cauldron),
                FluidStack.CODEC.optionalFieldOf("fluid").forGetter(Output::fluid)
        ).apply(instance, Output::new)).validate(Output::validate);

        private static DataResult<Output> validate(Output output) {
            if (output.cauldron.isPresent() == output.fluid.isPresent()) return DataResult.error(() -> "A tap output needs exactly one of \"cauldron\" or \"fluid\"");
            return DataResult.success(output);
        }
    }

    public record Conditions(Optional<HolderSet<Biome>> biomes, TimeOfDay time, Weather weather) {

        public static final Conditions ANY = new Conditions(Optional.empty(), TimeOfDay.ANY, Weather.ANY);
        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                RegistryCodecs.homogeneousList(Registries.BIOME).optionalFieldOf("biomes").forGetter(Conditions::biomes),
                TimeOfDay.CODEC.optionalFieldOf("time", TimeOfDay.ANY).forGetter(Conditions::time),
                Weather.CODEC.optionalFieldOf("weather", Weather.ANY).forGetter(Conditions::weather)
        ).apply(instance, Conditions::new));

        public boolean test(Level level, BlockPos pos) {
            if (this.biomes.isPresent() && !this.biomes.get().contains(level.getBiome(pos))) return false;
            boolean day = level.isDay();
            if (this.time == TimeOfDay.DAY && !day || this.time == TimeOfDay.NIGHT && day) return false;
            boolean raining = level.isRainingAt(pos.above());
            return this.weather == Weather.ANY || (this.weather == Weather.RAIN) == raining;
        }
    }

    public enum TimeOfDay implements StringRepresentable {
        ANY("any"),
        DAY("day"),
        NIGHT("night");

        public static final Codec<TimeOfDay> CODEC = StringRepresentable.fromEnum(TimeOfDay::values);

        private final String name;

        private TimeOfDay(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    public enum Weather implements StringRepresentable {
        ANY("any"),
        CLEAR("clear"),
        RAIN("rain");

        public static final Codec<Weather> CODEC = StringRepresentable.fromEnum(Weather::values);

        private final String name;

        private Weather(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
