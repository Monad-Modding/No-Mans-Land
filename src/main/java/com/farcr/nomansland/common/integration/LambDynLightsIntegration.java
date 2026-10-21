package com.farcr.nomansland.common.integration;

import net.minecraft.core.BlockPos;

import java.lang.reflect.Method;

public final class LambDynLightsIntegration {

    private static boolean initialized;
    private static Object instance;
    private static Method getDynamicLightLevel;

    private LambDynLightsIntegration() {
    }

    public static boolean isActive() {
        return Mods.LAMBDYNLIGHTS.isLoaded();
    }

    public static double getDynamicLightLevel(BlockPos pos) {
        if (!isActive()) return 0.0;
        if (!initialized) init();
        if (instance == null || getDynamicLightLevel == null) return 0.0;
        try {
            Object result = getDynamicLightLevel.invoke(instance, pos);
            return result instanceof Number number ? number.doubleValue() : 0.0;
        } catch (Throwable t) {
            return 0.0;
        }
    }

    private static synchronized void init() {
        if (initialized) return;
        initialized = true;
        try {
            Class<?> cls = Class.forName("dev.lambdaurora.lambdynlights.LambDynLights");
            instance = cls.getMethod("get").invoke(null);
            getDynamicLightLevel = cls.getMethod("getDynamicLightLevel", BlockPos.class);
        } catch (Throwable t) {
            instance = null;
            getDynamicLightLevel = null;
        }
    }
}
