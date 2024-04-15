package com.qurenie.relics_thirteenflames.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.qurenie.relics_thirteenflames.content.entities.LivingFleshEntity;
import com.qurenie.relics_thirteenflames.init.EntityModels;
import com.qurenie.relics_thirteenflames.init.register.RendererFactory;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class LivingFleshRenderer
		extends CommonRenderer<LivingFleshEntity>
{
	protected float scale;
	
	public LivingFleshRenderer(EntityRendererProvider.Context manager, RendererFactory.ModelConfiguration modelConfiguration, float shadowSize, ResourceLocation texture, float scale)
	{
		super(manager, modelConfiguration, shadowSize, texture);
		this.scale = scale;
	}
	
	protected float getScale(LivingFleshEntity entity)
	{
		return scale * entity.getScale();
	}
	
	@Override
	protected void renderNameTag(LivingFleshEntity pEntity, Component pDisplayName, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight)
	{
	}
	
	@Override
	protected void setupRotations(LivingFleshEntity pEntityLiving, PoseStack pMatrixStack, float pAgeInTicks, float pRotationYaw, float pPartialTicks)
	{
		float sc = getScale(pEntityLiving);
		pMatrixStack.scale(sc, sc, sc);
		super.setupRotations(pEntityLiving, pMatrixStack, pAgeInTicks, pRotationYaw, pPartialTicks);
	}
}