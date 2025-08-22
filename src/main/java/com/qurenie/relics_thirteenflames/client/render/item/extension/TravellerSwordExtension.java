package com.qurenie.relics_thirteenflames.client.render.item.extension;

import com.google.common.base.Supplier;
import com.qurenie.relics_thirteenflames.init.ComponentRegistry;
import com.qurenie.relics_thirteenflames.init.ItemsRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import static com.qurenie.relics_thirteenflames.client.render.item.extension.TravellerEnumExtension.TRAVELLER_ARM_POSE_LEFT;
import static com.qurenie.relics_thirteenflames.client.render.item.extension.TravellerEnumExtension.TRAVELLER_ARM_POSE_RIGHT;

public class TravellerSwordExtension extends CustomExtensionRenderer {
    
    public TravellerSwordExtension(Supplier<BlockEntityWithoutLevelRenderer> renderer) {
        super(renderer);
    }
    
    @Override
    public HumanoidModel.ArmPose getArmPose(@NotNull LivingEntity entityLiving, @NotNull InteractionHand hand, @NotNull ItemStack itemStack) {
        return itemStack.is(ItemsRegistry.TRAVELLER_SWORD) && itemStack.getOrDefault(ComponentRegistry.SPEED, 0f) >= 2
                ? hand == InteractionHand.OFF_HAND ? TRAVELLER_ARM_POSE_LEFT.getValue() : TRAVELLER_ARM_POSE_RIGHT.getValue() : null;
    }
    
}
