package com.qurenie.relics_thirteenflames.mixins;

import com.qurenie.api.BabySpawnCountEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(BreedGoal.class)
public class BreedMixin {
    
    @Shadow @Final protected Animal animal;
    
    @Shadow @Nullable protected Animal partner;
    
    @Shadow @Final protected Level level;
    
    @Inject(remap = false, method = "breed", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/Animal;spawnChildFromBreeding(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/animal/Animal;)V"))
    public void breedGoal(CallbackInfo ci) {
        final BabySpawnCountEvent event = new BabySpawnCountEvent(animal, partner);
        int count = net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(event).getCount();
        
        if (count <= 0)
            ci.cancel();
        else
            while (count --> 0)
                this.animal.spawnChildFromBreeding((ServerLevel)level, partner);
    }
    
}
