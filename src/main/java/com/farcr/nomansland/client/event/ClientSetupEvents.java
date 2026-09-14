package com.farcr.nomansland.client.event;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.DialogueLangLoader;
import com.farcr.nomansland.client.GraphicsCompat;
import com.farcr.nomansland.client.NMLArmorModels;
import com.farcr.nomansland.client.NMLModelLayers;
import com.farcr.nomansland.client.ambience.AmbienceHandler;
import com.farcr.nomansland.client.extensions.AncestralOathSwordClientExtensions;
import com.farcr.nomansland.client.extensions.NMLClientExtensions;
import com.farcr.nomansland.client.handler.CarvingClientHandler;
import com.farcr.nomansland.client.handler.InvertedBellClientHandler;
import com.farcr.nomansland.client.music.ContextualMusicHandler;
import com.farcr.nomansland.client.particle.*;
import com.farcr.nomansland.client.renderer.SunDogRenderer;
import com.farcr.nomansland.client.renderer.UpperAtmosphericRenderer;
import com.farcr.nomansland.client.renderer.dreams.MoonlightDreamRenderer;
import com.farcr.nomansland.client.renderer.effect.AccumulateZoomRenderer;
import com.farcr.nomansland.client.renderer.effect.GreyscaleEffectRenderer;
import com.farcr.nomansland.client.renderer.entity.*;
import com.farcr.nomansland.client.renderer.rendertype.AncestralGlintRenderLayer;
import com.farcr.nomansland.client.renderer.rendertype.AncestralGlintRenderType;
import com.farcr.nomansland.client.renderer.rendertype.MoonlightGlowRenderType;
import com.farcr.nomansland.common.integration.Mods;
import com.farcr.nomansland.common.integration.nirvana.NirvanaIntegration;
import com.farcr.nomansland.common.registry.NMLBlockEntities;
import com.farcr.nomansland.common.registry.NMLParticleTypes;
import com.farcr.nomansland.common.registry.entities.NMLEntities;
import com.farcr.nomansland.common.registry.items.NMLItems;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.FlameParticle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

import java.io.IOException;

@EventBusSubscriber(modid = NoMansLand.MODID, value = Dist.CLIENT)
public class ClientSetupEvents {
    @SubscribeEvent
    public static void onClientSetup(final FMLClientSetupEvent event) {
        AmbienceHandler.FOG_MODIFIER_HANDLER.fillFogModifiers();
        ContextualMusicHandler.buildMusicContext();

        event.enqueueWork(() -> {
            ItemProperties.register(NMLItems.BANDAGE.get(), NoMansLand.location("has_potion"),
                    (stack, level, entity, seed) -> {
                        final PotionContents potionContents = stack.get(DataComponents.POTION_CONTENTS);
                        if (potionContents != null && potionContents.getAllEffects().iterator().hasNext()) {
                            return 1.0F;
                        }
                        return 0.0F;
                    });

            ItemProperties.register(NMLItems.ANCIENT_BRONZE_CHISEL.get(), NoMansLand.location("using_chisel"),
                    (stack, level, entity, seed) -> {
                        if(entity != null && CarvingClientHandler.instance.isChiseling()){
                            InteractionHand hand = CarvingClientHandler.instance.getHand();
                            ItemStack itemInHand = entity.getItemInHand(hand);
                            if(itemInHand == stack) {
                                return 1.0f;
                            }
                        }

                        return 0.0f;
                    });
        });
    }

    @SubscribeEvent
    public static void registerModels(final ModelEvent.RegisterAdditional event) {
        event.register(ModelResourceLocation.standalone(NoMansLand.location("entity/firebomb")));
        event.register(ModelResourceLocation.standalone(NoMansLand.location("entity/ink_bomb")));
        event.register(ModelResourceLocation.standalone(NoMansLand.location("entity/explosive")));
        event.register(ModelResourceLocation.standalone(NoMansLand.location("entity/living_urn")));
        event.register(ModelResourceLocation.standalone(InvertedBellRenderer.BELL_MODEL.id()));
        event.register(ModelResourceLocation.standalone(InvertedBellRenderer.CLAPPER_MODEL.id()));
        event.register(ModelResourceLocation.standalone(InvertedBellRenderer.BEAM_MODEL.id()));
        if (Mods.NIRVANA.isLoaded()) event.register(ModelResourceLocation.standalone(NoMansLand.location("entity/fat_joint")));

        //Load all the pot models here otherwise you die
        for (int i = 0; i < 6; i++) {
            final var path = "block/ancient_pots/ancient_pot_small_" + (i+1);
            event.register(ModelResourceLocation.standalone(NoMansLand.location(path)));
        }
        event.register(ModelResourceLocation.standalone(NoMansLand.location("block/ancient_pots/ancient_pot_small_1alt")));
        for (int i = 0; i < 1; i++) {
            final var path = "block/ancient_pots/ancient_pot_large_" + (i + 1);
            event.register(ModelResourceLocation.standalone(NoMansLand.location(path)));
        }
        for (int i = 0; i < 6; i++) {
            final var path = "block/ancient_pots/alchemist_pot_small_" + (i+1);
            event.register(ModelResourceLocation.standalone(NoMansLand.location(path)));
        }
        event.register(ModelResourceLocation.standalone(NoMansLand.location("block/ancient_pots/alchemist_pot_small_1alt")));
        event.register(ModelResourceLocation.standalone(NoMansLand.location("block/ancient_pots/alchemist_pot_small_1_face_stern")));
        event.register(ModelResourceLocation.standalone(NoMansLand.location("block/ancient_pots/alchemist_pot_small_1_face_happy")));
        event.register(ModelResourceLocation.standalone(NoMansLand.location("block/ancient_pots/alchemist_pot_small_1_wiggle")));
        event.register(ModelResourceLocation.standalone(NoMansLand.location("block/ancient_pots/alchemist_pot_small_1_wiggle_gold")));
        event.register(ModelResourceLocation.standalone(NoMansLand.location("block/ancient_pots/alchemist_pot_small_1_wiggle_green")));
        event.register(ModelResourceLocation.standalone(NoMansLand.location("block/ancient_pots/alchemist_pot_small_1_wiggle_white")));
        event.register(ModelResourceLocation.standalone(NoMansLand.location("block/ancient_pots/alchemist_pot_small_2_face_stern")));
        event.register(ModelResourceLocation.standalone(NoMansLand.location("block/ancient_pots/alchemist_pot_small_2_face_happy")));
        for (int i = 0; i < 1; i++) {
            final var path = "block/ancient_pots/alchemist_pot_large_" + (i+1);
            event.register(ModelResourceLocation.standalone(NoMansLand.location(path)));
        }
        event.register(ModelResourceLocation.standalone(NoMansLand.location("block/ancient_pots/alchemist_pot_large_1_face_stern")));
        event.register(ModelResourceLocation.standalone(NoMansLand.location("block/ancient_pots/alchemist_pot_large_1_face_happy")));
        event.register(ModelResourceLocation.standalone(NoMansLand.location("block/ancient_pots/alchemist_pot_large_1_wiggle")));
        event.register(ModelResourceLocation.standalone(NoMansLand.location("block/ancient_pots/alchemist_pot_large_1_wiggle_gold")));
        event.register(ModelResourceLocation.standalone(NoMansLand.location("block/ancient_pots/alchemist_pot_large_1_wiggle_green")));
        event.register(ModelResourceLocation.standalone(NoMansLand.location("block/ancient_pots/alchemist_pot_large_1_wiggle_white")));
    }

    @SubscribeEvent
    public static void registerEntityRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(NMLEntities.BILLHOOK_BASS.get(), BillhookBassRenderer::new);

        event.registerEntityRenderer(NMLEntities.DEER.get(), DeerRenderer::new);
        event.registerEntityRenderer(NMLEntities.GOOSE.get(), GooseRenderer::new);
        // TODO: moose
//        event.registerEntityRenderer(NMLEntities.MOOSE.get(), MooseRenderer::new);
        event.registerEntityRenderer(NMLEntities.TORTOISE.get(), TortoiseRenderer::new);
        event.registerEntityRenderer(NMLEntities.BUDDY.get(), BuddyRenderer::new);
        event.registerEntityRenderer(NMLEntities.FRIENDERMAN.get(), FriendermanRenderer::new);

        event.registerEntityRenderer(NMLEntities.DREAMING_PLAYER.get(), DreamingPlayerRenderer::new);

        event.registerEntityRenderer(NMLEntities.FIREBOMB.get(), FirebombRenderer::new);
        event.registerEntityRenderer(NMLEntities.INK_BOMB.get(), InkBombRenderer::new);
        event.registerEntityRenderer(NMLEntities.EXPLOSIVE.get(), ExplosiveRenderer::new);
        event.registerEntityRenderer(NMLEntities.LIVING_URN.get(), LivingUrnRenderer::new);
        if (Mods.NIRVANA.isLoaded()) event.registerEntityRenderer(NirvanaIntegration.FAT_JOINT.get(), FatJointRenderer::new);

        event.registerEntityRenderer(NMLEntities.INCENDIARY_ARROW.get(), IncendiaryArrowRenderer::new);
        event.registerEntityRenderer(NMLEntities.EMBER.get(), NoopRenderer::new);

        event.registerEntityRenderer(NMLEntities.LINGERING_CLOUD.get(), NoopRenderer::new);
        event.registerEntityRenderer(NMLEntities.INK_CLOUD.get(), NoopRenderer::new);
        event.registerEntityRenderer(NMLEntities.PACIFIED_CLOUD.get(), NoopRenderer::new);

        event.registerBlockEntityRenderer(NMLBlockEntities.POT.get(), PotRenderer::new);
        event.registerEntityRenderer(NMLEntities.LIVING_POT.get(), LivingPotRenderer::new);
        event.registerEntityRenderer(NMLEntities.FALLING_POT.get(), FallingPotRenderer::new);

        event.registerBlockEntityRenderer(NMLBlockEntities.INVERTED_BELL.get(), InvertedBellRenderer::new);
        event.registerBlockEntityRenderer(NMLBlockEntities.MOONLIGHT_BASIN.get(), MoonlightBasinRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayers(final EntityRenderersEvent.RegisterLayerDefinitions event) {
        NMLModelLayers.registerLayers(event);
    }

    @SubscribeEvent
    public static void addLayers(final EntityRenderersEvent.AddLayers event) {
        NMLArmorModels.addLayers(event);
        AncestralGlintRenderLayer.addLayers(event);
    }

    @SubscribeEvent
    public static void registerClientExtensions(final RegisterClientExtensionsEvent event) {
        NMLClientExtensions.registerClientExtensions(event);
    }

    @SubscribeEvent
    public static void registerReloadListeners(final RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(DialogueLangLoader.INSTANCE);
    }

    @SubscribeEvent
    public static void registerParticleProviders(final RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(NMLParticleTypes.PALE_CHERRY_LEAVES.get(), sprites
                -> (simpleParticleType, clientLevel, d, e, f, g, h, i)
                -> new FallingParticle(clientLevel, d, e, f, sprites));
        event.registerSpriteSet(NMLParticleTypes.CAVE_DUST.get(), sprites
                -> (simpleParticleType, clientLevel, d, e, f, g, h, i)
                -> new CaveDustParticle(clientLevel, d, e, f, sprites));
        event.registerSpriteSet(NMLParticleTypes.RESIN_DROPLET.get(), sprites
                -> (simpleParticleType, clientLevel, d, e, f, g, h, i)
                -> new FluidFallingParticle(clientLevel, d, e, f, sprites, NMLParticleTypes.RESIN_DROPLET_FLAT));
        event.registerSpriteSet(NMLParticleTypes.RESIN_DROPLET_FLAT.get(), sprites
                -> (simpleParticleType, clientLevel, d, e, f, g, h, i)
                -> new FluidLandParticle(clientLevel, d, e, f, sprites));
        event.registerSpriteSet(NMLParticleTypes.MAPLE_SYRUP_DROPLET.get(), sprites
                -> (simpleParticleType, clientLevel, d, e, f, g, h, i)
                -> new FluidFallingParticle(clientLevel, d, e, f, sprites, NMLParticleTypes.MAPLE_SYRUP_DROPLET_FLAT));
        event.registerSpriteSet(NMLParticleTypes.MAPLE_SYRUP_DROPLET_FLAT.get(), sprites
                -> (simpleParticleType, clientLevel, d, e, f, g, h, i)
                -> new FluidLandParticle(clientLevel, d, e, f, sprites));
        event.registerSpriteSet(NMLParticleTypes.OIL.get(), sprites
                -> (simpleParticleType, clientLevel, d, e, f, g, h, i)
                -> new FluidFallingParticle(clientLevel, d, e, f, sprites, NMLParticleTypes.OIL_FLAT));
        event.registerSpriteSet(NMLParticleTypes.OIL_SPLASH.get(), sprites
                -> (simpleParticleType, clientLevel, d, e, f, g, h, i)
                -> new FluidSplashingParticle(clientLevel, d, e, f, g, h, i, sprites, NMLParticleTypes.OIL_FLAT));
        event.registerSpriteSet(NMLParticleTypes.OIL_FLAT.get(), sprites
                -> (simpleParticleType, clientLevel, d, e, f, g, h, i)
                -> new FluidLandParticle(clientLevel, d, e, f, sprites));
        event.registerSpriteSet(NMLParticleTypes.RESIN_OIL_BUBBLE.get(), sprites
                -> (simpleParticleType, clientLevel, d, e, f, g, h, i)
                -> new BubbleParticle(clientLevel, d, e, f, g, h, i, sprites, NMLParticleTypes.RESIN_OIL_BUBBLE_POP));
        event.registerSpriteSet(NMLParticleTypes.RESIN_OIL_BUBBLE_POP.get(), sprites
                -> (simpleParticleType, clientLevel, d, e, f, g, h, i)
                -> new BubblePopParticle(clientLevel, d, e, f, g, h, i, sprites));
        event.registerSpriteSet(NMLParticleTypes.SCULK_AMBIENCE.get(), sprites
                -> (simpleParticleType, clientLevel, d, e, f, g, h, i)
                -> new SculkAmbienceParticle(clientLevel, d, e, f, sprites));
        event.registerSpriteSet(NMLParticleTypes.ENTROPY_DUST.get(), sprites
                -> (simpleParticleType, clientLevel, d, e, f, g, h, i)
                -> new EntropyDustParticle(clientLevel, d, e, f, sprites));
        event.registerSpriteSet(NMLParticleTypes.MALEVOLENT_EMBERS.get(), sprites
                -> (simpleParticleType, clientLevel, d, e, f, g, h, i)
                -> new EmbersParticle(clientLevel, d, e, f, g, h, i, sprites));
        event.registerSpriteSet(NMLParticleTypes.MALEVOLENT_FLAME.get(), FlameParticle.Provider::new);
        event.registerSpriteSet(NMLParticleTypes.MILK_DROPLET.get(), sprites
                -> (simpleParticleType, clientLevel, d, e, f, g, h, i)
                -> new FluidFallingParticle(clientLevel, d, e, f, sprites, NMLParticleTypes.MILK_DROPLET_FLAT));
        event.registerSpriteSet(NMLParticleTypes.MILK_DROPLET_FLAT.get(), sprites
                -> (simpleParticleType, clientLevel, d, e, f, g, h, i)
                -> new FluidLandParticle(clientLevel, d, e, f, sprites));
        event.registerSpriteSet(NMLParticleTypes.TRANSLUCENT_DUST.get(), sprites
                -> (translucentDustParticleOptions, clientLevel, d, e, f, g, h, i)
                -> new TranslucentDustParticle(clientLevel, d, e, f, g, h, i, translucentDustParticleOptions, sprites));
        event.registerSpecial(NMLParticleTypes.MOONLIGHT_RAY.get(), (type, clientLevel, d, e, f, g, h, i)
                -> new MoonlightRayParticle(clientLevel, d, e, f, g, h, i));
        event.registerSpriteSet(NMLParticleTypes.MOONLIGHT_FLAME.get(), sprites
                -> (simpleParticleType, clientLevel, d, e, f, g, h, i)
                -> new MoonlightCandleFlameParticle(clientLevel, d, e, f, g, h, i, sprites));
        event.registerSpriteSet(NMLParticleTypes.MOONLIGHT_SPARK.get(), sprites
                -> (simpleParticleType, clientLevel, d, e, f, g, h, i)
                -> new MoonlightSparkParticle(clientLevel, d, e, f, g, h, i, sprites));
        event.registerSpriteSet(NMLParticleTypes.DEEP_SLEEP.get(), sprites
                -> (simpleParticleType, clientLevel, d, e, f, g, h, i)
                -> new DeepSleepParticle(clientLevel, d, e, f, g, h, i, sprites));

        event.registerSpriteSet(NMLParticleTypes.STASIS_HIT.get(), StasisHitProvider::new);
        event.registerSpriteSet(NMLParticleTypes.STASIS_HIT_PARRY.get(), StasisHitProvider::new);
        event.registerSpriteSet(NMLParticleTypes.STASIS_BREAK.get(), StasisHitProvider::new);

        event.registerSpriteSet(NMLParticleTypes.RITUAL_PICK_SMOKE.get(), sprites
                -> (simpleParticleType, clientLevel, d, e, f, g, h, i)
                -> new RitualPickSmokeParticle(clientLevel, d, e, f, g, h, i, sprites));

        event.registerSpriteSet(NMLParticleTypes.RITUAL_PICK_RESONANCE.get(), sprites
                -> (simpleParticleType, clientLevel, d, e, f, g, h, i)
                -> new RitualPickResonanceParticle(clientLevel, d, e, f, g, h, i, sprites));

        event.registerSpriteSet(NMLParticleTypes.RITUAL_PICK_DUST.get(), sprites
                -> (simpleParticleType, clientLevel, d, e, f, g, h, i)
                -> new RitualPickDustParticle(clientLevel, d, e, f, sprites));


        event.registerSpecial(NMLParticleTypes.POT_SHATTER.get(), new PotShatterParticle.Provider());
        event.registerSpriteSet(NMLParticleTypes.LIVING_URN_SHARD_FACE.get(), LivingUrnShardFaceParticle.Provider::new);
    }

    @SubscribeEvent
    public static void registerShaders(final RegisterShadersEvent event) throws IOException {
        if (GraphicsCompat.customShadersSupported()) {
//            GraphicsCompat.tryRegister(event, "rendertype_moonlight", DefaultVertexFormat.NEW_ENTITY,
//                    shader -> MoonlightRayParticle.MOONLIGHT_RENDER_SHADER = shader);
            GraphicsCompat.tryRegister(event, "rendertype_moonlight_glow", DefaultVertexFormat.POSITION_TEX,
                    shader -> MoonlightGlowRenderType.MOONLIGHT_GLOW_SHADER = shader);
            GraphicsCompat.tryRegister(event, "rendertype_ancestral_glint", DefaultVertexFormat.POSITION_TEX_COLOR,
                    shader -> AncestralGlintRenderType.ANCESTRAL_GLINT_SHADER = shader);
            GraphicsCompat.tryRegister(event, "sun_dog", DefaultVertexFormat.POSITION_TEX_COLOR,
                    shader -> SunDogRenderer.SUN_DOG_SHADER = shader);
            GraphicsCompat.tryRegister(event, "upper_atmosphere", DefaultVertexFormat.POSITION_COLOR,
                    shader -> UpperAtmosphericRenderer.UPPER_ATMOSPHERE_SHADER = shader);
            GraphicsCompat.tryRegister(event, "friend_moon_dream", DefaultVertexFormat.POSITION_COLOR,
                    shader -> MoonlightDreamRenderer.DREAM_SKY_SHADER = shader);
            GraphicsCompat.tryRegister(event, "dream_horizon_gradient", DefaultVertexFormat.POSITION_COLOR,
                    shader -> MoonlightDreamRenderer.GRADIENT_SHADER = shader);
            GraphicsCompat.tryRegister(event, "more_translucent_particle", DefaultVertexFormat.PARTICLE,
                    shader -> RitualPickSmokeParticle.SHADER = shader);
        }
        try {
            InvertedBellClientHandler.instance.postChain = new PostChain(
                    Minecraft.getInstance().getTextureManager(),
                    Minecraft.getInstance().getResourceManager(),
                    Minecraft.getInstance().getMainRenderTarget(),
                    InvertedBellClientHandler.INVERTED_BELL_SHADER
            );
        } catch (final IOException e) {
            NoMansLand.LOGGER.warn("Failed to load shader: {}", InvertedBellClientHandler.INVERTED_BELL_SHADER, e);
        } catch (final JsonSyntaxException e) {
            NoMansLand.LOGGER.warn("Failed to parse shader: {}", InvertedBellClientHandler.INVERTED_BELL_SHADER, e);
        }
        // Accumulate Zoom Shader
        try {
            AccumulateZoomRenderer.getInstance().setupPostChain(
                Minecraft.getInstance().getMainRenderTarget(),
                "nomansland:accumulate_zoom"
            );
            AncestralOathSwordClientExtensions.TRAIL_INSTANCE.setupPostChain(
                AncestralOathSwordClientExtensions.getRenderTarget(),
                "nomansland:accumulate_zoom_alpha"
            );
        } catch (final IOException e) {
            NoMansLand.LOGGER.warn("Failed to load shader: {}", AccumulateZoomRenderer.ACCUMULATE_ZOOM_SHADER, e);
        } catch (final JsonSyntaxException e) {
            NoMansLand.LOGGER.warn("Failed to parse shader: {}", AccumulateZoomRenderer.ACCUMULATE_ZOOM_SHADER, e);
        }
        // Greyscale post effect for stasis
        try {
            GreyscaleEffectRenderer.getInstance().setupPostChain();
        } catch (final IOException e) {
            NoMansLand.LOGGER.warn("Failed to load shader: {}", GreyscaleEffectRenderer.GREYSCALE_SHADER, e);
        } catch (final JsonSyntaxException e) {
            NoMansLand.LOGGER.warn("Failed to parse shader: {}", GreyscaleEffectRenderer.GREYSCALE_SHADER, e);
        }
    }

//    @SubscribeEvent
//    public static void registerRenderBuffers(final RegisterRenderBuffersEvent event) {
//        event.registerRenderBuffer(MoonlightGlowRenderType.MOONLIGHT_GLOW);
//    }
}
