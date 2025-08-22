package com.qurenie.relics_thirteenflames.mixins;

import com.qurenie.api.MeleeAttackCheckEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.neoforged.neoforge.common.NeoForge.EVENT_BUS;

@Mixin(MeleeAttackGoal.class)
public class MeleeAttackMixin {
    
    @Shadow @Final protected PathfinderMob mob;
    
    @Inject(remap = false, method = "canPerformAttack", cancellable = true, at = @At(value = "HEAD"))
    public void check(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        MeleeAttackCheckEvent event = new MeleeAttackCheckEvent(this.mob, entity);
        EVENT_BUS.post(event);
        if (event.isCanceled()) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
    
}
