package com.qurenie.relics_thirteenflames.mixins;

import com.qurenie.relics_thirteenflames.content.items.ItemKnefBow;
import com.qurenie.relics_thirteenflames.init.DamageSourceRegistry;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;


@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Shadow(remap = false)
    private DamageSource lastDamageSource;
    @Shadow(remap = false)
    private long lastDamageStamp;
    
    @Inject(method = "hurt", at = @At(value = "HEAD"), remap = false)
    public void hurt(DamageSource pSource, float pAmount, CallbackInfoReturnable<Boolean> cir) {
        if(Objects.equals(pSource, DamageSourceRegistry.SUCC)){
            this.lastDamageSource = DamageSourceRegistry.SUCC;
            this.lastDamageStamp =  ((LivingEntity)(Object)this).level().getGameTime();
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

    @Inject(method = "getHurtSound", at = @At(value = "HEAD"), cancellable = true, remap = false)
    public void getHurtSound(DamageSource pSource, CallbackInfoReturnable<SoundSource> cir) {
        if(Objects.equals(pSource, DamageSourceRegistry.SUCC)){
            cir.setReturnValue(null);
            cir.cancel();
        }
    }

}
