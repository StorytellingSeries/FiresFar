package com.qurenie.relics_thirteenflames.mixins;

import com.qurenie.relics_thirteenflames.content.items.ItemKnefBow;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;


@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntityMixin {

    @Inject(method = "hurt", at = @At(value = "HEAD"), cancellable = true)
    public void hurt(DamageSource pSource, float pAmount, CallbackInfoReturnable<Boolean> cir) {
        if(Objects.equals(pSource, ItemKnefBow.SUCC)){
            super.hurt(pSource, pAmount, cir);
            cir.cancel();
        }
    }

}
