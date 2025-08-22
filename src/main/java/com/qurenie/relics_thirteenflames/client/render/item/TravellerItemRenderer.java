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
                poseStack.mulPose(Axis.YP.rotationDegrees(30));
                poseStack.mulPose(Axis.XP.rotationDegrees(15));
                poseStack.translate(-0.4, 0.3, 0);
            }
            if (pTransformType == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND) {
                poseStack.mulPose(Axis.YP.rotationDegrees(-30));
                poseStack.mulPose(Axis.XP.rotationDegrees(-15));
                poseStack.translate(0.4, 0.3, -0.2);
            }
            if (pTransformType == ItemDisplayContext.FIRST_PERSON_LEFT_HAND) {
                poseStack.mulPose(Axis.YP.rotationDegrees(15));
                poseStack.translate(-0.2, 0.2, -0.6);
            }
            if (pTransformType == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND) {
                poseStack.mulPose(Axis.YP.rotationDegrees(-15));
                poseStack.translate(-0.2, 0.2, 0.6);
            }
        }
        
        super.renderByItem(pStack, pTransformType, poseStack, pBuffer, pPackedLight, pPackedOverlay);
    }
    
}
