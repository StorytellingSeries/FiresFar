package com.qurenie.relics_thirteenflames.activity.call.settings;

import lombok.AllArgsConstructor;
import lombok.Builder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;

@Builder
@AllArgsConstructor
public class ActivityCallSettings implements IActivityCallSettings {

    BiFunction<LivingEntity, ItemStack, ResourceLocation> resourceLocation;
    @Builder.Default
    BiPredicate<LivingEntity, ItemStack> visibility = (e, s) -> true;
    @Builder.Default
    InventoryType inventoryType = InventoryType.INVENTORY;
    BiFunction<LivingEntity, ItemStack, ActivityResult> cast;

    @Override
    public ResourceLocation getResourceLocation(LivingEntity entity, ItemStack itemStack) {
        return resourceLocation.apply(entity, itemStack);
    }

    @Override
    public boolean isVisible(LivingEntity entity, ItemStack itemStack) {
        return visibility.test(entity, itemStack);
    }

    @Override
    public ActivityResult cast(LivingEntity entity, ItemStack itemStack) {
        return cast.apply(entity, itemStack);
    }

    @Override
    public InventoryType getInventoryType() {
        return inventoryType;
    }
}
