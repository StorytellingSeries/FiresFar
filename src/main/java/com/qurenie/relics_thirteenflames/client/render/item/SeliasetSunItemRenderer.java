package com.qurenie.relics_thirteenflames.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.qurenie.relics_thirteenflames.client.render.entity.EntityRendererSeliasetSun;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;

import static com.qurenie.relics_thirteenflames.client.render.entity.EntityRendererSeliasetSun.TEXTURE;
import static com.qurenie.relics_thirteenflames.client.render.entity.EntityRendererSeliasetSun.TEXTURE_EMISSIVE;


public class SeliasetSunItemRenderer
		extends ZeithTechISTER
{
	@Override
	public void renderByItem(ItemStack pStack, ItemDisplayContext pTransformType, PoseStack pose, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay)
	{
		var er = EntityRendererSeliasetSun.instance;
		
		if(er == null) return;
		var model = er.model;
		var activity = 0;
		
		
		var core = model.getBone("Core");
		var body = model.getBone("Body");
		var coreOuter = model.getBone("CoreOuter");
		if(core == null || body == null || coreOuter == null) return;
		pPackedLight = LightTexture.pack(15, 15);
		
		pose.pushPose();
		pose.translate(0.5F, 0.125F, 0.5F);
		
		pose.scale(1.25F, 1.25F, 1.25F);
		
		
		var bodySize = body.getScale();
		var outerSize = coreOuter.getScale();
		
		var tmp1 = new Vector3f();
		var tmp2 = new Vector3f();
		
		
		pose.translate(0, activity, 0);
		
		pose.translate(0, -0.125F * activity, 0);
		
		model.applyAnimations(null, 0F);
		
		// Render inner solid part
		tmp1.set(bodySize);
		tmp2.set(outerSize);
		bodySize.set(0);
		outerSize.set(0);
		model.renderToBuffer(pose, pBuffer.getBuffer(RenderType.entitySolid(TEXTURE)),
				pPackedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1
		);
		bodySize.set(tmp1);
		outerSize.set(tmp2);
		
		// Then render translucent parts
		
		model.renderToBuffer(pose, pBuffer.getBuffer(RenderType.entityTranslucent(TEXTURE)),
				pPackedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1
		);
		
		model.renderToBuffer(pose, pBuffer.getBuffer(RenderType.entityTranslucentEmissive(TEXTURE_EMISSIVE)),
				pPackedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1
		);
		
		pose.popPose();
	}
}
