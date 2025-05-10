package com.qurenie.relics_thirteenflames.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import it.hurts.sskirillss.relics.utils.NBTUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import static com.qurenie.relics_thirteenflames.init.ComponentRegistry.PULL;

public class KnefBowItemRenderer
        extends ZeithTechISTER {

    @Override
    public void renderByItem(ItemStack pStack, ItemDisplayContext pTransformType, PoseStack poseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {

        renderLanternOverrides(pStack, pTransformType, poseStack, pBuffer, pPackedLight, pPackedOverlay);
    }

    public void renderLanternOverrides(@NotNull ItemStack stack, @NotNull ItemDisplayContext transformType, @NotNull PoseStack pose, @NotNull MultiBufferSource bufferSource, int uv2, int overlay) {

        var mc = Minecraft.getInstance();
        var ir = mc.getItemRenderer();


        var isterModel = ir.getModel(stack, mc.level, mc.player, 0);
        var overrides = isterModel.getOverrides().getOverrides();


        int lightmap = 16711935;
        float pull = stack.getOrDefault(PULL, 0f);

        if (transformType == ItemDisplayContext.GUI || transformType == ItemDisplayContext.GROUND || transformType == ItemDisplayContext.FIXED) {
            if (pull < 0.1) {
                renderOverrride(overrides.get(11), transformType, pose, stack, bufferSource, null, uv2, overlay);
            } else if (pull < 0.65) {
                renderOverrride(overrides.get(10), transformType, pose, stack, bufferSource, null, uv2, overlay);
            } else if (pull < 0.9) {
                renderOverrride(overrides.get(9), transformType, pose, stack, bufferSource, null, uv2, overlay);
            } else {
                renderOverrride(overrides.get(8), transformType, pose, stack, bufferSource, null, uv2, overlay);
            }
        }
        else {
            if (pull < 0.1) {
                renderOverrride(overrides.get(7), transformType, pose, stack, bufferSource, null, uv2, overlay);
                renderOverrride(overrides.get(6), transformType, pose, stack, bufferSource, null, lightmap, overlay);
            } else if (pull < 0.65) {
                renderOverrride(overrides.get(5), transformType, pose, stack, bufferSource, null, uv2, overlay);
                renderOverrride(overrides.get(4), transformType, pose, stack, bufferSource, null, lightmap, overlay);
            } else if (pull < 0.9) {
                renderOverrride(overrides.get(3), transformType, pose, stack, bufferSource, null, uv2, overlay);
                renderOverrride(overrides.get(2), transformType, pose, stack, bufferSource, null, lightmap, overlay);
            } else {
                renderOverrride(overrides.get(1), transformType, pose, stack, bufferSource, null, uv2, overlay);
                renderOverrride(overrides.get(0), transformType, pose, stack, bufferSource, null, lightmap, overlay);
            }
        }
    }
}
