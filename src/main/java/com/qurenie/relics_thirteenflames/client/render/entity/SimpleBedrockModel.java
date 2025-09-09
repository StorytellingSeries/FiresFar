package com.qurenie.relics_thirteenflames.client.render.entity;

import com.mojang.blaze3d.vertex.*;
import com.qurenie.relics_thirteenflames.client.render.entity.processor.base.CentralModelProcessor;
import com.qurenie.relics_thirteenflames.client.render.entity.processor.base.IProcessor;
import com.qurenie.relics_thirteenflames.init.register.RendererFactory;
import net.minecraft.client.model.EntityModel;
import net.minecraft.world.entity.Entity;
import org.zeith.hammeranims.api.HammerAnimationsApi;
import org.zeith.hammeranims.api.animsys.AnimationSystem;
import org.zeith.hammeranims.api.geometry.IGeometryContainer;
import org.zeith.hammeranims.api.geometry.event.RefreshStaleModelsEvent;
import org.zeith.hammeranims.api.geometry.model.*;
import org.zeith.hammeranims.api.tile.IAnimatedEntity;

public class SimpleBedrockModel<T extends Entity & IAnimatedEntity>
		extends EntityModel<T>
{
	protected final CentralModelProcessor<T> processor = new CentralModelProcessor<>();
	
	private final IGeometryContainer modelCtr;
	
	private IGeometricModel model;
	
	private final RenderData renderData = new RenderData();
	
	public SimpleBedrockModel(RendererFactory.ModelConfiguration config)
	{
		super(config.renderType());
		this.modelCtr = config.model();
		HammerAnimationsApi.EVENT_BUS.addListener(this::refreshGeometry);
	}
	
	public SimpleBedrockModel<T> withModelProcessor(IProcessor<? super T> processor)
	{
		this.processor.add(processor);
		return this;
	}
	
	public void createModel() {
		model = modelCtr.createModel();
	}
	
	private void refreshGeometry(RefreshStaleModelsEvent e)
	{
		createModel();
	}
	
	@Override
	public void setupAnim(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
	{
		model.applySystem(ageInTicks % 1, entityIn.getAnimationSystem());
		processor.apply(entityIn, this, ageInTicks % 1);
	}
	
	@Override
	public void renderToBuffer(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, int color) {
		renderData.apply(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
		renderData.applyColor(color);
		model.renderModel(renderData);
	}
	
	
	public IRenderableBone getRoot()
	{
		return model.getRoot();
	}
	
	public IRenderableBone getBone(String bone)
	{
		return model.getBone(bone);
	}
	
	public void applyAnimations(AnimationSystem sys, float pt)
	{
		if(sys == null)
		{
			model.resetPose();
			return;
		}
		
		model.applySystem(pt, sys);
	}
}