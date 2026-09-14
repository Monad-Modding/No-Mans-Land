package com.farcr.nomansland.common.mixin.client;

import com.farcr.nomansland.NoMansLand;
import com.mojang.authlib.minecraft.MinecraftProfileTextures;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@OnlyIn(Dist.CLIENT)
@Mixin(SkinManager.class)
public class SkinManagerMixin {
    @Inject(method = "registerTextures", at = @At("RETURN"), cancellable = true)
    private void registerRewardPlayerSkinTextures(UUID uuid, MinecraftProfileTextures textures, CallbackInfoReturnable<CompletableFuture<PlayerSkin>> cir) {
        List<String> UUIDS = List.of(
                "961e150b-8c68-430f-8926-1e84f4651599", // taze_r
                "4378df24-8433-4b5c-b865-bf635b003ebb", // Farcr
                "1341db98-ab30-430a-b7a5-dd3ba14e6eaa" // Probleyes
        );

        ResourceLocation CAPE_TEXTURE = NoMansLand.location("textures/misc/nml_cape.png");

        if (UUIDS.contains(uuid.toString())) {
            cir.setReturnValue(cir.getReturnValue().thenApply(skin -> {
                if (skin.capeTexture() == null)
                    return new PlayerSkin(skin.texture(), skin.textureUrl(), CAPE_TEXTURE, CAPE_TEXTURE, skin.model(), skin.secure());
                return skin;
            }));
        }
    }
}
