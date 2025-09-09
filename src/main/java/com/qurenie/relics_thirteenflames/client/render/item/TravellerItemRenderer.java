package com.qurenie.relics_thirteenflames.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.qurenie.relics_thirteenflames.init.ComponentRegistry;
import com.qurenie.relics_thirteenflames.init.ItemsRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class TravellerItemRenderer extends EmissiveItemRenderer {
    
    @Override
    public void renderByItem(@NotNull ItemStack pStack, @NotNull ItemDisplayContext pTransformType, @NotNull PoseStack poseStack, @NotNull MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        if(pStack.is(ItemsRegistry.TRAVELLER_SWORD) && pStack.getOrDefault(ComponentRegistry.SPEED, 0f) >= 2) {
            if (pTransformType == ItemDisplayContext.THIRD_PERSON_LEFT_HAND) {
                poseStack.translate(-0.35, 0.3, 0.5);
                poseStack.mulPose(Axis.ZP.rotationDegrees(15));
                poseStack.mulPose(Axis.XP.rotationDegrees(15));
                poseStack.mulPose(Axis.YP.rotationDegrees(35));
            }
            if (pTransformType == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND) {
                poseStack.translate(0.35, 0.3, -0.3);
                poseStack.mulPose(Axis.ZP.rotationDegrees(15));
                poseStack.mulPose(Axis.XP.rotationDegrees(-15));
                poseStack.mulPose(Axis.YP.rotationDegrees(-35));
            }
            if (pTransformType == ItemDisplayContext.FIRST_PERSON_LEFT_HAND) {
                poseStack.translate(0, 0.4, 0.6);
                poseStack.mulPose(Axis.YP.rotationDegrees(15));
                poseStack.mulPose(Axis.XP.rotationDegrees(30));
            }
            if (pTransformType == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND) {
                poseStack.translate(0.2, -0.1, -0.6);
                poseStack.mulPose(Axis.YP.rotationDegrees(-15));
                poseStack.mulPose(Axis.XP.rotationDegrees(-30));
            }
        }
        
        super.renderByItem(pStack, pTransformType, poseStack, pBuffer, pPackedLight, pPackedOverlay);
    }
    
}
