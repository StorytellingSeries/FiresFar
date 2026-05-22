package com.qurenie.relics_thirteenflames.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.qurenie.relics_thirteenflames.ThirteenFlames;
import com.qurenie.relics_thirteenflames.content.entities.EntitySeliasetSun;
import com.qurenie.relics_thirteenflames.init.EntityModels;
import com.qurenie.relics_thirteenflames.init.ItemsRegistry;
import com.qurenie.relics_thirteenflames.init.register.RendererFactory;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.InventoryMenu;
import org.joml.Vector3f;
import org.zeith.hammerlib.client.utils.RenderUtils;
import org.zeith.hammerlib.client.utils.TexturePixelGetter;

public class EntityRendererSeliasetSun
		extends EntityRenderer<EntitySeliasetSun>
{
	public static EntityRendererSeliasetSun instance;
	public final SimpleBedrockModel<?> model;
	public final SimpleBedrockModel<?> model_flawless;

	public static final ResourceLocation TEXTURE = ThirteenFlames.rl("textures/item/seliaset_sun/base.png");
	public static final ResourceLocation TEXTURE_EMISSIVE = ThirteenFlames.rl("textures/item/seliaset_sun/emissive.png");
	
	public static final ResourceLocation BIG_TEXTURE = ThirteenFlames.rl("textures/item/seliaset_sun/big.png");
	public static final ResourceLocation BIG_TEXTURE_EMISSIVE = ThirteenFlames.rl("textures/item/seliaset_sun/big_emissive.png");


	public static final ResourceLocation TEXTURE_FLAWLESS = ThirteenFlames.rl("textures/item/seliaset_sun/flawless.png");
	public static final ResourceLocation TEXTURE_EMISSIVE_FLAWLESS = ThirteenFlames.rl("textures/item/seliaset_sun/flawless_emissive.png");


	public EntityRendererSeliasetSun(EntityRendererProvider.Context pContext)
	{
		super(pContext);
		this.model = new SimpleBedrockModel<>(RendererFactory.ModelConfiguration.builder()
				.model(EntityModels.SELIASET_SUN)
				.build());
		this.model_flawless = new SimpleBedrockModel<>(RendererFactory.ModelConfiguration.builder()
				.model(EntityModels.SELIASET_SUN_FLAWLESS)
				.build());
		instance = this;
	}
	
	@Override
	public void render(EntitySeliasetSun ent, float yaw, float partialTicks, PoseStack pose, MultiBufferSource src, int light)
	{
		float activity = ent.getActivity(partialTicks);
		boolean altTexture = activity > 0.5F;

		boolean isFlawless = ItemsRegistry.SELIASET_SUN.getRelicData(null, ent.getSunItem()).isFlawless();
		var model = isFlawless ? model_flawless : this.model;

		var body = isFlawless ? model.getBone("frame") : model.getBone("Body");
		var coreOuter = isFlawless ? model.getBone("frame2") : model.getBone("CoreOuter");
		if(body == null || coreOuter == null) return;
		light = LightTexture.pack(15, 15);
		
		var bodySize = body.getScale();
		var outerSize = coreOuter.getScale();
		
		var tmp1 = new Vector3f();
		var tmp2 = new Vector3f();
		
		
		pose.pushPose();
		pose.translate(0, activity + 2F / 16F * (1F - activity), 0);
		
		pose.pushPose();
		float glScale = 1F + activity * 2;
		pose.translate(0, -0.5F * activity, 0);
		pose.scale(glScale, glScale, glScale);
		
		model.applyAnimations(ent.animations, partialTicks);
		
		// Render inner solid part
		tmp1.set(bodySize);
		tmp2.set(outerSize);
		bodySize.set(0);
		outerSize.set(0);
		model.renderToBuffer(pose, src.getBuffer(RenderType.entityCutout(isFlawless ? getFlawlessTexture(ent, true) : altTexture ? BIG_TEXTURE_EMISSIVE : TEXTURE_EMISSIVE)),
				light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF
		);
		bodySize.set(tmp1);
		outerSize.set(tmp2);
		
		// Then render translucent parts
		model.renderToBuffer(pose, src.getBuffer(RenderType.entityTranslucent(isFlawless ? getFlawlessTexture(ent, false) : altTexture ? BIG_TEXTURE : TEXTURE)),
				light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF
		);
		pose.popPose();
		
		pose.pushPose();
		pose.translate(0, isFlawless ? 1 : 0.25, 0);
		glScale = 2F + activity * 5;
		pose.scale(glScale, glScale, glScale);
		if(activity > 0F)
		{
			int[] colors = TexturePixelGetter.getAllColors(isFlawless ? ThirteenFlames.rl("textures/item/seliaset_sun/seliaset_sun_upgraded_emissive1.png") : BIG_TEXTURE_EMISSIVE);
			
			RenderUtils.renderColorfulLightRayEffects(src, pose,
					i -> colors[i % colors.length] | 0xFF << 24, 4324423, (ent.tickCount + partialTicks) / 500F,
					5, activity * 2F * (isFlawless ? 1.25f : 1), isFlawless ? 48: 32
			);
		}
		pose.popPose();
		
		pose.popPose();
	}

	public ResourceLocation getFlawlessTexture(LivingEntity pEntity, boolean emissive)
	{
		return ThirteenFlames.rl(String.format("textures/item/seliaset_sun/seliaset_sun_upgraded%s%d.png",
				emissive ? "_emissive" : "", (pEntity.tickCount / 2) % 8 + 1));
	}
	
	@Override
	public ResourceLocation getTextureLocation(EntitySeliasetSun pEntity)
	{
		return InventoryMenu.BLOCK_ATLAS;
	}
	
	@Override
	public boolean shouldRender(EntitySeliasetSun pLivingEntity, Frustum pCamera, double pCamX, double pCamY, double pCamZ)
	{
		return true;
	}
}
