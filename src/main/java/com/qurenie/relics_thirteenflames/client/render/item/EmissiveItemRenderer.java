package com.qurenie.relics_thirteenflames.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class EmissiveItemRenderer
        extends ZeithTechISTER {

    @Override
    public void renderByItem(@NotNull ItemStack pStack, ItemTransforms.@NotNull TransformType pTransformType, @NotNull PoseStack poseStack, @NotNull MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        renderLanternOverrides(pStack, pTransformType, poseStack, pBuffer, pPackedLight, pPackedOverlay);
    }

    public void renderLanternOverrides(@NotNull ItemStack stack, @NotNull ItemTransforms.@NotNull TransformType transformType, @NotNull PoseStack pose, @NotNull MultiBufferSource bufferSource, int uv2, int overlay) {
        var mc = Minecraft.getInstance();
        var ir = mc.getItemRenderer();

        var isterModel = ir.getModel(stack, mc.level, mc.player, 0);
        var overrides = isterModel.getOverrides().getOverrides();

        for (int i = overrides.size() - 1; i >= 0; i--) {
            var override = overrides.get(i);

            int lightmap = uv2;
            if (i == 0) lightmap = 15728880;

            renderOverrride(override, transformType, pose, stack, bufferSource, null, lightmap, overlay);
        }
    }

}
