package com.farcr.nomansland.client.ambience.fogmodifiers;

import com.farcr.nomansland.client.ambience.FogModifierHandler;
import com.farcr.nomansland.client.renderer.FriendMoonRenderer;
import net.minecraft.client.Minecraft;

public class FriendMoonFogModifier extends FogModifier {
    @Override
    public float getFogEndAddend() {
        return 0f;
    }

    public float opacity() {
        return FriendMoonRenderer.getInstance().getFriendMoonOpacity();
    }

    @Override
    public float getFogStartAddend() {
        return 0f;
    }

    static float redModifier = (116 / 255f);
    static float greenModifier = (119 / 255f);
    static float blueModifier = (78 / 255f);

    @Override
    public float getFogRedMultiplier() { return redModifier * opacity(); }

    @Override
    public float getFogGreenMultiplier() { return greenModifier * opacity(); }

    @Override
    public float getFogBlueMultiplier() { return blueModifier * opacity(); }

    @Override
    boolean active(FogModifierHandler.FogContext context) {
        return Minecraft.getInstance().player != null && opacity() > 0.01f;
    }
}
