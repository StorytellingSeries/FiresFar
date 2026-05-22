package com.qurenie.relics_thirteenflames.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.qurenie.relics_thirteenflames.init.ItemsRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import static com.qurenie.relics_thirteenflames.init.ComponentRegistry.BLOCKED;
import static com.qurenie.relics_thirteenflames.init.ComponentRegistry.RHONAS_CHARGES;

public class RonasShieldItemRenderer
        extends ZeithTechISTER {

    private static final int FLAWLESS_OVERRIDES = 1;

    @Override
    public void renderByItem(@NotNull ItemStack pStack, @NotNull ItemDisplayContext pTransformType, @NotNull PoseStack poseStack, @NotNull MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        renderLanternOverrides(pStack, pTransformType, poseStack, pBuffer, pPackedLight, pPackedOverlay);
    }

    public void renderLanternOverrides(@NotNull ItemStack stack, @NotNull ItemDisplayContext transformType, @NotNull PoseStack pose, @NotNull MultiBufferSource bufferSource, int uv2, int overlay) {
        var mc = Minecraft.getInstance();
        var ir = mc.getItemRenderer();

        var isterModel = ir.getModel(stack, mc.level, mc.player, 0);
        var overrides = isterModel.getOverrides().getOverrides();
        
        boolean blocking = stack.getOrDefault(BLOCKED, false);
        if(mc.player != null && blocking) {
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

        int lightmap = 16711935;

        int charges = stack.getOrDefault(RHONAS_CHARGES, 0);

        boolean isFlawless = ItemsRegistry.RONAS_SHIELD.getRelicData(null, stack).isFlawless();

        pose.pushPose();
        renderOverrride(overrides.get(isFlawless ? 7 - charges : 7 - charges), transformType, pose, stack, bufferSource, RenderType.cutout(), uv2, overlay);

//        renderOverrride(overrides.get(3), transformType, pose, stack, bufferSource, null, lightmap, overlay);
        pose.popPose();
        pose.pushPose();
        if(charges > 0 || isFlawless) renderOverrride(overrides.get(isFlawless ?3 - charges : 3 - charges), transformType, pose, stack, bufferSource, null, lightmap, overlay);
        pose.popPose();
    }

}
