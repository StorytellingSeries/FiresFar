package com.qurenie.relics_thirteenflames.content.items.base;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface IRenderableCurioHand {
    
    HumanoidModel<? extends LivingEntity> getModel(ItemStack stack, HumanoidArm arm);
    
    ResourceLocation getTexture(ItemStack stack, HumanoidArm arm);
    
}
