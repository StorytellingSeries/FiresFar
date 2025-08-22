package com.qurenie.relics_thirteenflames.mixins;

import com.qurenie.relics_thirteenflames.util.MixinHooks;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityPushMixin {
    
    @Inject(remap = false, method = "push(Lnet/minecraft/world/entity/Entity;)V", cancellable = true,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;push(DDD)V", remap = false))
    public void push(Entity entity, CallbackInfo ci) {
        if (MixinHooks.isEntityTravellerBoosted(entity)
                || MixinHooks.isEntityTravellerBoosted((Entity) (Object) this))
            ci.cancel();
    }
    
}
