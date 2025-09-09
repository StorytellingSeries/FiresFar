package com.qurenie.relics_thirteenflames.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.qurenie.relics_thirteenflames.ThirteenFlames;
import com.qurenie.relics_thirteenflames.client.render.entity.SimpleBedrockModel;
import com.qurenie.relics_thirteenflames.content.items.ItemJodahStaff;
import com.qurenie.relics_thirteenflames.content.items.misc.JodahTier;
import com.qurenie.relics_thirteenflames.init.ComponentRegistry;
import com.qurenie.relics_thirteenflames.init.EntityModels;
import com.qurenie.relics_thirteenflames.init.register.RendererFactory;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class JodahStaffItemRenderer extends ZeithTechISTER {
    
    SimpleBedrockModel<?> model;
    
    public JodahStaffItemRenderer() {
        this.model = new SimpleBedrockModel<>(RendererFactory.ModelConfiguration.builder()
                .model(EntityModels.JODAH_STAFF_MODEL)
                .build());
        model.createModel();
    }
    
    @Override
    public void renderByItem(@NotNull ItemStack stack, @NotNull ItemDisplayContext transformType, @NotNull PoseStack pose, @NotNull MultiBufferSource bufferSource, int i, int j) {
        if (!(stack.getItem() instanceof ItemJodahStaff staff))
            return;
       
        JodahTier tier = stack.getOrDefault(ComponentRegistry.JODAH_TIER, JodahTier.D);
        boolean active = stack.getOrDefault(ComponentRegistry.ACTIVE_TICK, 0) > 0;
        
        ResourceLocation texture = ThirteenFlames.rl(String.format("textures/item/jodah_staff_rank%d%s.png",
                JodahTier.values().length - tier.ordinal(),
                active ? "_purple" : ""));
        
        ResourceLocation textureEmissive = ThirteenFlames.rl(String.format("textures/item/jodah_staff_rank%d%s_emissive.png",
                JodahTier.values().length - tier.ordinal(),
                active ? "_purple" : ""));
        
        applyTrasforms(pose, transformType);
        
        VertexConsumer base = bufferSource.getBuffer(RenderType.entityCutout(texture));
        model.renderToBuffer(pose, base, i, j);
        
        VertexConsumer emissive = bufferSource.getBuffer(RenderType.entityTranslucentEmissive(textureEmissive));
        model.renderToBuffer(pose, emissive, i, j);
    }
    
    protected void applyTrasforms(PoseStack poseStack, @NotNull ItemDisplayContext transformType) {
        poseStack.translate(0.5F, 0F, 0.5F);
        switch (transformType) {
            case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> {
                poseStack.pushPose();
                poseStack.translate(0.0F, -9.75F / 16.0F, 1.75F / 16.0F);
                poseStack.scale(1.51F, 1.51F, 1.51F);
                poseStack.popPose();
            }
            case FIRST_PERSON_LEFT_HAND -> poseStack.translate(5.5F / 16.0F, 0.0F, 0.0F);
            case FIRST_PERSON_RIGHT_HAND -> poseStack.translate(-5.5F / 16.0F, 0.0F, 0.0F);
        }
    }
    
}
