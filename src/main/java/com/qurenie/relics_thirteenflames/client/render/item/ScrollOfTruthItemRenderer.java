package com.qurenie.relics_thirteenflames.client.render.item;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.qurenie.relics_thirteenflames.content.items.ItemKnefRose;
import com.qurenie.relics_thirteenflames.content.items.scroll_of_truth.ScrollColorMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.zeith.hammerlib.client.render.TintingVertexConsumer;

import java.awt.*;

import static com.qurenie.relics_thirteenflames.init.ComponentRegistry.SCROLL_COLOR_MODE;

public class ScrollOfTruthItemRenderer
		extends ZeithTechISTER
{
	
	@Override
	public void renderByItem(ItemStack pStack, ItemDisplayContext pTransformType, PoseStack poseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay)
	{
		renderScrollOverrides(pStack, pTransformType, poseStack, pBuffer, pPackedLight, pPackedOverlay);
	}
	
	public void renderScrollOverrides(@NotNull ItemStack stack, @NotNull ItemDisplayContext transformType, @NotNull PoseStack pose, @NotNull MultiBufferSource bufferSource, int uv2, int overlay)
	{
		var mc = Minecraft.getInstance();
		var ir = mc.getItemRenderer();
		
		var isterModel = ir.getModel(stack, mc.level, mc.player, 0);
		var overrides = isterModel.getOverrides().getOverrides();

		Color color = stack.getOrDefault(SCROLL_COLOR_MODE, ScrollColorMode.RED).color;

		int lightmap = 15728880;

		pose.pushPose();
		if (transformType == ItemDisplayContext.GUI || transformType == ItemDisplayContext.GROUND || transformType == ItemDisplayContext.FIXED) {
			renderOverrride(overrides.get(1), transformType, pose, stack, bufferSource, null, uv2, overlay);

			renderOverrride(overrides.get(0), transformType, pose, stack, type -> TintingVertexConsumer.wrap(bufferSource.getBuffer(type), color.getRed()/255f, color.getGreen()/255f, color.getBlue()/255f, 1), null, lightmap, overlay);
		}
		else {
			renderOverrride(overrides.get(3), transformType, pose, stack, bufferSource, null, uv2, overlay);

			renderOverrride(overrides.get(2), transformType, pose, stack, type -> TintingVertexConsumer.wrap(bufferSource.getBuffer(type), color.getRed()/255f, color.getGreen()/255f, color.getBlue()/255f, 1), null, lightmap, overlay);
		}
		pose.popPose();
		

	}
}
