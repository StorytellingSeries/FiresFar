package com.qurenie.relics_thirteenflames.activity.call.settings;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface IActivityCallSettings {

    ResourceLocation getResourceLocation(LivingEntity entity, ItemStack itemStack);

    boolean isVisible(LivingEntity entity, ItemStack itemStack);

    ActivityResult cast(LivingEntity entity, ItemStack itemStack);

    InventoryType getInventoryType();

}
