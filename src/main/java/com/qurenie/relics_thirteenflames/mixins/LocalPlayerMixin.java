package com.qurenie.relics_thirteenflames.mixins;

import com.qurenie.relics_thirteenflames.content.items.ItemKnefBow;
import com.qurenie.relics_thirteenflames.content.items.ItemRonasShield;
import com.qurenie.relics_thirteenflames.init.DamageSourceRegistry;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;


@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends LivingEntityMixin {

    @Inject(method = "hurt", at = @At(value = "HEAD"), cancellable = true)
    public void hurt(DamageSource pSource, float pAmount, CallbackInfoReturnable<Boolean> cir) {
        if(Objects.equals(pSource, DamageSourceRegistry.SUCC)){
            super.hurt(pSource, pAmount, cir);
            cir.cancel();
        }
    }

    @Inject(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z"))
    public void mojank(CallbackInfo ci) {
        LocalPlayer deez = ((LocalPlayer)(Object) this);
        if(deez.getUseItem().getItem() instanceof ItemRonasShield shit) {
            float speedmodif = (float) shit.getAbilityValue(deez.getUseItem(), "block", "speed");
            deez.input.leftImpulse *= 5F * speedmodif;
            deez.input.forwardImpulse *= 5F * speedmodif;
        }
    }
}
