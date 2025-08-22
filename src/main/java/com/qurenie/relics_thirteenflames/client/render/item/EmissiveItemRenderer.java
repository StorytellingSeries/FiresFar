package com.qurenie.relics_thirteenflames.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.world.item.ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
import static net.minecraft.world.item.ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
import static net.neoforged.neoforge.client.ClientHooks.handleCameraTransforms;

public class EmissiveItemRenderer
        extends ZeithTechISTER {

    @Override
    public void renderByItem(@NotNull ItemStack pStack, @NotNull ItemDisplayContext pTransformType, @NotNull PoseStack poseStack, @NotNull MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        renderLanternOverrides(pStack, pTransformType, poseStack, pBuffer, pPackedLight, pPackedOverlay);
    }

    public void renderLanternOverrides(@NotNull ItemStack stack, @NotNull ItemDisplayContext transformType, @NotNull PoseStack pose, @NotNull MultiBufferSource bufferSource, int uv2, int overlay) {
        var mc = Minecraft.getInstance();
        var ir = mc.getItemRenderer();
        
        pose.pushPose();
        var isterModel = ir.getModel(stack, mc.level, mc.player, 0);
//        isterModel = handleCameraTransforms(pose, isterModel, transformType, transformType == THIRD_PERSON_LEFT_HAND || transformType == FIRST_PERSON_LEFT_HAND);;
        
        var overrides = isterModel.getOverrides().getOverrides();

        for (int i = overrides.size() - 1; i >= 0; i--) {
            var override = overrides.get(i);

            int lightmap = uv2;
            if (i == 0) lightmap = 15728880;

            renderOverrride(override, transformType, pose, stack, bufferSource, getRenderType(stack, i), lightmap, overlay);
        }
        
        pose.popPose();
    }
    
    @Nullable
    protected RenderType getRenderType(@NotNull ItemStack stack, int index) {
        return null;
    }

}
