package com.qurenie.relics_thirteenflames.mixins;

import com.qurenie.api.event.AnvilUpdatePostEvent;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.DataSlot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.neoforged.neoforge.common.NeoForge.EVENT_BUS;

@Mixin(AnvilMenu.class)
public class AnvilMixin {
    
    @Shadow @Final private DataSlot cost;
    
    @Inject(method = "createResult",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/DataSlot;get()I", ordinal = 1), remap = false)
    public void createResultEvent(CallbackInfo ci) {
        AnvilUpdatePostEvent event = new AnvilUpdatePostEvent(cost.get(), ((ItemCombinerAccessor) this).getPlayer());
        EVENT_BUS.post(event);
        cost.set(event.getCost());
    }
    
}
