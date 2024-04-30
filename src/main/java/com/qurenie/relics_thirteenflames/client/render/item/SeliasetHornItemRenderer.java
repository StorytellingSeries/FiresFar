package com.qurenie.relics_thirteenflames.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.qurenie.relics_thirteenflames.init.ItemsRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SeliasetHornItemRenderer
        extends EmissiveItemRenderer {

    @Override
    public void renderByItem(@NotNull ItemStack pStack, ItemDisplayContext transformType, @NotNull PoseStack poseStack, @NotNull MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        Minecraft mc = Minecraft.getInstance();
        if(mc.player != null && mc.player.getUseItem().is(ItemsRegistry.SELIASET_HORN)) {
            if (transformType == ItemDisplayContext.THIRD_PERSON_LEFT_HAND) {
                poseStack.mulPose(Axis.YP.rotationDegrees(50));
                poseStack.translate(-0.65, -0.2, 0.3);
            }
            if (transformType == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND) {
                poseStack.mulPose(Axis.YP.rotationDegrees(-50));
                poseStack.translate(0.35, -0.2, -0.6);
            }


        }

        super.renderByItem(pStack, transformType, poseStack, pBuffer, pPackedLight, pPackedOverlay);
    }


}
