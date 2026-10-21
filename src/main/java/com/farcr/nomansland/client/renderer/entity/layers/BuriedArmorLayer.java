package com.farcr.nomansland.client.renderer.entity.layers;

import com.farcr.nomansland.client.model.BuriedModel;
import com.farcr.nomansland.common.entity.Buried;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

public class BuriedArmorLayer extends RenderLayer<Buried, BuriedModel> {
    private static final ResourceLocation GOLD_OUTER = ResourceLocation.withDefaultNamespace("textures/models/armor/gold_layer_1.png");
    private static final ResourceLocation GOLD_INNER = ResourceLocation.withDefaultNamespace("textures/models/armor/gold_layer_2.png");

    private final ModelPart innerArmor;
    private final ModelPart outerArmor;

    public BuriedArmorLayer(RenderLayerParent<Buried, BuriedModel> parent, ModelPart innerArmor, ModelPart outerArmor) {
        super(parent);
        this.innerArmor = innerArmor;
        this.outerArmor = outerArmor;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, Buried entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        renderPiece(poseStack, buffer, packedLight, entity, EquipmentSlot.HEAD, this.outerArmor, GOLD_OUTER);
        renderPiece(poseStack, buffer, packedLight, entity, EquipmentSlot.CHEST, this.outerArmor, GOLD_OUTER);
        renderPiece(poseStack, buffer, packedLight, entity, EquipmentSlot.LEGS, this.innerArmor, GOLD_INNER);
        renderPiece(poseStack, buffer, packedLight, entity, EquipmentSlot.FEET, this.outerArmor, GOLD_OUTER);
    }

    private void renderPiece(PoseStack poseStack, MultiBufferSource buffer, int packedLight, Buried entity, EquipmentSlot slot, ModelPart armorRoot, ResourceLocation texture) {
        ItemStack stack = entity.getItemBySlot(slot);
        if (!(stack.getItem() instanceof ArmorItem)) return;
        copyPose(armorRoot);
        setPartVisibility(armorRoot, slot);
        VertexConsumer consumer = buffer.getBuffer(RenderType.armorCutoutNoCull(texture));
        armorRoot.render(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY);
    }

    private void copyPose(ModelPart armorRoot) {
        ModelPart source = this.getParentModel().root().getChild("buried");
        ModelPart target = armorRoot.getChild("buried");
        target.copyFrom(source);
        target.getChild("LeftLeg").copyFrom(source.getChild("LeftLeg"));
        target.getChild("RightLeg").copyFrom(source.getChild("RightLeg"));

        ModelPart sourceBody = source.getChild("Body");
        ModelPart targetBody = target.getChild("Body");
        targetBody.copyFrom(sourceBody);
        targetBody.getChild("Head").copyFrom(sourceBody.getChild("Head"));
        targetBody.getChild("LeftArm").copyFrom(sourceBody.getChild("LeftArm"));
        targetBody.getChild("RightArm").copyFrom(sourceBody.getChild("RightArm"));
    }

    private void setPartVisibility(ModelPart armorRoot, EquipmentSlot slot) {
        ModelPart buried = armorRoot.getChild("buried");
        ModelPart body = buried.getChild("Body");
        buried.visible = true;
        body.visible = true;
        body.getChild("Head").visible = slot == EquipmentSlot.HEAD;
        body.getChild("BodyArmor").visible = slot == EquipmentSlot.CHEST || slot == EquipmentSlot.LEGS;
        boolean arms = slot == EquipmentSlot.CHEST;
        body.getChild("LeftArm").visible = arms;
        body.getChild("RightArm").visible = arms;
        boolean legs = slot == EquipmentSlot.LEGS || slot == EquipmentSlot.FEET;
        buried.getChild("LeftLeg").visible = legs;
        buried.getChild("RightLeg").visible = legs;
    }
}
