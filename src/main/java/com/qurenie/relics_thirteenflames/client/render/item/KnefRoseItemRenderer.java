package com.qurenie.relics_thirteenflames.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.qurenie.relics_thirteenflames.content.items.ItemKnefRose;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class KnefRoseItemRenderer
		extends ZeithTechISTER
{
	
	@Override
	public void renderByItem(ItemStack pStack, ItemDisplayContext pTransformType, PoseStack poseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay)
	{
		renderRoseOverrides(pStack, pTransformType, poseStack, pBuffer, pPackedLight, pPackedOverlay);
	}
	
	public void renderRoseOverrides(@NotNull ItemStack stack, @NotNull ItemDisplayContext transformType, @NotNull PoseStack pose, @NotNull MultiBufferSource bufferSource, int uv2, int overlay)
	{
		var mc = Minecraft.getInstance();
		var ir = mc.getItemRenderer();
		
		var isterModel = ir.getModel(stack, mc.level, mc.player, 0);
		var overrides = isterModel.getOverrides().getOverrides();
		
		var emission = stack.getItem() instanceof ItemKnefRose r && r.getBones(stack) > 0;
		
		pose.pushPose();
		if (transformType == ItemDisplayContext.GUI || transformType == ItemDisplayContext.GROUND || transformType == ItemDisplayContext.FIXED) {
			renderOverrride(emission ? overrides.get(0) : overrides.get(1), transformType, pose, stack, bufferSource, null, uv2, overlay);
		}
		else {
			renderOverrride(overrides.get(3), transformType, pose, stack, bufferSource, null, uv2, overlay);
			int lightmap = 15728880;
			if(emission) renderOverrride(overrides.get(2), transformType, pose, stack, bufferSource, null, lightmap, overlay);
		}
		pose.popPose();
	}
}
