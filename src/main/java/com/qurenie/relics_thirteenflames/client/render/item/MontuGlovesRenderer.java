package com.qurenie.relics_thirteenflames.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.qurenie.relics_thirteenflames.ThirteenFlames;
import com.qurenie.relics_thirteenflames.content.items.models.MontuGlovesArmorLeft;
import com.qurenie.relics_thirteenflames.content.items.models.MontuGlovesArmorRight;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

import java.util.List;

public class MontuGlovesRenderer implements ICurioRenderer {
    
    private static final ResourceLocation TEXTURE = ThirteenFlames.rl("textures/armor/montu_gloves.png");
    private static final ResourceLocation EMISSION = ThirteenFlames.rl("textures/armor/montu_gloves_emissive.png");
    
    private final MontuGlovesArmorRight<LivingEntity> right;
    private final MontuGlovesArmorLeft<LivingEntity> left;
    
    public MontuGlovesRenderer() {
        this.right = new MontuGlovesArmorRight<>(Minecraft.getInstance().getEntityModels().bakeLayer(MontuGlovesArmorRight.LAYER_LOCATION));
        this.left = new MontuGlovesArmorLeft<>(Minecraft.getInstance().getEntityModels().bakeLayer(MontuGlovesArmorLeft.LAYER_LOCATION));
    }
    
    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack, SlotContext slotContext, PoseStack poseStack, RenderLayerParent<T, M> renderLayerParent,
                                                                          MultiBufferSource renderTypeBuffer, int light, float limbSwing, float limbSwingAmount, float partialTicks,
                                                                          float ageInTicks, float netHeadYaw, float headPitch) {
        
        for (var model : List.of(right, left)) {
            // Stick model to entity
            LivingEntity entity = slotContext.entity();
            model.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTicks);
            ICurioRenderer.followBodyRotations(entity, model);
            model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            
            poseStack.pushPose();
            poseStack.scale(0.8F, 0.8F, 0.8F);
            
            // base
            VertexConsumer vc = ItemRenderer.getArmorFoilBuffer(renderTypeBuffer, RenderType.armorCutoutNoCull(TEXTURE), stack.hasFoil());
            model.renderToBuffer(poseStack, vc, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
            
            // emission
//        vc = ItemRenderer.getArmorFoilBuffer(renderTypeBuffer, RenderType.armorCutoutNoCull(EMISSION), stack.hasFoil());
//        this.model.renderToBuffer(poseStack, vc, 16711935, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
            
            poseStack.popPose();
        }
    }
    
}