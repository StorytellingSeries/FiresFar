package com.qurenie.relics_thirteenflames.client.render.entity;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.qurenie.relics_thirteenflames.client.render.entity.processor.base.IProcessor;
import com.qurenie.relics_thirteenflames.init.register.RendererFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4fStack;
import org.joml.Quaternionf;
import org.zeith.hammeranims.api.tile.IAnimatedEntity;

public class CommonRenderer<T extends LivingEntity & IAnimatedEntity>
        extends LivingEntityRenderer<T, SimpleBedrockModel<T>> {
    
    private final ResourceLocation texture;

    public CommonRenderer(EntityRendererProvider.Context manager, RendererFactory.ModelConfiguration modelConfiguration, float shadowSize, ResourceLocation texture) {
        super(manager, new SimpleBedrockModel<>(modelConfiguration), shadowSize);
        this.texture = texture;
    }
    
    @Override
    protected boolean shouldShowName(@NotNull T entity) {
        return false;
    }
    
    @Override
    protected void setupRotations(@NotNull T pEntityLiving, @NotNull PoseStack pMatrixStack, float pAgeInTicks, float pRotationYaw, float pPartialTicks, float p_320045_) {
        super.setupRotations(pEntityLiving, pMatrixStack, pAgeInTicks, pRotationYaw, pPartialTicks, p_320045_);
        pMatrixStack.translate(0, 1.5F, 0);
        pMatrixStack.mulPose(Axis.XP.rotationDegrees(180f));
        pMatrixStack.mulPose(Axis.YP.rotationDegrees(180f));
    }

    public void addModelProcessor(IProcessor<T> proc) {
        getModel().withModelProcessor(proc);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull T entity) {
        return texture;
    }
    
}