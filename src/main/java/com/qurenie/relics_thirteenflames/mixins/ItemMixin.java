package com.qurenie.relics_thirteenflames.mixins;

import com.qurenie.api.IActivityContainer;
import com.qurenie.relics_thirteenflames.init.ComponentRegistry;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(Item.class)
public class ItemMixin {

    @Inject(remap = false, method = "verifyComponentsAfterLoad", at = @At(value = "HEAD"))
    public void relicGeniusMixin(ItemStack stack, CallbackInfo ci) {
        if (stack.getItem() instanceof IActivityContainer && !stack.has(ComponentRegistry.UNIQUE_UUID))
            stack.set(ComponentRegistry.UNIQUE_UUID, UUID.randomUUID());
    }
}
