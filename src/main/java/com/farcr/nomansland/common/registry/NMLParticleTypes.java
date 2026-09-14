package com.farcr.nomansland.common.registry;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.particle.TranslucentDustParticleOptions;
import com.farcr.nomansland.common.block.pots.PotShatterParticleOption;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;
import java.util.function.Supplier;

public class NMLParticleTypes {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, NoMansLand.MODID);
    public static final Supplier<SimpleParticleType> PALE_CHERRY_LEAVES = register("pale_cherry_leaves");
    
    public static final Supplier<SimpleParticleType> CAVE_DUST = register("cave_dust");
    
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> RESIN_DROPLET = register("resin_droplet");
    
    public static final Supplier<SimpleParticleType> RESIN_DROPLET_FLAT = register("resin_droplet_flat");
    
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> MAPLE_SYRUP_DROPLET = register("maple_syrup_droplet");
    
    public static final Supplier<SimpleParticleType> MAPLE_SYRUP_DROPLET_FLAT = register("maple_syrup_droplet_flat");

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> OIL = register("oil");

    public static final Supplier<SimpleParticleType> OIL_SPLASH = register("oil_splash");

    public static final Supplier<SimpleParticleType> OIL_FLAT = register("oil_flat");
    
    public static final Supplier<SimpleParticleType> RESIN_OIL_BUBBLE = register("resin_oil_bubble");
    
    public static final Supplier<SimpleParticleType> RESIN_OIL_BUBBLE_POP = register("resin_oil_bubble_pop");
    
    public static final Supplier<SimpleParticleType> MALEVOLENT_FLAME = register("malevolent_flame");
            
    public static final Supplier<SimpleParticleType> MALEVOLENT_EMBERS = register("malevolent_embers");

    public static final Supplier<SimpleParticleType> SCULK_AMBIENCE = register("sculk_ambience");

    public static final Supplier<SimpleParticleType> ENTROPY_DUST = register("entropy_dust");

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> MILK_DROPLET = register("milk_droplet");
    public static final Supplier<SimpleParticleType> MILK_DROPLET_FLAT = register("milk_droplet_flat");

    public static final Supplier<ParticleType<TranslucentDustParticleOptions>> TRANSLUCENT_DUST = register(
            "translucent_dust", false,
            TranslucentDustParticleOptions::codec, TranslucentDustParticleOptions::streamCodec
    );

    public static final Supplier<SimpleParticleType> MOONLIGHT_RAY = register("moonlight_ray");
    public static final Supplier<SimpleParticleType> MOONLIGHT_FLAME = register("moonlight_flame");
    public static final Supplier<SimpleParticleType> MOONLIGHT_SPARK = register("moonlight_spark");

    public static final Supplier<SimpleParticleType> DEEP_SLEEP = register("deep_sleep");

    public static final Supplier<SimpleParticleType> STASIS_HIT = register("stasis_hit");
    public static final Supplier<SimpleParticleType> STASIS_HIT_PARRY = register("stasis_hit_parry");
    public static final Supplier<SimpleParticleType> STASIS_BREAK = register("stasis_break");

    public static final Supplier<ParticleType<PotShatterParticleOption>> POT_SHATTER = register(
            "pot_shatter", false,
            PotShatterParticleOption::codec, PotShatterParticleOption::streamCodec
    );

    public static final Supplier<SimpleParticleType> RITUAL_PICK_SMOKE = register("ritual_pick_smoke");
    public static final Supplier<SimpleParticleType> RITUAL_PICK_RESONANCE = register("ritual_pick_resonance");
    public static final Supplier<SimpleParticleType> RITUAL_PICK_DUST = register("ritual_pick_dust");
    public static final Supplier<SimpleParticleType> LIVING_URN_SHARD_FACE = register("living_urn_shard_face");

    private static <T extends ParticleOptions> Supplier<ParticleType<T>> register(String name, boolean overrideLimitter, final Function<ParticleType<T>, MapCodec<T>> codecGetter, final Function<ParticleType<T>, StreamCodec<? super RegistryFriendlyByteBuf, T>> streamCodecGetter) {
        return PARTICLE_TYPES.register(name, () -> new ParticleType<T>(overrideLimitter) {
            public MapCodec<T> codec() {
                return codecGetter.apply(this);
            }

            public StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec() {
                return streamCodecGetter.apply(this);
            }
        });
    }

    private static DeferredHolder<ParticleType<?>, SimpleParticleType> register(String name) {
        return PARTICLE_TYPES.register(name, () -> new SimpleParticleType(false));
    }
}
