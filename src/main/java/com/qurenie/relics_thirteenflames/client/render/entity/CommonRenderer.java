package com.qurenie.relics_thirteenflames.client.render.entity;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.qurenie.relics_thirteenflames.client.render.entity.processor.base.IProcessor;
import com.qurenie.relics_thirteenflames.init.register.RendererFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;
import org.zeith.hammeranims.api.tile.IAnimatedEntity;

public class CommonRenderer<T extends LivingEntity & IAnimatedEntity>
        extends LivingEntityRenderer<T, SimpleBedrockModel<T>> {
    private final ResourceLocation texture;
    private ResourceLocation lastUsedTexture;

    public CommonRenderer(EntityRendererProvider.Context manager, RendererFactory.ModelConfiguration modelConfiguration, float shadowSize, ResourceLocation texture) {
        super(manager, new SimpleBedrockModel<>(modelConfiguration), shadowSize);
        this.texture = texture;
    }

    @Override
    protected void setupRotations(T pEntityLiving, PoseStack pMatrixStack, float pAgeInTicks, float pRotationYaw, float pPartialTicks) {
        super.setupRotations(pEntityLiving, pMatrixStack, pAgeInTicks, pRotationYaw, pPartialTicks);
        pMatrixStack.translate(0, 1.5F, 0);
        pMatrixStack.mulPose(Axis.XP.rotationDegrees(180f));
        pMatrixStack.mulPose(Axis.YP.rotationDegrees(180f));
    }

    public void addModelProcessor(IProcessor<T> proc) {
        getModel().withModelProcessor(proc);
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return texture;
    }

    public void inventoryRender(int pPosX, int pPosY, int pScale, float pMouseX, float pMouseY, LivingEntity pLivingEntity) {
        float f = (float) Math.atan(pMouseX / 40.0F);
        float f1 = (float) Math.atan(pMouseY / 40.0F);

        PoseStack posestack = RenderSystem.getModelViewStack();
        posestack.pushPose();
        posestack.translate(pPosX, pPosY, 1050.0D);
        posestack.scale(1.0F, 1.0F, -1.0F);
        RenderSystem.applyModelViewMatrix();
        PoseStack posestack1 = new PoseStack();
        posestack1.translate(0.0D, 0.0D, 1000.0D);
        posestack1.scale((float) pScale, (float) pScale, (float) pScale);
        Quaternionf quaternion = Axis.ZP.rotationDegrees(180.0F);
        Quaternionf quaternion1 = Axis.XP.rotationDegrees(f1 * 20.0F);
        quaternion.mul(quaternion1);
        posestack1.mulPose(quaternion);
        float f2 = pLivingEntity.yBodyRot;
        float f3 = pLivingEntity.getYRot();
        float f4 = pLivingEntity.getXRot();
        float f5 = pLivingEntity.yHeadRotO;
        float f6 = pLivingEntity.yHeadRot;
        pLivingEntity.yBodyRot = 180.0F + f * 20.0F;
        pLivingEntity.setYRot(f * 20.0F);
        pLivingEntity.setXRot(-f1 * 20.0F);
        pLivingEntity.yHeadRot = pLivingEntity.getYRot();
        pLivingEntity.yHeadRotO = pLivingEntity.getYRot();
        Lighting.setupForEntityInInventory();
        EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        quaternion1.conjugate();
        entityrenderdispatcher.overrideCameraOrientation(quaternion1);
        entityrenderdispatcher.setRenderShadow(false);
        MultiBufferSource.BufferSource multibuffersource$buffersource = Minecraft.getInstance().renderBuffers()
                .bufferSource();
        RenderSystem.runAsFancy(() ->
        {
            entityrenderdispatcher.render(pLivingEntity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, posestack1, multibuffersource$buffersource, 15728880);
        });
        multibuffersource$buffersource.endBatch();
        entityrenderdispatcher.setRenderShadow(true);
        pLivingEntity.yBodyRot = f2;
        pLivingEntity.setYRot(f3);
        pLivingEntity.setXRot(f4);
        pLivingEntity.yHeadRotO = f5;
        pLivingEntity.yHeadRot = f6;
        posestack.popPose();
        RenderSystem.applyModelViewMatrix();
        Lighting.setupFor3DItems();
    }

    public void toroRender(PoseStack matrixStack2, int x, int y, int size, float mouseX, float mouseY, LivingEntity entity, float scale) {
        EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();

        float f = (float) Math.atan(mouseX / 40.0F);
        float g = (float) Math.atan(mouseY / 40.0F);
		
		float scaleF = size;
		//if(entity instanceof IToroScaledEntity t) scaleF = t.getToroScale();

        PoseStack matrixStack = RenderSystem.getModelViewStack();
        matrixStack.pushPose();
        matrixStack.translate((double) x * (double) scale, (double) y * (double) scale, 1050.0 * (double) scale);
        matrixStack.scale(1.0F, 1.0F, -1.0F);
        RenderSystem.applyModelViewMatrix();
        matrixStack2.pushPose();
        matrixStack2.translate(0.0, 0.0, 1000.0);
		matrixStack2.scale(scaleF, scaleF, scaleF);
        Quaternionf quaternion = Axis.ZP.rotationDegrees(180.0F);
        Quaternionf quaternion2 = Axis.XP.rotationDegrees(g * 20.0F);

        quaternion.mul(quaternion2);
        matrixStack2.mulPose(quaternion);
        
        float h = entity.yBodyRot;
        float i = entity.getYRot();
        float j = entity.getXRot();
        float k = entity.yHeadRotO;
        float l = entity.yHeadRot;

        entity.yBodyRot = 180.0F + f * 20.0F;
        entity.setYRot(f * 20.0F);
        entity.setXRot(-g * 20.0F);

        entity.yHeadRotO = entity.yHeadRot = entity.getYRot();
        Lighting.setupForEntityInInventory();
        quaternion2.conjugate();
        entityrenderdispatcher.overrideCameraOrientation(quaternion2);
        entityrenderdispatcher.setRenderShadow(false);

        MultiBufferSource.BufferSource immediate = Minecraft.getInstance().renderBuffers().bufferSource();
        RenderSystem.runAsFancy(() ->
                entityrenderdispatcher.render(entity, 0.0, 0.0, 0.0, 0.0F, 1.0F, matrixStack2, immediate, 15728880));
        immediate.endBatch();

        entityrenderdispatcher.setRenderShadow(true);

        entity.yBodyRot = h;
        entity.setYRot(i);
        entity.setXRot(j);
        entity.yHeadRotO = k;
        entity.yHeadRot = l;

        matrixStack.popPose();
        matrixStack2.popPose();
        RenderSystem.applyModelViewMatrix();
        Lighting.setupFor3DItems();
    }
}