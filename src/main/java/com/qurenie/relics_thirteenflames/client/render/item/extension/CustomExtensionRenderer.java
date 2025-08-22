package com.qurenie.relics_thirteenflames.client.render.item.extension;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

public class CustomExtensionRenderer implements IClientItemExtensions {
    
    Supplier<BlockEntityWithoutLevelRenderer> renderer;
    
    @Override
    public HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
        return IClientItemExtensions.super.getHumanoidArmorModel(livingEntity, itemStack, equipmentSlot, original);
    }
    
    public CustomExtensionRenderer(Supplier<BlockEntityWithoutLevelRenderer> renderer) {
        this.renderer = Suppliers.memoize(renderer);
    }
    
    @Override
    public @NotNull BlockEntityWithoutLevelRenderer getCustomRenderer() {
        return renderer.get();
    }
    
}
