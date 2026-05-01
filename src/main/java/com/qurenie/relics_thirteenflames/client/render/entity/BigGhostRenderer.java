package com.qurenie.relics_thirteenflames.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.qurenie.relics_thirteenflames.content.entities.GhostBigEntity;
import com.qurenie.relics_thirteenflames.content.entities.GhostSmallEntity;
import com.qurenie.relics_thirteenflames.init.register.RendererFactory;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class BigGhostRenderer
		extends CommonRenderer<GhostBigEntity>
{
	protected float scale;

	public BigGhostRenderer(EntityRendererProvider.Context manager, RendererFactory.ModelConfiguration modelConfiguration, float shadowSize, ResourceLocation texture, float scale)
	{
		super(manager, modelConfiguration, shadowSize, texture);
		this.scale = scale;
	}
	
	protected float getScale(GhostBigEntity entity)
	{
		return (float) (scale * Math.pow(entity.getScale(), 1 / 4f));
	}
	
	@Override
	protected void renderNameTag(@NotNull GhostBigEntity p_114498_, @NotNull Component p_114499_, PoseStack p_114500_, MultiBufferSource p_114501_, int p_114502_, float p_316698_) {
	}
	
	@Override
	protected void setupRotations(@NotNull GhostBigEntity pEntityLiving, @NotNull PoseStack pMatrixStack, float pAgeInTicks, float pRotationYaw, float pPartialTicks, float p_320045_) {
		float sc = getScale(pEntityLiving);
		pMatrixStack.scale(sc, sc, sc);
		super.setupRotations(pEntityLiving, pMatrixStack, pAgeInTicks, pRotationYaw, pPartialTicks, p_320045_);
	}

}