package com.qurenie.relics_thirteenflames.mixins;

import com.qurenie.relics_thirteenflames.content.entities.RespawnBookEntity;
import com.qurenie.relics_thirteenflames.init.EffectsRegistry;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public class RegisterGoalMixin {
    
    @Shadow @Final public GoalSelector targetSelector;
    
    @Inject(method = "registerGoals", at = @At("HEAD"), remap = false)
    public void registerGoal(CallbackInfo ci) {
        if (((Object) this) instanceof Monster)
            this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>((Monster) (Object) this, RespawnBookEntity.class, true));
    }
    
    @Inject(method = "isNoAi", at = @At("HEAD"), remap = false, cancellable = true)
    public void isNoAi(CallbackInfoReturnable<Boolean> cir) {
        if (((Mob) (Object) this).hasEffect(EffectsRegistry.DISABILITY_EFFECT)
                && ((Mob) (Object) this).getEffect(EffectsRegistry.DISABILITY_EFFECT).getAmplifier() > 1) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }
}
