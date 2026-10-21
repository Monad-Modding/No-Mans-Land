package com.farcr.nomansland.client;

import com.farcr.nomansland.common.extension.TranslucentSign;
import dev.ryanhcode.sable.companion.SableCompanion;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Position;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class TranslucentSignFading {
    private static final double FADE_START = 1.0;
    private static final double FADE_LENGTH = 4.0;
    private static final float HIDDEN = 0.05F;
    private static final int MIN_FONT_ALPHA = 16;
    private static final float NONE = -1.0F;

    @Nullable
    private static SignBlockEntity sign;
    private static float signAlpha = NONE;
    private static float alpha = NONE;

    private TranslucentSignFading() {
    }

    public static void begin(BlockEntity blockEntity) {
        sign = null;
        signAlpha = NONE;
        alpha = NONE;
        if (!(blockEntity instanceof SignBlockEntity signBlockEntity) || !(blockEntity instanceof TranslucentSign translucent) || blockEntity.getLevel() == null) return;
        if (!translucent.nml$isTranslucent(true) && !translucent.nml$isTranslucent(false)) return;
        Vec3 centre = SableCompanion.INSTANCE.projectOutOfSubLevel(blockEntity.getLevel(), (Position) Vec3.atCenterOf(blockEntity.getBlockPos()));
        double distance = Math.sqrt(Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().distanceToSqr(centre));
        sign = signBlockEntity;
        signAlpha = (float) Mth.clamp(1.0 - (distance - FADE_START) / FADE_LENGTH, 0.0, 1.0);
    }

    public static void side(SignText text) {
        if (sign == null) return;
        boolean front = text == sign.getFrontText();
        if (!front && text != sign.getBackText()) return;
        alpha = ((TranslucentSign) sign).nml$isTranslucent(front) ? signAlpha : NONE;
    }

    public static void end() {
        sign = null;
        signAlpha = NONE;
        alpha = NONE;
    }

    public static boolean isHidden() {
        return alpha != NONE && alpha <= HIDDEN;
    }

    public static int fade(int color) {
        if (alpha == NONE) return color;
        int faded = Mth.clamp((int) (alpha * 255.0F), MIN_FONT_ALPHA, 255);
        return (faded << 24) | (color & 0x00FFFFFF);
    }
}
