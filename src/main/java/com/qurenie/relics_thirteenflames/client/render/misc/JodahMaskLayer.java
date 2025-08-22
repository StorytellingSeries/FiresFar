package com.qurenie.relics_thirteenflames.client.render.misc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.qurenie.relics_thirteenflames.ThirteenFlames;
import com.qurenie.relics_thirteenflames.content.items.ItemJodahMask;
import com.qurenie.relics_thirteenflames.content.items.misc.MaskState;
import com.qurenie.relics_thirteenflames.content.items.models.InterworlderMask;
import com.qurenie.relics_thirteenflames.content.items.models.MontuGlovesArmorRight;
import com.qurenie.relics_thirteenflames.init.ComponentRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public class JodahMaskLayer <T extends Player, M extends EntityModel<T>> extends RenderLayer<T, M> {
    
    private static final ResourceLocation EMISSION = ThirteenFlames.rl("textures/armor/interworlder_head_emissive.png");
    private final InterworlderMask<LivingEntity> mask;
    
    public JodahMaskLayer(RenderLayerParent<T, M> renderer) {
        super(renderer);
        this.mask = new InterworlderMask<>(Minecraft.getInstance().getEntityModels().bakeLayer(InterworlderMask.LAYER_LOCATION));
    }
    
    @Override
    public void render(@NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight, @NotNull T entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        ItemStack stack = entity.getItemBySlot(EquipmentSlot.HEAD);
        
        if (stack.getItem() instanceof ItemJodahMask) {
            MaskState state = stack.getOrDefault(ComponentRegistry.MASK_STATE, MaskState.NEUTRAL);
            
            ResourceLocation loc = ThirteenFlames.rl(String.format("textures/armor/interworlder_head%s.png", state.getTexturePostfix()));
            
            mask.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTicks);
            ICurioRenderer.followBodyRotations(entity, mask);
            mask.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            
            poseStack.pushPose();
            
            // base
            VertexConsumer vc = ItemRenderer.getArmorFoilBuffer(bufferSource, RenderType.armorCutoutNoCull(loc), stack.hasFoil());
            mask.renderToBuffer(poseStack, vc, packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
            
            // emission
            poseStack.pushPose();
            vc = ItemRenderer.getArmorFoilBuffer(bufferSource, RenderType.armorCutoutNoCull(EMISSION), stack.hasFoil());
            this.mask.renderToBuffer(poseStack, vc, 16711935, OverlayTexture.NO_OVERLAY, state.getEyesColor());
            poseStack.popPose();
            
            poseStack.popPose();
        }
    }
    
}
