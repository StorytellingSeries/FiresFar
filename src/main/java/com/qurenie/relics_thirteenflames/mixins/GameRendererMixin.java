package com.qurenie.relics_thirteenflames.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import com.qurenie.relics_thirteenflames.content.items.ItemKnefBow;
import com.qurenie.relics_thirteenflames.init.ItemsRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;
import java.util.Random;


@Mixin(GameRenderer.class)
public class GameRendererMixin {


    @Inject(method = "bobHurt", at = @At(value = "HEAD"), cancellable = true)
    public void bobHurt(PoseStack pMatrixStack, float pPartialTicks, CallbackInfo ci) {
        Player player = Minecraft.getInstance().player;
        if(player != null && Objects.equals(player.getLastDamageSource(), ItemKnefBow.SUCC)) ci.cancel();
    }

}
