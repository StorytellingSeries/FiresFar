package com.qurenie.relics_thirteenflames.client.render.item.extension;

import com.google.common.base.Supplier;
import com.qurenie.relics_thirteenflames.init.ItemsRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import static com.qurenie.relics_thirteenflames.client.render.item.extension.RonasEnumExtension.FLAMES_ARM_POSE_LEFT;
import static com.qurenie.relics_thirteenflames.client.render.item.extension.RonasEnumExtension.FLAMES_ARM_POSE_RIGHT;

public class RonasShieldExtension extends CustomExtensionRenderer {
    
    public RonasShieldExtension(Supplier<BlockEntityWithoutLevelRenderer> renderer) {
        super(renderer);
    }
    
    @Override
    public HumanoidModel.ArmPose getArmPose(@NotNull LivingEntity entityLiving, @NotNull InteractionHand hand, @NotNull ItemStack itemStack) {
        return entityLiving.getUseItem().is(ItemsRegistry.RONAS_SHIELD) ? entityLiving.getUsedItemHand().equals(InteractionHand.OFF_HAND) && Minecraft.getInstance().options.mainHand().get() == HumanoidArm.RIGHT ? FLAMES_ARM_POSE_LEFT.getValue() : FLAMES_ARM_POSE_RIGHT.getValue() : HumanoidModel.ArmPose.ITEM;
    }
    
}
