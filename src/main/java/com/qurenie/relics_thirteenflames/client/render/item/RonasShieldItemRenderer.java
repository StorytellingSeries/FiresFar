package com.qurenie.relics_thirteenflames.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.qurenie.relics_thirteenflames.init.ItemsRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class RonasShieldItemRenderer
        extends ZeithTechISTER {

    @Override
    public void renderByItem(@NotNull ItemStack pStack, ItemDisplayContext pTransformType, @NotNull PoseStack poseStack, @NotNull MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        renderLanternOverrides(pStack, pTransformType, poseStack, pBuffer, pPackedLight, pPackedOverlay);
    }

    public void renderLanternOverrides(@NotNull ItemStack stack, @NotNull ItemDisplayContext transformType, @NotNull PoseStack pose, @NotNull MultiBufferSource bufferSource, int uv2, int overlay) {
        var mc = Minecraft.getInstance();
        var ir = mc.getItemRenderer();

        var isterModel = ir.getModel(stack, mc.level, mc.player, 0);
        var overrides = isterModel.getOverrides().getOverrides();

        if(mc.player != null && mc.player.getUseItem().is(ItemsRegistry.RONAS_SHIELD)) {
            if (transformType == ItemDisplayContext.THIRD_PERSON_LEFT_HAND) {
                pose.translate(1.15, 0.5, 0);
                pose.mulPose(Axis.YP.rotationDegrees(-65));
                pose.mulPose(Axis.XN.rotationDegrees(-30));
            }
            if (transformType == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND) {
                pose.translate(-0.5, 0.5, 0.9);
                pose.mulPose(Axis.YP.rotationDegrees(65));
                pose.mulPose(Axis.XN.rotationDegrees(-30));
            }
            if (transformType == ItemDisplayContext.FIRST_PERSON_LEFT_HAND) {
                pose.translate(-0.45, -0.2, -0.8);
                pose.mulPose(Axis.ZN.rotationDegrees(10));
            }
            if (transformType == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND) {
                pose.translate(0.6, -0.35, -0.9);
                pose.mulPose(Axis.ZN.rotationDegrees(-15));
                pose.mulPose(Axis.XN.rotationDegrees(-5));
            }

        }
        if (transformType == ItemDisplayContext.GROUND) {
            pose.translate(0, 0.65, 0);
            pose.mulPose(Axis.XN.rotationDegrees(-90));
        }

        for (int i = overrides.size() - 1; i >= 0; i--) {
            var override = overrides.get(i);

            int lightmap = uv2;
            if (i == 0)
                lightmap = 15728880;

            renderOverrride(override, transformType, pose, stack, bufferSource, null, lightmap, overlay);
        }
    }

}
