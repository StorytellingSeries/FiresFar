package com.qurenie.relics_thirteenflames.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.qurenie.relics_thirteenflames.content.items.base.IRenderableCurioHand;
import it.hurts.sskirillss.relics.client.models.items.CurioModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderArmEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

@EventBusSubscriber(value = Dist.CLIENT)
public class RenderEvents {
    
    @SubscribeEvent
    public static void onRenderHand(RenderArmEvent event) {
        HumanoidArm arm = event.getArm();
        PoseStack poseStack = event.getPoseStack();
        CuriosApi.getCuriosInventory(event.getPlayer()).ifPresent((itemHandler) -> {
            
            for (ICurioStacksHandler curioHandler : itemHandler.getCurios().values()) {
                IDynamicStackHandler stackHandler = curioHandler.getStacks();
                
                for (int i = 0; i < stackHandler.getSlots(); ++i) {
                    if (curioHandler.getRenders().get(i)) {
                        ItemStack stack = stackHandler.getStackInSlot(i);
                        if (stack.getItem() instanceof IRenderableCurioHand renderable) {
                            if (!CurioModel.getLayerLocation(stack.getItem()).toString().equals("minecraft:air#air")) {
                                HumanoidModel<?> model = renderable.getModel(stack, arm);
                                
                                poseStack.pushPose();
                                float scale = 1F;
                                if (arm == HumanoidArm.RIGHT) {
                                    poseStack.mulPose(Axis.ZN.rotationDegrees(-5.0F));
                                    poseStack.scale(scale, scale, scale);
                                    poseStack.translate(-0.2, -0.1, -0.0);
                                } else {
                                    poseStack.mulPose(Axis.ZN.rotationDegrees(5.0F));
                                    poseStack.scale(scale, scale, scale);
                                    poseStack.translate(0.2 , -0.1, 0.0);
                                }
                                
                                model.renderToBuffer(poseStack, ItemRenderer.getArmorFoilBuffer(event.getMultiBufferSource(), RenderType.armorCutoutNoCull(renderable.getTexture(stack, arm)), stack.hasFoil()), event.getPackedLight(), OverlayTexture.NO_OVERLAY);
                                poseStack.popPose();
                            }
                        }
                    }
                }
            }
            
        });
    }
    
}
