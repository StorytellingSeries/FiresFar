package com.qurenie.relics_thirteenflames.mixins.client;

import com.qurenie.relics_thirteenflames.content.items.ItemKnefBow;
import com.qurenie.relics_thirteenflames.init.DamageSourceRegistry;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {


    @Inject(method = "getOverlayCoords", at = @At(value = "HEAD"), cancellable = true, remap = false)
    private static void getOverlayCoords(LivingEntity pLivingEntity, float pU, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(OverlayTexture.pack(OverlayTexture.u(pU), OverlayTexture.v((pLivingEntity.hurtTime > 0 || pLivingEntity.deathTime > 0) && pLivingEntity.getLastDamageSource() != DamageSourceRegistry.SUCC)));
        cir.cancel();
    }



}
