package com.qurenie.relics_thirteenflames.mixins;

import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractContainerMenu.class)
public interface AbstractContainerMenuAccessor {
    
    @Accessor(value = "lastSlots", remap = false)
    NonNullList<ItemStack> getLastSlots();
    
    @Accessor(value = "remoteSlots", remap = false)
    NonNullList<ItemStack> getRemoteSlots();

}
