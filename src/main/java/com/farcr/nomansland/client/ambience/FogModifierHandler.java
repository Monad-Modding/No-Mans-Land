package com.farcr.nomansland.client.ambience;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.ambience.fogmodifiers.FogModifierInstance;
import com.farcr.nomansland.common.registry.NMLRegistries;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.CubicSampler;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.fluids.FluidType;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(value = Dist.CLIENT, modid = NoMansLand.MODID)
public class FogModifierHandler {
    private static final String FOG_MODIFIER_PROFILER = NoMansLand.MODID + ".fogModifierTick";

    private final List<FogModifierInstance> fogModifiers;
    private final FogContext context = new FogContext();

    // HELL
    private float fogStartMult = 1, fogEndMult = 1, fogStartAdd = 0, fogEndAdd = 0;
    private float pFogStartMult = 1, pFogEndMult = 1, pFogStartAdd = 0, pFogEndAdd = 0;
    private final Vector3f colorMult = new Vector3f(1, 1, 1);
    private final Vector3f pColorMult = new Vector3f(colorMult);

    private int ticksSinceLastUpdate = 0;
    private static final int TICKS_BETWEEN_UPDATES = 20;

    public FogModifierHandler() {
        this.fogModifiers = new ArrayList<>();
    }

    public float getFogStartMultiplier(float partialTicks) { return Mth.lerp(partialTicks, pFogStartMult, fogStartMult); }
    public float getFogEndMultiplier(float partialTicks) { return Mth.lerp(partialTicks, pFogEndMult, fogEndMult); }
    public float getFogStartAddend(float partialTicks) { return Mth.lerp(partialTicks, pFogStartAdd, fogStartAdd); }
    public float getFogEndAddend(float partialTicks) { return Mth.lerp(partialTicks, pFogEndAdd, fogEndAdd); }
    private final Vector3f returnColor = new Vector3f();
    public Vector3f getFogColorMultiplier(float partialTicks) { return pColorMult.lerp(colorMult, partialTicks, returnColor); }

    public void fillFogModifiers() {
        NMLRegistries.FOG_MODIFIERS.holders().forEach((reference) -> this.fogModifiers.add(new FogModifierInstance(reference.value())));
    }

    public void tick(ClientLevel level, Vec3 playerPos, float undergroundness) {
        Minecraft.getInstance().getProfiler().push(FOG_MODIFIER_PROFILER);
        // this is just a bunch of lerps, so updated every tick!
        this.tickModifierInterpolation();
        // this is updated less frequently because it gathers a lot of context about the world
        ticksSinceLastUpdate++;
        if (this.ticksSinceLastUpdate >= TICKS_BETWEEN_UPDATES) {
            this.ticksSinceLastUpdate = 0;
            this.updateActiveModifiers(level, playerPos, undergroundness);
        }


        Minecraft.getInstance().getProfiler().pop();
    }

    protected void tickModifierInterpolation() {
        this.pFogStartMult = this.fogStartMult;
        this.pFogEndMult = this.fogEndMult;
        this.pFogStartAdd = this.fogStartAdd;
        this.pFogEndAdd = this.fogEndAdd;
        this.pColorMult.set(this.colorMult);

        this.fogStartMult = 1; this.fogEndMult = 1;
        this.fogStartAdd = 0; this.fogEndAdd = 0;
        this.colorMult.set(1, 1, 1);
        for (FogModifierInstance modifier : fogModifiers) {
            modifier.tick();
            if (modifier.modifier.isEnabledWithFogType(Minecraft.getInstance().gameRenderer.getMainCamera().getFluidInCamera())) {
                fogStartMult *= modifier.getFogStartMultiplier();
                fogEndMult *= modifier.getFogEndMultiplier();

                fogStartAdd += modifier.getFogStartAddend();
                fogEndAdd += modifier.getFogEndAddend();

                modifier.multiplyFogColors(this.colorMult);
            }
        }
    }

    protected void updateActiveModifiers(ClientLevel level, Vec3 playerPos, float undergroundness) {
        this.updateFogContext(level, playerPos, undergroundness);
        for (FogModifierInstance modifier : fogModifiers) modifier.update(this.context);
    }

    private void updateFogContext(ClientLevel level, Vec3 playerPos, float undergroundness) {
        BlockPos blockPos = BlockPos.containing(playerPos);
        context.level = level;
        context.undergroundness = undergroundness;
        context.weather = WeatherState.fromWorld(level, blockPos);
        context.playerPos.set(playerPos.x(), playerPos.y(), playerPos.z());
        context.biome = level.getBiome(blockPos);
        context.fluidType = Minecraft.getInstance().cameraEntity.getEyeInFluidType();

        // effects
        if (Minecraft.getInstance().cameraEntity instanceof LivingEntity entity) {
            if (entity.hasEffect(MobEffects.BLINDNESS)) {
                MobEffectInstance effectInstance = entity.getEffect(MobEffects.BLINDNESS);
                context.blindnessFactor = effectInstance.isInfiniteDuration() ? 1 : Mth.lerp(Math.min(1.0F, (float)effectInstance.getDuration() / 20.0F), 0, 1);
            } else {
                context.blindnessFactor = 0;
            }
            if (entity.hasEffect(MobEffects.DARKNESS)) {
                MobEffectInstance effectInstance = entity.getEffect(MobEffects.DARKNESS);
                context.darknessFactor = Mth.lerp(effectInstance.getBlendFactor(entity, 1), 0, 1);
            } else {
                context.darknessFactor = 0;
            }
        } else {
            context.blindnessFactor = 0;
            context.darknessFactor = 0;
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void renderFog(ViewportEvent.RenderFog event) {
        if (event.isCanceled()) return;
        float partialTicks = (float) event.getPartialTick();

        float startMul = AmbienceHandler.FOG_MODIFIER_HANDLER.getFogStartMultiplier(partialTicks);
        float endMul = AmbienceHandler.FOG_MODIFIER_HANDLER.getFogEndMultiplier(partialTicks);
        float renderDistance = Minecraft.getInstance().options.getEffectiveRenderDistance() * 16.0F;
        float startAdd = AmbienceHandler.FOG_MODIFIER_HANDLER.getFogStartAddend(partialTicks) * renderDistance;
        float endAdd = AmbienceHandler.FOG_MODIFIER_HANDLER.getFogEndAddend(partialTicks) * renderDistance;

        // if we don't actually change anything, don't cancel the event.
        if ((startMul != 1) || (endMul != 1) || (startAdd != 0) || (endAdd != 0)) {
            event.setCanceled(true);
            event.setNearPlaneDistance(event.getNearPlaneDistance() * startMul + startAdd);
            event.setFarPlaneDistance(event.getFarPlaneDistance() * endMul + endAdd);
        }
    }

    @SubscribeEvent
    public static void renderColor(ViewportEvent.ComputeFogColor event) {
        float partialTicks = (float) event.getPartialTick();
        Vector3f color = AmbienceHandler.FOG_MODIFIER_HANDLER.getFogColorMultiplier(partialTicks);

        // differing from vanilla here a little bit - if you're sufficiently underground, it will stop considering day/night or sunset fog.
        float red = event.getRed();
        float green = event.getGreen();
        float blue = event.getBlue();
        float undergroundFactor = 1 - AmbienceHandler.SURFACE_AMBIENCE_HANDLER.getAboveGroundFactor(partialTicks);
        if (undergroundFactor > 0.001 && event.getCamera().getFluidInCamera() == FogType.NONE
                && !(Minecraft.getInstance().cameraEntity instanceof LivingEntity le && (le.hasEffect(MobEffects.BLINDNESS) || le.hasEffect(MobEffects.DARKNESS)))) {
            ClientLevel level = Minecraft.getInstance().level;
            BiomeManager biomemanager = level.getBiomeManager();
            Vec3 biomePos = event.getCamera().getPosition().subtract(2.0, 2.0, 2.0).scale(0.25);
            Vec3 rawBiomeColor = CubicSampler.gaussianSampleVec3(biomePos,
                    (x, y, z) -> level.effects().getBrightnessDependentFogColor(Vec3.fromRGB24(biomemanager.getNoiseBiomeAtQuart(x, y, z).value().getFogColor()), 1)
            );
            red = Mth.lerp(undergroundFactor, red, (float) rawBiomeColor.x());
            green = Mth.lerp(undergroundFactor, green, (float) rawBiomeColor.y());
            blue = Mth.lerp(undergroundFactor, blue, (float) rawBiomeColor.z());
        }

        event.setRed(red * color.x());
        event.setGreen(green * color.y());
        event.setBlue(blue * color.z());
    }


    public static class FogContext {
        private ClientLevel level;
        private Holder<Biome> biome;
        private float undergroundness;
        private WeatherState weather;
        private final Vector3d playerPos = new Vector3d();
        private float blindnessFactor = 0.0F;
        private float darknessFactor = 0.0F;
        private FluidType fluidType;

        public ClientLevel level() { return level; }
        public Holder<Biome> biome() { return biome; }
        public float undergroundness() { return undergroundness; }
        public WeatherState weather() { return weather; }
        public Vector3dc playerPos() { return playerPos; }
        public float blindnessFactor() { return blindnessFactor; }
        public float darknessFactor() { return darknessFactor; }
        public FluidType fluidType() { return fluidType; }
    }
}
