package com.farcr.nomansland.common.registry.worldgen;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.registry.NMLRegistries;
import com.farcr.nomansland.common.world.feature.decorator.BoulderDecorator;
import com.farcr.nomansland.common.world.feature.decorator.BoulderDecoratorType;
import com.farcr.nomansland.common.world.feature.decorator.ClusterOnStoneBoulderDecorator;
import com.farcr.nomansland.common.world.feature.decorator.DoublePlantAroundBoulderDecorator;
import com.farcr.nomansland.common.world.feature.decorator.MultifaceOnStoneBoulderDecorator;
import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class NMLBoulderDecoratorTypes {
    public static final DeferredRegister<BoulderDecoratorType<?>> BOULDER_DECORATOR_TYPES =
            DeferredRegister.create(NMLRegistries.BOULDER_DECORATOR_TYPE, NoMansLand.MODID);

    public static final Supplier<BoulderDecoratorType<ClusterOnStoneBoulderDecorator>>
            CLUSTER_ON_STONE = register("cluster_on_stone", ClusterOnStoneBoulderDecorator.CODEC);

    public static final Supplier<BoulderDecoratorType<MultifaceOnStoneBoulderDecorator>>
            MULTIFACE_ON_STONE = register("multiface_on_stone", MultifaceOnStoneBoulderDecorator.CODEC);

    public static final Supplier<BoulderDecoratorType<DoublePlantAroundBoulderDecorator>>
            DOUBLE_PLANT_AROUND = register("double_plant_around", DoublePlantAroundBoulderDecorator.CODEC);

    private static <P extends BoulderDecorator> Supplier<BoulderDecoratorType<P>> register (String name, MapCodec<P> codec) {
        return BOULDER_DECORATOR_TYPES.register(name, () -> new BoulderDecoratorType<>(codec));
    }
}
