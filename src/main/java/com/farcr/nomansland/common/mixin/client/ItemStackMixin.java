package com.farcr.nomansland.common.mixin.client;

import com.farcr.nomansland.client.renderer.rendertype.MoonlightGlowRenderType;
import com.farcr.nomansland.common.friend.dialogue.DialogueUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.List;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Shadow public abstract Item getItem();

    @Inject(method = "getTooltipLines", at = @At("RETURN"), cancellable = true)
    public void nameAppend(Item.TooltipContext tooltipContext, @Nullable Player player, TooltipFlag tooltipFlag, CallbackInfoReturnable<List<Component>> cir) {
        if (!MoonlightGlowRenderType.itemCanBeOffered(this.getItem()))
            return;

        List<Component> originalList = cir.getReturnValue();
        originalList.add(1, Component.literal("*").withColor(DialogueUtil.FRIEND_MOON_TEXT_COLOR));
        cir.setReturnValue(originalList);
    }
}
