package com.qurenie.relics_thirteenflames.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.qurenie.relics_thirteenflames.client.render.entity.EntityRendererSeliasetSun;
import com.qurenie.relics_thirteenflames.init.ItemsRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import static com.qurenie.relics_thirteenflames.client.render.entity.EntityRendererSeliasetSun.TEXTURE;
import static com.qurenie.relics_thirteenflames.client.render.entity.EntityRendererSeliasetSun.TEXTURE_EMISSIVE;


public class SeliasetSunItemRenderer
        extends ZeithTechISTER {
    @Override
    public void renderByItem(@NotNull ItemStack pStack, @NotNull ItemDisplayContext pTransformType, @NotNull PoseStack pose, @NotNull MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {

        var mc = Minecraft.getInstance();
        var ir = mc.getItemRenderer();

        var isterModel = ir.getModel(pStack, mc.level, mc.player, 0);
        var overrides = isterModel.getOverrides().getOverrides();

        if (pTransformType == ItemDisplayContext.GUI) {
            renderOverrride(overrides.getFirst(), pTransformType, pose, pStack, pBuffer, null, LightTexture.FULL_BRIGHT, pPackedOverlay);
            return;
        }

        var er = EntityRendererSeliasetSun.instance;

        if (er == null) return;
        boolean isFlawless = ItemsRegistry.SELIASET_SUN.getRelicData(null, pStack).isFlawless();
        var model = isFlawless ? er.model_flawless : er.model;
        var activity = 0;


        var body = isFlawless ? model.getBone("frame") : model.getBone("Body");
        var coreOuter = isFlawless ? model.getBone("frame2") : model.getBone("CoreOuter");
        if (body == null || coreOuter == null) return;
        pPackedLight = LightTexture.pack(15, 15);

        pose.pushPose();
        pose.translate(0.5F, 0.125F, 0.5F);

        pose.scale(1.25F, 1.25F, 1.25F);
        var bodySize = body.getScale();
        var outerSize = coreOuter.getScale();

        var tmp1 = new Vector3f();
        var tmp2 = new Vector3f();
        pose.translate(0, activity, 0);

        pose.translate(0, -0.125F * activity, 0);

        model.applyAnimations(null, 0F);

        // Render inner solid part
        tmp1.set(bodySize);
        tmp2.set(outerSize);
        bodySize.set(0);
        outerSize.set(0);
        model.renderToBuffer(pose, pBuffer.getBuffer(RenderType.entityCutout(isFlawless ? er.getFlawlessTexture(mc.player, true) : TEXTURE_EMISSIVE)),
                pPackedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF
        );
        bodySize.set(tmp1);
        outerSize.set(tmp2);

        // Then render translucent parts

        if (!isFlawless)
            model.renderToBuffer(pose, pBuffer.getBuffer(RenderType.entityTranslucent(TEXTURE)),
                    pPackedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF
            );

        model.renderToBuffer(pose, pBuffer.getBuffer(RenderType.entityTranslucentEmissive(isFlawless ? er.getFlawlessTexture(mc.player, false) : TEXTURE_EMISSIVE)),
                pPackedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF
        );

        pose.popPose();

    }
}
