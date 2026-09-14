package com.farcr.nomansland.client.ambience;

import com.farcr.nomansland.NoMansLand;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@SuppressWarnings("unused")
@EventBusSubscriber(value = Dist.CLIENT, modid = NoMansLand.MODID)
public class AmbienceHandler {
    private static final String AMBIENCE_PROFILER = NoMansLand.MODID + ".ambienceTick";

    public static final SurfaceAmbience SURFACE_AMBIENCE_HANDLER = new SurfaceAmbience(Minecraft.getInstance());
    public static final FogModifierHandler FOG_MODIFIER_HANDLER = new FogModifierHandler();

    @SubscribeEvent
    public static void onTick(ClientTickEvent.Pre event) {
        Minecraft.getInstance().getProfiler().push(AMBIENCE_PROFILER);

        boolean shouldUpdate = shouldUpdate();
        SURFACE_AMBIENCE_HANDLER.tick(shouldUpdate);
        if (shouldUpdate)
            FOG_MODIFIER_HANDLER.tick(Minecraft.getInstance().level, Minecraft.getInstance().gameRenderer.getMainCamera().getPosition(), SURFACE_AMBIENCE_HANDLER.getAboveGroundFactor(1.0F));

        Minecraft.getInstance().getProfiler().pop();
    }

    private static boolean shouldUpdate() {
        return Minecraft.getInstance().level != null &&
               Minecraft.getInstance().level.isLoaded(Minecraft.getInstance().gameRenderer.getMainCamera().getBlockPosition()) &&
               !Minecraft.getInstance().isPaused();
    }
}
