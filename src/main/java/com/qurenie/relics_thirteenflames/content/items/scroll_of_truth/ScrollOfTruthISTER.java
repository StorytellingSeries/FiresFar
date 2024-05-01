package com.qurenie.relics_thirteenflames.content.items.scroll_of_truth;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.qurenie.relics_thirteenflames.ThirteenFlames;
import com.qurenie.relics_thirteenflames.client.render.entity.SimpleBedrockModel;
import com.qurenie.relics_thirteenflames.init.EntityModels;
import com.qurenie.relics_thirteenflames.init.register.RendererFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;

import java.awt.*;

public class ScrollOfTruthISTER extends BlockEntityWithoutLevelRenderer {

    public static final ResourceLocation MAIN_TEXT = ThirteenFlames.rl("textures/item/scroll_of_truth/scroll_item_nogem.png");
    public static final ResourceLocation EMISSIVE = ThirteenFlames.rl("textures/item/scroll_of_truth/scroll_item_rgb_emissive.png");

    public final SimpleBedrockModel<?> scrollModel;


    public ScrollOfTruthISTER() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
        scrollModel = new SimpleBedrockModel<>(RendererFactory.ModelConfiguration.builder().model(EntityModels.SCROLL_OF_TRUTH).build());

    }

    @Override
    public void renderByItem(ItemStack item, ItemDisplayContext transform, PoseStack matrices, MultiBufferSource src, int light, int overlay) {
        matrices.pushPose();
        matrices.translate(0.5f,0.5f,0.5f);
        if (transform == ItemDisplayContext.GUI){
            matrices.translate(0,0.2,0);
            matrices.mulPose(Axis.YP.rotationDegrees(-45));
            matrices.mulPose(Axis.ZP.rotationDegrees(-20));
            matrices.mulPose(Axis.XP.rotationDegrees(20));
            matrices.scale(0.75f,0.75f,0.75f);
        }else if (transform == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || transform == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND){
            matrices.translate(0,0.25,0);
        }else if (transform == ItemDisplayContext.THIRD_PERSON_LEFT_HAND || transform == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND){
            matrices.translate(0,0,-0.05);
        }else if (transform == ItemDisplayContext.GROUND){
            matrices.translate(0,0.25,0);
            matrices.scale(0.5f,0.5f,0.5f);
        }else if (transform == ItemDisplayContext.FIXED){
            matrices.translate(0,0.25,0);
        }
        this.renderModel(item,matrices,src,light);
        matrices.popPose();
    }

    private void renderModel(ItemStack scroll,PoseStack matrices,MultiBufferSource src,int light){
        this.scrollModel.renderToBuffer(matrices,src.getBuffer(RenderType.entityTranslucent(MAIN_TEXT)),light, OverlayTexture.NO_OVERLAY,1,1,1,1);
        if (scroll.getTag() != null && scroll.getTag().contains("scrollColorMode")) {
            ScrollColorMode mode = ScrollColorMode.fromTag(scroll.getOrCreateTag());
            Color color = mode.color;
            this.scrollModel.renderToBuffer(matrices,src.getBuffer(RenderType.entityTranslucent(EMISSIVE)),
                    15728880,OverlayTexture.NO_OVERLAY,
                    color.getRed()/255f,
                    color.getGreen()/255f,
                    color.getBlue()/255f,
                    1f
                    );
        }
    }
}
