package com.farcr.nomansland.client.gui;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.handler.CarvingClientHandler;
import com.farcr.nomansland.common.carving.CarvingType;
import com.farcr.nomansland.common.registry.NMLRegistries;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class CarvingSelectionScreen extends Screen {
    private static final ResourceLocation SLOT = NoMansLand.location("item_slot");
    private static final ResourceLocation SLOT_SELECTED = NoMansLand.location("item_slot_selected");
    private static CarvingType lastSelectedType = null;

    private final BlockPos startPos;
    private final Direction face;
    private final InteractionHand hand;
    private final List<CarvingType> types;

    private int selectedIndex = 0;
    private float offset = 0;

    protected CarvingSelectionScreen(BlockPos startPos, Direction face, InteractionHand hand, List<CarvingType> types) {
        super(Component.translatable("screen.nomansland.carving"));
        this.startPos = startPos;
        this.face = face;
        this.hand = hand;
        this.types = types;

        if(lastSelectedType != null) {
            if(this.types.contains(lastSelectedType)) {
                this.selectedIndex = this.types.indexOf(lastSelectedType);
                this.offset = this.selectedIndex;
            } else {
                lastSelectedType = null;
            }
        }
    }

    public static void open(BlockPos clickedPos, Direction clickedFace, InteractionHand hand, List<CarvingType> types) {
        Minecraft.getInstance().setScreen(new CarvingSelectionScreen(clickedPos, clickedFace, hand, types));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        List<ItemStack> items = new ArrayList<>();
        for (CarvingType carving : this.types) {
            items.add(carving.getIcon());
        }

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        guiGraphics.drawCenteredString(this.font, this.title, centerX, centerY - font.lineHeight * 4, 0xffffffff);

        int width = 24;
        int height = 24;
        centerX -= width / 2;
        centerY -= height / 2;

        int slotSize = 22;
        int padding = 4;

        PoseStack pose = guiGraphics.pose();
        for (int i = 0; i < items.size(); i++) {
            ItemStack stack = items.get(i);
            pose.pushPose();
            this.offset = Mth.lerp(partialTick / 2, this.offset, this.selectedIndex);
            pose.translate(-this.offset * (slotSize + padding), 0, 0);

            int x = i * (slotSize + padding);

            guiGraphics.blitSprite(SLOT, centerX + x + 1, centerY + 1, 22, 22);
            guiGraphics.renderItem(stack, centerX + x + 4, centerY + 4);
            pose.popPose();
        }

        guiGraphics.blitSprite(SLOT_SELECTED, centerX, centerY, 2, 24, 24);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int lastSelected = this.selectedIndex;
        this.selectedIndex = Math.floorMod(this.selectedIndex + (int) Math.signum(scrollY), this.types.size());
        lastSelectedType = this.types.get(this.selectedIndex);
        if(this.selectedIndex != lastSelected) {
            Minecraft.getInstance().player.playSound(SoundEvents.UI_BUTTON_CLICK.value, 0.3f, 1.1f);
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            CarvingClientHandler handler = CarvingClientHandler.instance;

            CarvingType type = this.types.get(this.selectedIndex);
            ResourceLocation id = NMLRegistries.CARVING_TYPE.getKey(type);
            handler.startChisel(this.startPos, this.face, this.hand, id);

            this.onClose();
            return true;
        }
        if(button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            this.onClose();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode >= GLFW.GLFW_KEY_1 && keyCode <= GLFW.GLFW_KEY_9) {
            int lastSelected = this.selectedIndex;
            this.selectedIndex = Math.clamp(keyCode - GLFW.GLFW_KEY_1, 0, this.types.size() - 1);
            lastSelectedType = this.types.get(this.selectedIndex);
            if (this.selectedIndex != lastSelected) {
                Minecraft.getInstance().player.playSound(SoundEvents.UI_BUTTON_CLICK.value, 0.3f, 1.1f);
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
