package com.qurenie.relics_thirteenflames.mixins;

import com.qurenie.relics_thirteenflames.client.render.entity.IJodahGlowed;
import com.qurenie.relics_thirteenflames.init.DamageSourceRegistry;
import com.qurenie.relics_thirteenflames.init.EffectsRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;


@Mixin(LivingEntity.class)
@Implements(@Interface(iface = IJodahGlowed.class, prefix = "jg$"))
public class LivingEntityMixin {
    
    @Unique
    private static final EntityDataAccessor<Boolean> JODAH_GLOW = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.BOOLEAN);
    
    @Unique
    private boolean firesFar$hasJodahGlowEffect;
    
    @Shadow(remap = false)
    private DamageSource lastDamageSource;
    @Shadow(remap = false)
    private long lastDamageStamp;
    
    @Shadow
    private boolean effectsDirty;
    
    @Inject(method = "hurt", at = @At(value = "HEAD"), remap = false)
    public void hurt(DamageSource pSource, float pAmount, CallbackInfoReturnable<Boolean> cir) {
        if (Objects.equals(pSource, DamageSourceRegistry.SUCC)) {
            this.lastDamageSource = DamageSourceRegistry.SUCC;
            this.lastDamageStamp = ((LivingEntity) (Object) this).level().getGameTime();
            //((LivingEntity)(Object)this).level.broadcastEntityEvent(((LivingEntity)(Object)this), (byte) 29);
            float absorbedo = ((LivingEntity) (Object) this).getAbsorptionAmount();
            ((LivingEntity) (Object) this).setAbsorptionAmount(absorbedo - pAmount);
            
            pAmount -= absorbedo;
            if (pAmount > 0) {
                ((LivingEntity) (Object) this).setHealth(((LivingEntity) (Object) this).getHealth() - pAmount);
            }
            cir.cancel();
        }
    }
    
    @Inject(method = "getHurtSound", at = @At(value = "HEAD"), cancellable = true, remap = false)
    public void getHurtSound(DamageSource pSource, CallbackInfoReturnable<SoundSource> cir) {
        if (Objects.equals(pSource, DamageSourceRegistry.SUCC)) {
            cir.setReturnValue(null);
            cir.cancel();
        }
    }
    
    @Inject(method = "defineSynchedData", at = @At(value = "TAIL"), remap = false)
    public void defineSyncData(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(JODAH_GLOW, false);
    }
    
    @Inject(method = "readAdditionalSaveData", at = @At(value = "TAIL"), remap = false)
    public void readData(CompoundTag compound, CallbackInfo ci) {
        firesFar$hasJodahGlowEffect = compound.getBoolean("firesFar$hasJodahGlowEffect");
    }
    
    @Inject(method = "addAdditionalSaveData", at = @At(value = "TAIL"), remap = false)
    public void addData(CompoundTag compound, CallbackInfo ci) {
        if (firesFar$hasJodahGlowEffect)
            compound.putBoolean("firesFar$hasJodahGlowEffect", true);
    }
    
    @Inject(method = "updateGlowingStatus", at = @At(value = "HEAD"), remap = false)
    public void tickEffects(CallbackInfo ci) {
        firesFar$updateJodahGlowStatus();
    }
    
    @Unique
    private void firesFar$updateJodahGlowStatus() {
        boolean flag = this.jg$hasJodahGlowEffect();
        if (((LivingEntity) (Object) this).getEntityData().get(JODAH_GLOW) != flag)
            ((LivingEntity) (Object) this).getEntityData().set(JODAH_GLOW, flag);
    }
    
    public boolean jg$hasJodahGlowEffect() {
        return !((LivingEntity) (Object) this).level().isClientSide()
                ? ((LivingEntity) (Object) this).hasEffect(EffectsRegistry.JODAH_VISION) || firesFar$hasJodahGlowEffect
                : ((LivingEntity) (Object) this).getEntityData().get(JODAH_GLOW);
    }
    
    public void jg$setJodahGlowEffect(boolean enabled) {
        this.firesFar$hasJodahGlowEffect = enabled;
        ((LivingEntity) (Object) this).getEntityData().set(JODAH_GLOW, enabled);
    }
    
}
