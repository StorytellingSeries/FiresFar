package com.qurenie.relics_thirteenflames.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.qurenie.relics_thirteenflames.ThirteenFlames;
import com.qurenie.relics_thirteenflames.client.render.entity.SimpleBedrockModel;
import com.qurenie.relics_thirteenflames.content.items.ItemJodahStaff;
import com.qurenie.relics_thirteenflames.content.items.misc.JodahTier;
import com.qurenie.relics_thirteenflames.init.ComponentRegistry;
import com.qurenie.relics_thirteenflames.init.EntityModels;
import com.qurenie.relics_thirteenflames.init.register.RendererFactory;
import com.qurenie.relics_thirteenflames.mixins.client.BakedOverrideAccessor;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

public class JodahStaffItemRenderer extends ZeithTechISTER {
    
    SimpleBedrockModel<?> model;

    public JodahStaffItemRenderer() {
        this.model = new SimpleBedrockModel<>(RendererFactory.ModelConfiguration.builder()
                .model(EntityModels.JODAH_STAFF_MODEL)
                .build());
        model.createModel();
    }
    
    @Override
    public void renderByItem(@NotNull ItemStack stack, @NotNull ItemDisplayContext transformType, @NotNull PoseStack pose, @NotNull MultiBufferSource bufferSource, int uv2, int overlay) {
        if (!(stack.getItem() instanceof ItemJodahStaff staff))
            return;

        JodahTier tier = stack.getOrDefault(ComponentRegistry.JODAH_TIER, JodahTier.D);
        boolean active = stack.getOrDefault(ComponentRegistry.ACTIVE_TICK, 0) > 0;

        boolean isFlawless = staff.getRelicData(null, stack).isFlawless();

        if (!isFlawless) {
            int rank = JodahTier.values().length - tier.ordinal();

            ResourceLocation texture = ThirteenFlames.rl(String.format("textures/item/jodah_staff_rank%d%s.png",
                                rank, active ? "_purple" : ""));

            ResourceLocation textureEmissive = ThirteenFlames.rl(String.format("textures/item/jodah_staff_rank%d%s_emissive.png",
                                rank, active ? "_purple" : ""));

            applyTrasforms(pose, transformType);

            VertexConsumer base = bufferSource.getBuffer(RenderType.entityCutout(texture));
            model.renderToBuffer(pose, base, uv2, overlay);

            VertexConsumer emissive = bufferSource.getBuffer(RenderType.entityTranslucentEmissive(textureEmissive));
            model.renderToBuffer(pose, emissive, uv2, overlay);
        } else {
            var mc = Minecraft.getInstance();

            int rank = Math.min(3, (JodahTier.values().length - 1) - tier.ordinal());
            String prefix = String.format("item/jodah_staff_%d%s_flawless", rank, active ? "_active" : "");
            ResourceLocation baseLoc = ThirteenFlames.rl(prefix);
            ModelResourceLocation modelLoc = ModelResourceLocation.standalone(baseLoc);

            var resolvedModel = mc.getModelManager().getModel(modelLoc);
            var currentOverrides = resolvedModel.getOverrides().getOverrides();

            if (currentOverrides.size() < 2) {
                ThirteenFlames.LOGGER.warn("Jodah staff: expected base+emissive for {}, got {}", modelLoc, currentOverrides.size());
                return;
            }

            for (int k = currentOverrides.size() - 1; k >= 0; k--) {
                var override = currentOverrides.get(k);
                int lightmap = (k == 0) ? 16711935 : uv2;
                pose.pushPose();
//                pose.translate(-0.5F, 0F, -0.5F);
                switch (transformType) {
                    case THIRD_PERSON_RIGHT_HAND -> {
//                        pose.mulPose(Axis.ZP.rotationDegrees(90));
                        pose.mulPose(Axis.YP.rotationDegrees(-45));
                        pose.mulPose(Axis.XP.rotationDegrees(-75));
                        pose.translate(6 / 8f, 11 / 16f, -3 /16f);
                        pose.translate(-2, -4, -1);
                        pose.scale(4f, 4f, 4f);
                    }
                    case THIRD_PERSON_LEFT_HAND -> {
//                        pose.mulPose(Axis.ZP.rotationDegrees(-90));
                        pose.mulPose(Axis.YP.rotationDegrees(45));
                        pose.mulPose(Axis.XP.rotationDegrees(-75));
                        pose.translate(-2, -4, -1);
                        pose.scale(4f, 4f, 4f);
                    }
                    case FIRST_PERSON_LEFT_HAND -> {
                        pose.mulPose(Axis.YP.rotationDegrees(-135));
                        pose.translate(-2 / 8f, 0, -7/8f);
                        pose.scale(1.6f, 1.6f, 1.6f);
                    }
                    case FIRST_PERSON_RIGHT_HAND -> {
                        pose.mulPose(Axis.YP.rotationDegrees(-45));
                        pose.translate(-6 / 8f, 0, -1/8f);
                        pose.scale(1.6f, 1.6f, 1.6f);
                    }
                }
                super.renderOverrride(override, transformType, pose, stack, bufferSource, RenderType.cutout(), lightmap, overlay);
                pose.popPose();
            }
        }
    }

    private ResourceLocation getFlawlessTexture(int rank, boolean active, boolean emissive) {
        boolean isNamedFlawless = !active && !emissive; // Fuck Morgen

        return ThirteenFlames.rl(String.format("textures/item/staff_flawless/jodah_staff_upgraded_rank%d%s%s%s.png",
                rank, active ? "_purple" : "", emissive ? "_emissive" : "", isNamedFlawless ? "_flawless" : ""));
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
            case FIRST_PERSON_LEFT_HAND -> poseStack.translate(5.5F / 16.0F, -4.0F / 16F, 0.0F);
            case FIRST_PERSON_RIGHT_HAND -> poseStack.translate(-5.5F / 16.0F, -4.0F / 16F, 0.0F);
        }
    }
    
}
