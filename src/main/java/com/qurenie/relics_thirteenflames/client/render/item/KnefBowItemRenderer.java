package com.qurenie.relics_thirteenflames.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.qurenie.relics_thirteenflames.init.ItemsRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class KnefBowItemRenderer
        extends ZeithTechISTER
{

    @Override
    public void renderByItem(ItemStack pStack, ItemTransforms.TransformType pTransformType, PoseStack poseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay)
    {

        renderLanternOverrides(pStack, pTransformType, poseStack, pBuffer, pPackedLight, pPackedOverlay);
    }

    public void renderLanternOverrides(@NotNull ItemStack stack, @NotNull ItemTransforms.@NotNull TransformType transformType, @NotNull PoseStack pose, @NotNull MultiBufferSource bufferSource, int uv2, int overlay)
    {
        var mc = Minecraft.getInstance();
        var ir = mc.getItemRenderer();


        var isterModel = ir.getModel(stack, mc.level, mc.player, 0);
        var overrides = isterModel.getOverrides().getOverrides();

        for(int i = overrides.size() - 1; i >= 0; i--)
        {
            var override = overrides.get(i);
            int lightmap = 16711935;

            float pull = ItemProperties.getProperty(ItemsRegistry.KNEF_BOW, new ResourceLocation("relics_thirteenflames", "pull")).call(stack, mc.level, mc.player, 0);
            if(pull < 0.1){ //Хуета какая-то, чтоб я еще раз что бы то ни было с рендерами делал
                renderOverrride(overrides.get(7), transformType, pose, stack, bufferSource, null, uv2, overlay);
                renderOverrride(overrides.get(6), transformType, pose, stack, bufferSource, null, lightmap, overlay);
            } else if (pull < 0.65) {
                renderOverrride(overrides.get(5), transformType, pose, stack, bufferSource, null, uv2, overlay);
                renderOverrride(overrides.get(4), transformType, pose, stack, bufferSource, null, lightmap, overlay);
            } else if (pull < 0.9) {
                renderOverrride(overrides.get(3), transformType, pose, stack, bufferSource, null, uv2, overlay);
                renderOverrride(overrides.get(2), transformType, pose, stack, bufferSource, null, lightmap, overlay);
            } else{
                renderOverrride(overrides.get(1), transformType, pose, stack, bufferSource, null, uv2, overlay);
                renderOverrride(overrides.get(0), transformType, pose, stack, bufferSource, null, lightmap, overlay);
            }
        }
    }
}
