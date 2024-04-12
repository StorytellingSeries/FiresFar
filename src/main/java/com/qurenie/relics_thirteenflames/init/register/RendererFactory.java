package com.qurenie.relics_thirteenflames.init.register;

import com.qurenie.relics_thirteenflames.client.render.entity.CommonRenderer;
import lombok.Builder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.zeith.hammeranims.api.geometry.IGeometryContainer;
import org.zeith.hammeranims.api.tile.IAnimatedEntity;

import java.util.function.Function;

public interface RendererFactory<T extends LivingEntity & IAnimatedEntity, R extends CommonRenderer<T>>
{
	R create(EntityRendererProvider.Context manager, ModelConfiguration modelConfiguration, float shadowSize, ResourceLocation texture);
	
	static <T extends LivingEntity & IAnimatedEntity, R extends CommonRenderer<T>> WithScale<T, R> wrap(RendererFactory<T, R> origin)
	{
		return (manager, modelConfiguration, shadowSize, texture, scale) -> origin.create(manager, modelConfiguration, shadowSize, texture);
	}
	
	interface WithScale<T extends LivingEntity & IAnimatedEntity, R extends CommonRenderer<T>>
	{
		R create(EntityRendererProvider.Context manager, ModelConfiguration modelConfiguration, float shadowSize, ResourceLocation texture, float scale);
	}
	
	@Builder
	record ModelConfiguration(IGeometryContainer model, Function<ResourceLocation, RenderType> renderType)
	{
		public ModelConfiguration(IGeometryContainer model)
		{
			this(model, RenderType::entityCutoutNoCull);
		}
	}
}