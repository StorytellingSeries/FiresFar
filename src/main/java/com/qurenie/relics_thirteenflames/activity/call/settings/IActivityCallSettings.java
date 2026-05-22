package com.qurenie.relics_thirteenflames.activity.call.settings;

import com.qurenie.relics_thirteenflames.activity.call.CallInput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface IActivityCallSettings {

    ResourceLocation getResourceLocation(LivingEntity entity, ItemStack itemStack);

    boolean isVisible(LivingEntity entity, ItemStack itemStack);

    ActivityResult cast(LivingEntity entity, ItemStack itemStack);

    InventoryType getInventoryType();

    void selectionNotify(LivingEntity entity, ItemStack itemStack, SelectionContext context);

}
