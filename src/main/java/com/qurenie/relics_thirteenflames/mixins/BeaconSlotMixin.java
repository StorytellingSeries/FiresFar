package com.qurenie.relics_thirteenflames.mixins;

import com.qurenie.relics_thirteenflames.init.ItemsRegistry;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.world.inventory.BeaconMenu$PaymentSlot", remap = false)
public class BeaconSlotMixin {
    
    @Inject(method = "mayPlace", at = @At("RETURN"), cancellable = true, remap = false)
    private void onMayPlace(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(stack.is(ItemTags.BEACON_PAYMENT_ITEMS) || stack.is(ItemsRegistry.AURITEKH_INGOT));
        // если не наш тег — поведение упадёт к ваниле (если надо — можно явно возвратить false)
    }

}
