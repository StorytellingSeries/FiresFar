package com.qurenie.relics_thirteenflames.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import com.qurenie.api.IRenderableArmorItem;
import com.qurenie.relics_thirteenflames.content.items.base.IArmor;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidArmorLayer.class)
public abstract class HumanoidArmorLayerMixin<T extends LivingEntity, A extends HumanoidModel<T>> {
    
    @Shadow protected abstract void setPartVisibility(A model, EquipmentSlot slot);
    
    @Shadow protected abstract Model getArmorModelHook(T entity, ItemStack itemStack, EquipmentSlot slot, A model);
    
    @Shadow protected abstract boolean usesInnerModel(EquipmentSlot slot);
    
    @Shadow protected abstract void renderModel(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, A model, int dyeColor, ResourceLocation textureLocation);
    
    @Shadow protected abstract void renderTrim(Holder<ArmorMaterial> armorMaterial, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, ArmorTrim trim, A model, boolean innerTexture);
    
    @Shadow protected abstract void renderGlint(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, A model);
    
    @Unique
    private HumanoidArmorLayer self() {
        return (HumanoidArmorLayer) (Object) this;
    }
    
    @Inject(remap = false, method = "renderArmorPiece(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/model/HumanoidModel;FFFFFF)V", at = @At("HEAD"), cancellable = true)
    public void push(PoseStack poseStack, MultiBufferSource bufferSource, T livingEntity, EquipmentSlot slot, int packedLight, A p_model, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        ItemStack itemstack = livingEntity.getItemBySlot(slot);
        if (itemstack.getItem() instanceof IRenderableArmorItem armoritem && itemstack.getItem().getEquipmentSlot(itemstack) == slot) {
            armoritem.render(poseStack, bufferSource, livingEntity, slot, packedLight, limbSwing, limbSwingAmount, partialTick, ageInTicks, netHeadYaw, headPitch);
            if (armoritem.cancelDefaultRenderer(livingEntity, slot))
                ci.cancel();
        }
        if (itemstack.getItem() instanceof IArmor armoritem) {
            if (armoritem.getType().getSlot() == slot) {
                self().getParentModel().copyPropertiesTo(p_model);
                setPartVisibility(p_model, slot);
                net.minecraft.client.model.Model model = getArmorModelHook(livingEntity, itemstack, slot, p_model);
                boolean flag = this.usesInnerModel(slot);
                ArmorMaterial armormaterial = armoritem.getMaterial().value();
                
                net.neoforged.neoforge.client.extensions.common.IClientItemExtensions extensions = net.neoforged.neoforge.client.extensions.common.IClientItemExtensions.of(itemstack);
                extensions.setupModelAnimations(livingEntity, itemstack, slot, model, limbSwing, limbSwingAmount, partialTick, ageInTicks, netHeadYaw, headPitch);
                int fallbackColor = extensions.getDefaultDyeColor(itemstack);
                for (int layerIdx = 0; layerIdx < armormaterial.layers().size(); layerIdx++) {
                    ArmorMaterial.Layer armormaterial$layer = armormaterial.layers().get(layerIdx);
                    int j = extensions.getArmorLayerTintColor(itemstack, livingEntity, armormaterial$layer, layerIdx, fallbackColor);
                    if (j != 0) {
                        var texture = net.neoforged.neoforge.client.ClientHooks.getArmorTexture(livingEntity, itemstack, armormaterial$layer, flag, slot);
                        renderModel(poseStack, bufferSource, packedLight, (A) model, j, texture);
                    }
                }
                
                ArmorTrim armortrim = itemstack.get(DataComponents.TRIM);
                if (armortrim != null) {
                    renderTrim(armoritem.getMaterial(), poseStack, bufferSource, packedLight, armortrim, (A) model, flag);
                }
                
                if (itemstack.hasFoil()) {
                    renderGlint(poseStack, bufferSource, packedLight, (A) model);
                }
            }
        }
    }
    
}
