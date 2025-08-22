package com.qurenie.relics_thirteenflames.client.render.misc;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.qurenie.relics_thirteenflames.ThirteenFlames;
import com.qurenie.relics_thirteenflames.client.ThirteenRenderTypes;
import com.qurenie.relics_thirteenflames.client.render.entity.SimpleBedrockModel;
import com.qurenie.relics_thirteenflames.init.EntityModels;
import com.qurenie.relics_thirteenflames.init.register.RendererFactory;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.UUID;
import java.util.function.Supplier;

@UtilityClass
public class JodahStaffRenderUtil {
    
    private static final Supplier<SimpleBedrockModel<?>> WINGS_MODEL = Suppliers.memoize(() ->
            new SimpleBedrockModel<>(RendererFactory.ModelConfiguration.builder()
                    .model(EntityModels.JODAH_WINGS)
                    .build()));
    private static final HashMap<UUID, SimpleBedrockModel<?>> WINGS_RENDERER = new HashMap<>();
    private static final ResourceLocation EYE_TEXTURE = ThirteenFlames.rl("textures/misc/jodah_eye.png");
    
    @OnlyIn(Dist.CLIENT)
    public static void renderWings(MultiBufferSource buffer, PoseStack poseStack, Player player, float partialTicks) {
        poseStack.pushPose();
        float bodyYaw = Mth.lerp(partialTicks, player.yBodyRotO, player.yBodyRot);
        float pitch = Mth.lerp(partialTicks, player.xRotO, player.getXRot());
        
        float yawRad = (float) Math.toRadians(-bodyYaw);
        float pitchRad = (float) Math.toRadians(-pitch);
        poseStack.mulPose(Axis.YP.rotation(yawRad));
        poseStack.mulPose(Axis.XP.rotation(pitchRad));
        poseStack.translate(0.0, 0.0, 0.2);
        SimpleBedrockModel<?> model = WINGS_MODEL.get();
        
        poseStack.popPose();
    }
    
    @OnlyIn(Dist.CLIENT)
    public static void renderEye(MultiBufferSource buffer, PoseStack poseStack, float x, float y, float z, Camera renderInfo) {
        Quaternionf quaternionf = new Quaternionf();
        quaternionf.set(renderInfo.rotation());
        // как
        poseStack.pushPose();
        float f = 2;
        VertexConsumer vertexConsumer = buffer.getBuffer(ThirteenRenderTypes.entityGlowingNoDepth(EYE_TEXTURE));
        
        float size = 0.2f;
        
        renderVertex(vertexConsumer, poseStack, quaternionf, x, y, z, size, -size / 1.6f, f, 0, 1);
        renderVertex(vertexConsumer, poseStack, quaternionf, x, y, z, size, size / 1.6f, f, 0, 0);
        renderVertex(vertexConsumer, poseStack, quaternionf, x, y, z, -size, size / 1.6f, f, 1, 0);
        renderVertex(vertexConsumer, poseStack, quaternionf, x, y, z, -size, -size / 1.6f, f, 1, 1);
        
        poseStack.popPose();
    }
    
    private static void renderVertex(
            VertexConsumer buffer,
            PoseStack poseStack,
            Quaternionf quaternion,
            float x,
            float y,
            float z,
            float xOffset,
            float yOffset,
            float quadSize,
            float u,
            float v
    ) {
        Vector3f vector3f = new Vector3f(xOffset, yOffset, 0.0F).rotate(quaternion).mul(quadSize).add(x, y, z);
        buffer.addVertex(poseStack.last().pose(), vector3f.x(), vector3f.y(), vector3f.z())
                .setUv(u, v)
                .setColor(255, 255, 255, 255);
    }
    
}
