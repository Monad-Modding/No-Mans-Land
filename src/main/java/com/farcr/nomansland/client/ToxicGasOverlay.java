package com.farcr.nomansland.client;

import com.farcr.nomansland.common.block.ToxicGasBlock;
import com.farcr.nomansland.common.item.GasMaskItem;
import com.farcr.nomansland.common.registry.entities.NMLEffects;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ToxicGasOverlay {
    private static final ResourceLocation VIGNETTE = ResourceLocation.withDefaultNamespace("textures/misc/vignette.png");
    private static final float BASE = 0.6F;
    private static final float MASKED = 0.375F;
    private static final float MAX = 1.5F;
    private static final int FULL_AT = 200;
    private static final float FADE_SPEED = 0.08F;

    private static float current = 0.0F;

    public static void render(GuiGraphics guiGraphics) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;

        float target = (player != null && ToxicGasBlock.isHeadInGas(player)) ? targetIntensity(player) : 0.0F;
        current = Mth.lerp(FADE_SPEED, current, target);
        if (current <= 0.002F) {
            current = 0.0F;
            return;
        }

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        for (float remaining = current; remaining > 0.0F; remaining -= 1.0F) {
            float pass = Math.min(remaining, 1.0F);
            guiGraphics.setColor(pass, pass, pass, 1.0F);
            guiGraphics.blit(VIGNETTE, 0, 0, -90, 0.0F, 0.0F, guiGraphics.guiWidth(), guiGraphics.guiHeight(), guiGraphics.guiWidth(), guiGraphics.guiHeight());
        }
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private static float targetIntensity(Player player) {
        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        if (head.getItem() instanceof GasMaskItem && GasMaskItem.getDuration(head) > 0) {
            return MASKED;
        }
        MobEffectInstance corrosion = player.getEffect(NMLEffects.CORROSION);
        int duration = corrosion != null ? corrosion.getDuration() : 0;
        float t = Mth.clamp(duration / (float) FULL_AT, 0.0F, 1.0F);
        return BASE + t * (MAX - BASE);
    }
}
