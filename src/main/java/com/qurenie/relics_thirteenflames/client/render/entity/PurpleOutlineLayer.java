package com.qurenie.relics_thirteenflames.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public class PurpleOutlineLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    
    public PurpleOutlineLayer(RenderLayerParent<T, M> renderer) {
        super(renderer);
    }
    
    @Override
    public void render(@NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight, @NotNull T livingEntity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!shouldRenderOutline(livingEntity)) return;
        
        VertexConsumer vertexConsumer = Minecraft.getInstance().renderBuffers().outlineBufferSource().getBuffer(RenderType.outline(getTextureLocation(livingEntity)));
        
        // Важно! Задаём цвет вручную при рендере
        this.getParentModel().renderToBuffer(
                poseStack,
                vertexConsumer,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                0xFF9933CC // фиолетовый (RGBA)
        );
    }
    
    private boolean shouldRenderOutline(T entity) {
        return IJodahGlowed.of(entity).hasJodahGlowEffect();
    }
    
}
