package com.farcr.nomansland.client.event;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.handler.CarvingClientHandler;
import com.farcr.nomansland.client.handler.InvertedBellClientHandler;
import com.farcr.nomansland.client.renderer.DialogueRenderer;
import com.farcr.nomansland.client.renderer.FriendMoonRenderer;
import com.farcr.nomansland.client.renderer.dreams.ClientDreamRenderer;
import com.farcr.nomansland.common.block.FrostedGrassBlock;
import com.farcr.nomansland.common.dreams.dreamtypes.MoonlightDreamType;
import com.farcr.nomansland.common.extension.LivingEntityExtension;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.function.Function;
import java.util.function.Predicate;

@EventBusSubscriber(modid = NoMansLand.MODID, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void addSectionGeometryEvent(AddSectionGeometryEvent event) {
        BlockPos origin = event.getSectionOrigin();
        LevelChunk chunk = event.getLevel().getChunkAt(origin);
        LevelChunkSection section = chunk.getSection(chunk.getSectionIndex(origin.getY()));
        if (section.maybeHas(FrostedGrassAdditionalGeoRenderer.STATE_PREDICATE))
            event.addRenderer(new FrostedGrassAdditionalGeoRenderer(Minecraft.getInstance().getBlockRenderer(), RandomSource.create(), SectionPos.of(origin)));
    }

    static class FrostedGrassAdditionalGeoRenderer implements AddSectionGeometryEvent.AdditionalSectionRenderer {
        private static final Predicate<BlockState> STATE_PREDICATE = state -> state.getOptionalValue(FrostedGrassBlock.SNOWLOGGED).orElse(false);
        private static final BlockState SNOW_BLOCKSTATE = Blocks.SNOW.defaultBlockState();
        final RandomSource randomsource;
        final ModelBlockRenderer modelBlockRenderer;
        final BakedModel snowModel;
        final SectionPos sectionPos;

        FrostedGrassAdditionalGeoRenderer(BlockRenderDispatcher blockRenderer, RandomSource randomsource, SectionPos sectionPos) {
            this.randomsource = randomsource;
            this.modelBlockRenderer = blockRenderer.getModelRenderer();
            this.snowModel = blockRenderer.getBlockModel(SNOW_BLOCKSTATE);
            this.sectionPos = sectionPos;
        }

        @Override
        public void render(AddSectionGeometryEvent.SectionRenderingContext context) {
            BlockAndTintGetter region = context.getRegion();
            PoseStack stack = context.getPoseStack();
            sectionPos.blocksInside()
                    // filter for only snowlogged blocks
                    .filter((pos) -> STATE_PREDICATE.test(context.getRegion().getBlockState(pos)))
                    // for each snowlogged block, render snow at that position.
                    .forEach((pos) -> drawSnow(stack, region, pos, context::getOrCreateChunkBuffer));
        }

        private void drawSnow(PoseStack stack, BlockAndTintGetter region, BlockPos pos, Function<RenderType, VertexConsumer> consumer) {
            stack.pushPose();
            stack.translate(SectionPos.sectionRelative(pos.getX()), SectionPos.sectionRelative(pos.getY()), SectionPos.sectionRelative(pos.getZ()));
            modelBlockRenderer.tesselateWithAO(
                    region,
                    snowModel,
                    SNOW_BLOCKSTATE,
                    pos, stack,
                    consumer.apply(RenderType.cutoutMipped()),
                    true,
                    randomsource, SNOW_BLOCKSTATE.getSeed(pos), OverlayTexture.NO_OVERLAY,
                    ModelData.EMPTY,
                    RenderType.cutoutMipped()
            );
            stack.popPose();
        }
    }

    @SubscribeEvent
    public static void onClientLogOut(ClientPlayerNetworkEvent.LoggingOut event) {
        FriendMoonRenderer.destroy();
        ClientDreamRenderer.destroy();
        DialogueRenderer.setCurrentState(null);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) FriendMoonRenderer.getInstance().tickClientState();
        if (mc.player == null) return;
        InvertedBellClientHandler.instance.tick();
        CarvingClientHandler.instance.tick();
    }

    @SubscribeEvent
    public static void onClickEvent(InputEvent.InteractionKeyMappingTriggered event) {
        if (((LivingEntityExtension)Minecraft.getInstance().player).nml$getBellParalysis() > 0) {
            event.setCanceled(true);
            event.setSwingHand(false);
        }
    }

    @SubscribeEvent
    public static void calculatePlayerTurn(CalculatePlayerTurnEvent event) {
        if (ClientDreamRenderer.getInstance().dreamShouldRender()
        && ClientDreamRenderer.getInstance().getDreamClientInstance()
            instanceof MoonlightDreamType.MoonlightDreamTypeInstance moonlightDreamType
        && moonlightDreamType.moonPresenceTime > 0)
            event.setCinematicCameraEnabled(true);
    }

    @SubscribeEvent
    public static void renderLevelStage(RenderLevelStageEvent event) {
        if(event.getStage() == RenderLevelStageEvent.Stage.AFTER_ENTITIES && CarvingClientHandler.instance.isChiseling()) {
            CarvingClientHandler.instance.render(event.getPoseStack(), event.getLevelRenderer(), event.getCamera(), event.getPartialTick());
        }
    }

    @SubscribeEvent
    public static void playerAttack(PlayerInteractEvent.LeftClickBlock event) {
        if(event.getLevel().isClientSide() && CarvingClientHandler.instance.isChiseling()) {
            CarvingClientHandler.instance.clear();
        }
    }

    @SubscribeEvent
    public static void playerAttack(PlayerInteractEvent.LeftClickEmpty event) {
        if(CarvingClientHandler.instance.isChiseling()) {
            CarvingClientHandler.instance.clear();
        }
    }

}
