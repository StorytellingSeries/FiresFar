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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;


@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Shadow
    private DamageSource lastDamageSource;
    @Shadow
    private long lastDamageStamp;
    @Inject(method = "hurt", at = @At(value = "HEAD"), cancellable = true)
    public void hurt(DamageSource pSource, float pAmount, CallbackInfoReturnable<Boolean> cir) {
        if(Objects.equals(pSource, ItemKnefBow.SUCC)){
            this.lastDamageSource = ItemKnefBow.SUCC;
            this.lastDamageStamp =  ((LivingEntity)(Object)this).level.getGameTime();
            //((LivingEntity)(Object)this).level.broadcastEntityEvent(((LivingEntity)(Object)this), (byte) 29);
            float absorbedo = ((LivingEntity)(Object)this).getAbsorptionAmount();
            ((LivingEntity)(Object)this).setAbsorptionAmount(absorbedo - pAmount);

            pAmount -= absorbedo;
            if(pAmount > 0) {
                ((LivingEntity) (Object) this).setHealth(((LivingEntity)(Object)this).getHealth() - pAmount);
            }
            cir.cancel();
        }
    }

    @Inject(method = "getHurtSound", at = @At(value = "HEAD"), cancellable = true)
    public void getHurtSound(DamageSource pSource, CallbackInfoReturnable<SoundSource> cir) {
        if(Objects.equals(pSource, ItemKnefBow.SUCC)){
            cir.setReturnValue(null);
            cir.cancel();
        }
    }

}
